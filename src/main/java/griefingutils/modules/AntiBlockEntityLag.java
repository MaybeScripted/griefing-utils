package griefingutils.modules;

import griefingutils.utils.ListMode;
import meteordevelopment.meteorclient.events.render.RenderBlockEntityEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;

import java.util.List;


public class AntiBlockEntityLag extends BetterModule {
    public SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("render-radius")
        .description("The radius in which the blocks will render.")
        .defaultValue(16)
        .range(0, 64)
        .sliderRange(0, 64)
        .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("filter-blocks")
        .description("The blocks to filter.")
        .defaultValue(
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.LIGHT_GRAY_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX
        )
        .filter(AntiBlockEntityLag::isBlockEntity)
        .build()
    );

    private final Setting<ListMode> throwFilterType = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("filter-type")
        .description("The type of the filter.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );

    public AntiBlockEntityLag() {
        super(Categories.DEFAULT, "anti-block-entity-lag", "Lets you change the render distance of specified block entities.");
    }

    @EventHandler
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        BlockEntity be = event.blockEntity;

        if (PlayerUtils.squaredDistanceTo(be.getPos()) < radius.get() * radius.get()) return;

        if (blocks.get().contains(be.getCachedState().getBlock())) {
            event.cancel();
        }
    }

    private static boolean isBlockEntity(Block block) {
        return block instanceof BlockWithEntity;
    }
}
