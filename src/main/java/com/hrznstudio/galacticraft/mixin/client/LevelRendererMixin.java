/*
 * Copyright (c) 2019-2021 HRZN LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hrznstudio.galacticraft.mixin.client;

import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.accessor.WorldRendererAccessor;
import com.hrznstudio.galacticraft.world.dimension.GalacticraftDimensions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.Set;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
@Mixin(LevelRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class LevelRendererMixin implements WorldRendererAccessor {
    private static final ResourceLocation EARTH_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/celestialbodies/earth.png");
    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/celestialbodies/sun.png");

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private ClientLevel level;

    @Shadow
    @Final
    private VertexFormat skyFormat;

    @Shadow
    private double lastCameraX;
    @Shadow
    private double lastCameraY;
    @Shadow
    private double lastCameraZ;
    @Shadow private Set<ChunkRenderDispatcher.RenderChunk> chunksToCompile;
    @Shadow private ViewArea viewArea;
    private VertexBuffer starBufferMoon;

    @Inject(at = @At("RETURN"), method = "<init>")
    private void initGalacticraft(Minecraft client, RenderBuffers bufferBuilders, CallbackInfo ci) {
        starBufferMoon = new VertexBuffer(skyFormat);
        this.generateStarBufferMoon();
    }

    @Inject(method = "renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;FDDD)V", at = @At("HEAD"), cancellable = true)
    private void renderClouds(PoseStack matrices, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (this.level.dimension() == GalacticraftDimensions.MOON) {
            ci.cancel();
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "renderSky", cancellable = true)
    private void renderSkyGC(PoseStack matrices, float delta, CallbackInfo ci) {
        if (this.level.dimension() == GalacticraftDimensions.MOON) {
            this.minecraft.getProfiler().push("moon_sky_render");
            RenderSystem.disableTexture();
            RenderSystem.disableFog();
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
            RenderSystem.disableLighting();
            RenderSystem.depthMask(false);

            final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            float starBrightness = getStarBrightness(delta);

//            this.lightSkyBufferMoon.bind();
//            this.skyVertexFormat.startDrawing(0L);
//            this.lightSkyBufferMoon.draw(matrices.peek().getModel(), 7);
//            VertexBuffer.unbind();
//            this.skyVertexFormat.endDrawing();

            matrices.pushPose();
            matrices.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
            matrices.mulPose(Vector3f.XP.rotationDegrees(this.level.getSunAngle(delta) * 360.0F));
            matrices.mulPose(Vector3f.YP.rotationDegrees(-19.0F));
            RenderSystem.color4f(1.0F, 0.95F, 0.9F, starBrightness); //browner stars?

            this.starBufferMoon.bind();
            this.skyFormat.setupBufferState(0L);
            this.starBufferMoon.draw(matrices.last().pose(), 7);
            VertexBuffer.unbind();
            this.skyFormat.clearBufferState();

            matrices.popPose();
            matrices.pushPose();

            matrices.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
            matrices.mulPose(Vector3f.XP.rotationDegrees(this.level.getSunAngle(delta) * 360.0F));

            RenderSystem.disableTexture();

            Matrix4f matrix = matrices.last().pose();
            RenderSystem.enableTexture();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float size = 15.0F;
            this.minecraft.getTextureManager().bind(SUN_TEXTURE);
            buffer.begin(7, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -size, 100.0F, -size).uv(0.0F, 0.0F).endVertex();
            buffer.vertex(matrix, size, 100.0F, -size).uv(1.0F, 0.0F).endVertex();
            buffer.vertex(matrix, size, 100.0F, size).uv(1.0F, 1.0F).endVertex();
            buffer.vertex(matrix, -size, 100.0F, size).uv(0.0F, 1.0F).endVertex();
            buffer.end();
            BufferUploader.end(buffer);

            matrices.popPose();

            matrices.pushPose();
            matrix = matrices.last().pose();

            size = 10.0F;
            assert this.minecraft.player != null;
            float earthRotation = (float) (this.level.getSharedSpawnPos().getZ() - this.minecraft.player.getZ()) * 0.01F;
            matrices.scale(0.6F, 0.6F, 0.6F);
            matrices.mulPose(Vector3f.XP.rotationDegrees((this.level.getSunAngle(delta) * 360.0F) * 0.001F));
            matrices.mulPose(Vector3f.XP.rotationDegrees(earthRotation + 200.0F));

            this.minecraft.getTextureManager().bind(EARTH_TEXTURE);

            buffer.begin(7, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix, -size, -100.0F, size).uv(0.0F, 1.0F).endVertex();
            buffer.vertex(matrix, size, -100.0F, size).uv(1.0F, 1.0F).endVertex();
            buffer.vertex(matrix, size, -100.0F, -size).uv(1.0F, 0.0F).endVertex();
            buffer.vertex(matrix, -size, -100.0F, -size).uv(0.0F, 0.0F).endVertex();
            buffer.end();
            BufferUploader.end(buffer);

            matrices.popPose();

            RenderSystem.enableRescaleNormal();
            RenderSystem.disableTexture();
            RenderSystem.depthMask(true);
            RenderSystem.enableColorMaterial();
            RenderSystem.enableFog();
            this.minecraft.getProfiler().pop();
            ci.cancel();
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;isFoggyAt(II)Z"))
    private boolean useThickFogGC(DimensionSpecialEffects skyProperties, int camX, int camY) {
        if (this.minecraft.level.dimension().equals(GalacticraftDimensions.MOON)) {
            //noinspection ConstantConditions
            return this.minecraft.level.getBiome(new BlockPos(lastCameraX, lastCameraY, lastCameraZ)).getSpecialEffects().getFogColor() == 1447446;
        }
        return skyProperties.isFoggyAt(camX, camY);
    }

    @Unique
    private float getStarBrightness(float delta) {
        final float var2 = this.level.getSunAngle(delta);
        float var3 = 1.0F - (Mth.cos((float) (var2 * Math.PI * 2.0D) * 2.0F + 0.25F));

        if (var3 < 0.0F) {
            var3 = 0.0F;
        }

        if (var3 > 1.0F) {
            var3 = 1.0F;
        }

        return var3 * var3 * 0.5F + 0.3F;
    }

    @Unique
    private void generateStarBufferMoon() {
        Random random = new Random(1671120782L);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION);
        for (int i = 0; i < 12000; ++i) {
            double j = random.nextFloat() * 2.0F - 1.0F;
            double k = random.nextFloat() * 2.0F - 1.0F;
            double l = random.nextFloat() * 2.0F - 1.0F;
            double m = 0.15F + random.nextFloat() * 0.1F;
            double n = j * j + k * k + l * l;

            if (n < 1.0D && n > 0.01D) {
                n = 1.0D / Math.sqrt(n);
                j *= n;
                k *= n;
                l *= n;
                double o = j * 100.0D;
                double p = k * 100.0D;
                double q = l * 100.0D;
                double r = Math.atan2(j, l);
                double s = Math.sin(r);
                double t = Math.cos(r);
                double u = Math.atan2(Math.sqrt(j * j + l * l), k);
                double v = Math.sin(u);
                double w = Math.cos(u);
                double x = random.nextDouble() * Math.PI * 2.0D;
                double y = Math.sin(x);
                double z = Math.cos(x);

                for (int a = 0; a < 4; ++a) {
                    double b = 0.0D;
                    double c = ((a & 2) - 1) * m;
                    double d = ((a + 1 & 2) - 1) * m;
                    double e = c * z - d * y;
                    double f = d * z + c * y;
                    double g = e * v + b * w;
                    double h = b * v - e * w;
                    double aa = h * s - f * t;
                    double ab = f * s + h * t;
                    buffer.vertex((o + aa) * (i > 6000 ? -1 : 1), (p + g) * (i > 6000 ? -1 : 1), (q + ab) * (i > 6000 ? -1 : 1)).endVertex();
                }
            }
        }
        buffer.end();
        starBufferMoon.upload(buffer);
    }

    @Override
    public void addChunkToRebuild(int x, int y, int z) {
        this.viewArea.setDirty(x, y, z, false);
    }
}