package com.grinderwolf.swm.nms.v1181;

import com.flowpowered.nbt.CompoundTag;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.nms.CraftSlimeWorld;
import com.grinderwolf.swm.nms.SlimeNMS;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import lombok.Getter;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class v1181SlimeNMS implements SlimeNMS {

    private static final Logger LOGGER = LogManager.getLogger("SWM");
    private static final File UNIVERSE_DIR;
    public static LevelStorageSource CONVERTABLE;
    private static boolean isPaperMC;

    static {
        Path path;

        try {
            path = Files.createTempDirectory("swm-" + UUID.randomUUID().toString().substring(0, 5) + "-");
        } catch (IOException ex) {
//            LOGGER.log(Level.FATAL, "Failed to create temp directory", ex);
            path = null;
            System.exit(1);
        }

        UNIVERSE_DIR = path.toFile();
        CONVERTABLE = LevelStorageSource.createDefault(path);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            try {
                FileUtils.deleteDirectory(UNIVERSE_DIR);
            } catch (IOException ex) {
//                LOGGER.log(Level.FATAL, "Failed to delete temp directory", ex);
            }

        }));
    }

    private final byte worldVersion = 0x07;

    private boolean loadingDefaultWorlds = true; // If true, the addWorld method will not be skipped

    private CustomWorldServer defaultWorld;
    private CustomWorldServer defaultNetherWorld;
    private CustomWorldServer defaultEndWorld;

    public v1181SlimeNMS(boolean isPaper) {
        try {
            isPaperMC = isPaper;
//            CraftCLSMBridge.initialize(this);
        } catch (NoClassDefFoundError ex) {
            LOGGER.error("Failed to find ClassModifier classes. Are you sure you installed it correctly?", ex);
            Bukkit.getServer().shutdown();
        }
    }

    @Override
    public void setDefaultWorlds(SlimeWorld normalWorld, SlimeWorld netherWorld, SlimeWorld endWorld) {
        if (normalWorld != null) {
            defaultWorld = createDefaultWorld(normalWorld, LevelStem.OVERWORLD, ServerLevel.OVERWORLD);
        }

        if (netherWorld != null) {
            defaultNetherWorld = createDefaultWorld(netherWorld, LevelStem.NETHER, ServerLevel.NETHER);
        }

        if (endWorld != null) {
            defaultEndWorld = createDefaultWorld(endWorld, LevelStem.END, ServerLevel.END);
        }

        loadingDefaultWorlds = false;
    }

    private CustomWorldServer createDefaultWorld(SlimeWorld world, ResourceKey<LevelStem> dimensionKey,
                                                 ResourceKey<Level> worldKey) {
        PrimaryLevelData worldDataServer = createWorldData(world);

        MappedRegistry<LevelStem> registryMaterials = worldDataServer.worldGenSettings().dimensions();
        LevelStem worldDimension = registryMaterials.get(dimensionKey);
        DimensionType dimensionManager = worldDimension.type();
        ChunkGenerator chunkGenerator = worldDimension.generator();

        World.Environment environment = getEnvironment(world);

        if (dimensionKey == LevelStem.NETHER && environment != World.Environment.NORMAL) {
//            LOGGER.warn("The environment for the default world should always be 'NORMAL'.");
        }

        try {
            return new CustomWorldServer((CraftSlimeWorld) world, worldDataServer,
                    worldKey, dimensionKey, dimensionManager, chunkGenerator, environment);
        } catch (IOException ex) {
            throw new RuntimeException(ex); // TODO do something better with this?
        }
    }

    @Override
    public void generateWorld(SlimeWorld world) {
        String worldName = world.getName();

        if (Bukkit.getWorld(worldName) != null) {
            throw new IllegalArgumentException("World " + worldName + " already exists! Maybe it's an outdated SlimeWorld object?");
        }

        System.out.println("WORLD: " + world);
        PrimaryLevelData worldDataServer = createWorldData(world);
        System.out.println("WDS: " + worldDataServer);
        World.Environment environment = getEnvironment(world);
        ResourceKey<LevelStem> dimension;

        switch(environment) {
            case NORMAL:
                dimension = LevelStem.OVERWORLD;
                break;
            case NETHER:
                dimension = LevelStem.NETHER;
                break;
            case THE_END:
                dimension = LevelStem.END;
                break;
            default:
                throw new IllegalArgumentException("Unknown dimension supplied");
        }

        MappedRegistry<LevelStem> materials = worldDataServer.worldGenSettings().dimensions();
        LevelStem worldDimension = materials.get(dimension);
        DimensionType dimensionManager = worldDimension.type();
        ChunkGenerator chunkGenerator = worldDimension.generator();

        ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY,
                new ResourceLocation(worldName.toLowerCase(java.util.Locale.ENGLISH)));

        CustomWorldServer server;

        try {
            server = new CustomWorldServer((CraftSlimeWorld) world, worldDataServer,
                    worldKey, dimension, dimensionManager, chunkGenerator, environment);
        } catch (IOException ex) {
            throw new RuntimeException(ex); // TODO do something better with this?
        }

        EndDragonFight dragonBattle = server.dragonFight();
        boolean runBattle = world.getPropertyMap().getValue(SlimeProperties.DRAGON_BATTLE);

        if(dragonBattle != null && !runBattle) {
            dragonBattle.dragonEvent.setVisible(false);
            // todo the dragon event should be removed from the world
        }

        server.setReady(true);

        MinecraftServer mcServer = MinecraftServer.getServer();
        mcServer.initWorld(server, worldDataServer, mcServer.getWorldData(), worldDataServer.worldGenSettings());

        mcServer.server.addWorld(server.getWorld());
        mcServer.levels.put(worldKey, server);

        server.setSpawnSettings(world.getPropertyMap().getValue(SlimeProperties.ALLOW_MONSTERS), world.getPropertyMap().getValue(SlimeProperties.ALLOW_ANIMALS));

        Bukkit.getPluginManager().callEvent(new WorldInitEvent(server.getWorld()));
        mcServer.prepareLevels(server.chunkSource.chunkMap.progressListener, server);
