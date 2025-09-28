package com.example.addon.modules;

import com.example.addon.AddonTemplate;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.ItemStack;
import meteordevelopment.meteorclient.events.entity.player.PickItemsEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class EmptySlot extends Module {
    private int delayTicks = -1;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Example setting
    private final Setting<Integer> slot = sgGeneral.add(new IntSetting.Builder()
        .name("slot")
        .description("Inventory slot to keep free (0–35).")
        .defaultValue(0)
        .min(0)
        .max(35)
        .sliderMax(35)
        .build()
    );
    private final Setting<Boolean> Stack = sgGeneral.add(new BoolSetting.Builder()
        .name("Stack")
        .description("Drop whole Stack?")
        .defaultValue(true)
        .build()
    );

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (delayTicks > 0) {
            delayTicks--;
        } else if (delayTicks == 0) {
            delayTicks = -1;
        }
    }




    @EventHandler
    private void onItemPickup(PickItemsEvent event) {
        delayTicks = 3;
        mc.execute(() -> {
            ItemStack stack = mc.player.getInventory().getStack(slot.get());

            if (stack.isEmpty()) {
            } else {
                if (slot.get() >= 0 && slot.get() <= 8) {
                    int originalSlot = mc.player.getInventory().getSelectedSlot();
                    InvUtils.swap(slot.get(), false);
                    mc.player.dropSelectedItem(Stack.get());
                    InvUtils.swap(originalSlot, true);
                }

                else {
                    mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    slot.get(),
                    Stack.get() ? 1 : 0,
                    SlotActionType.THROW,
                    mc.player
                    );
                }
            }
        });
    }





    public EmptySlot() {
        super(AddonTemplate.CATEGORY, "EmptySlot", "Keeps a specific slot empty by dropping items in it.");
    }
}
