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

package com.hrznstudio.galacticraft.world.gen;

import com.hrznstudio.galacticraft.blocks.GalacticraftBlocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.LakeDecoratorConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.LakeFeatureConfig;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class OilPoolGenerator {

    public static void registerOilLake() {
        for (Biome biome : Biome.BIOMES) {
            if (!biome.getCategory().equals(Biomes.NETHER.getCategory()) && !biome.getCategory().equals(Biomes.THE_END.getCategory())) {

                if (biome.getCategory() == Biome.Category.DESERT) {
                    biome.addFeature(GenerationStep.Feature.UNDERGROUND_DECORATION, Biome.configureFeature(Feature.LAKE, new LakeFeatureConfig(GalacticraftBlocks.CRUDE_OIL.getDefaultState()), Decorator.WATER_LAKE, new LakeDecoratorConfig(2)));
                } else {
                    biome.addFeature(GenerationStep.Feature.UNDERGROUND_DECORATION, Biome.configureFeature(Feature.LAKE, new LakeFeatureConfig(GalacticraftBlocks.CRUDE_OIL.getDefaultState()), Decorator.WATER_LAKE, new LakeDecoratorConfig(1)));
                }
            }
        }
    }
}
