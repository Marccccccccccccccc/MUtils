package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class ItemLog extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Item> item = sgGeneral.add(new ItemSetting.Builder()
        .name("item")
        .description("Item to check count for.")
        .defaultValue(Items.TOTEM_OF_UNDYING)
        .build()
    );

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("item-count")
        .description("Disconnect when item count is at or below this amount.")
        .min(0)
        .defaultValue(1)
        .build()
    );

    private final Setting<Boolean> toggle = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle")
        .description("Toggle off when triggered")
        .defaultValue(true)
        .build()
    );

    private boolean hasDisconnected = false;

    public ItemLog() {
        super(MUtils.CATEGORY, "ItemLog", "Disconnects you when there's a certain amount of items");
    }

    @Override
    public void onActivate() {
        hasDisconnected = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || hasDisconnected) return;

        // Find the item in inventory
        FindItemResult result = InvUtils.find(item.get());

        // Check if count is at or below threshold
        if (result.count() <= amount.get()) {
            //ChatUtils.info("Item count (" + result.count() + ") reached threshold (" + amount.get() + "), disconnecting!");

            // Instant disconnect via invalid packet
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(10));
            hasDisconnected = true;
            if (toggle.get()) toggle();
        }
    }
}
