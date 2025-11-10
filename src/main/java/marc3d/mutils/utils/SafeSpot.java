package marc3d.mutils.utils;


import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SafeSpot {
    // Default interact range in Minecraft
    //Made for AutoEC
    private static final double DEFAULT_RADIUS = 4.5;
    private static final int MAX_CHECKS = 100;
    private static final Random random = new Random();

    /**
     * Finds a safe spot around a BlockPos within interact range
     * Returns the first safe spot found using randomized checking
     *
     * @param center Center BlockPos to scan around
     * @return Vec3d of safe spot, or null if none found
     */
    public static Vec3d findSafeSpot(BlockPos center) {
        return findSafeSpot(center, DEFAULT_RADIUS);
    }

    /**
     * Finds a safe spot around a BlockPos within a spherical radius
     * Uses randomized checking to avoid repeatedly checking obstructed blocks
     *
     * @param center Center BlockPos to scan around
     * @param radius radius to scan
     * @return Vec3d of safe spot, or null if none found
     */
    public static Vec3d findSafeSpot(BlockPos center, double radius) {
        double radiusSquared = radius * radius;

        for (int i = 0; i < MAX_CHECKS; i++) {
            // random point within sphere
            double x = center.getX() + (random.nextDouble() * 2 - 1) * radius;
            double y = center.getY() + (random.nextDouble() * 2 - 1) * radius;
            double z = center.getZ() + (random.nextDouble() * 2 - 1) * radius;

            // useless check
            double dx = x - center.getX();
            double dy = y - center.getY();
            double dz = z - center.getZ();
            double distanceSquared = dx * dx + dy * dy + dz * dz;

            if (distanceSquared <= radiusSquared) {
                Vec3d pos = new Vec3d(x, y, z);
                if (isSafeSpot(pos)) {
                    return pos;
                }
            }
        }

        return null; // No safe spot found after MAX_CHECKS attempts
    }

    /**
     * Checks if a specific coordinate is a safe spot (2 blocks of air)
     *
     * @param pos Vec3d position to check
     * @return true if the location is safe
     */
    private static boolean isSafeSpot(Vec3d pos) {
        BlockPos blockPos1 = BlockPos.ofFloored(pos);
        BlockPos blockPos2 = BlockPos.ofFloored(pos.x, pos.y + 1, pos.z);

        BlockState block1 = mc.world.getBlockState(blockPos1);
        BlockState block2 = mc.world.getBlockState(blockPos2);

        // Check if both blocks are passable
        return isPassable(block1) && isPassable(block2);
    }

    /**
     * Checks if a block is passable (not solid)
     */
    private static boolean isPassable(BlockState state) {
        return !state.isSolid();
    }

    /**
     * Example usage
     */
    public static void example() {
        BlockPos center = new BlockPos(100, 64, 100);

        // Use default interact range
        Vec3d safeSpot = findSafeSpot(center);

        // Or specify custom radius
        // Vec3d safeSpot = findSafeSpot(center, 10.0);
        /*
        if (safeSpot != null) {
            System.out.println("Found safe spot at: " + safeSpot.x + ", " + safeSpot.y + ", " + safeSpot.z);
        } else {
            System.out.println("No safe spot found within radius");
        }
        */
    }
}
