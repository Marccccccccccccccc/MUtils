package com.example.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

/**
 * The Meteor Client command API uses the <a href="https://github.com/Mojang/brigadier">same command system as Minecraft does</a>.
 */
public class CommandExample extends Command {
    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public CommandExample() {
        super("example", "Sends a message.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("arg1", StringArgumentType.string())
            .then(argument("arg2", StringArgumentType.greedyString())
                .executes(context -> {
                    String arg1 = StringArgumentType.getString(context, "arg1");
                    String arg2 = StringArgumentType.getString(context, "arg2");

                    //logic
                    info("First argument: " + arg1);
                    info("Second argument: " + arg2);

                    return SINGLE_SUCCESS;//TODO: FIX TS
                })
            )
        );
    }
}
