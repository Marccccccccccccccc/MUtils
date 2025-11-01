package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

public class Boykisser extends Module {
    private static final Identifier BOYKISSER_SKIN = Identifier.of("mutils", "textures/boykisserskin.png");

    private static Boykisser INSTANCE;

    public Boykisser() {
        super(MUtils.CATEGORY, "boykisser", "Makes everyone a boykisser :3");
    }

    @Override
    public void onActivate() {
        INSTANCE = this;
    }

    @Override
    public void onDeactivate() {
        INSTANCE = null;
    }

    public static boolean isEnabled() {
        return INSTANCE != null && INSTANCE.isActive();
    }

    public static SkinTextures getSkinTextures() {
        return new SkinTextures(
            BOYKISSER_SKIN,
            null,
            null,
            null,
            SkinTextures.Model.WIDE,
            false
        );
    }
}
