package marc3d.mutils.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import marc3d.mutils.utils.CloseWithoutPacket;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ECTakeCommand extends Command {



    public ECTakeCommand() {
        super("ectake", "Takes an item from Ender Chest by slot number.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // Command with just slot (requires EC to be open)
        builder.then(argument("slot", IntegerArgumentType.integer(0, 26))
            .executes(context -> {
                int slot = context.getArgument("slot", Integer.class);

                // Check if EC is open
                if (mc.player.currentScreenHandler == null || !(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                    ChatUtils.error("Ender Chest must be open!");
                    return SINGLE_SUCCESS;
                }

                // Take the item
                CloseWithoutPacket.swapItem(slot);
                ChatUtils.sendMsg("ECTake",Text.of("Taking item from EC slot " + slot));

                return SINGLE_SUCCESS;
            })
            // Command with slot and skipCheck boolean
            .then(argument("skipCheck", BoolArgumentType.bool())
                .executes(context -> {
                    int slot = context.getArgument("slot", Integer.class);
                    boolean skipCheck = context.getArgument("skipCheck", Boolean.class);

                    if (!skipCheck) {
                        // Check if EC is open
                        if (mc.player.currentScreenHandler == null || !(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
                            ChatUtils.error("Ender Chest must be open!");
                            return SINGLE_SUCCESS;
                        }
                    } else {
                        ChatUtils.warning("Skipping EC open check - this may cause issues!");
                    }

                    // Take the item
                    CloseWithoutPacket.swapItem(slot);
                    ChatUtils.sendMsg("ECTake", Text.of("Taking item from EC slot " + slot));

                    return SINGLE_SUCCESS;
                })));
    }
}
