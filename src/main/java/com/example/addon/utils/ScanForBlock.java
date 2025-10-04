
package com.example.addon.utils;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Educational module: collect positions of "trial spawner" like blocks in loaded chunks.
 * ONLY FOR SINGLEPLAYER / DEVELOPMENT / VISUALIZATION.
 */
public class ScanForBlock extends Module {
    // Collected block positions — cleared and repopulated each tick
    private final List<BlockPos> found = new ArrayList<>();

    public ScanForBlock() {
        super(MUtils.CATEGORY, "AutoTrial", "desc");
    }

    /**
     * Simple predicate to determine whether a BlockState should be considered a "trial spawner".
     * Replace the logic with the actual block check you want for your dev work.
     */
    private boolean isTrialSpawner(BlockState state) {
        if (state == null) return false;

        Block b = state.getBlock();

        // EXAMPLE approaches:
        // 1) If there's a named block in vanilla/tags: check by identity (replace with your block)
        //    return b == Blocks.SPAWNER; // example - replace or extend

        // 2) If it's a modded block identified by an identifier:
        //    Identifier id = Registry.BLOCK.getId(b);
        //    return id != null && id.toString().equals("modid:trial_spawner");

        // 3) Heuristic: check block's translationKey / class name (dev-time only)
        String name = b.getTranslationKey(); // e.g. block.minecraft.spawner
        return name.toLowerCase().contains("trial") || name.toLowerCase().contains("spawner");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;

        // Safety: only run when client world exists
        ClientWorld world = mc.world;

        found.clear();

        // View distance in chunks from client options (Meteor's mc.options.getViewDistance() returns chunk radius)
        int viewDistanceChunks = mc.options.getViewDistance().getValue(); // chunk radius

        // Player's chunk coordinates
        int playerChunkX = mc.player.getChunkPos().x;
        int playerChunkZ = mc.player.getChunkPos().z;

        // Iterate chunk coordinates in square around player
        for (int dx = -viewDistanceChunks; dx <= viewDistanceChunks; dx++) {
            for (int dz = -viewDistanceChunks; dz <= viewDistanceChunks; dz++) {
                int cx = playerChunkX + dx;
                int cz = playerChunkZ + dz;

                // Get the loaded chunk. This returns null or an unloaded chunk if not present client-side.
                Chunk chunk = mc.world.getChunk(cx, cz); // client-safe chunk access (adjust if your MC version differs)
                //if (!(chunk instanceof WorldChunk)) continue; // skip if not a fully loaded client chunk

                WorldChunk wchunk = (WorldChunk) chunk;

                // Iterate through blocks inside the chunk bounds (16x16), and reasonable Y range.
                // To reduce cost, we can iterate only relevant Y range (e.g., 0..world.getTopY()).
                int baseX = cx << 4;
                int baseZ = cz << 4;
                int minY = -1;
                int maxY = 0;

                for (int localX = 0; localX < 16; localX++) {
                    for (int localZ = 0; localZ < 16; localZ++) {
                        // Optionally narrow Y scan if you know the typical Y-range of the block
                        for (int y = minY; y <= maxY; y++) {
                            int worldX = baseX + localX;
                            int worldZ = baseZ + localZ;
                            BlockPos pos = new BlockPos(worldX, y, worldZ);

                            BlockState state = wchunk.getBlockState(pos);
                            if (isTrialSpawner(state)) {
                                found.add(pos);
                            }
                        }
                    }
                }
            }
        }

        ///info("Found " + found.size() + " matching block(s) in loaded chunks.");
    }

    /**
     * Expose collected positions for other in-mod features (renderers, loggers) — still singleplayer/dev only.
     */
    public List<BlockPos> getFound() {
        return found;
    }
}
