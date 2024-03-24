package griefingutils.modules;

import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChestPuke extends BetterModule {
    private final SettingGroup sgRender = settings.createGroup("Render");

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

    public Deque<BlockPos> pukedChests = new ArrayDeque<>();

    public ChestPuke() {
        super(Categories.DEFAULT, "chest-puke", "Pukes the content of nearby chests");
    }

    @Override
    public void onDeactivate() {
        pukedChests.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for(BlockEntity be : Utils.blockEntities()) {
            if (!(be instanceof ChestBlockEntity cbe)) continue;
            if (pukedChests.contains(be.getPos())) continue;

            if (!PlayerUtils.isWithinReach(cbe.getPos())) continue;

            mc.interactionManager.interactBlock(
                mc.player,
                Hand.MAIN_HAND,
                new BlockHitResult(
                    be.getPos().toCenterPos().offset(Direction.DOWN, 0.5),
                    Direction.DOWN,
                    be.getPos(),
                    false
                )
            );

            if (mc.world.getBlockState(be.getPos()).getBlock() instanceof ChestBlock cb) {
                cb.getBlockEntitySource(
                    mc.world.getBlockState(be.getPos()),
                    mc.world,
                    be.getPos(),
                    false
                ).apply(new ChestHandler());
            }

            break;
        }
    }

    @EventHandler
    private void onInventory(InventoryEvent event) {
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler)) return;
        sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), 90, mc.player.isOnGround()));

        int size = handler.getRows() * 9;
        for (int i = 0; i < size; i++) {
            // https://wiki.vg/Protocol#Click_Container
            // SlotActionType.THROW = Mode 4
            // button 1 = Control + Drop key (Q)
            if (handler.getInventory().getStack(i).isEmpty()) continue;
            mc.interactionManager.clickSlot(handler.syncId, i, 1, SlotActionType.THROW, mc.player);
        }
        mc.player.closeHandledScreen();
    }

    private void renderPos(BlockPos pos) {
        if (!render.get()) return;
        RenderUtils.renderTickingBlock(
            pos.toImmutable(),
            sideColor.get(),
            lineColor.get(),
            shapeMode.get(),
            0,
            10,
            true,
            false
        );
    }

    private class ChestHandler implements DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Void> {
        @Override
        public Void getFromBoth(ChestBlockEntity first, ChestBlockEntity second) {
            pukedChests.add(first.getPos());
            renderPos(first.getPos());
            pukedChests.add(second.getPos());
            renderPos(second.getPos());
            return null;
        }

        @Override
        public Void getFrom(ChestBlockEntity single) {
            pukedChests.add(single.getPos());
            renderPos(single.getPos());
            return null;
        }

        @Override
        public Void getFallback() {
            return null;
        }
    }
}
