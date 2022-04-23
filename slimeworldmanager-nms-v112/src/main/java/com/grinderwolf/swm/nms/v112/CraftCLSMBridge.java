package com.grinderwolf.swm.nms.v112;

import com.grinderwolf.swm.clsm.CLSMBridge;
import com.grinderwolf.swm.clsm.ClassModifier;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_12_R1.WorldServer;

@RequiredArgsConstructor
public class CraftCLSMBridge implements CLSMBridge {
    
    private final v112SlimeNMS nmsInstance;
    
    @Override
    public Object[] getDefaultWorlds() {
        WorldServer defaultWorld = nmsInstance.getDefaultWorld();
        WorldServer netherWorld = nmsInstance.getDefaultNetherWorld();
        WorldServer endWorld = nmsInstance.getDefaultEndWorld();
        
        if (defaultWorld != null || netherWorld != null || endWorld != null) {
            return new WorldServer[] { defaultWorld, netherWorld, endWorld };
        }
        
        // Returning null will just run the original load world method
        return null;
    }
    
    @Override
    public boolean isCustomWorld(Object world) {
        return world instanceof CustomWorldServer;
    }
    
    @Override
    public boolean skipWorldAdd(Object world) {
        if (!isCustomWorld(world) || nmsInstance.isLoadingDefaultWorlds()) {
            return false;
        }
        
        CustomWorldServer worldServer = (CustomWorldServer) world;
        return !worldServer.isReady();
    }
    
    static void initialize(v112SlimeNMS instance) {
        ClassModifier.setLoader(new CraftCLSMBridge(instance));
    }
}