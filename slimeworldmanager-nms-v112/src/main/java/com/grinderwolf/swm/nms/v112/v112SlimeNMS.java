package com.grinderwolf.swm.nms.v112;

import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.nms.CraftSlimeWorld;
import com.grinderwolf.swm.nms.SlimeNMS;
import com.grinderwolf.swm.nms.VoidGenerator;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.DimensionManager;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.WorldServer;
import net.minecraft.server.v1_12_R1.WorldSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

@Getter
public class v112SlimeNMS implements SlimeNMS {
    
    private static final Logger LOGGER = LogManager.getLogger("SWM");
    public static boolean IS_PAPER;
    
    private final byte worldVersion = 0x03;
    
    private boolean loadingDefaultWorlds = true; // If true, the addWorld method will not be skipped
    
    private WorldServer defaultWorld;
    private WorldServer defaultNetherWorld;
    private WorldServer defaultEndWorld;
    
    public v112SlimeNMS(boolean isPaper) {
        IS_PAPER = isPaper;
        try {
            CraftCLSMBridge.initialize(this);
        }  catch (NoClassDefFoundError ex) {
            LOGGER.error("Failed to find ClassModifier classes. Are you sure you installed it correctly?");
            System.exit(1); // No ClassModifier, no party
        }
    }
    
    @Override
    public void setDefaultWorlds(SlimeWorld normalWorld, SlimeWorld netherWorld, SlimeWorld endWorld) {
        if (normalWorld != null) {
            World.Environment env = World.Environment.valueOf(normalWorld.getPropertyMap().getValue(SlimeProperties.ENVIRONMENT).toUpperCase());
            
            if (env != World.Environment.NORMAL) {
                LOGGER.warn("The environment for the default world must always be 'NORMAL'.");
            }
            
            defaultWorld = new CustomWorldServer((CraftSlimeWorld) normalWorld, new CustomDataManager(normalWorld), 0);
        }
        
        if (netherWorld != null) {
            World.Environment env = World.Environment.valueOf(netherWorld.getPropertyMap().getValue(SlimeProperties.ENVIRONMENT).toUpperCase());
            defaultNetherWorld = new CustomWorldServer((CraftSlimeWorld) netherWorld, new CustomDataManager(netherWorld), env.getId());
        }
        
        if (endWorld != null) {
            World.Environment env = World.Environment.valueOf(endWorld.getPropertyMap().getValue(SlimeProperties.ENVIRONMENT).toUpperCase());
            defaultEndWorld = new CustomWorldServer((CraftSlimeWorld) endWorld, new CustomDataManager(endWorld), env.getId());
        }
        
        loadingDefaultWorlds = false;
    }
    
    
    public Object createNMSWorld(SlimeWorld world) {
        CustomDataManager dataManager = new CustomDataManager(world);
        MinecraftServer mcServer = MinecraftServer.getServer();
        
        int dimension = CraftWorld.CUSTOM_DIMENSION_OFFSET + mcServer.worlds.size();
        boolean used = false;
        
        do {
            for (WorldServer server : mcServer.worlds) {
                used = server.dimension == dimension;
                
                if (used) {
                    dimension++;
                    break;
                }
            }
        } while (used);
        
        return new CustomWorldServer((CraftSlimeWorld) world, dataManager, dimension).b();
    }
    
    @Override
    public void generateWorld(SlimeWorld world) {
        /*String worldName = world.getName();
    
        if (Bukkit.getWorld(worldName) != null) {
            throw new IllegalArgumentException("World " + worldName + " already exists! Maybe it's an outdated SlimeWorld object?");
        }
        MinecraftServer mcServer = MinecraftServer.getServer();
    
        CustomDataManager dataManager = new CustomDataManager(world);
        int dimension = CraftWorld.CUSTOM_DIMENSION_OFFSET + mcServer.worlds.size();
        boolean used = false;
    
        do {
            for (WorldServer server : mcServer.worlds) {
                used = server.dimension == dimension;
            
                if (used) {
                    dimension++;
                    break;
                }
            }
        } while (used);
        
        CustomWorldServer server = new CustomWorldServer((CraftSlimeWorld) world, dataManager, dimension);
    
        LOGGER.info("Loading world " + worldName);
        long startTime = System.currentTimeMillis();
    
        server.setReady(true);
        mcServer.server.addWorld(server.getWorld());
        mcServer.worlds.add(server);
    
    
        Bukkit.getPluginManager().callEvent(new WorldInitEvent(server.getWorld()));
    
        if (server.getWorld().getKeepSpawnInMemory()) {
            LOGGER.debug("Preparing start region for dimension '{}'/{}", worldName, DimensionManager.a(0));
        }
    
        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(server.getWorld()));
    
        LOGGER.info("World " + worldName + " loaded in " + (System.currentTimeMillis() - startTime) + "ms.");*/
        WorldCreator creator = new WorldCreator(world.getName());
        creator.generator(new VoidGenerator());
        creator.createWorld();
        //addWorldToServerList(createNMSWorld(world));
    }
    
    
    public void addWorldToServerList(Object worldObject) {
        if (!(worldObject instanceof WorldServer)) {
            throw new IllegalArgumentException("World object must be an instance of WorldServer!");
        }
        
        CustomWorldServer server = (CustomWorldServer) worldObject;
        String worldName = server.getWorldData().getName();
        
        if (Bukkit.getWorld(worldName) != null) {
            throw new IllegalArgumentException("World " + worldName + " already exists! Maybe it's an outdated SlimeWorld object?");
        }
        
        LOGGER.info("Loading world " + worldName);
        long startTime = System.currentTimeMillis();
        
        server.setReady(true);
        MinecraftServer mcServer = MinecraftServer.getServer();
        
        mcServer.server.addWorld(server.getWorld());
        mcServer.worlds.add(server);
        
        Bukkit.getPluginManager().callEvent(new WorldInitEvent(server.getWorld()));
        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(server.getWorld()));
        
        LOGGER.info("World " + worldName + " loaded in " + (System.currentTimeMillis() - startTime) + "ms.");
    }
    
    @Override
    public SlimeWorld getSlimeWorld(World world) {
        CraftWorld craftWorld = (CraftWorld) world;
        
        if (!(craftWorld.getHandle( ) instanceof CustomWorldServer)) {
            return null;
        }
        CustomWorldServer  worldServer = (CustomWorldServer) craftWorld.getHandle();
        return worldServer.getSlimeWorld();
    }
}