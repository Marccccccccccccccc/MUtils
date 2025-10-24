package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.text.Text;

public class ModlistLogger extends Module {
    public ModlistLogger() {
        super(MUtils.CATEGORY, "Modlist Logger", "Logs custom payload packets.");
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof CustomPayloadC2SPacket) {
            CustomPayloadC2SPacket packet = (CustomPayloadC2SPacket) event.packet;
            CustomPayload payload = packet.payload();
            ChatUtils.sendMsg(Text.of("Payload: " + payload.toString()));
        }
    }
}