//        try {
//            world.getLoader().loadWorld(worldName, world.isReadOnly());
//        } catch(UnknownWorldException | WorldInUseException | IOException e) {
//            e.printStackTrace();
//        }
        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(server.getWorld()));
    }

    private World.Environment getEnvironment(SlimeWorld world) {
        return World.Environment.valueOf(world.getPropertyMap().getValue(SlimeProperties.ENVIRONMENT).toUpperCase());
    }

    private PrimaryLevelData createWorldData(SlimeWorld world) {
        String worldName = world.getName();
        CompoundTag extraData = world.getExtraData();
        PrimaryLevelData worldDataServer;
        net.minecraft.nbt.CompoundTag extraTag = (net.minecraft.nbt.CompoundTag) Converter.convertTag(extraData);
        MinecraftServer mcServer = MinecraftServer.getServer();
        DedicatedServerProperties serverProps = ((DedicatedServer) mcServer).getProperties();

        if (extraTag.getTagType("LevelData") == Tag.TAG_COMPOUND) {
            net.minecraft.nbt.CompoundTag levelData = extraTag.getCompound("LevelData");
            int dataVersion = levelData.getTagType("DataVersion") == Tag.TAG_INT ? levelData.getInt("DataVersion") : -1;
            Dynamic<Tag> dynamic = mcServer.getFixerUpper().update(DataFixTypes.LEVEL.getType(),
                    new Dynamic<>(NbtOps.INSTANCE, levelData), dataVersion, SharedConstants.getCurrentVersion()
                            .getWorldVersion());

            LevelVersion levelVersion = LevelVersion.parse(dynamic);
            LevelSettings worldSettings = LevelSettings.parse(dynamic, mcServer.datapackconfiguration);

            worldDataServer = PrimaryLevelData.parse(dynamic, mcServer.getFixerUpper(), dataVersion, null,
                    worldSettings, levelVersion, serverProps.getWorldGenSettings(mcServer.registryHolder), Lifecycle.stable());
        } else {

            // Game rules
            Optional<CompoundTag> gameRules = extraData.getAsCompoundTag("gamerules");
            GameRules rules = new GameRules();

            gameRules.ifPresent(compoundTag -> {
                net.minecraft.nbt.CompoundTag compound = ((net.minecraft.nbt.CompoundTag) Converter.convertTag(compoundTag));
                Map<String, GameRules.Key<?>> gameRuleKeys = CraftWorld.getGameRulesNMS();

                compound.getAllKeys().forEach(gameRule -> {
                    if(gameRuleKeys.containsKey(gameRule)) {
                        GameRules.Value<?> gameRuleValue = rules.getRule(gameRuleKeys.get(gameRule));
                        String theValue = compound.getString(gameRule);
                        gameRuleValue.deserialize(theValue);
                        gameRuleValue.onChanged(mcServer);
                    }
                });
            });

            LevelSettings worldSettings = new LevelSettings(worldName, serverProps.gamemode, false,
                    serverProps.difficulty, false, rules, mcServer.datapackconfiguration);

            worldDataServer = new PrimaryLevelData(worldSettings, serverProps.getWorldGenSettings(mcServer.registryHolder), Lifecycle.stable());
        }

        worldDataServer.checkName(worldName);
        worldDataServer.setModdedInfo(mcServer.getServerModName(), mcServer.getModdedStatus().shouldReportAsModified());
        worldDataServer.setInitialized(true);

        return worldDataServer;
    }

    @Override
    public SlimeWorld getSlimeWorld(World world) {
        CraftWorld craftWorld = (CraftWorld) world;

        if (!(craftWorld.getHandle() instanceof CustomWorldServer)) {
            return null;
        }

        CustomWorldServer worldServer = (CustomWorldServer) craftWorld.getHandle();
        return worldServer.getSlimeWorld();
    }

    @Override
    public CompoundTag convertChunk(CompoundTag tag) {
        net.minecraft.nbt.CompoundTag nmsTag = (net.minecraft.nbt.CompoundTag) Converter.convertTag(tag);
        int version = nmsTag.getInt("DataVersion");

        net.minecraft.nbt.CompoundTag newNmsTag = NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.CHUNK, nmsTag, version);

        return (CompoundTag) Converter.convertTag("", newNmsTag);
    }
}