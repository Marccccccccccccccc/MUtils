package marc3d.mutils.modules;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.events.entity.player.BlockBreakingCooldownEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
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

    private final Setting<Integer> startDelay = sgGeneral.add(new IntSetting.Builder()
        .name("start-delay")
        .description("Delay in milliseconds between sending START packets for each block.")
        .defaultValue(0)
        .min(0)
        .sliderMax(100)
        .build()
    );

    private final Setting<Integer> startStopDelay = sgGeneral.add(new IntSetting.Builder()
        .name("start-stop-delay")
        .description("Delay in milliseconds between START and STOP packets (simulates mining time).")
        .defaultValue(0)
        .min(0)
        .sliderMax(500)
        .build()
    );

    private final Setting<Integer> clientsideDelay = sgGeneral.add(new IntSetting.Builder()
        .name("clientside-delay")
        .description("Milliseconds to wait before attempting to break the same block again clientside.")
        .defaultValue(2000)
        .min(0)
        .sliderMax(5000)
        .build()
    );

    private final Setting<Boolean> tpsSync = sgGeneral.add(new BoolSetting.Builder()
        .name("tps-sync")
        .description("Automatically adjust delays based on server TPS.")
        .defaultValue(false)
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

    // Whitelist and blacklist

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

    private final Setting<Keybind> selectBlockBind = sgWhitelist.add(new KeybindSetting.Builder()
        .name("select-block-bind")
        .description("Adds targeted block to list when this button is pressed.")
        .defaultValue(Keybind.none())
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
    private final List<PendingBreak> pendingBreaks = new ArrayList<>();

    private final BlockPos.Mutable pos1 = new BlockPos.Mutable();
    private final BlockPos.Mutable pos2 = new BlockPos.Mutable();
    private long lastBlockBreakTime = 0;
    int maxh = 0;
    int maxv = 0;

    public RattenNuker() {
        super(MUtils.CATEGORY, "Rattennuker", "Assumes instant mining for rapid block breaking on Paper servers.");
    }

    @Override
    public void onActivate() {
        recentlyBroken.clear();
        brokenBlockTimers.clear();
        pendingBreaks.clear();
        lastBlockBreakTime = 0;
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

    //Removed Unnecessary Code

    @EventHandler
    private void onTickPre(TickEvent.Pre event) {
        // Process pending STOP packets
        long currentTime = System.currentTimeMillis();
        pendingBreaks.removeIf(pending -> {
            if (currentTime >= pending.stopTime) {
                sendStopPacket(pending.pos);

                if (enableRenderBreaking.get()) {
                    RenderUtils.renderTickingBlock(pending.pos, sideColor.get(), lineColor.get(), shapeModeBreak.get(), 0, 8, true, false);
                }

                if (assumeInstamine.get()) {
                    recentlyBroken.add(pending.pos.toImmutable());
                    brokenBlockTimers.add(new BrokenBlock(pending.pos.toImmutable(), currentTime));
                }
                return true;
            }
            return false;
        });

        // Update broken block timers
        brokenBlockTimers.removeIf(bb -> {
            if (currentTime - bb.breakTime >= clientsideDelay.get()) {///TODO: Make tps adjusted maybe
                recentlyBroken.remove(bb.pos);
                return true;
            }
            return false;
        });

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

            // Break multiple blocks with overlapping technique (non-blocking)
            int count = 0;
            long currentTimeMillis = System.currentTimeMillis();
            long startPacketTime = currentTimeMillis;

            // Send all START packets and schedule STOP packets
            for (BlockPos block : blocks) {
                if (count >= maxBlocksPerTick.get()) break;

                if (rotate.get()) {
                    Rotations.rotate(Rotations.getYaw(block), Rotations.getPitch(block),
                        () -> sendStartPacket(block));
                } else {
                    sendStartPacket(block);
                }

                // Schedule STOP packet for later (non-blocking)
                long stopTime = startPacketTime + (count * getAdjustedStartDelay()) + getAdjustedStartStopDelay();
                pendingBreaks.add(new PendingBreak(block.toImmutable(), stopTime));

                count++;
            }

            // Swing hand once for all blocks
            if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);

            blocks.clear();
        });
    }

    private static final List<Long> blockBreakTimes = new ArrayList<>();

    private void sendStartPacket(BlockPos blockPos) {
        mc.getNetworkHandler().sendPacket(
            new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                blockPos, BlockUtils.getDirection(blockPos), 0));
    }

    private void sendStopPacket(BlockPos blockPos) {
        mc.getNetworkHandler().sendPacket(
            new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                blockPos, BlockUtils.getDirection(blockPos), 0));
    }

    private void breakBlock(BlockPos blockPos) {
        sendStartPacket(blockPos);
        if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
        sendStopPacket(blockPos);
    }

    private int getAdjustedStartDelay() {
        if (!tpsSync.get()) return startDelay.get();

        float tps = mc.world != null ? mc.world.getTickManager().getTickRate() : 20f;
        float tpsMultiplier = 20f / Math.max(tps, 1f);
        return (int) (startDelay.get() * tpsMultiplier);
    }

    private int getAdjustedStartStopDelay() {
        if (!tpsSync.get()) return startStopDelay.get();

        float tps = mc.world != null ? mc.world.getTickManager().getTickRate() : 20f;
        float tpsMultiplier = 20f / Math.max(tps, 1f);
        return (int) (startStopDelay.get() * tpsMultiplier);
    }
    /*
    private int getAdjustedBlockDelay() {
        if (!tpsSync.get()) return blockDelay.get();

        float tps = mc.world != null ? mc.world.getTickManager().getTickRate() : 20f;
        // Scale delay based on TPS - lower TPS = higher delay
        float tpsMultiplier = 20f / Math.max(tps, 1f);
        return (int) (blockDelay.get() * tpsMultiplier);
    }

    private int getAdjustedClientDelay() {
        if (!tpsSync.get()) return clientsideDelay.get();

        float tps = mc.world != null ? mc.world.getTickManager().getTickRate() : 20f;
        // Scale delay based on TPS
        float tpsMultiplier = 20f / Math.max(tps, 1f);
        return (int) (clientsideDelay.get() * tpsMultiplier);
    }
    */

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof BlockUpdateS2CPacket packet) {
            if (packet.getState().isAir()) {
                // Block was broken
                blockBreakTimes.add(System.currentTimeMillis());
            }
        }
    }

    public static double getBlocksPerSecond() {
        long currentTime = System.currentTimeMillis();
        blockBreakTimes.removeIf(time -> currentTime - time > 1000);
        return blockBreakTimes.size();
    }

    @Override
    public String getInfoString() {
        return String.format("%.1f BPS", getBlocksPerSecond());
    }

    private boolean isOutOfRange(BlockPos blockPos) {
        Vec3d pos = blockPos.toCenterPos();
        RaycastContext raycastContext = new RaycastContext(mc.player.getEyePos(), pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
        BlockHitResult result = mc.world.raycast(raycastContext);
        if (result == null || !result.getBlockPos().equals(blockPos))
            return !PlayerUtils.isWithin(pos, wallsRange.get());

        return false;
    }

    private void addTargetedBlockToList() {
        if (!selectBlockBind.get().isPressed() || mc.currentScreen != null) return;

        HitResult hitResult = mc.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
        Block targetBlock = mc.world.getBlockState(pos).getBlock();

        List<Block> list = listMode.get() == Nuker.ListMode.Whitelist ? whitelist.get() : blacklist.get();
        String modeName = listMode.get().name();

        if (list.contains(targetBlock)) {
            list.remove(targetBlock);
            info("Removed " + Names.get(targetBlock) + " from " + modeName);
        } else {
            list.add(targetBlock);
            info("Added " + Names.get(targetBlock) + " to " + modeName);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockBreakingCooldown(BlockBreakingCooldownEvent event) {
        event.cooldown = 0;
    }

    private static class BrokenBlock {
        BlockPos pos;
        long breakTime;

        BrokenBlock(BlockPos pos, long breakTime) {
            this.pos = pos;
            this.breakTime = breakTime;
        }
    }

    private static class PendingBreak {
        BlockPos pos;
        long stopTime;

        PendingBreak(BlockPos pos, long stopTime) {
            this.pos = pos;
            this.stopTime = stopTime;
        }
    }
}
