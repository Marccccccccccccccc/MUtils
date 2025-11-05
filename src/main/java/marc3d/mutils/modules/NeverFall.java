package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import marc3d.mutils.interfaces.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NeverFall extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<GroundMode> groundMode = sgGeneral.add(new EnumSetting.Builder<GroundMode>()
        .name("ground-mode")
        .description("How to spoof the onGround value.")
        .defaultValue(GroundMode.AlwaysFalse)
        .build()
    );

    public NeverFall() {
        super(MUtils.CATEGORY, "NeverFall", "Does magic things that you cant know about");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof PlayerMoveC2SPacket packet) {
            switch (groundMode.get()) {
                case AlwaysTrue -> ((IPlayerMoveC2SPacket) packet).setOnGround(true);
                case AlwaysFalse -> ((IPlayerMoveC2SPacket) packet).setOnGround(false);
                case Vanilla -> {} //do nothing
            }
        }
    }

    public enum GroundMode {
        AlwaysTrue,
        AlwaysFalse,
        Vanilla
    }
}
