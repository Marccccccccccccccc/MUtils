package marc3d.mutils;

import marc3d.mutils.commands.ECTakeCommand;
import marc3d.mutils.commands.Setpearl;
import marc3d.mutils.modules.*;
import com.mojang.logging.LogUtils;
import marc3d.mutils.utils.ECUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import org.slf4j.Logger;


import static meteordevelopment.meteorclient.MeteorClient.mc;

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
        ///Modules.get().add(new StashFinderPlus());
        Modules.get().add(new AntiSuffocate());
        Modules.get().add(new AutoTrial());
        Modules.get().add(new VaultESP());
        Modules.get().add(new MessageRepeater());
        Modules.get().add(new AOTV());
        Modules.get().add(new ForceCrawl());
        //Modules.get().add(new EnderNuker());
        //Modules.get().add(new DoubleBreak());
        Modules.get().add(new ItemLog());
        Modules.get().add(new ModlistLogger());
        Modules.get().add(new ScaredyCatRewrite());
        Modules.get().add(new AdminAbuseNotify());
        Modules.get().add(new CWP());
        Modules.get().add(new BlockBreakLogger());
        Modules.get().add(new DamageLogger());
        Modules.get().add(new ECTracker());
        Modules.get().add(new Boykisser());
        /// TODO: Finish Forcecrawl (Water in head) + Block underneath you (Scaffold)
        /// TODO: ScaredyCatRewrite + Crawl holes
        /// TODO: Finish AutoTrial
        /// TODO "Forke" e621 hud for any website eg botcontrol (;
        /// TODO: Add cmd control somehow later


        // Commands
        Commands.add(new ECTakeCommand());

        // HUD
        //Hud.get().register(BPS.INFO);

        //SS
        MeteorStarscript.ss.set("ECOpen", "N/A");

        //Utils
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
        Modules.registerCategory(CATEGORY2);
    }



    @Override
    public String getPackage() {
        return "marc3d.mutils";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("Marccccccccccccccc", "MUtils");
    }
}
