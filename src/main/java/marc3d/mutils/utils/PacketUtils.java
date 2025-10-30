/*

package com.addon.utils;

import autoplayaddon.mixins.VehicleMoveC2SPacketInterface;
import autoplayaddon.tracker.ServerSideValues;
import io.netty.channel.Channel;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.ClientConnection;

public class PacketUtils {
    public static final Queue<Packet<?>> packetQueue = new LinkedList<>();

    public static void sendPackets(Queue<Packet<?>> packets) {
        StringBuilder packetNames = new StringBuilder();
        int amount = packetQueue.size();
        if (amount == 0) {
            return;
        }
        for (Packet<?> packet : packetQueue) {
            String packetName = packet.getClass().getSimpleName();
            String packetName2 = packetName.replace("C2SPacket", "");
            if (!packetNames.isEmpty()) {
                packetNames.append(", ");
            }
            packetNames.append(packetName2);
        }
        Channel channel = MeteorClient.mc.getNetworkHandler().getConnection().channel;
        for (Packet<?> packet2 : packetQueue) {
            channel.write(packet2);
        }
        channel.flush();
        ServerSideValues.updateAndAdd(amount, System.nanoTime());
    }

    public static void addVehicleMovePacket(double x, double y, double z, float yaw, float pitch) {
        Packet<?> vehicleMovePacket = new VehicleMoveC2SPacket(MeteorClient.mc.player);
        VehicleMoveC2SPacketInterface accessor = (VehicleMoveC2SPacketInterface) vehicleMovePacket;
        accessor.setX(x);
        accessor.setY(y);
        accessor.setZ(z);
        accessor.setYaw(yaw);
        accessor.setPitch(pitch);
        packetQueue.add(vehicleMovePacket);
    }

    public static void addAttackPacketToQueue(int id) throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, SecurityException, IllegalArgumentException {
        try {
            Class<?> packetClass = Class.forName("net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket");
            Field attackField = packetClass.getDeclaredField("ATTACK");
            attackField.setAccessible(true);
            Object attackHandler = attackField.get(null);
            Constructor<?> constructor = packetClass.getDeclaredConstructor(Integer.TYPE, Boolean.TYPE, attackField.getType());
            constructor.setAccessible(true);
            packetQueue.add((PlayerInteractEntityC2SPacket) constructor.newInstance(Integer.valueOf(id), Boolean.valueOf(MeteorClient.mc.player.isSneaking()), attackHandler));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendAllPacketsInQueue() {
        StringBuilder packetNames = new StringBuilder();
        int amount = packetQueue.size();
        if (amount == 0) {
            return;
        }
        for (Packet<?> packet : packetQueue) {
            String packetName = packet.getClass().getSimpleName();
            String packetName2 = packetName.replace("C2SPacket", "");
            if (!packetNames.isEmpty()) {
                packetNames.append(", ");
            }
            packetNames.append(packetName2);
        }
        ClientConnection networkHandler = MeteorClient.mc.getNetworkHandler();
        if (networkHandler == null) {
            return;
        }
        Channel channel = networkHandler.getConnection().channel;
        if (MeteorClient.mc.player == null) {
            ChatUtils.error("Player is null");
            return;
        }
        if (channel == null) {
            ChatUtils.error("Channel is null");
            return;
        }
        if (!channel.isActive()) {
            ChatUtils.error("Channel is not active");
            return;
        }
        for (Packet<?> packet2 : packetQueue) {
            channel.write(packet2);
        }
        channel.flush();
        packetQueue.clear();
        ServerSideValues.updateAndAdd(amount, System.nanoTime());
    }

    public static void addMovePacketToQueue(boolean onGround, Vec3d pos, Float pitch, Float yaw) {
        PlayerMoveC2SPacket movePacket;
        if (pitch != null && yaw != null) {
            if (pos != null) {
                movePacket = new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, yaw.floatValue(), pitch.floatValue(), onGround);
            } else {
                movePacket = new PlayerMoveC2SPacket.LookAndOnGround(yaw.floatValue(), pitch.floatValue(), onGround);
            }
        } else if (pos == null) {
            if (ServerSideValues.predictallowedPlayerTicks() > 20) {
                movePacket = new PlayerMoveC2SPacket.LookAndOnGround(MeteorClient.mc.player.getYaw(), MeteorClient.mc.player.getPitch(), onGround);
            } else {
                movePacket = new PlayerMoveC2SPacket.OnGroundOnly(onGround);
            }
        } else if (ServerSideValues.predictallowedPlayerTicks() > 20) {
            movePacket = new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, MeteorClient.mc.player.getYaw(), MeteorClient.mc.player.getPitch(), onGround);
        } else {
            movePacket = new PlayerMoveC2SPacket.PositionAndOnGround(pos.x, pos.y, pos.z, onGround);
        }
        ServerSideValues.HandleMovePacketSafe(movePacket);
        packetQueue.add(movePacket);
    }

    public static void sendPacket(Packet<?> packet) {
        ServerSideValues.updateAndAdd(1L, System.nanoTime());
        if ((packet instanceof PlayerInteractItemC2SPacket) || (packet instanceof PlayerInteractBlockC2SPacket)) {
            ServerSideValues.handleUse();
        }
        if (packet instanceof PlayerMoveC2SPacket) {
            ServerSideValues.HandleMovepacket((PlayerMoveC2SPacket) packet, true);
        }
        Channel channel = MeteorClient.mc.getNetworkHandler().getConnection().channel;
        channel.writeAndFlush(packet);
    }
}
*/
