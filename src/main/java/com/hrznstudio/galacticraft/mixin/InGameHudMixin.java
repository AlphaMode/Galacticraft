/*
 * Copyright (c) 2019 HRZN LTD
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

package com.hrznstudio.galacticraft.mixin;

import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.accessor.GCPlayerAccessor;
import com.hrznstudio.galacticraft.api.atmosphere.AtmosphericGas;
import com.hrznstudio.galacticraft.api.celestialbodies.CelestialBodyType;
import com.hrznstudio.galacticraft.items.OxygenTankItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    private static final int OXYGEN_X = 0;
    private static final int OXYGEN_Y = 40;
    private static final int OXYGEN_WIDTH = 12;
    private static final int OXYGEN_HEIGHT = 40;
    private static final int OXYGEN_OVERLAY_X = 24;
    private static final int OXYGEN_OVERLAY_Y = 80;
    @Shadow
    private int scaledWidth;
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void draw(float float_1, CallbackInfo ci) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (CelestialBodyType.getByDimType(client.player.world.dimension.getType()).isPresent() && !CelestialBodyType.getByDimType(client.player.world.dimension.getType()).get().getAtmosphere().getComposition().containsKey(AtmosphericGas.OXYGEN)) {
            DiffuseLighting.enableGuiDepthLighting();
            client.getTextureManager().bindTexture(new Identifier(Constants.MOD_ID, Constants.ScreenTextures.getRaw(Constants.ScreenTextures.OVERLAY)));

            this.blit(this.scaledWidth - 17, 5, OXYGEN_X, OXYGEN_Y, OXYGEN_WIDTH, OXYGEN_HEIGHT);
            this.blit(this.scaledWidth - 34, 5, OXYGEN_X, OXYGEN_Y, OXYGEN_WIDTH, OXYGEN_HEIGHT);

            if (!client.player.isCreative()) {
                FullFixedItemInv gearInventory = ((GCPlayerAccessor) this.client.player).getGearInventory();
                if (gearInventory.getInvStack(6).getItem() instanceof OxygenTankItem) {
                    this.blit(this.scaledWidth - 17 + OXYGEN_WIDTH, 5 + OXYGEN_HEIGHT, OXYGEN_OVERLAY_X, OXYGEN_OVERLAY_Y, -OXYGEN_WIDTH, (int) -((double) OXYGEN_HEIGHT - ((double) OXYGEN_HEIGHT * (((double) gearInventory.getInvStack(6).getMaxDamage() - (double) gearInventory.getInvStack(6).getDamage()) / (double) gearInventory.getInvStack(6).getMaxDamage()))));
                } else if (client.player.isCreative()) {
                    this.blit(this.scaledWidth - 17 + OXYGEN_WIDTH, 5 + OXYGEN_HEIGHT, OXYGEN_OVERLAY_X, OXYGEN_OVERLAY_Y, -OXYGEN_WIDTH, -OXYGEN_HEIGHT);
                }
                if (gearInventory.getInvStack(7).getItem() instanceof OxygenTankItem) {
                    this.blit(this.scaledWidth - 34 + OXYGEN_WIDTH, 5 + OXYGEN_HEIGHT, OXYGEN_OVERLAY_X, OXYGEN_OVERLAY_Y, -OXYGEN_WIDTH, (int) -((double) OXYGEN_HEIGHT - ((double) OXYGEN_HEIGHT * (((double) gearInventory.getInvStack(7).getMaxDamage() - (double) gearInventory.getInvStack(7).getDamage()) / (double) gearInventory.getInvStack(7).getMaxDamage()))));
                } else if (client.player.isCreative()) {
                    this.blit(this.scaledWidth - 34 + OXYGEN_WIDTH, 5 + OXYGEN_HEIGHT, OXYGEN_OVERLAY_X, OXYGEN_OVERLAY_Y, -OXYGEN_WIDTH, -OXYGEN_HEIGHT);
                }
            } else {
                this.blit(this.scaledWidth - 17, 5, 12, 40, OXYGEN_WIDTH, OXYGEN_HEIGHT);
                this.blit(this.scaledWidth - 34, 5, 12, 40, OXYGEN_WIDTH, OXYGEN_HEIGHT);
            }
        }
    }
}
