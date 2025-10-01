package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HitNotifier extends Module {
    // Settings group
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Which hotbar slot to swap to on hit (0-8)
    private final Setting<Integer> swapSlot = sgGeneral.add(new IntSetting.Builder()
        .name("swap-slot")
        .description("The hotbar slot to swap to when you hit an entity (0-8).")
        .defaultValue(3)
        .min(0)
        .max(8)
        .sliderMax(8)
        .build()
    );

    // Whether to swap back after a delay
    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Swap back to the original slot after the configured delay.")
        .defaultValue(true)
        .build()
    );

    // Delay in ticks before swapping back
    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Ticks to wait before swapping back.")
        .defaultValue(1)
        .min(1)
        .max(20)
        .sliderMax(10)
        .build()
    );

    // Internal queue for pending swap-backs
    private final List<ScheduledSwap> scheduled = new LinkedList<>();

    public HitNotifier() {
        super(MUtils.CATEGORY, "Attribute Swap", "Swaps to a configured hotbar slot when you hit an entity, and optionally swaps back.");
    }

    @EventHandler
    private void onAttack(AttackEntityEvent event) {
        ///if (mc.player == null || event.entity == null) return;
        handleSwap();
    }
    @EventHandler
    private void onBlockBreak(StartBreakingBlockEvent event) {
        ///if (mc.player == null || event.getClass().getName() == null) return;
        handleSwap();
    }


    private void handleSwap() {
        ///if (mc.player == null ||  == null) return;

        // Save original selected slot (capture final for lambda/queue)
        final int originalSlot = mc.player.getInventory().getSelectedSlot(); ///Fix Possible NullPointerException
        final int target = swapSlot.get();

        // If target is same as original, do nothing
        if (target == originalSlot) return;

        // Swap to the configured slot immediately
        InvUtils.swap(target, false);

        // If swap-back disabled, we're done
        if (!swapBack.get()) return;

        // Schedule swap-back after N ticks
        int ticks = delayTicks.get();
        synchronized (scheduled) {
            scheduled.add(new ScheduledSwap(originalSlot, ticks));
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        synchronized (scheduled) {
            if (scheduled.isEmpty()) return;

            Iterator<ScheduledSwap> it = scheduled.iterator();
            while (it.hasNext()) {
                ScheduledSwap s = it.next();
                s.ticks--;
                if (s.ticks <= 0) {
                    // Perform the swap-back on main thread (we are on tick thread)
                    // Use InvUtils.swap to keep behavior consistent
                    InvUtils.swap(s.originalSlot, false);
                    it.remove();
                }
            }
        }
    }

    // Small helper for scheduled swaps
    private static class ScheduledSwap {
        final int originalSlot;
        int ticks;

        ScheduledSwap(int originalSlot, int ticks) {
            this.originalSlot = originalSlot;
            this.ticks = ticks;
        }
    }
}
