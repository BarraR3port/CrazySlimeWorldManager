package com.grinderwolf.swm.plugin.upgrade;

import com.grinderwolf.swm.nms.CraftSlimeWorld;

public interface Upgrade {

    void upgrade(CraftSlimeWorld world);

    @Deprecated()
    default void downgrade(CraftSlimeWorld world) {
        throw new UnsupportedOperationException("Not implemented anymore.");
    }
}
