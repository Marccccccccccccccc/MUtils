package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class UnfocusedCPU extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> fps = sgGeneral.add(new IntSetting.Builder()
        .name("fps")
        .description("FPS limit when the window is unfocused.")
        .defaultValue(1)
        .min(1)
        .sliderMin(1)
        .sliderMax(30)
        .build()
    );

    private final Setting<Integer> tickRate = sgGeneral.add(new IntSetting.Builder()
        .name("tick-rate")
        .description("Client tick rate when the window is unfocused (ticks per second).")
        .defaultValue(5)
        .min(1)
        .max(20)
        .sliderMin(1)
        .sliderMax(20)
        .build()
    );

    private boolean wasFocused = true;
    private int originalFpsLimit = -1;
    private int tickCounter = 0;

    public UnfocusedCPU() {
        super(MUtils.CATEGORY2, "UnfocusedCPU", "Reduces CPU usage when Minecraft window is not focused.");

        // Make it always on by default
        this.runInMainMenu = true;
    }

    @Override
    public void onActivate() {
        wasFocused = mc.isWindowFocused();
        if (originalFpsLimit == -1) {
            originalFpsLimit = mc.options.getMaxFps().getValue();
        }

        // Toggle on by default when module is first loaded
        if (!isActive()) {
            toggle();
        }
    }

    @Override
    public void onDeactivate() {
        // Restore original FPS limit
        if (originalFpsLimit != -1) {
            mc.options.getMaxFps().setValue(originalFpsLimit);
            originalFpsLimit = -1;
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean focused = mc.isWindowFocused();

        // Window focus changed
        if (focused != wasFocused) {
            if (!focused) {
                // Window just lost focus - reduce FPS
                originalFpsLimit = mc.options.getMaxFps().getValue();
                mc.options.getMaxFps().setValue(fps.get());
            } else {
                // Window just gained focus - restore FPS
                if (originalFpsLimit != -1) {
                    mc.options.getMaxFps().setValue(originalFpsLimit);
                }
            }
            wasFocused = focused;
        }

        // If unfocused, limit tick rate
        if (!focused) {
            tickCounter++;
            int ticksPerSecond = tickRate.get();
            int skipTicks = 20 / ticksPerSecond;

            // Skip ticks to reduce CPU usage
            if (tickCounter % skipTicks != 0) {
                // Skip rendering for this tick
                skipRenderThisTick();
            }
        } else {
            tickCounter = 0;
        }
    }

    private void skipRenderThisTick() {
        // Placeholder for potential future optimizations
    }
}
