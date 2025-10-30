package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageRepeater extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilters = settings.createGroup("Filters");

    private final Setting<Boolean> onlyFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("only-friends")
        .description("")
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
        .defaultValue(500)
        .min(100)
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

    private final Set<String> processedMessages = new HashSet<>();
    private boolean isRepeating = false;

    private static final Pattern[] MSG_PATTERNS = {
        //Pattern.compile("^(\\[.*?\\] )?([\\w]+) whispers to you: (.+)$"),
        Pattern.compile("^(\\[.*?\\] )?([\\w]+) -> me: (.+)$"),
        Pattern.compile("^From ([\\w]+): (.+)$"),
        Pattern.compile("^\\[([\\w]+) -> me\\] (.+)$"),
        Pattern.compile("^([\\w]+) flüstert dir zu: (.+)$"),
    };

    public MessageRepeater() {
        super(MUtils.CATEGORY2, "msg-repeater", "Use this for Commands and trolling");
    }

    @EventHandler
    private void onMessageReceive(ReceiveMessageEvent event) {
        // stop recursively calling the method
        if (isRepeating || mc.player == null) return;

        try {
            String message = event.getMessage().getString();

            // Ignoriere leere Nachrichten
            if (message == null || message.trim().isEmpty()) return;

            // Verhindere doppeltes Verarbeiten
            String messageHash = message + System.currentTimeMillis() / 1000; // Sekunden-Precision
            if (processedMessages.contains(messageHash)) return;
            processedMessages.add(messageHash);

            // Cleanup alte Einträge
            if (processedMessages.size() > 100) {
                processedMessages.clear();
            }

            // Prüfe ob es eine private Nachricht ist
            if (privateOnly.get()) {
                String[] result = parsePrivateMessage(message);
                if (result != null) {
                    String sender = result[0];
                    String content = result[1];
                    handleMessage(sender, content);
                }
            } else {
                // Auch öffentliche Nachrichten verarbeiten
                Pattern publicPattern = Pattern.compile("^(?:\\[.*?\\] )?<([\\w]+)> (.+)$");
                Matcher matcher = publicPattern.matcher(message);

                if (matcher.matches()) {
                    String sender = matcher.group(1);
                    String content = matcher.group(2);
                    handleMessage(sender, content);
                }
            }
        } catch (Exception e) {
            // Fange alle Fehler ab um Crashes zu vermeiden
            if (debug.get()) {
                System.err.println("[MessageRepeater] Error: " + e.getMessage());
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
        try {
            // Ignoriere eigene Nachrichten
            if (mc.player != null && sender.equalsIgnoreCase(mc.player.getName().getString())) {
                return;
            }

            // Prüfe ob Sender ein Freund ist
            if (onlyFriends.get() && Friends.get().get(sender) == null) {
                return;
            }

            // Filter anwenden
            if (shouldIgnoreMessage(content)) {
                return;
            }

            // Nachricht wiederholen
            repeatMessage(content);
        } catch (Exception e) {
            if (debug.get()) {
                System.err.println("[MessageRepeater] HandleMessage Error: " + e.getMessage());
            }
        }
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

                // Setze Flag um Rekursion zu vermeiden
                isRepeating = true;

                if (mc.player != null && mc.player.networkHandler != null) {
                    mc.player.networkHandler.sendChatMessage(message);
                }

                // Warte kurz bevor wir das Flag zurücksetzen
                Thread.sleep(100);
                isRepeating = false;

            } catch (Exception e) {
                isRepeating = false;
                if (debug.get()) {
                    System.err.println("[MessageRepeater] Repeat Error: " + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void onActivate() {
        processedMessages.clear();
        isRepeating = false;
    }

    @Override
    public void onDeactivate() {
        processedMessages.clear();
        isRepeating = false;
    }
}

