package griefingutils.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.AutoSign;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class SignChanger extends BetterModule {
    private final SettingGroup sgFront = settings.createGroup("Front Side");
    private final SettingGroup sgBack = settings.createGroup("Back Side");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<String> frontLine1 = sgFront.add(new StringSetting.Builder()
        .name("1st line")
        .description("First line of the front side of the sign.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> frontLine2 = sgFront.add(new StringSetting.Builder()
        .name("2nd line")
        .description("Second line of the front side of the sign.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> frontLine3 = sgFront.add(new StringSetting.Builder()
        .name("3rd line")
        .description("Third line of the front side of the sign.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> frontLine4 = sgFront.add(new StringSetting.Builder()
        .name("4th line")
        .description("Fourth line of the front side of the sign.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> backLine1 = sgBack.add(new StringSetting.Builder()
        .name("1st line")
        .description("First line of the back side of the sign.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> backLine2 = sgBack.add(new StringSetting.Builder()
        .name("2nd line")
        .description("Second line of the back side of the sign.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> backLine3 = sgBack.add(new StringSetting.Builder()
        .name("3rd line")
        .description("Third line of the back side of the sign.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> backLine4 = sgBack.add(new StringSetting.Builder()
        .name("4th line")
        .description("Fourth line of the back side of the sign.")
        .defaultValue("")
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Whether to render things.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 75, 0, 127))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 75, 0))
        .visible(render::get)
        .build()
    );

    private boolean writingSign = false;
    private boolean isFront = true;

    public SignChanger() {
        super(Categories.DEFAULT, "sign-changer", "Changes nearby signs to your text.");
    }

    @Override
    public void onActivate() {
        if (Modules.get().isActive(AutoSign.class))
            warning("Sign Changer may overwrite text from Auto Sign!");
    }

    @Override
    public void onDeactivate() {
        writingSign = false;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (!(Utils.canUpdate() && !isSpectator()) || writingSign) return;

        for (BlockEntity be : Utils.blockEntities()) {
            if (!(be instanceof SignBlockEntity sbe) || sbe.isWaxed()) continue;

            if (!PlayerUtils.isWithinReach(sbe.getPos())) continue;

            SignSides signSides;

            signSides = getSignSides(sbe);

            if (signSides == SignSides.BOTH) continue;
            else if (signSides == SignSides.NONE || signSides == SignSides.BACK) {
                isFront = true;
            } else if (signSides == SignSides.FRONT) {
                isFront = false;
            }

            writingSign = true;

            mc.interactionManager.sendSequencedPacket(mc.world, sequence ->
                new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResultOf(sbe.getPos()), sequence)
            );
            break;
        }
    }

    private BlockPos currentSignPos = null;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof SignEditorOpenS2CPacket packet) {
            event.cancel();
            currentSignPos = packet.getPos();
            try {
                // This fixes event.cancel() in singleplayer?!
                // Because of this I wasted 2 hours of my life debugging the EventBus
                if(mc.isInSingleplayer()) Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            sendEditSignPacket(packet.getPos(), isFront);
        } else if(event.packet instanceof BlockEntityUpdateS2CPacket packet) {
            if (!packet.getPos().equals(currentSignPos)) return;
            writingSign = false;
            if (render.get()) renderPos(currentSignPos);
        }
    }

    private SignSides getSignSides(SignBlockEntity sbe) {
        Text[] frontText = sbe.getFrontText().getMessages(false);
        Text[] backText = sbe.getBackText().getMessages(false);

        boolean front = Objects.equals(frontText[0].getString(), frontLine1.get()) &&
            Objects.equals(frontText[1].getString(), frontLine2.get()) &&
            Objects.equals(frontText[2].getString(), frontLine3.get()) &&
            Objects.equals(frontText[3].getString(), frontLine4.get());

        boolean back = Objects.equals(backText[0].getString(), backLine1.get()) &&
            Objects.equals(backText[1].getString(), backLine2.get()) &&
            Objects.equals(backText[2].getString(), backLine3.get()) &&
            Objects.equals(backText[3].getString(), backLine4.get());

        return SignSides.get(front, back);
    }

    public static BlockHitResult blockHitResultOf(BlockPos bp) {
        return new BlockHitResult(Vec3d.ofBottomCenter(bp), Direction.DOWN, bp, false);
    }

    private void sendEditSignPacket(BlockPos pos, boolean front) {
        String line1, line2, line3, line4;
        if (front) {
            line1 = frontLine1.get();
            line2 = frontLine2.get();
            line3 = frontLine3.get();
            line4 = frontLine4.get();
        } else {
            line1 = backLine1.get();
            line2 = backLine2.get();
            line3 = backLine3.get();
            line4 = backLine4.get();
        }
        sendPacket(new UpdateSignC2SPacket(pos, front, line1, line2, line3, line4));
    }

    private void renderPos(BlockPos pos) {
        RenderUtils.renderTickingBlock(pos.toImmutable(), sideColor.get(), lineColor.get(), shapeMode.get(), 0, 20, true, false);
    }

    private enum SignSides {
        NONE(false, false),
        FRONT(true, false),
        BACK(false, true),
        BOTH(true, true);

        public final boolean front;
        public final boolean back;

        SignSides(boolean front, boolean back) {
            this.front = front;
            this.back = back;
        }

        public static SignSides get(boolean front, boolean back) {
            if (!front && !back) return NONE;
            else if (front && !back) return FRONT;
            else if (!front) return BACK;
            else return BOTH;
        }
    }
}
