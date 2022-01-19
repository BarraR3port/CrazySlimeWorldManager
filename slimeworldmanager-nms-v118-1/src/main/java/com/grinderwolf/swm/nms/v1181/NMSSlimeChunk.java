package com.grinderwolf.swm.nms.v1181;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.LongArrayTag;
import com.grinderwolf.swm.api.utils.NibbleArray;
import com.grinderwolf.swm.api.world.SlimeChunk;
import com.grinderwolf.swm.api.world.SlimeChunkSection;
import com.grinderwolf.swm.nms.CraftSlimeChunkSection;
import io.netty.buffer.UnpooledByteBufAllocator;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class NMSSlimeChunk implements SlimeChunk {

    private LevelChunk chunk;

    @Override
    public String getWorldName() {
        return chunk.getLevel().getMinecraftWorld().serverLevelData.getLevelName();
    }

    @Override
    public int getX() {
        return chunk.getPos().x;
    }

    @Override
    public int getZ() {
        return chunk.getPos().z;
    }

    @Override
    public SlimeChunkSection[] getSections() {
        SlimeChunkSection[] sections = new SlimeChunkSection[16];
        LevelLightEngine lightEngine = chunk.getLevel().getChunkSource().getLightEngine();

        for (int sectionId = 0; sectionId < chunk.getSections().length; sectionId++) {
            LevelChunkSection section = chunk.getSections()[sectionId];

            if (section != null) {
                section.recalcBlockCounts();

                if (section.hasOnlyAir()) { // If the section is empty, just ignore it to save space
                    // Block Light Nibble Array
                    NibbleArray blockLightArray = Converter.convertArray(lightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunk.getPos(), sectionId)));

                    // Sky light Nibble Array
                    NibbleArray skyLightArray = Converter.convertArray(lightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunk.getPos(), sectionId)));

                    // Tile/Entity Data

                    // Block Data
                    PalettedContainer<BlockState> dataPaletteBlock = section.states;

                    final UnpooledByteBufAllocator allocator = new UnpooledByteBufAllocator(false);
                    FriendlyByteBuf blockStates = new FriendlyByteBuf(allocator.buffer());
                    FriendlyByteBuf biomes = new FriendlyByteBuf(allocator.buffer());

                    section.states.write(blockStates);
                    section.getBiomes().write(biomes);

                    sections[sectionId] = new CraftSlimeChunkSection(null, null, null, null, blockStates.array(), biomes.array(), blockLightArray, skyLightArray);
                }
            }
        }

        return sections;
    }

    @Override
    public CompoundTag getHeightMaps() {
        // HeightMap
        CompoundMap heightMaps = new CompoundMap();

        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.heightmaps.entrySet()) {
            Heightmap.Types type = entry.getKey();
            Heightmap map = entry.getValue();

            heightMaps.put(type.name(), new LongArrayTag(type.name(), map.getRawData()));
        }

        return new CompoundTag("", heightMaps);
    }

    @Override
    public int[] getBiomes() {
        return new int[0]; // todo biomes stored in sections now, could merge together here
    }

    @Override
    public List<CompoundTag> getTileEntities() {
        List<CompoundTag> tileEntities = new ArrayList<>();

        for (BlockEntity entity : chunk.blockEntities.values()) {
            final net.minecraft.nbt.CompoundTag entityNbt = entity.saveWithFullMetadata();
            tileEntities.add((CompoundTag) Converter.convertTag(entityNbt.getString("id"), entityNbt));
        }

        return tileEntities;
    }

    @Override
    public List<CompoundTag> getEntities() {
        List<CompoundTag> entities = new ArrayList<>();

        PersistentEntitySectionManager<Entity> entityManager = chunk.level.entityManager;
        Iterator<Entity> entitySlices = entityManager.getEntityGetter().getAll().iterator();

        while (entitySlices.hasNext()) {
            Entity entity = entitySlices.next();

            ChunkPos chunkPos = chunk.getPos();
            ChunkPos entityPos = entity.chunkPosition();

            if (chunkPos.x == entityPos.x && chunkPos.z == entityPos.z) {
                net.minecraft.nbt.CompoundTag entityNbt = new net.minecraft.nbt.CompoundTag();
                if (entity.save(entityNbt)) {
                    chunk.setLightCorrect(true);
                    entities.add((CompoundTag) Converter.convertTag("", entityNbt));
                }
            }
        }
        return entities;
    }
}
