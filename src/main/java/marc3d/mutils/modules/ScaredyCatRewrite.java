package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class ScaredyCatRewrite extends Module {
    public ScaredyCatRewrite() {
        super(MUtils.CATEGORY, "Scaredy Cat Rewrite", "ScaredyCat");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup Logic = settings.createGroup("Teleport Logic");


    private final Setting<Boolean> direction = sgGeneral.add(new BoolSetting.Builder()
        .name("Direction")
        .description("Teleports in the direction your looking at")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> Distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("Distance")
        .description("Distance to change the Teleport Vector in the view direction")
        .min(1)
        .visible(() -> !direction.get())
        .build()
    );

    private final Setting<Double> minDist = Logic.add(new DoubleSetting.Builder()
        .name("Min TP Distance")
        .description("Minimum distance from Last Teleport Spot")
        .min(1)
        .max(20)
        .build()
    );

    private final Setting<Double> maxDist = Logic.add(new DoubleSetting.Builder()
        .name("Max TP Distance")
        .description("Maximum distance from Last Teleport Spot")
        .min(1)
        .max(20)
        .build()
    );

    /// TODO: Add Smooth Teleports / Decoupled Camara

    private final Setting<Boolean> ServerSide = sgGeneral.add(new BoolSetting.Builder()
        .name("Server Sided")
        .description("If the teleport should not be visible to yourself")
        .defaultValue(false)
        .build()
    );
    /*
    @EventHandler
    private void onRecievePacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket packet) {
            PacketUtils.sendPacket(new TeleportConfirmC2SPacket(packet.getTeleportId()));
            Vec3d packetpos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            if (packetpos.distanceTo(mc.player.getPos()) > 100) {
                mc.player.setPosition(packetpos);
            }
            event.cancel();
        }
    }*/



}
