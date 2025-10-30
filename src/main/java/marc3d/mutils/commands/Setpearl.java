package marc3d.mutils.commands;

import com.sun.jdi.connect.Connector;
import meteordevelopment.meteorclient.commands.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import com.mojang.brigadier.arguments.StringArgumentType;

public class Setpearl extends Command {
    public Setpearl() {super("setpearl", "Sets the coordinate of the trapdoor/Interactable to TP a player");}

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("arg1", StringArgumentType.greedyString())
            .then(argument("arg2", StringArgumentType.greedyString())
                .executes(context -> {
                    String arg1 = StringArgumentType.getString(context, "arg1");
                    String arg2 = StringArgumentType.getString(context, "arg2");

                    // Your command logic here
                    info("First argument: " + arg1);
                    info("Second argument: " + arg2);

                    return SINGLE_SUCCESS;
                })
            )
        );
    }
}
