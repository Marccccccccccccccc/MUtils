package com.example.addon.modules;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageRepeater extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilters = settings.createGroup("Filters");

    private final Setting<Boolean> onlyFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("only-friends")
        .description("Wiederholt nur Nachrichten von Freunden.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> privateOnly = sgGeneral.add(new BoolSetting.Builder()
        .name("private-only")
        .description("Wiederholt nur private Nachrichten (/msg).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Verzögerung in Millisekunden vor dem Wiederholen.")
        .defaultValue(100)
        .min(0)
        .max(5000)
        .sliderMax(2000)
        .build()
    );

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .description("Zeigt Debug-Informationen.")
        .defaultValue(false)
        .build()
    );

    // Filter Settings
    private final Setting<Boolean> ignoreCommands = sgFilters.add(new BoolSetting.Builder()
        .name("ignore-commands")
        .description("Ignoriert Nachrichten die mit / starten.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreDotCommands = sgFilters.add(new BoolSetting.Builder()
        .name("ignore-dot-commands")
        .description("Ignoriert Nachrichten die mit . starten (Meteor Commands).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreHashCommands = sgFilters.add(new BoolSetting.Builder()
        .name("ignore-hash-commands")
        .description("Ignoriert Nachrichten die mit # starten.")
        .defaultValue(true)
        .build()
    );

    // Patterns für verschiedene /msg Formate
    private static final Pattern[] MSG_PATTERNS = {
        Pattern.compile("^(\\[.*?\\] )?([\\w]+) whispers to you: (.+)$"), // Standard Essentials
        Pattern.compile("^(\\[.*?\\] )?([\\w]+) -> me: (.+)$"), // Alternative Format
        Pattern.compile("^From ([\\w]+): (.+)$"), // Einfaches Format
        Pattern.compile("^\\[([\\w]+) -> me\\] (.+)$"), // Bracket Format
        Pattern.compile("^([\\w]+) flüstert dir zu: (.+)$"), // Deutsch
    };

    public MessageRepeater() {
        super(Categories.Misc, "msg-repeater", "Wiederholt Nachrichten von Freunden im Chat.");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        if (mc.player == null) return;

        String message = event.getMessage().getString();

        if (debug.get()) {
            info("Received: " + message);
        }

        // Prüfe ob es eine private Nachricht ist
        if (privateOnly.get()) {
            String[] result = parsePrivateMessage(message);
            if (result != null) {
                String sender = result[0];
                String content = result[1];

                if (debug.get()) {
                    info("Private message from: " + sender + " | Content: " + content);
                }

                handleMessage(sender, content);
            }
        } else {
            // Auch öffentliche Nachrichten verarbeiten
            // Format: <username> message oder [prefix] <username> message
            Pattern publicPattern = Pattern.compile("^(?:\\[.*?\\] )?<([\\w]+)> (.+)$");
            Matcher matcher = publicPattern.matcher(message);

            if (matcher.matches()) {
                String sender = matcher.group(1);
                String content = matcher.group(2);
                handleMessage(sender, content);
            }
        }
    }

    private String[] parsePrivateMessage(String message) {
        for (Pattern pattern : MSG_PATTERNS) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.matches()) {
                if (matcher.groupCount() == 2) {
                    return new String[]{matcher.group(1), matcher.group(2)};
                } else if (matcher.groupCount() == 3) {
                    return new String[]{matcher.group(2), matcher.group(3)};
                }
            }
        }
        return null;
    }

    private void handleMessage(String sender, String content) {
        // Prüfe ob Sender ein Freund ist
        if (onlyFriends.get() && Friends.get().get(sender) == null) {
            if (debug.get()) {
                info("Ignored: " + sender + " is not a friend");
            }
            return;
        }

        // Filter anwenden
        if (shouldIgnoreMessage(content)) {
            if (debug.get()) {
                info("Ignored by filter: " + content);
            }
            return;
        }

        // Nachricht wiederholen
        repeatMessage(content);
    }

    private boolean shouldIgnoreMessage(String message) {
        if (message == null || message.trim().isEmpty()) return true;

        String trimmed = message.trim();

        if (ignoreCommands.get() && trimmed.startsWith("/")) return true;
        if (ignoreDotCommands.get() && trimmed.startsWith(".")) return true;
        if (ignoreHashCommands.get() && trimmed.startsWith("#")) return true;

        return false;
    }

    private void repeatMessage(String message) {
        if (mc.player == null) return;

        new Thread(() -> {
            try {
                Thread.sleep(delay.get());

                if (mc.player != null) {
                    mc.player.networkHandler.sendChatMessage(message);

                    if (debug.get()) {
                        info("Repeated: " + message);
                    }
                }
            } catch (InterruptedException e) {
                error("Message repeat interrupted");
            }
        }).start();
    }

    @Override
    public void onActivate() {
        if (debug.get()) {
            info("MessageRepeater activated");
            info("Only friends: " + onlyFriends.get());
            info("Private only: " + privateOnly.get());
        }
    }
}
