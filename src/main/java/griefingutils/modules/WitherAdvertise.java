package griefingutils.modules;

import griefingutils.utils.CreativeUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;

public class WitherAdvertise extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("name")
        .description("of the withers")
        .defaultValue("Griefed by 0x06")
        .build()
    );

    private final Setting<String> color = sgGeneral.add(new StringSetting.Builder()
        .name("color")
        .description("of the name")
        .defaultValue("#FF0000")
        .build()
    );

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("of withers")
        .defaultValue(1)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("the range")
        .defaultValue(100)
        .range(1, 200)
        .sliderRange(1, 200)
        .build()
    );

    public WitherAdvertise() {
        super(Categories.DEFAULT, "wither-advertise", "spawns withers nearby with a name");
    }

    // TODO code style
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isCreative()) {
            warning("You don't have creative");
            toggle();
            return;
        }
        ItemStack lastStack = mc.player.getMainHandStack();
        for (int i = 0; i < amount.get(); i++) {
            double x = mc.player.getX() + range.get() - range.get() * 2 * mc.player.getRandom().nextFloat();
            double z = mc.player.getZ() + range.get() - range.get() * 2 * mc.player.getRandom().nextFloat();

            if (!mc.world.getChunkManager().isChunkLoaded(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z))) return;

            double y = mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING, MathHelper.floor(x), MathHelper.floor(z)) + 20;

            String nbt = "{EntityTag:{id:\"minecraft:wither\",Pos:[" + x + "," + y + "," + z + "],Health:4206969f,CustomName:'{\"text\": \"" + name.get() + "\", \"color\": \"" + color.get() + "\"}'}}";
            CreativeUtils.giveItemWithNbtToSelectedSlot(Items.WITHER_SPAWN_EGG, nbt, null, 1);
            BlockHitResult bhr = bhrAbovePlayer();
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        }
        mc.interactionManager.clickCreativeStack(lastStack, 36 + mc.player.getInventory().selectedSlot);
        toggle();
    }
}
