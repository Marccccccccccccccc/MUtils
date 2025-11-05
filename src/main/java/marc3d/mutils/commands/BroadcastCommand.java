package marc3d.mutils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.List;

public class BroadcastCommand extends Command {
    private static final List<String> linkedUsers = new ArrayList<>();

    public BroadcastCommand() {
        super("broadcast", "Broadcasts a message to linked users.", "b");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Broadcast message
        builder.then(argument("message", StringArgumentType.greedyString())
            .executes(context -> {
                String message = context.getArgument("message", String.class);

                if (linkedUsers.isEmpty()) {
                    error("No users linked. Use .b add <username> to add users.");
                    return SINGLE_SUCCESS;
                }

                new Thread(() -> {
                    for (int i = 0; i < linkedUsers.size(); i++) {
                        String user = linkedUsers.get(i);

                        // Send message on main thread
                        mc.execute(() -> {
                            mc.player.networkHandler.sendChatCommand("msg " + user + " " + message);
                        });

                        // Wait before next message (except for last one)
                        if (i < linkedUsers.size() - 1) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }

                    // Notify completion on main thread
                    mc.execute(() -> {
                        info("Broadcast complete!");
                    });
                }).start();

                info("Broadcasted message to " + linkedUsers.size() + " user(s).");
                return SINGLE_SUCCESS;
            })
        );

        // Add user to linked list
        builder.then(literal("add")
            .then(argument("username", StringArgumentType.word())
                .executes(context -> {
                    String username = context.getArgument("username", String.class);

                    if (linkedUsers.contains(username)) {
                        error("User '" + username + "' is already in the linked list.");
                    } else {
                        linkedUsers.add(username);
                        info("Added '" + username + "' to linked list. Total users: " + linkedUsers.size());
                    }
                    return SINGLE_SUCCESS;
                })
            )
        );

        // Remove user from linked list
        builder.then(literal("remove")
            .then(argument("username", StringArgumentType.word())
                .executes(context -> {
                    String username = context.getArgument("username", String.class);

                    if (linkedUsers.remove(username)) {
                        info("Removed '" + username + "' from linked list. Remaining users: " + linkedUsers.size());
                    } else {
                        error("User '" + username + "' is not in the linked list.");
                    }
                    return SINGLE_SUCCESS;
                })
            )
        );

        // List all linked users
        builder.then(literal("list")
            .executes(context -> {
                if (linkedUsers.isEmpty()) {
                    info("No users linked.");
                } else {
                    info("Linked users (" + linkedUsers.size() + "):");
                    for (String user : linkedUsers) {
                        ChatUtils.info("  - " + user);
                    }
                }
                return SINGLE_SUCCESS;
            })
        );

        // Clear all linked users
        builder.then(literal("clear")
            .executes(context -> {
                int count = linkedUsers.size();
                linkedUsers.clear();
                info("Cleared " + count + " user(s) from linked list.");
                return SINGLE_SUCCESS;
            })
        );
    }
}
