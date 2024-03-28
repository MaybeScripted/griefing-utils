package griefingutils.modules;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerEntityAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class VanillaFlight extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Horizontal speed in blocks per second.")
        .defaultValue(20)
        .range(0, 300)
        .sliderRange(0, 300)
        .build()
    );

    private final Setting<Double> vertSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Vertical speed in blocks per second.")
        .defaultValue(20)
        .range(0, 300)
        .sliderRange(0, 300)
        .build()
    );

    private final Setting<Double> normalSpeedMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("normal-speed-multiplier")
        .description("The multiplier of your speed when you're not sprinting.")
        .defaultValue(0.75)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );

    public VanillaFlight() {
        super(Categories.DEFAULT, "vanilla-flight", "Flight with smart packet-based anti-kick.");
    }

    // don't clip into blocks
    private boolean canMoveHorizontally(double amount) {
        Box boundingBox = mc.player.getBoundingBox();
        boundingBox = boundingBox.offset(0, amount, 0).union(boundingBox);
        if (mc.world.getBlockCollisions(null, boundingBox).iterator().hasNext()) return false;
        return true;
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;

        // packet anti kick
        if (mc.world.getTime() % 10 == 0) if (canMoveHorizontally(-0.1)) {
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ(), false));
            ((ClientPlayerEntityAccessor) mc.player).setTicksSinceLastPositionPacketSent(19);
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        double speedMul = mc.player.isSprinting() ? 1 : normalSpeedMultiplier.get();

        double velY = 0;
        if (mc.options.jumpKey.isPressed()) velY += (vertSpeed.get() + 0.001) * speedMul / 20;
        if (mc.options.sneakKey.isPressed()) velY -= (vertSpeed.get() + 0.001) * speedMul / 20;

        Vec3d vel = PlayerUtils.getHorizontalVelocity(speed.get() * speedMul - 0.001);
        Vec3d newVelocity = vel.add(0, velY, 0);
        ((IVec3d)mc.player.getVelocity()).set(newVelocity.x, newVelocity.y, newVelocity.z);
    }
}

