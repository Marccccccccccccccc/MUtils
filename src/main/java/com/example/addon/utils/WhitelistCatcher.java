package com.example.addon.utils;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WhitelistCatcher {

    public static void catchWhitelistException(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unauthorized access - Player not whitelisted")) {
                ChatUtils.error("You are not whitelisted on this server!");
                // Optionally do something else here
            } else {
                // Re-throw if it's a different exception
                throw e;
            }
        }
    }

    public static boolean isWhitelistException(Exception e) {
        return e instanceof RuntimeException &&
            e.getMessage() != null &&
            e.getMessage().contains("Unauthorized access - Player not whitelisted");
    }
}
