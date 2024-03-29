package griefingutils.modules;

import griefingutils.toast.NotificationToast;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;

public class AntiCrash extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> cancelDemoScreen = sgGeneral.add(new BoolSetting.Builder()
        .name("cancel-demo-screen")
        .description("Cancels demo related packets that should never be sent.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> message = sgGeneral.add(new BoolSetting.Builder()
        .name("message")
        .description("Puts a message in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notification = sgGeneral.add(new BoolSetting.Builder()
        .name("notification")
        .description("Notifies you with a toast.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notificationImportant = sgGeneral.add(new BoolSetting.Builder()
        .name("important")
        .description("The notification will flash red and alert you.")
        .defaultValue(false)
        .build()
    );

    public AntiCrash() {
        super(Categories.DEFAULT, "anti-crash", "Cancels packets that may freeze your game.");
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof ExplosionS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid explosion");
        } else if (event.packet instanceof ParticleS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid particles");
        } else if (event.packet instanceof PlayerPositionLookS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid movement");
        } else if (event.packet instanceof EntityVelocityUpdateS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid velocity update");
        } else if (event.packet instanceof InventoryS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid inventory");
        } else if (event.packet instanceof ScreenHandlerSlotUpdateS2CPacket packet && isInvalid(packet)) {
            cancel(event, "invalid slot update");
        } else if (event.packet instanceof GameStateChangeS2CPacket packet && cancelDemoScreen.get() && isInvalid(packet)) {
            cancel(event, "demo packet");
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

    private boolean isInvalid(InventoryS2CPacket packet) {
        if (mc.player == null) return true;
        if (packet.getSyncId() == 0) {
            return packet.getContents().size() > mc.player.playerScreenHandler.slots.size();
        } else
            return mc.player.currentScreenHandler == null ||
                packet.getContents().size() > mc.player.currentScreenHandler.slots.size() + mc.player.playerScreenHandler.slots.size();
    }

    private boolean isInvalid(ScreenHandlerSlotUpdateS2CPacket packet) {
        if (mc.player == null) return true;
        if (packet.getSyncId() == 0) {
            return packet.getSlot() > mc.player.playerScreenHandler.slots.size();
        } else
            return mc.player.currentScreenHandler == null ||
                packet.getSlot() > mc.player.currentScreenHandler.slots.size() + mc.player.playerScreenHandler.slots.size();
    }

    private boolean isInvalid(GameStateChangeS2CPacket packet) {
        return packet.getReason() == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN;
    }

    private void cancel(PacketEvent.Receive event, String reason) {
        if (message.get()) info("Received a bad packet: " + reason);
        if (notification.get()) addToastWithLimit(() -> new NotificationToast(
            Text.of("Anti Crash"),
            Text.of("Received a bad packet:"),
            Text.of(reason),
            Items.BARRIER,
            notificationImportant.get()
        ));
        event.cancel();
    }
}
