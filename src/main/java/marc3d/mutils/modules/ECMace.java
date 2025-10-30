/*

package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import marc3d.mutils.utils.CloseWithoutPacket;
import marc3d.mutils.utils.other.PacketUtils;
import marc3d.mutils.utils.other.TargetUtils;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;

import java.util.List;
import java.util.Set;

public class ECMace extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgTiming = settings.createGroup("Timing");

    private int previousSlot = -1;

    // Slot settings
    private final Setting<Integer> maceSlot = sgGeneral.add(new IntSetting.Builder()
        .name("mace-slot")
        .description("Which Ender Chest slot contains the mace (0-26).")
        .defaultValue(0)
        .range(0, 26)
        .sliderRange(0, 26)
        .build()
    );

    private final Setting<Integer> hotbarSlot = sgGeneral.add(new IntSetting.Builder()
        .name("hotbar-slot")
        .description("Which hotbar slot to swap the mace to (0-8).")
        .defaultValue(0)
        .range(0, 8)
        .sliderRange(0, 8)
        .build()
    );

    // Timing settings
    private final Setting<Integer> swapDelay = sgTiming.add(new IntSetting.Builder()
        .name("swap-delay")
        .description("Delay in ticks before swapping to mace (after taking from EC).")
        .defaultValue(1)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Integer> attackDelay = sgTiming.add(new IntSetting.Builder()
        .name("attack-delay")
        .description("Delay in ticks before attacking (after swapping to mace).")
        .defaultValue(1)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    private final Setting<Integer> putBackDelay = sgTiming.add(new IntSetting.Builder()
        .name("put-back-delay")
        .description("Delay in ticks before putting mace back in EC (after attack).")
        .defaultValue(1)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    // Target settings
    private final Setting<Double> targetRange = sgTargeting.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("Maximum range to search for targets.")
        .defaultValue(5.0)
        .range(1.0, 10.0)
        .sliderRange(1.0, 10.0)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<TargetUtils.Mode> targetMode = sgTargeting.add(new EnumSetting.Builder<TargetUtils.Mode>()
        .name("target-mode")
        .description("How to choose a target.")
        .defaultValue(TargetUtils.Mode.Closest)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Don't attack friends.")
        .defaultValue(true)
        .visible(() -> targetMode.get() != TargetUtils.Mode.PlayerName)
        .build()
    );

    private final Setting<List<String>> players = sgTargeting.add(new StringListSetting.Builder()
        .name("targets")
        .description("Players to target.")
        .defaultValue(List.of())
        .visible(() -> targetMode.get() == TargetUtils.Mode.PlayerName)
        .build()
    );

    private final Setting<Boolean> ignoreNamed = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-named")
        .description("Don't attack named entities.")
        .defaultValue(true)
        .visible(() -> targetMode.get() != TargetUtils.Mode.PlayerName)
        .build()
    );

    private final Setting<Boolean> ignoreOnFire = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-on-fire")
        .description("Don't attack burning entities.")
        .defaultValue(false)
        .visible(() -> targetMode.get() != TargetUtils.Mode.PlayerName)
        .build()
    );

    // Behavior settings
    private final Setting<Boolean> closeScreen = sgGeneral.add(new BoolSetting.Builder()
        .name("close-screen")
        .description("Close the Ender Chest screen after taking the mace.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyWhenFalling = sgGeneral.add(new BoolSetting.Builder()
        .name("only-when-falling")
        .description("Only attack when falling (for maximum mace damage).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> minFallDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-fall-distance")
        .description("Minimum fall distance before attacking (blocks).")
        .defaultValue(3.0)
        .range(0.0, 50.0)
        .sliderRange(0.0, 20.0)
        .visible(onlyWhenFalling::get)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Automatically disable after one attack.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> debugMode = sgGeneral.add(new BoolSetting.Builder()
        .name("debug-mode")
        .description("Show additional info in chat for debugging.")
        .defaultValue(false)
        .build()
    );

    // Keybind
    private final Setting<Keybind> attackKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("attack")
        .description("Attack with mace from EC.")
        .defaultValue(Keybind.none())
        .action(this::performAttack)
        .build()
    );

    public ECMace() {
        super(MUtils.CATEGORY, "ECMace", "Takes out a mace from your Ender Chest before performing Macekill then putting it back");
    }

    private void performAttack() {
        // Check if Ender Chest is open
        if (mc.player.currentScreenHandler == null || !(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
            ChatUtils.error("Ender Chest must be open!");
            return;
        }

        // Get target
        List<Entity> entityList = TargetUtils.getTarget(
            ignoreFriends.get(),
            ignoreNamed.get(),
            ignoreOnFire.get(),
            targetMode.get(),
            players.get(),
            entities.get()
        );

        if (entityList.isEmpty()) {
            ChatUtils.error("No target found.");
            return;
        }

        Entity target = entityList.get(0);

        // Check range
        if (mc.player.distanceTo(target) > targetRange.get()) {
            ChatUtils.error("Target out of range!");
            return;
        }

        // Check fall distance
        if (onlyWhenFalling.get()) {
            if (mc.player.fallDistance < minFallDistance.get()) {
                ChatUtils.error("Not falling enough! Current: " + String.format("%.1f", mc.player.fallDistance) + " blocks");
                return;
            }
        }

        if (debugMode.get()) {
            ChatUtils.info("Targeting " + target.getName().getString());
            if (onlyWhenFalling.get()) {
                ChatUtils.info("Fall distance: " + String.format("%.1f", mc.player.fallDistance) + " blocks");
            }
        }

        // Save current slot
        previousSlot = mc.player.getInventory().selectedSlot;

        // Take mace from EC
        CloseWithoutPacket.takeOutItem(maceSlot.get());

        if (debugMode.get()) {
            ChatUtils.info("Took mace from EC slot " + maceSlot.get());
        }

        // Close screen if enabled
        if (closeScreen.get()) {
            CloseWithoutPacket.closeScreen();
        }

        // Wait swap delay then swap to hotbar slot
        new Thread(() -> {
            try {
                Thread.sleep(swapDelay.get() * 50L); // Convert ticks to ms

                mc.execute(() -> {
                    // Swap to the hotbar slot
                    mc.player.getInventory().selectedSlot = hotbarSlot.get();

                    if (debugMode.get()) {
                        ChatUtils.info("Swapped to hotbar slot " + hotbarSlot.get());
                    }
                });

                // Wait attack delay then attack
                Thread.sleep(attackDelay.get() * 50L);

                mc.execute(() -> {
                    // Attack the target
                    PacketUtils.packetQueue.clear();
                    PacketUtils.packetQueue.add(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
                    PacketUtils.sendAllPacketsInQueue();

                    if (debugMode.get()) {
                        ChatUtils.info("Attacked " + target.getName().getString());
                    }
                });

                // Wait put back delay then restore
                Thread.sleep(putBackDelay.get() * 50L);

                mc.execute(() -> {
                    restoreSlot();

                    if (autoDisable.get()) {
                        toggle();
                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void restoreSlot() {
        if (previousSlot != -1) {
            InvUtils.swap(previousSlot, false);
            if (debugMode.get()) {
                ChatUtils.info("Restored to slot " + previousSlot);
            }
            previousSlot = -1;
        }
    }
}*/
