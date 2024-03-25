package griefingutils.modules;

import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends BetterModule {
    public NoFall() {
        super(Categories.DEFAULT, "better-no-fall", "Universal No-Fall (works on almost any anti-cheat).");
    }

    @EventHandler
    private void onPreTick(SendMovementPacketsEvent.Post event) {
        if (!mc.player.isOnGround() && mc.player.fallDistance > 3f) {
            sendPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(),
                mc.player.getY() + Math.random() * 0.000001,
                mc.player.getZ(),
                mc.player.getYaw(),
                mc.player.getPitch(),
                false
            ));
            mc.player.onLanding();
        }
    }
}
