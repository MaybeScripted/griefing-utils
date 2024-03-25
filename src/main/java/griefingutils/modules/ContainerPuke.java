package griefingutils.modules;

import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

public class ContainerPuke extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<ThrowDirection> throwDirection = sgGeneral.add(new EnumSetting.Builder<ThrowDirection>()
        .name("throw-direction")
        .description("Lets you change the direction you puke the items at.")
        .defaultValue(ThrowDirection.DOWNWARDS)
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Whether to render things.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderUnopenedContainers = sgRender.add(new BoolSetting.Builder()
        .name("render-unopened-containers")
        .description("Wheter to render unopened containers.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> renderRange = sgRender.add(new IntSetting.Builder()
        .name("render-range")
        .description("Render range for unopened containers.")
        .range(8, 128)
        .sliderRange(8, 128)
        .defaultValue(32)
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

    public Deque<BlockPos> pukedChests = new ArrayDeque<>();
    private boolean isPuking = false;
    public int count = 0;


    public ContainerPuke() {
        super(Categories.DEFAULT, "container-puke", "Pukes the content of nearby containers");
    }

    @Override
    public void onDeactivate() {
        pukedChests.clear();
        isPuking = false;
        count = 0;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ClientPlayerEntity p = mc.player;
        if (p.isSneaking() || p.currentScreenHandler != p.playerScreenHandler || isPuking || isSpectator()) return;
        for(BlockEntity be : Utils.blockEntities()) {
            BlockState blockState = mc.world.getBlockState(be.getPos());
            Block block = blockState.getBlock();
            if (!(block instanceof ChestBlock ||
                block instanceof ShulkerBoxBlock ||
                block instanceof BarrelBlock ||
                block instanceof HopperBlock ||
                block instanceof DispenserBlock ||
                block instanceof FurnaceBlock ||
                block instanceof BlastFurnaceBlock ||
                block instanceof SmokerBlock ||
                block instanceof BrewingStandBlock
            )) continue;

            if (pukedChests.contains(be.getPos())) continue;

            if (renderUnopenedContainers.get()) {
                int rangeSquared = renderRange.get() * renderRange.get();
                if (mc.player.getEyePos().squaredDistanceTo(be.getPos().toCenterPos()) < rangeSquared)
                    renderPos(be.getPos(), 1, false, false);
            }

            if (isPuking) continue;

            if (!PlayerUtils.isWithinReach(be.getPos())) continue;

            BlockHitResult bhr = new BlockHitResult(be.getPos().toCenterPos().offset(Direction.DOWN, 0.5), Direction.DOWN, be.getPos(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
            isPuking = true;

            if (block instanceof ChestBlock cb) {
                cb.getBlockEntitySource(blockState, mc.world, be.getPos(), true).apply(new ChestHandler());
            } else {
                pukedChests.add(be.getPos());
                count++;
                renderPos(be.getPos());
            }
        }
    }

    @EventHandler
    private void onInventory(InventoryEvent event) {
        Vec2f rot = throwDirection.get().getThrowAngle(mc.player);
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rot.x, rot.y, mc.player.isOnGround()));
        afterRotate();
    }

    private void afterRotate() {
        ScreenHandler handler = mc.player.currentScreenHandler;
        int size = SlotUtils.indexToId(SlotUtils.MAIN_START);

        for (int i = 0; i < size; i++) {
            if (!handler.getSlot(i).hasStack()) continue;
            mc.interactionManager.clickSlot(handler.syncId, i, 1, SlotActionType.THROW, mc.player);
        }

        mc.player.closeHandledScreen();
        isPuking = false;
    }

    private class ChestHandler implements DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Void> {
        @Override
        public Void getFromBoth(ChestBlockEntity first, ChestBlockEntity second) {
            pukedChests.add(first.getPos());
            pukedChests.add(second.getPos());
            count += 2;
            renderPos(first.getPos());
            renderPos(second.getPos());
            return null;
        }
        @Override
        public Void getFrom(ChestBlockEntity single) {
            pukedChests.add(single.getPos());
            count++;
            renderPos(single.getPos());
            return null;
        }

        @Override
        public Void getFallback() {
            return null;
        }

    }

    private void renderPos(BlockPos pos) {
        renderPos(pos, 10, true, false);
    }

    private void renderPos(BlockPos pos, int duration, boolean fade, boolean shrink) {
        if (!render.get()) return;
        RenderUtils.renderTickingBlock(
            pos.toImmutable(), sideColor.get(), lineColor.get(), shapeMode.get(), 0, duration, fade, shrink
        );
    }

    private enum ThrowDirection {
        FORWARDS(Vec2f::new),
        DOWNWARDS((yaw, pitch) -> new Vec2f(yaw, 90)),
        UPWARDS((yaw, pitch) -> new Vec2f(yaw, -90)),
        BACKWARDS((yaw, pitch) -> new Vec2f(MathHelper.wrapDegrees(yaw + 180), -30));

        private final BiFunction<Float, Float, Vec2f> rotationGetter;

        ThrowDirection(BiFunction<Float, Float, Vec2f> rotationGetter) {
            this.rotationGetter = rotationGetter;
        }

        Vec2f getThrowAngle(ClientPlayerEntity player) {
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            return rotationGetter.apply(yaw, pitch);
        }
    }
}
