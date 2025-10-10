package com.example.addon;

import com.example.addon.commands.CommandExample;
import com.example.addon.commands.Setpearl;
import com.example.addon.hud.HudExample;
import com.example.addon.modules.*;
import com.example.addon.utils.Straighttp;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;

public class MUtils extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("MUtils");
    public static final Category CATEGORY2 = new Category("MUtils-BOT");
    public static final HudGroup HUD_GROUP = new HudGroup("Example");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Addon MUtils");

        // Modules
        Modules.get().add(new HitNotifier());
        Modules.get().add(new EmptySlot());
        Modules.get().add(new Alert());
        Modules.get().add(new StashFinderPlus());
        Modules.get().add(new AntiSuffocate());
        Modules.get().add(new AutoTrial());
        Modules.get().add(new VaultESP());
        Modules.get().add(new MessageRepeater());
        Modules.get().add(new AOTV());
        /// TODO: Add an module that places obsidian above and below you to get you to crawl
        /// TODO: Add an module that teleports you in crawlholes
        /// TODO: Add an module that leaves when you have a specific amount of an item eg Totems
        /// TODO: Finish AutoTrial
        /// TODO "Forke" e621 hud for any website eg botcontrol (;


        // Commands
        Commands.add(new CommandExample());
        Commands.add(new Setpearl());

        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
        Modules.registerCategory(CATEGORY2);
    }



    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Marccccccccccccccc", "MUtils");
    }
}
