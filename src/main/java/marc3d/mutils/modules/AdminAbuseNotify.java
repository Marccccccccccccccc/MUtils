package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.PlayerListEntry;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.GameMode;

import java.util.HashSet;
import java.util.Set;

public class AdminAbuseNotify extends Module {
    private Set<String> notifiedPlayers = new HashSet<>();

    public AdminAbuseNotify() {
        super(MUtils.CATEGORY2, "AdminAbuseNotify", "Notifies when players are in creative mode");
    }

    @Override
    public void onActivate() {
        notifiedPlayers.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.player.networkHandler == null) return;

        for (PlayerListEntry player : mc.player.networkHandler.getPlayerList()) {
            String playerName = player.getProfile().getName();
            GameMode gameMode = player.getGameMode();

            // Skip if gamemode is null
            if (gameMode == null) continue;

            if (gameMode == GameMode.CREATIVE && !notifiedPlayers.contains(playerName)) {
                ChatUtils.info("Player " + playerName + " is in Creative Mode!");
                mc.player.networkHandler.sendChatMessage(playerName + " is in Creative Mode! ADMINABUSE");
                notifiedPlayers.add(playerName);
            }

            // Remove from set if they're no longer in creative
            if (gameMode != GameMode.CREATIVE && notifiedPlayers.contains(playerName)) {
                notifiedPlayers.remove(playerName);
            }
        }
    }
} ///TODO: Make it work for other players
