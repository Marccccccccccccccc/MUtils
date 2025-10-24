package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.SetPlayerInventoryS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.EventListener;

public class ItemLog extends Module {



    public ItemLog() {
        super(MUtils.CATEGORY, "ItemLog", "Disconnects you when theres a certain amount of items");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Item> item = sgGeneral.add(new ItemSetting.Builder()
        .name("item")
        .description("Item to display")
        .defaultValue(Items.TOTEM_OF_UNDYING)
        .build()
    );

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("Item Count")
        .description("At what Itemcount to disconnect")
        .min(0)
        .defaultValue(1)
        .build()
    );

    ItemStack itemStack = new ItemStack(item.get(), InvUtils.find(item.get()).count());



    @EventHandler
    private void onInvUpdate(TickEvent.Post event) {
        if (itemStack.getCount() <= amount.get()) ChatUtils.sendMsg(Text.of("fuck you"));
    }
    @EventHandler
    private void onPlayerInvUpdate(SetPlayerInventoryS2CPacket event) {
        ChatUtils.sendMsg(Text.of("PlayerInventoryEvent"));
    }
}
