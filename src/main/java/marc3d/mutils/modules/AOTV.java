package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import marc3d.mutils.utils.movement.Movement;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import static marc3d.mutils.utils.Straighttp.moveByYawPitch;

public class AOTV extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgMoreSettings = settings.createGroup("More Settings");

    private final Setting<Double> Distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("Distance")
        .description("Distance to teleport")
        .min(1)
        .max(210)
        .build()
    );
    private final Setting<Integer> Delay = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay in Ticks between Teleports")
        .min(1)
        .sliderMin(1)
        .sliderMax(60)
        .build()
    );
    private final Setting<Boolean> Sword = sgGeneral.add(new BoolSetting.Builder()
        .name("Only with Sword")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> playsound = sgGeneral.add(new BoolSetting.Builder()
        .name("Whether to play the Enderman sound")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> setClientSided = sgMoreSettings.add(new BoolSetting.Builder()
        .name("setClientSided")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> onGroud = sgMoreSettings.add(new BoolSetting.Builder()
        .name("onGroud")
        .defaultValue(false)
        .build()
    );

    private int cooldown = Delay.get();

    public AOTV() {
        super(MUtils.CATEGORY, "AOTV", "Hypixel mfs will love this");
    }


    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player.getInventory().getSelectedStack().getUseAction() != UseAction.NONE) return;
        if (!mc.options.useKey.isPressed()) return;
        ItemStack held = mc.player.getMainHandStack();
        boolean isSword =
            held.isOf(Items.WOODEN_SWORD) ||
                held.isOf(Items.STONE_SWORD) ||
                held.isOf(Items.IRON_SWORD) ||
                held.isOf(Items.GOLDEN_SWORD) ||
                held.isOf(Items.DIAMOND_SWORD) ||
                held.isOf(Items.DIAMOND_SHOVEL) ||
                held.isOf(Items.NETHERITE_SWORD);
        if (cooldown > 0) cooldown--;
        if (cooldown == 0) {
            if (Sword.get() && isSword) {
                Vec3d newPos = moveByYawPitch(mc.player.getEyePos(), mc.player.getYaw(), mc.player.getPitch(), Distance.get());
                if (playsound.get()) mc.world.playSoundFromEntity(mc.player, mc.player, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 3.0F, 1.0F);
                Movement.teleport(newPos, setClientSided.get(), onGroud.get());
                cooldown = Delay.get();
            }
        }
    }
}
