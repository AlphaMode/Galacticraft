/*
 * Copyright (c) 2020 HRZN LTD
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
 *
 */

package com.hrznstudio.galacticraft.world.biome.moon.mare;

import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.block.GalacticraftBlocks;
import com.hrznstudio.galacticraft.world.gen.surfacebuilder.GalacticraftSurfaceBuilders;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.ChanceDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.DecoratedFeature;
import net.minecraft.world.gen.feature.ForestRockFeatureConfig;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

public final class MoonMareRocksBiome extends MoonMareBiome {

    public static final TernarySurfaceConfig MOON_MARE_ROCK_CONFIG = new TernarySurfaceConfig(GalacticraftBlocks.MOON_BASALT.getDefaultState(), GalacticraftBlocks.MOON_BASALT.getDefaultState(), GalacticraftBlocks.MOON_BASALT.getDefaultState());

    public MoonMareRocksBiome() {
        super((new Settings())
                .configureSurfaceBuilder(GalacticraftSurfaceBuilders.MOON_SURFACE_BUILDER, MOON_MARE_ROCK_CONFIG)
                .precipitation(Precipitation.NONE)
                .category(Category.NONE)
                .depth(0.65F)
                .scale(0.0065F)
                .temperature(-100.0F)
                .downfall(0.00002F)
                .effects(new BiomeEffects.Builder()
                        .waterColor(9937330)
                        .waterFogColor(11243183)
                        .fogColor(0)
                        .build())
                .parent(Constants.MOD_ID + ":" + Constants.Biomes.MOON_HIGHLANDS_ROCKS));
        this.addFeature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, DecoratedFeature.FOREST_ROCK.configure(new ForestRockFeatureConfig(GalacticraftBlocks.MOON_BASALT.getDefaultState(), 6)).createDecoratedFeature(Decorator.WATER_LAKE.configure(new ChanceDecoratorConfig(4))));
    }

    @Override
    protected String getBiomeName() {
        return "rocks";
    }
}
