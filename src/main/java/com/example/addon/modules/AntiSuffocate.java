package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;

public class AntiSuffocate extends Module {

    public AntiSuffocate() {
        super(MUtils.CATEGORY, "AntiSuffocate", "Breaks the Block you are currently suffocating in. Auto Tool recommended");
    }

    @EventHandler
    private void onSuffocation(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Only break if actually suffocating
        if (mc.player.isInsideWall()) {
            BlockUtils.breakBlock(mc.player.getBlockPos(), false);
        }
    }
}
