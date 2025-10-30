package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class BlockBreakLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDebug = settings.createGroup("Debug");

    // Track what block each player is looking at
    private final Map<PlayerEntity, BlockPos> playerLookingAt = new HashMap<>();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Detection mode.")
        .defaultValue(Mode.Raycast)
        .build()
    );

    private final Setting<Double> raycastRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("raycast-range")
        .description("How far to raycast from players.")
        .defaultValue(6.0)
        .range(1.0, 20.0)
        .sliderRange(1.0, 10.0)
        .visible(() -> mode.get() == Mode.Raycast)
        .build()
    );

    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-distance")
        .description("Maximum distance to consider a player as the breaker.")
        .defaultValue(10.0)
        .range(1.0, 50.0)
        .sliderRange(1.0, 20.0)
        .visible(() -> mode.get() == Mode.Distance)
        .build()
    );

    private final Setting<Boolean> logToChat = sgGeneral.add(new BoolSetting.Builder()
        .name("log-to-chat")
        .description("Log block breaks to chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logToConsole = sgGeneral.add(new BoolSetting.Builder()
        .name("log-to-console")
        .description("Log block breaks to console.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> showCoordinates = sgGeneral.add(new BoolSetting.Builder()
        .name("show-coordinates")
        .description("Show coordinates of broken block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showBlockType = sgGeneral.add(new BoolSetting.Builder()
        .name("show-block-type")
        .description("Show what type of block was broken.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-distance")
        .description("Show distance from breaker to block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreSelf = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-self")
        .description("Don't log blocks you break.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> debugMode = sgDebug.add(new BoolSetting.Builder()
        .name("debug-mode")
        .description("Show detailed debug information.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> showTracking = sgDebug.add(new BoolSetting.Builder()
        .name("show-tracking")
        .description("Show what each player is looking at (periodic updates).")
        .defaultValue(false)
        .visible(() -> debugMode.get() && mode.get() == Mode.Raycast)
        .build()
    );

    public enum Mode {
        Raycast,
        Distance
    }

    public BlockBreakLogger() {
        super(MUtils.CATEGORY2, "BlockBreakLogger", "Logs who broke blocks by tracking what players are looking at");
    }

    @Override
    public void onActivate() {
        playerLookingAt.clear();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.world == null || mode.get() != Mode.Raycast) return;

        // Update what each player is looking at
        playerLookingAt.clear();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (ignoreSelf.get() && player.equals(mc.player)) continue;

            BlockPos lookingAt = raycastFromPlayer(player);
            if (lookingAt != null) {
                playerLookingAt.put(player, lookingAt);
            }
        }

        // Debug: show tracking info every 2 seconds
        if (debugMode.get() && showTracking.get() && mc.player.age % 40 == 0) {
            ChatUtils.info("=== Player Tracking ===");
            ChatUtils.info("Tracking " + playerLookingAt.size() + " players");
            for (Map.Entry<PlayerEntity, BlockPos> entry : playerLookingAt.entrySet()) {
                ChatUtils.info(entry.getKey().getName().getString() + " looking at " + entry.getValue());
            }
        }
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        if (mc.world == null || mc.player == null) return;

        BlockPos pos = event.pos;

        // Check if block turned to air (was broken)
        if (event.newState.getBlock() == Blocks.AIR && event.oldState.getBlock() != Blocks.AIR) {
            String blockName = event.oldState.getBlock().getName().getString();

            if (debugMode.get()) {
                ChatUtils.info("Block broken at " + pos + ": " + blockName);
            }

            PlayerEntity breaker = null;
            double distance = 0;

            if (mode.get() == Mode.Raycast) {
                // Find who was looking at this block
                for (Map.Entry<PlayerEntity, BlockPos> entry : playerLookingAt.entrySet()) {
                    if (entry.getValue().equals(pos)) {
                        breaker = entry.getKey();
                        distance = Math.sqrt(breaker.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

                        if (debugMode.get()) {
                            ChatUtils.info("Found breaker via raycast: " + breaker.getName().getString());
                        }
                        break;
                    }
                }
            } else if (mode.get() == Mode.Distance) {
                // Find the closest player
                double closestDist = Double.MAX_VALUE;

                for (PlayerEntity player : mc.world.getPlayers()) {
                    if (ignoreSelf.get() && player.equals(mc.player)) continue;

                    double dist = Math.sqrt(player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (dist <= maxDistance.get() && dist < closestDist) {
                        closestDist = dist;
                        breaker = player;
                        distance = dist;
                    }
                }

                if (debugMode.get()) {
                    if (breaker != null) {
                        ChatUtils.info("Found breaker via distance: " + breaker.getName().getString() + " (" + String.format("%.1f", distance) + "m)");
                    }
                }
            }

            if (breaker == null) {
                if (debugMode.get()) {
                    ChatUtils.warning("No breaker found for block at " + pos);
                }
                return;
            }

            if (ignoreSelf.get() && breaker.equals(mc.player)) {
                if (debugMode.get()) {
                    ChatUtils.info("Ignoring self break");
                }
                return;
            }

            // Build log message
            StringBuilder message = new StringBuilder();
            message.append(breaker.getName().getString());
            message.append(" broke");

            if (showBlockType.get()) {
                message.append(" ").append(blockName);
            } else {
                message.append(" a block");
            }

            if (showCoordinates.get()) {
                message.append(" at (").append(pos.getX())
                    .append(", ").append(pos.getY())
                    .append(", ").append(pos.getZ()).append(")");
            }

            if (showDistance.get()) {
                message.append(" [").append(String.format("%.1f", distance)).append("m]");
            }

            // Log to chat
            if (logToChat.get()) {
                ChatUtils.info(message.toString());
            }

            // Log to console
            if (logToConsole.get()) {
                info(message.toString());
            }
        }
    }

    private BlockPos raycastFromPlayer(PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(raycastRange.get()));

        HitResult result = mc.world.raycast(new net.minecraft.world.RaycastContext(
            start,
            end,
            net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
            net.minecraft.world.RaycastContext.FluidHandling.NONE,
            player
        ));

        if (result.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = ((BlockHitResult) result).getBlockPos();

            if (debugMode.get() && showTracking.get() && mc.player.age % 40 == 0) {
                ChatUtils.info(player.getName().getString() + " raycast hit: " + hitPos);
            }

            return hitPos;
        }

        return null;
    }
}
