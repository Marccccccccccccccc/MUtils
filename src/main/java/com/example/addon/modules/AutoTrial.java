package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockEntityIterator;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class AutoTrial extends Module {

    public AutoTrial(Category category, String name, String description, String... aliases) {
        super(MUtils.CATEGORY, "AutoTrial", "Tries to automate Trial Chambers");
    }

    // Method to get all trial spawner locations
    private List<BlockPos> getAllTrialSpawners() {
        List<BlockPos> spawners = new ArrayList<>();

        BlockEntityIterator iterator = new BlockEntityIterator();

        while (iterator.hasNext()) {
            BlockEntity blockEntity = iterator.next();

            // Check if it's a trial spawner
            if (blockEntity instanceof TrialSpawnerBlockEntity) {
                spawners.add(blockEntity.getPos());
            }
        }

        return spawners;
    }

    // Usage in your module
    @Override
    public void onActivate() {
        List<BlockPos> trialSpawners = getAllTrialSpawners();

        info("Found " + trialSpawners.size() + " trial spawners");

        for (BlockPos pos : trialSpawners) {
            info("Trial spawner at: " + pos);
        }
    }

    // Or for rendering
    @EventHandler
    private void onRender(Render3DEvent event) {
        BlockEntityIterator iterator = new BlockEntityIterator();

        while (iterator.hasNext()) {
            BlockEntity blockEntity = iterator.next();

            if (blockEntity instanceof TrialSpawnerBlockEntity) {
                BlockPos pos = blockEntity.getPos();

                // Render box around trial spawner
                event.renderer.box(pos, Color.RED, ShapeMode.Both, 0);
            }
        }
    }
}
