package com.example.addon.modules;

import com.example.addon.MUtils;
import com.example.addon.utils.CloseWithoutPacket;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.text.Text;

public class CWP extends Module {
    private boolean enderChestOpen = false;

    public CWP() {
        super(MUtils.CATEGORY, "CloseWithoutPacket", "Tracks Ender Chest open/close and closes it after 1s.");
    }

    @EventHandler
    private void onChestOpen(OpenScreenEvent event) {
        if (event.screen instanceof GenericContainerScreen screen) {
            Text title = screen.getTitle();

            if (title.getString().toLowerCase().contains("ender chest")) {
                // Close after 1s
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                    CloseWithoutPacket.takeOutItem(0);
                    CloseWithoutPacket.closeScreen();
                }).start();
            }
        }
    }
}
