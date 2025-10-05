package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockEntityIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.enums.VaultState;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class VaultESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<SettingColor> ominous = sgGeneral.add(new ColorSetting.Builder()
        .name("Ominous")
        .description("The color of Ominous Vaults")
        .defaultValue(new SettingColor(152, 66, 245, 255))
        .build()
    );
    private final Setting<SettingColor> normal = sgGeneral.add(new ColorSetting.Builder()
        .name("Normal")
        .description("The color of Normal Vaults.")
        .defaultValue(new SettingColor(255, 141, 41, 255))
        .build()
    );
    public VaultESP() {
        super(MUtils.CATEGORY, "VaultESP", "Highlights Vaults and their Variants");
    }
    private  List<VaultBlockEntity> getAllVaults() {
        List<VaultBlockEntity> vaults = new ArrayList<>();

        BlockEntityIterator iterator = new BlockEntityIterator();

        while (iterator.hasNext()) {
            BlockEntity blockEntity = iterator.next();

            if (blockEntity instanceof VaultBlockEntity) {
                vaults.add((VaultBlockEntity)blockEntity);
            }
        }

        return vaults;
    }


    @EventHandler
    private void onRender(Render3DEvent event) {
        BlockEntityIterator iterator = new BlockEntityIterator();
        assert mc.world != null;

        while (iterator.hasNext()) {
            BlockEntity blockEntity = iterator.next();
            if (blockEntity instanceof VaultBlockEntity vault) {
                BlockPos pos = vault.getPos();

                // Get the block state
                BlockState state = mc.world.getBlockState(pos);

                // Get ominous property
                boolean isOminous = state.get(VaultBlock.OMINOUS);

                // Get vault state
                //VaultState vaultState = state.get(VaultBlock.STATE);

                if (isOminous) {
                    event.renderer.box(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                        ominous.get(),
                        ominous.get(),
                        ShapeMode.Lines,
                        0
                    );
                } else {
                    event.renderer.box(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                        normal.get(),
                        normal.get(),
                        ShapeMode.Lines,
                        0
                    );
                }
            }

        }
    }
}
