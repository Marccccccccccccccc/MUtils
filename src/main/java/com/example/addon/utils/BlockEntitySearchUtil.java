package example.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockEntitySearchUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    /**
     * Scans loaded chunks for block entities of given class.
     * @param clazz the block entity class to match
     * @return list of BlockPos where found
     */
    public static List<BlockPos> findBlockEntities(Class<? extends BlockEntity> clazz) {
        List<BlockPos> result = new ArrayList<>();

        if (mc == null || mc.world == null) return result;
        ClientWorld world = mc.world;

        // ** This part depends on your mapping **
        // Suppose the ChunkManager has a method like `getLoadedChunks()`
        for (Chunk chunk : world.getChunkManager().getChunk(mc.player.getChunkPos())) {
            if (!(chunk instanceof WorldChunk)) continue;
            WorldChunk wchunk = (WorldChunk) chunk;

            // Suppose WorldChunk has a method like getBlockEntityMap() or getBlockEntities()
            Map<BlockPos, BlockEntity> beMap = wchunk.getBlockEntityMap();  // adjust if the name differs

            for (Map.Entry<BlockPos, BlockEntity> e : beMap.entrySet()) {
                BlockEntity be = e.getValue();
                if (be != null && clazz.isInstance(be)) {
                    result.add(e.getKey().toImmutable());
                }
            }
        }

        return result;
    }
}
