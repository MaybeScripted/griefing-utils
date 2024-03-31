package griefingutils;

import com.mojang.logging.LogUtils;
import griefingutils.commands.*;
import griefingutils.hud.BrandHud;
import griefingutils.modules.*;
import griefingutils.modules.creative.DoomBoom;
import griefingutils.modules.creative.ExplosiveHands;
import griefingutils.modules.op.SidebarAdvertise;
import griefingutils.modules.op.WorldDeleter;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

public class GriefingUtils extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final MinecraftClient MC = MinecraftClient.getInstance();

    @Override
    public void onInitialize() {
        LOG.info("Initializing 0x06's Griefing Utils");
        registerModules();
        registerCommands();
        registerHUDs();
    }

    private static void registerModules() {
        Modules.get().add(new AntiBlockEntityLag());
        Modules.get().add(new AntiCrash());
        Modules.get().add(new AntiItemLag());
        Modules.get().add(new AutoLavacast());
        Modules.get().add(new BetterPauseScreen());
        Modules.get().add(new ContainerAction());
        Modules.get().add(new CrackedKickModule());
        Modules.get().add(new DoomBoom());
        Modules.get().add(new ExplosiveHands());
        Modules.get().add(new GamemodeNotifier());
        Modules.get().add(new Privacy());
        Modules.get().add(new SidebarAdvertise());
        Modules.get().add(new SignChanger());
        Modules.get().add(new VanillaFlight());
        Modules.get().add(new WitherAdvertise());
        Modules.get().add(new WorldDeleter());
    }

    private static void registerCommands() {
        Commands.add(new CommandCompleteCrash());
        Commands.add(new ClipboardGive());
        Commands.add(new CrackedKickCommand());
        Commands.add(new Hologram());
        Commands.add(new PurpurCrash());
    }

    private static void registerHUDs() {
        Hud.get().register(BrandHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(Categories.DEFAULT);
    }

    @Override
    public String getPackage() {
        return "griefingutils";
    }
}
