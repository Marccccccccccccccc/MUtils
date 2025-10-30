package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.Surround;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ForceCrawl extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("What blocks to use. Trapdoor recommended")
        .defaultValue(Blocks.OAK_TRAPDOOR)
        //.filter(this::blockFilter)
        .build()
    );

    private final Setting<Surround.Center> center = sgGeneral.add(new EnumSetting.Builder<Surround.Center>()
        .name("center")
        .description("Teleports you to the center of the block.")
        .defaultValue(Surround.Center.OnActivate)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Automatically faces towards the obsidian being placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autotrapdoor = sgGeneral.add(new BoolSetting.Builder()
        .name("AutoTrapdoor")
        .description("Automates getting into crawl mode with a Trapdoor")
        .defaultValue(true)
        .build()
    );

    public ForceCrawl() {
        super(MUtils.CATEGORY, "ForceCrawl", "description");
        ///Velocity Recommended
    }
    @Override
    public void onActivate() {
        BlockPos blockPos = null;
        if (mc.player != null) {
            blockPos = mc.player.getBlockPos().up();
        }
        FindItemResult itemResult = InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
        if (!itemResult.found()) {
            ChatUtils.sendMsg(Text.of("Block Not Found in Hotbar"));
            return;
        }
        if (center.get() == Surround.Center.OnActivate) PlayerUtils.centerPlayer();
        boolean result = BlockUtils.place(
            blockPos,
            itemResult,
            rotate.get(),
            100,
            false);

        if (result) ChatUtils.sendMsg("ForceCrawl", Text.of("Successfully Placed"));
        if (!result) ChatUtils.sendMsg("ForceCrawl", Text.of("Failed Placing"));
        /// TODO: Research if there are better ways to go prone
        /// Get rid of false positives/negatives
        if (autotrapdoor.get()) {
            mc.player.setPos(
                mc.player.getX()+0.5,
                mc.player.getY(),
                mc.player.getZ()
            );
            BlockUtils.interact(new BlockHitResult(blockPos.toCenterPos(), BlockUtils.getDirection(blockPos), blockPos, true), Hand.MAIN_HAND, false);
            /// TODO: fix the interact
        }

        //toggle();
    }
}
