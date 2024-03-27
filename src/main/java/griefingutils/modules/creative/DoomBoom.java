package griefingutils.modules.creative;

import griefingutils.modules.BetterModule;
import griefingutils.modules.Categories;
import griefingutils.utils.CreativeUtils;
import griefingutils.utils.entity.ExplosiveEntity;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

public class DoomBoom extends BetterModule{
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<ExplosiveEntity> entity = sgGeneral.add(new EnumSetting.Builder<ExplosiveEntity>()
        .name("entity")
        .description("The entity to spawn.")
        .defaultValue(ExplosiveEntity.CREEPER)
        .build()
    );

    private final Setting<Integer> strength = sgGeneral.add(new IntSetting.Builder()
        .name("strength")
        .description("The strength of the explosion.")
        .defaultValue(10)
        .range(1, 127)
        .sliderRange(1, 50)
        .visible(() -> entity.get().hasCustomExplosionSize)
        .build()
    );

    private final Setting<Integer> rate = sgGeneral.add(new IntSetting.Builder()
        .name("rate")
        .description("How much things to spawn per tick.")
        .defaultValue(1)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("How far away to spawn things.")
        .defaultValue(100)
        .range(1, 200)
        .sliderRange(1, 200)
        .build()
    );

    public DoomBoom() {
        super(Categories.DEFAULT, "doom-boom", "Obliterates nearby terrain. (requires creative mode)");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isCreative()) {
            warning("You're not in creative mode!");
            toggle();
            return;
        }
        ItemStack lastStack = mc.player.getMainHandStack();
        for (int i = 0; i < rate.get(); i++) {
            Vec3d pos = getRandomPos();
            if (pos == null) continue;
            NbtCompound nbt = entity.get().generator.asEggNbt(pos, NbtByte.of(strength.get().byteValue()));
            CreativeUtils.giveToSelectedSlot(Items.MOOSHROOM_SPAWN_EGG, nbt, null, 1);
            BlockHitResult bhr = bhrAtEyes();
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        }
        mc.interactionManager.clickCreativeStack(lastStack, 36 + mc.player.getInventory().selectedSlot);
    }

    @Nullable
    private Vec3d getRandomPos() {
        double x = mc.player.getX() + range.get() - range.get() * 2 * mc.player.getRandom().nextFloat();
        double z = mc.player.getZ() + range.get() - range.get() * 2 * mc.player.getRandom().nextFloat();
        int sx = ChunkSectionPos.getSectionCoord(x);
        int sz = ChunkSectionPos.getSectionCoord(z);
        if (!mc.world.getChunkManager().isChunkLoaded(sx, sz)) return null;
        double y = mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, MathHelper.floor(x), MathHelper.floor(z)) - 1;
        return new Vec3d(x, y, z);
    }
}
