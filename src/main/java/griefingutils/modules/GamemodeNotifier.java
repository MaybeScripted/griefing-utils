package griefingutils.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.world.GameMode;

public class GamemodeNotifier extends BetterModule {
    public GamemodeNotifier() {
        super(Categories.DEFAULT, "gamemode-notifier", "Alerts you when someone changes their gamemode.");
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (!Utils.canUpdate() || !(event.packet instanceof PlayerListS2CPacket packet)) return;

        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            for (Action action : packet.getActions()) {
                if (action != Action.UPDATE_GAME_MODE ||
                    packet.getPlayerAdditionEntries().contains(entry)) continue;

                GameMode gameMode = entry.gameMode();
                String player = networkHandler().getPlayerListEntry(entry.profileId()).getProfile().getName();
                info("%s has switched to %s mode!".formatted(player, gameMode.getName()));

            }
        }
    }
}
