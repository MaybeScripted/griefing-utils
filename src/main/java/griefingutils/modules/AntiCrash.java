package griefingutils.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.s2c.play.*;

public class AntiCrash extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> log = sgGeneral.add(new BoolSetting.Builder()
        .name("log")
        .description("Logs when a crash packet is detected.")
        .defaultValue(true)
        .build()
    );

    public AntiCrash() {
        super(Categories.DEFAULT, "anti-crash", "Cancels packets that may freeze your game.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ExplosionS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid explosion");
        } else if (event.packet instanceof ParticleS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid particles");
        } else if (event.packet instanceof PlayerPositionLookS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid movement");
        } else if (event.packet instanceof EntityVelocityUpdateS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid velocity update");
        } else if (event.packet instanceof GameStateChangeS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid game state change");
        }
    }

    private boolean isInvalid(ExplosionS2CPacket p) {
        return p.getX() > 30_000_000 ||
            p.getY() > 30_000_000 ||
            p.getZ() > 30_000_000 ||
            p.getX() < -30_000_000 ||
            p.getY() < -30_000_000 ||
            p.getZ() < -30_000_000 ||
            p.getRadius() > 1000 ||
            p.getAffectedBlocks().size() > 100_000 ||
            p.getPlayerVelocityX() > 30_000_000 ||
            p.getPlayerVelocityY() > 30_000_000 ||
            p.getPlayerVelocityZ() > 30_000_000 ||
            p.getPlayerVelocityX() < -30_000_000 ||
            p.getPlayerVelocityY() < -30_000_000 ||
            p.getPlayerVelocityZ() < -30_000_000;
    }

    private boolean isInvalid(ParticleS2CPacket p) {
        return p.getCount() > 100_000 ||
            p.getSpeed() > 100_000;
    }

    private boolean isInvalid(PlayerPositionLookS2CPacket p) {
        return p.getX() > 30_000_000 ||
            p.getY() > 30_000_000 ||
            p.getZ() > 30_000_000 ||
            p.getX() < -30_000_000 ||
            p.getY() < -30_000_000 ||
            p.getZ() < -30_000_000;
    }

    private boolean isInvalid(EntityVelocityUpdateS2CPacket p) {
        return p.getVelocityX() > 30_000_000 ||
            p.getVelocityY() > 30_000_000 ||
            p.getVelocityZ() > 30_000_000 ||
            p.getVelocityX() < -30_000_000 ||
            p.getVelocityY() < -30_000_000 ||
            p.getVelocityZ() < -30_000_000;
    }

    private boolean isInvalid(GameStateChangeS2CPacket packet) {
        return packet.getReason() == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN;
    }

    private void cancel(PacketEvent.Receive event, String reason) {
        if (log.get()) info("Server sent funny packet to you: " + reason);
        event.cancel();
    }
}
