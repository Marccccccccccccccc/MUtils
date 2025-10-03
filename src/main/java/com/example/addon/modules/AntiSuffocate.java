package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;

public class AntiSuffocate extends Module {

    public AntiSuffocate() {
        super(MUtils.CATEGORY, "AntiSuffocate", "Breaks the Block you are currently suffocating in. Auto Tool recommended");
    }

    @EventHandler
    private void onSuffocation(LivingEntityMoveEvent event) {
        if (mc.player != null && mc.player.isInsideWall()) {
            BlockUtils.breakBlock(mc.player.getBlockPos(), false);
        }
    }
}
/// TODO: Fix Block Breaking happening every tick
/// TODO: Fix Possible Crash (idk if this module caused it)

