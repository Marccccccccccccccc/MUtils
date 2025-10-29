package com.example.addon.hud;

import com.example.addon.MUtils;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

//import static com.example.addon.modules.EnderNuker.getBlocksPerSecond;

public class BPS extends HudElement {
    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public static final HudElementInfo<BPS> INFO = new HudElementInfo<>(MUtils.HUD_GROUP, "BPS", "Displays the Blocks per Second", BPS::new);

    public BPS() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth("BPS", true), renderer.textHeight(true));

        // Render background
        renderer.quad(x, y, getWidth(), getHeight(), Color.BLACK);

        // Render text
        //renderer.text(String.format("%.1f BPS", getBlocksPerSecond()), x, y, Color.WHITE, true);
    }
}
