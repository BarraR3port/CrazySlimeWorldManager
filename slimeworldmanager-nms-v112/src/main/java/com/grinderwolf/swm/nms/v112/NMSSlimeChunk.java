package com.grinderwolf.swm.nms.v112;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntArrayTag;
import com.grinderwolf.swm.api.utils.NibbleArray;
import com.grinderwolf.swm.api.world.SlimeChunk;
import com.grinderwolf.swm.api.world.SlimeChunkSection;
import com.grinderwolf.swm.nms.CraftSlimeChunkSection;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.server.v1_12_R1.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class NMSSlimeChunk implements SlimeChunk {
    
    private Chunk chunk;
    
    @Override
    public String getWorldName( ){
        return chunk.getWorld( ).getWorldData( ).getName( );
    }
    
    @Override
    public int getX( ){
        return chunk.locX;
    }
    
    @Override
    public int getZ( ){
        return chunk.locZ;
    }
    
    @Override
    public SlimeChunkSection[] getSections( ){
        SlimeChunkSection[] sections = new SlimeChunkSection[16];
        
        for ( int sectionId = 0; sectionId < chunk.getSections( ).length; sectionId++ ) {
            ChunkSection section = chunk.getSections( )[sectionId];
            
            if ( section != null ) {
                section.recalcBlockCounts( );
                
                if ( !section.a( ) ) { // If the section is empty, just ignore it to save space
                    // Block Light Nibble Array
                    NibbleArray blockLightArray = Converter.convertArray( section.getEmittedLightArray( ) );
                    
                    // Sky light Nibble Array
                    NibbleArray skyLightArray = Converter.convertArray( section.getSkyLightArray( ) );
                    
                    // Block Data
                    byte[] blocks = new byte[4096];
                    
                    net.minecraft.server.v1_12_R1.NibbleArray minecraftBlockDataArray = new net.minecraft.server.v1_12_R1.NibbleArray( );
                    DataPaletteBlock dataPaletteBlock = section.getBlocks( );
                    dataPaletteBlock.exportData( blocks , minecraftBlockDataArray );
                    
                    NibbleArray blockDataArray = Converter.convertArray( minecraftBlockDataArray );
                    
                    
                    
                    sections[sectionId] = new CraftSlimeChunkSection( sectionId , blocks , blockDataArray , null , null , null , null , blockLightArray , skyLightArray );
                }
            }
        }
        
        return sections;
    }
    
    @Override
    public int getMinSection( ){
        return 0;
    }
    
    @Override
    public int getMaxSection( ){
        return 256;
    }
    
    @Override
    public CompoundTag getHeightMaps( ){
        CompoundTag heightMapsCompound = new CompoundTag( "" , new CompoundMap( ) );
        heightMapsCompound.getValue( ).put( "heightMap" , new IntArrayTag( "heightMap" , chunk.heightMap ) );
        
        return heightMapsCompound;
    }
    
    @Override
    public int[] getBiomes( ){
        return toIntArray( chunk.getBiomeIndex( ) );
    }
    
    @Override
    public List < CompoundTag > getTileEntities( ){
        List < CompoundTag > tileEntities = new ArrayList <>( );
        
        for ( TileEntity entity : chunk.getTileEntities( ).values( ) ) {
            NBTTagCompound entityNbt = new NBTTagCompound( );
            entity.save( entityNbt );
            tileEntities.add( ( CompoundTag ) Converter.convertTag( "" , entityNbt ) );
        }
        
        return tileEntities;
    }
    
    @Override
    public List < CompoundTag > getEntities( ){
        List < CompoundTag > entities = new ArrayList <>( );
        
        for ( int i = 0; i < chunk.getEntitySlices( ).length; i++ ) {
            for ( Entity entity : chunk.getEntitySlices( )[i] ) {
                NBTTagCompound entityNbt = new NBTTagCompound( );
                
                if ( entity.d( entityNbt ) ) {
                    chunk.g( true );
                    entities.add( ( CompoundTag ) Converter.convertTag( "" , entityNbt ) );
                }
            }
        }
        
        return entities;
    }
    
    private static int[] toIntArray( byte[] buf ){
        ByteBuffer buffer = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN );
        int[] ret = new int[buf.length / 4];
        
        buffer.asIntBuffer( ).get( ret );
        
        return ret;
    }
}