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
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

public class ContainerPuke extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> throwAtFeet = sgGeneral.add(new BoolSetting.Builder()
        .name("throw-at-feet")
        .description("Throws the items from the container at your feet")
        .defaultValue(true)
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

    public Deque<BlockPos> pukedChests = new ArrayDeque<>();
    public int count = 0;

    public ContainerPuke() {
        super(Categories.DEFAULT, "container-puke", "Pukes the content of nearby containers");
    }

    @Override
    public void onDeactivate() {
        pukedChests.clear();
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }

    private boolean isPuking = false;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ClientPlayerEntity p = mc.player;
        if (p.isSneaking() || p.currentScreenHandler != p.playerScreenHandler || isPuking || isSpectator()) return;
        for(BlockEntity be : Utils.blockEntities()) {
            Block block = mc.world.getBlockState(be.getPos()).getBlock();
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

            if (!PlayerUtils.isWithinReach(be.getPos())) continue;

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
            isPuking = true;

            if (block instanceof ChestBlock cb) {
                cb.getBlockEntitySource(
                    mc.world.getBlockState(be.getPos()),
                    mc.world,
                    be.getPos(),
                    false
                ).apply(new ChestHandler());
            } else {
                pukedChests.add(be.getPos());
                count++;
                renderPos(be.getPos());
            }

            break;
        }
    }

    @EventHandler
    private void onInventory(InventoryEvent event) {
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (throwAtFeet.get())
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), 90, mc.player.isOnGround()));

        int size = SlotUtils.indexToId(SlotUtils.MAIN_START);

        for (int i = 0; i < size; i++) {
            if (!handler.getSlot(i).hasStack()) continue;
            mc.interactionManager.clickSlot(handler.syncId, i, 1, SlotActionType.THROW, mc.player);
        }

        mc.player.closeHandledScreen();
        isPuking = false;
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
}
