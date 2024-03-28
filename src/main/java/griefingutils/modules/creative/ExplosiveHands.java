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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ExplosiveHands extends BetterModule {
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

    public ExplosiveHands() {
        super(Categories.DEFAULT, "explosive-hands", "Spawns explosions at the block you're looking at. (requires creative mode)");
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (!isCreative()) {
            warning("You're not in creative mode!");
            toggle();
            return;
        }

        if (!mc.options.attackKey.isPressed() || mc.currentScreen != null) return;
        ItemStack lastStack = mc.player.getMainHandStack();

        HitResult hitResult = mc.cameraEntity.raycast(900, 0, false);
        if (hitResult.getType() == HitResult.Type.MISS) return;
        Vec3d pos = hitResult.getPos().offset(Direction.DOWN, 1);
        NbtCompound nbt = entity.get().generator.asEggNbt(pos, NbtByte.of(strength.get().byteValue()));

        CreativeUtils.giveToSelectedSlot(Items.MOOSHROOM_SPAWN_EGG, nbt, null, 1);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhrAtEyes());

        CreativeUtils.giveToSelectedSlot(lastStack);

    }
}
