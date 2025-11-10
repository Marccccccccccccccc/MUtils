package marc3d.mutils.modules;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import meteordevelopment.meteorclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
//import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class RattenNuker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgWhitelist = settings.createGroup("Whitelist");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Nuker.Shape> shape = sgGeneral.add(new EnumSetting.Builder<Nuker.Shape>()
        .name("shape")
        .description("The shape of nuking algorithm.")
        .defaultValue(Nuker.Shape.Sphere)
        .build()
    );

    private final Setting<Nuker.Mode> mode = sgGeneral.add(new EnumSetting.Builder<Nuker.Mode>()
        .name("mode")
        .description("The way the blocks are broken.")
        .defaultValue(Nuker.Mode.All)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("range")
        .description("The break range.")
        .defaultValue(4)
        .min(0)
        .visible(() -> shape.get() != Nuker.Shape.Cube)
        .build()
    );

    private final Setting<Integer> range_up = sgGeneral.add(new IntSetting.Builder()
        .name("up")
        .description("The break range.")
        .defaultValue(1)
        .min(0)
        .visible(() -> shape.get() == Nuker.Shape.Cube)
        .build()
    );

    private final Setting<Integer> range_down = sgGeneral.add(new IntSetting.Builder()
        .name("down")
        .description("The break range.")
        .defaultValue(1)
        .min(0)
        .visible(() -> shape.get() == Nuker.Shape.Cube)
        .build()
    );

    private final Setting<Integer> range_left = sgGeneral.add(new IntSetting.Builder()
        .name("left")
        .description("The break range.")
        .defaultValue(1)
        .min(0)
        .visible(() -> shape.get() == Nuker.Shape.Cube)
        .build()
    );

    private final Setting<Integer> range_right = sgGeneral.add(new IntSetting.Builder()
        .name("right")
        .description("The break range.")
        .defaultValue(1)
        .min(0)
        .visible(() -> shape.get() == Nuker.Shape.Cube)
        .build()
    );

    private final Setting<Integer> range_forward = sgGeneral.add(new IntSetting.Builder()
        .name("forward")
        .description("The break range.")
        .defaultValue(1)
        .min(0)
        .visible(() -> shape.get() == Nuker.Shape.Cube)
        .build()
    );

    private final Setting<Integer> range_back = sgGeneral.add(new IntSetting.Builder()
        .name("back")
        .description("The break range.")
        .defaultValue(1)
        .min(0)
        .visible(() -> shape.get() == Nuker.Shape.Cube)
        .build()
    );

    private final Setting<Double> wallsRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("walls-range")
        .description("Range in which to break when behind blocks.")
        .defaultValue(4.0)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Integer> maxBlocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("max-blocks-per-tick")
        .description("Maximum blocks to break per tick. High values for instant mining.")
        .defaultValue(20)
        .min(1)
        .sliderMax(100)
        .build()
    );

    private final Setting<Integer> clientsideDelay = sgGeneral.add(new IntSetting.Builder()
        .name("clientside-delay")
        .description("Ticks to wait before attempting to break the same block again clientside.")
        .defaultValue(40)
        .min(0)
        .sliderMax(100)
        .build()
    );

    private final Setting<Nuker.SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<Nuker.SortMode>()
        .name("sort-mode")
        .description("The blocks you want to mine first.")
        .defaultValue(Nuker.SortMode.Closest)
        .build()
    );

    private final Setting<Boolean> assumeInstamine = sgGeneral.add(new BoolSetting.Builder()
        .name("assume-instamine")
        .description("Assumes you can instamine cobblestone and continues breaking without server confirmation.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates server-side to the block being mined.")
        .defaultValue(true)
        .build()
    );

    // why is it called a blacklist that's kinda racist

    private final Setting<Nuker.ListMode> listMode = sgWhitelist.add(new EnumSetting.Builder<Nuker.ListMode>()
        .name("list-mode")
        .description("Selection mode.")
        .defaultValue(Nuker.ListMode.Blacklist)
        .build()
    );

    private final Setting<List<Block>> blacklist = sgWhitelist.add(new BlockListSetting.Builder()
        .name("blacklist")
        .description("The blocks you don't want to mine.")
        .visible(() -> listMode.get() == Nuker.ListMode.Blacklist)
        .build()
    );

    private final Setting<List<Block>> whitelist = sgWhitelist.add(new BlockListSetting.Builder()
        .name("whitelist")
        .description("The blocks you want to mine.")
        .visible(() -> listMode.get() == Nuker.ListMode.Whitelist)
        .build()
    );

    // Rendering

    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder()
        .name("swing")
        .description("Whether to swing hand client-side.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enableRenderBounding = sgRender.add(new BoolSetting.Builder()
        .name("bounding-box")
        .description("Enable rendering bounding box for Cube and Uniform Cube.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeModeBox = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("nuke-box-mode")
        .description("How the shape for the bounding box is rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColorBox = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the bounding box.")
        .defaultValue(new SettingColor(16,106,144, 100))
        .build()
    );

    private final Setting<SettingColor> lineColorBox = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the bounding box.")
        .defaultValue(new SettingColor(16,106,144, 255))
        .build()
    );

    private final Setting<Boolean> enableRenderBreaking = sgRender.add(new BoolSetting.Builder()
        .name("broken-blocks")
        .description("Enable rendering broken blocks.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeModeBreak = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("nuke-block-mode")
        .description("How the shapes for broken blocks are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(enableRenderBreaking::get)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the target block rendering.")
        .defaultValue(new SettingColor(255, 0, 0, 80))
        .visible(enableRenderBreaking::get)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the target block rendering.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(enableRenderBreaking::get)
        .build()
    );

    private final List<BlockPos> blocks = new ArrayList<>();
    private final Set<BlockPos> recentlyBroken = new ObjectOpenHashSet<>();
    private final List<BrokenBlock> brokenBlockTimers = new ArrayList<>();

    private final BlockPos.Mutable pos1 = new BlockPos.Mutable();
    private final BlockPos.Mutable pos2 = new BlockPos.Mutable();
    int maxh = 0;
    int maxv = 0;

    public RattenNuker() {
        super(Categories.World, "Rattennuker", "Very fast block deletor. Made for Cobblestone/Almost instamine blocks");
    }

    @Override
    public void onActivate() {
        recentlyBroken.clear();
        brokenBlockTimers.clear();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (enableRenderBounding.get()) {
            if (shape.get() != Nuker.Shape.Sphere && mode.get() != Nuker.Mode.Smash) {
                int minX = Math.min(pos1.getX(), pos2.getX());
                int minY = Math.min(pos1.getY(), pos2.getY());
                int minZ = Math.min(pos1.getZ(), pos2.getZ());
                int maxX = Math.max(pos1.getX(), pos2.getX());
                int maxY = Math.max(pos1.getY(), pos2.getY());
                int maxZ = Math.max(pos1.getZ(), pos2.getZ());
                event.renderer.box(minX, minY, minZ, maxX, maxY, maxZ, sideColorBox.get(), lineColorBox.get(), shapeModeBox.get(), 0);
            }
        }
    }



    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        // Update broken block timers
        brokenBlockTimers.removeIf(bb -> {
            bb.ticksLeft--;
            if (bb.ticksLeft <= 0) {
                recentlyBroken.remove(bb.pos);
                return true;
            }
            return false;
        });

        // Calculate position parameters
        double pX = mc.player.getX(), pY = mc.player.getY(), pZ = mc.player.getZ();
        double rangeSq = Math.pow(range.get(), 2);
        BlockPos playerBlockPos = mc.player.getBlockPos();

        if (shape.get() == Nuker.Shape.UniformCube) range.set((double) Math.round(range.get()));

        double pX_ = pX;
        double pZ_ = pZ;
        int r = (int) Math.round(range.get());

        if (shape.get() == Nuker.Shape.UniformCube) {
            pX_ += 1;
            pos1.set(pX_ - r, pY - r + 1, pZ - r + 1);
            pos2.set(pX_ + r - 1, pY + r, pZ + r);
            maxh = 0;
            maxv = 0;
        } else {
            Direction direction = mc.player.getHorizontalFacing();
            switch (direction) {
                case SOUTH -> {
                    pZ_ += 1;
                    pX_ += 1;
                    pos1.set(pX_ - (range_right.get() + 1), Math.ceil(pY) - range_down.get(), pZ_ - (range_back.get() + 1));
                    pos2.set(pX_ + range_left.get(), Math.ceil(pY + range_up.get() + 1), pZ_ + range_forward.get());
                }
                case WEST -> {
                    pos1.set(pX_ - range_forward.get(), Math.ceil(pY) - range_down.get(), pZ_ - range_right.get());
                    pos2.set(pX_ + range_back.get() + 1, Math.ceil(pY + range_up.get() + 1), pZ_ + range_left.get() + 1);
                }
                case NORTH -> {
                    pX_ += 1;
                    pZ_ += 1;
                    pos1.set(pX_ - (range_left.get() + 1), Math.ceil(pY) - range_down.get(), pZ_ - (range_forward.get() + 1));
                    pos2.set(pX_ + range_right.get(), Math.ceil(pY + range_up.get() + 1), pZ_ + range_back.get());
                }
                case EAST -> {
                    pX_ += 1;
                    pos1.set(pX_ - (range_back.get() + 1), Math.ceil(pY) - range_down.get(), pZ_ - range_left.get());
                    pos2.set(pX_ + range_forward.get(), Math.ceil(pY + range_up.get() + 1), pZ_ + range_right.get() + 1);
                }
            }

            maxh = 1 + Math.max(Math.max(Math.max(range_back.get(), range_right.get()), range_forward.get()), range_left.get());
            maxv = 1 + Math.max(range_up.get(), range_down.get());
        }

        // Flatten mode
        if (mode.get() == Nuker.Mode.Flatten) pos1.setY((int) Math.floor(pY + 0.5));

        Box box = new Box(pos1.toCenterPos(), pos2.toCenterPos());

        // Find blocks to break
        BlockIterator.register(Math.max((int) Math.ceil(range.get() + 1), maxh), Math.max((int) Math.ceil(range.get()), maxv), (blockPos, blockState) -> {
            // Skip recently broken blocks
            if (recentlyBroken.contains(blockPos)) return;

            Vec3d center = blockPos.toCenterPos();
            switch (shape.get()) {
                case Sphere -> {
                    if (Utils.squaredDistance(pX, pY, pZ, center.getX(), center.getY(), center.getZ()) > rangeSq) return;
                }
                case UniformCube -> {
                    if (Nuker.chebyshevDist(playerBlockPos.getX(), playerBlockPos.getY(), playerBlockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ()) >= range.get()) return;
                }
                case Cube -> {
                    if (!box.contains(center)) return;
                }
            }

            // Flatten mode
            if (mode.get() == Nuker.Mode.Flatten && blockPos.getY() + 0.5 < pY) return;

            // Smash mode
            if (mode.get() == Nuker.Mode.Smash && blockState.getHardness(mc.world, blockPos) != 0) return;

            // Block must be breakable
            if (!BlockUtils.canBreak(blockPos, blockState)) return;

            // Raycast to block
            if (isOutOfRange(blockPos)) return;

            // Check whitelist or blacklist
            if (listMode.get() == Nuker.ListMode.Whitelist && !whitelist.get().contains(blockState.getBlock())) return;
            if (listMode.get() == Nuker.ListMode.Blacklist && blacklist.get().contains(blockState.getBlock())) return;

            // Add block
            blocks.add(blockPos.toImmutable());
        });

        // Break blocks if found
        BlockIterator.after(() -> {
            // Sort blocks
            if (sortMode.get() == Nuker.SortMode.TopDown)
                blocks.sort(Comparator.comparingDouble(value -> -value.getY()));
            else if (sortMode.get() != Nuker.SortMode.None)
                blocks.sort(Comparator.comparingDouble(value -> Utils.squaredDistance(pX, pY, pZ, value.getX() + 0.5, value.getY() + 0.5, value.getZ() + 0.5) * (sortMode.get() == Nuker.SortMode.Closest ? 1 : -1)));

            if (blocks.isEmpty()) return;

            // Break multiple blocks
            int count = 0;
            for (BlockPos block : blocks) {
                if (count >= maxBlocksPerTick.get()) break;

                if (rotate.get()) Rotations.rotate(Rotations.getYaw(block), Rotations.getPitch(block), () -> breakBlock(block));
                else breakBlock(block);

                if (enableRenderBreaking.get()) {
                    RenderUtils.renderTickingBlock(block, sideColor.get(), lineColor.get(), shapeModeBreak.get(), 0, 8, true, false);
                }

                // Add to recently broken with clientside delay
                if (assumeInstamine.get()) {
                    recentlyBroken.add(block.toImmutable());
                    brokenBlockTimers.add(new BrokenBlock(block.toImmutable(), clientsideDelay.get()));
                }

                count++;
            }

            blocks.clear();
        });
    }
    private static final List<Long> blockBreakTimes = new ArrayList<>();

    private void breakBlock(BlockPos blockPos) {
        // Send start break packet
        mc.getNetworkHandler().sendPacket(
            new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                blockPos, BlockUtils.getDirection(blockPos), 0));

        // Swing hand
        if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
        //else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        // Send stop break packet (assume instant break)
        mc.getNetworkHandler().sendPacket(
            new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                blockPos, BlockUtils.getDirection(blockPos), 0));
    }
    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof BlockUpdateS2CPacket packet) {
            if (packet.getState().isAir()) {
                // Block was broken
                blockBreakTimes.add(System.currentTimeMillis());
            }
        }
    }
    /// THIS CAUSES THE CONCURRENTMODIFICATIONEXEPTION
    public static double getBlocksPerSecond() {
        long currentTime = System.currentTimeMillis();

        // Remove all block break times older than 5 seconds
        synchronized (blockBreakTimes) {
            blockBreakTimes.removeIf(time -> currentTime - time > 5000);
            return (double) blockBreakTimes.size() / 5;
        }
    }

    @Override
    public String getInfoString() {
        return String.format("%.1f BPS", getBlocksPerSecond());
    }

    /// THIS IS FINE
    private boolean isOutOfRange(BlockPos blockPos) {
        Vec3d pos = blockPos.toCenterPos();
        RaycastContext raycastContext = new RaycastContext(mc.player.getEyePos(), pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
        BlockHitResult result = mc.world.raycast(raycastContext);
        if (result == null || !result.getBlockPos().equals(blockPos))
            return !PlayerUtils.isWithin(pos, wallsRange.get());

        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        event.cooldown = 0;
    }

    private static class BrokenBlock {
        BlockPos pos;
        int ticksLeft;

        BrokenBlock(BlockPos pos, int ticksLeft) {
            this.pos = pos;
            this.ticksLeft = ticksLeft;
        }
    }
}
