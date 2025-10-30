package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import marc3d.mutils.utils.Webhook;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import meteordevelopment.meteorclient.systems.friends.Friends;
import net.minecraft.text.Text;



public class Alert extends Module {
    String webhookUrl = System.getenv("DISCORD_WEBHOOK_URL");

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public Alert() {
        super(MUtils.CATEGORY, "Alert", "description");
    }


    @EventHandler
    private void onNewplayer(EntityAddedEvent event) {
        if (!(event.entity instanceof PlayerEntity)) return;
        if (mc.player.equals(event.entity)) return;
        if (Friends.get().isFriend((PlayerEntity) event.entity)) return;
        PlayerEntity player = (PlayerEntity) event.entity;
        String name = player.getGameProfile().getName();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        String payload = "{\"player\":\"" + name + "\", \"x\":\"" + x + "\", \"y\":\"" + y + "\", \"z\":\"" + z + "\"}";
        payload = "player:" + name;

        if (webhookUrl == null) {
                System.err.println("No webhook URL found! Set DISCORD_WEBHOOK_URL env variable.");
                ChatUtils.sendMsg(Text.of("No webhook URL found! Set DISCORD_WEBHOOK_URL env variable."));
                return;
            } else {
                Webhook.send(webhookUrl,payload);
        }

        System.out.println(payload);
        ///ChatUtils.sendMsg(Text.of("New Entity Added!"));
    }
}



