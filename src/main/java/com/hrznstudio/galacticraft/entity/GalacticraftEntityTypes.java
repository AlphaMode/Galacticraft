package com.hrznstudio.galacticraft.entity;

import com.hrznstudio.galacticraft.Constants;
import com.hrznstudio.galacticraft.Galacticraft;
import com.hrznstudio.galacticraft.entity.moonvillager.EntityMoonVillager;
import com.hrznstudio.galacticraft.entity.moonvillager.T1RocketEntity;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class GalacticraftEntityTypes {

    public static final EntityType<EntityMoonVillager> MOON_VILLAGER = FabricEntityTypeBuilder.create(EntityCategory.CREATURE, EntityMoonVillager::new).build();
    public static final EntityType<T1RocketEntity> T1_ROCKET = Registry.register(Registry.ENTITY_TYPE, new Identifier(Constants.MOD_ID, Constants.Entities.T1_ROCKET), FabricEntityTypeBuilder.create(EntityCategory.MISC, T1RocketEntity::new).size(EntityDimensions.fixed(2, 6)).build());

    public static void register() {
        Registry.register(Registry.ENTITY_TYPE, new Identifier(Constants.Entities.MOON_VILLAGER), MOON_VILLAGER);
        Galacticraft.logger.info("Registered entity types!");
    }
}
