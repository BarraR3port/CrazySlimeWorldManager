package com.grinderwolf.swm.nms;

import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.ListTag;
import com.grinderwolf.swm.api.utils.NibbleArray;
import com.grinderwolf.swm.api.world.SlimeChunkSection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CraftSlimeChunkSection implements SlimeChunkSection {

    private final int sectionIndex;

    // Pre 1.13 block data
    private final byte[] blocks;
    private final NibbleArray data;

    // Post 1.13 block data
    private final ListTag<CompoundTag> palette;
    private final long[] blockStates;

    // Post 1.17 block data
    private final CompoundTag blockStatesTag;
    private final CompoundTag biomeTag;

    private final NibbleArray blockLight;
    private final NibbleArray skyLight;
}
