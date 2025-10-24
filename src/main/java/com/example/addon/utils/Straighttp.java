package com.example.addon.utils;

import net.minecraft.util.math.Vec3d;

public class Straighttp {
    public static Vec3d moveByYawPitch(Vec3d origin, float yaw, float pitch, double distance) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double x = origin.x - distance * Math.sin(yawRad) * Math.cos(pitchRad);
        double y = origin.y + distance * Math.sin(-pitchRad);
        double z = origin.z + distance * Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vec3d(x, y, z);
    }
}
