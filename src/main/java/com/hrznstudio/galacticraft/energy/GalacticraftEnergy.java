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

package com.hrznstudio.galacticraft.energy;

import alexiil.mc.lib.attributes.item.filter.ItemClassFilter;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.api.item.EnergyHolderItem;
import com.hrznstudio.galacticraft.items.OxygenTankItem;
import io.github.cottonmc.energy.CottonEnergy;
import io.github.cottonmc.energy.api.EnergyType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class GalacticraftEnergy {
    public static final EnergyType GALACTICRAFT_JOULES = new GalacticraftEnergyType();
    public static final EnergyType GALACTICRAFT_OXYGEN = new OxygenEnergyType();

    public static final ItemFilter ENERGY_HOLDER_ITEM_FILTER = new ItemClassFilter(EnergyHolderItem.class);

    public static void register() {
        Registry.register(CottonEnergy.ENERGY_REGISTRY, new Identifier(Constants.MOD_ID, Constants.Energy.GALACTICRAFT_JOULES), GALACTICRAFT_JOULES);
        Registry.register(CottonEnergy.ENERGY_REGISTRY, new Identifier(Constants.MOD_ID, Constants.Energy.GALACTICRAFT_OXYGEN), GALACTICRAFT_OXYGEN);
    }

    public static boolean isEnergyItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof EnergyHolderItem;
    }

    public static int getBatteryEnergy(ItemStack battery) {
        if (!isEnergyItem(battery)) {
            throw new IllegalArgumentException("Provided argument is not an energy item!");
        }

        return battery.hasTag() && battery.getTag().containsKey("Energy") ? battery.getTag().getInt("Energy") : Integer.MAX_VALUE;
    }

    public static int getMaxBatteryEnergy(ItemStack battery) {
        if (!isEnergyItem(battery)) {
            throw new IllegalArgumentException("Provided argument is not an energy item!");
        }

        return ((EnergyHolderItem) battery.getItem()).getMaxEnergy(battery);
    }

    public static void incrementEnergy(ItemStack stack, int energyToAdd) {
        int newEnergy = getBatteryEnergy(stack);
        newEnergy = Math.min(newEnergy + energyToAdd, getMaxBatteryEnergy(stack));

        setEnergy(stack, newEnergy);
    }

    public static void decrementEnergy(ItemStack stack, int energyToRemove) {
        int newEnergy = getBatteryEnergy(stack);
        newEnergy = Math.max(newEnergy - energyToRemove, 0);

        setEnergy(stack, newEnergy);
    }

    public static void setEnergy(ItemStack stack, int newEnergy) {
        if (!isEnergyItem(stack)) {
            throw new IllegalArgumentException("Provided argument is not an energy item!");
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("Energy", newEnergy);
        stack.setTag(tag);
        stack.setDamage(stack.getMaxDamage() - newEnergy);
    }

    public static boolean isOxygenItem(ItemStack itemStack) {
        if (!itemStack.hasTag()) {
            return false;
        }

        CompoundTag tag = itemStack.getTag();
        return tag.containsKey(OxygenTankItem.OXYGEN_NBT_KEY) && tag.containsKey(OxygenTankItem.MAX_OXYGEN_NBT_KEY);
    }
}
