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
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoTrial extends Module {


    public AutoTrial() {
        super(MUtils.CATEGORY, "AutoTrial", "Tries to automate Trial Chambers");
    }

    // Method to get all trial spawner locations
    private List<TrialSpawnerBlockEntity> getAllTrialSpawners() {
        List<TrialSpawnerBlockEntity> spawners = new ArrayList<>();

        BlockEntityIterator iterator = new BlockEntityIterator();

        while (iterator.hasNext()) {
            BlockEntity blockEntity = iterator.next();

            // Check if it's a trial spawner
            if (blockEntity instanceof TrialSpawnerBlockEntity) {
                spawners.add((TrialSpawnerBlockEntity)blockEntity);
            }
        }

        return spawners;
    }

    // Usage in your module
    @Override
    public void onActivate() {
        List<TrialSpawnerBlockEntity> trialSpawners = getAllTrialSpawners();



        info("Found " + trialSpawners.size() + " trial spawners");

        for (TrialSpawnerBlockEntity blockEntity : trialSpawners) {
            //TrialSpawnerLogic logic = spawner.getTrialSpawner();
            //TrialSpawnerState state = logic.getState();

            info("Trial Spawner at " + blockEntity.getPos() + " - State: " + blockEntity.getSpawnerState());
            if (blockEntity.getSpawnerState() == TrialSpawnerState.COOLDOWN) {
                info("Cooldown block found at " + blockEntity.getPos());
            }
        }
    }

    // Or for rendering
    @EventHandler
    private void onRender(Render3DEvent event) {
        BlockEntityIterator iterator = new BlockEntityIterator();
        assert mc.world != null;
        ///long currentTime = mc.world.getTime();

        while (iterator.hasNext()) {
            BlockEntity blockEntity = iterator.next();
            if (blockEntity instanceof TrialSpawnerBlockEntity tE) {
                BlockPos pos = blockEntity.getPos();
                TrialSpawnerState tEState = tE.getSpawnerState();

                ///Vec3d textPos = new Vec3d(
                ///    pos.getX() + 0.5,
                ///    pos.getY() + 1.5,  // Above the spawner
                ///    pos.getZ() + 0.5
                ///);


                if (tEState == TrialSpawnerState.COOLDOWN) {
                    event.renderer.box(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                        new Color(255, 0, 0, 75),
                        new Color(255, 0, 0, 255),
                        ShapeMode.Both,
                        0
                    );

                    } else if (tEState == TrialSpawnerState.ACTIVE) {
                    event.renderer.box(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                        new Color(0, 255, 0, 75),
                        new Color(0, 255, 0, 255),
                        ShapeMode.Both,
                        0);
                } //else if (tEState == TrialSpawnerState.)
            }
        }
    }
}
