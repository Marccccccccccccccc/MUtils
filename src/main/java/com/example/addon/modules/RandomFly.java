package com.example.addon.modules;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;

public class RandomFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> minSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-speed")
        .description("Minimum flight speed.")
        .defaultValue(0.5)
        .min(0.0)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Double> maxSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("max-speed")
        .description("Maximum flight speed.")
        .defaultValue(3.0)
        .min(0.0)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Double> avgSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("avg-speed")
        .description("Average flight speed (higher weight).")
        .defaultValue(1.5)
        .min(0.0)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Integer> changeInterval = sgGeneral.add(new IntSetting.Builder()
        .name("change-interval")
        .description("How often to change speed in ticks.")
        .defaultValue(20)
        .min(1)
        .sliderMax(200)
        .build()
    );

    public final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Current randomized flight speed (read-only, auto-updated).")
        .defaultValue(0.1)
        .min(0.0)
        .build()
    );

    private int tickCounter = 0;

    public RandomFly() {
        super(MUtils.CATEGORY, "random-flight-speed", "Randomly changes Flight module speed.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Flight flight = Modules.get().get(Flight.class);

        if (flight == null || !flight.isActive()) return;

        tickCounter++;

        if (tickCounter >= changeInterval.get()) {
            tickCounter = 0;

            double min = minSpeed.get();
            double max = maxSpeed.get();
            double avg = avgSpeed.get();

            // Ensure min <= max
            if (min > max) {
                double temp = min;
                min = max;
                max = temp;
            }

            // Clamp avg between min and max
            avg = Math.max(min, Math.min(max, avg));

            // Generate random speed weighted towards average
            // Uses triangular distribution for more natural randomness
            double random = Math.random();
            double newSpeed;

            if (random < 0.5) {
                // Lower half - interpolate between min and avg
                newSpeed = min + Math.sqrt(2 * random) * (avg - min);
            } else {
                // Upper half - interpolate between avg and max
                newSpeed = max - Math.sqrt(2 * (1 - random)) * (max - avg);
            }

            // Update our speed setting
            speed.set(newSpeed);
        }

        // Always apply our speed to Flight
        try {
            Setting<Double> flightSpeed = (Setting<Double>) flight.settings.get("speed");
            if (flightSpeed != null) {
                flightSpeed.set(speed.get());
            }
        } catch (Exception e) {
            // Ignore casting exceptions
        }
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
    }
}
