package marc3d.mutils.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import marc3d.mutils.utils.CloseWithoutPacket;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.GenericContainerScreenHandler;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ECTakeCommand extends Command {

    public ECTakeCommand() {
        super("ectake", "Takes an item from Ender Chest by slot number.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
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
                ChatUtils.info("Taking item from EC slot " + slot);

                return SINGLE_SUCCESS;
            }));
    }
}
