package com.grinderwolf.swm.nms.v112;

import com.flowpowered.nbt.CompoundTag;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.nms.CraftSlimeWorld;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;

import java.util.Optional;

@Getter
public class CustomWorldData extends WorldData {

    private final CraftSlimeWorld world;
    private final WorldType type;

    CustomWorldData(CraftSlimeWorld world) {
        this.world = world;
        this.type = WorldType.getType(world.getPropertyMap().getValue(SlimeProperties.WORLD_TYPE).toUpperCase());
        this.setGameType(EnumGamemode.NOT_SET);

        // Game rules
        CompoundTag extraData = world.getExtraData();
        Optional<CompoundTag> gameRules = extraData.getAsCompoundTag("gamerules");
        gameRules.ifPresent(compoundTag -> this.w().a((NBTTagCompound) Converter.convertTag(compoundTag)));
    }

    @Override
    public String getName() {
        return world.getName();
    }

}