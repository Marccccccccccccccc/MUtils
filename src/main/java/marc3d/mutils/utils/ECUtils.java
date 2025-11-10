package marc3d.mutils.utils;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.screen.GenericContainerScreenHandler;


import static meteordevelopment.meteorclient.MeteorClient.EVENT_BUS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ECUtils {

    private static ECUtils instance;

    public ECUtils() {
        // Register this class to receive events
        EVENT_BUS.subscribe(this);
        instance = this;
    }

    public static ECUtils getInstance() {
        if (instance == null) {
            instance = new ECUtils();
        }
        return instance;
    }

    public boolean isEnderChestOpen() {
        return mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler
            && handler.getInventory() == mc.player.getEnderChestInventory();
    }

    @EventHandler
    private void onTick(TickEvent event) {
        MeteorStarscript.ss.set("ECOpen", isEnderChestOpen());
        ChatUtils.info(String.valueOf(isEnderChestOpen()));
    }
}
