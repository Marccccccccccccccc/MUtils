package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.PlayerListEntry;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.HashSet;
import java.util.Set;

public class AdminAbuseNotify extends Module {
    private Set<String> notifiedPlayers = new HashSet<>();

    public AdminAbuseNotify() {
        super(MUtils.CATEGORY, "AdminAbuseNotify", "desc");
    }

    @Override
    public void onActivate() {
        notifiedPlayers.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.getNetworkHandler() == null) return;

        for (PlayerListEntry player : mc.getNetworkHandler().getPlayerList()) {
            String playerName = player.getProfile().getName();
            GameMode gameMode = player.getGameMode();

            if (gameMode == GameMode.CREATIVE && !notifiedPlayers.contains(playerName)) {
                ChatUtils.sendMsg(Text.of("Player " + playerName + " is in Creative Mode!"));
                mc.player.networkHandler.sendChatMessage(playerName + " is in Creative Mode! ADMINABUSE");
                notifiedPlayers.add(playerName);
            }

            // Remove from set if they're no longer in creative
            if (gameMode != GameMode.CREATIVE && notifiedPlayers.contains(playerName)) {
                notifiedPlayers.remove(playerName);
            }
        }
    }
}
