package griefingutils.modules.creative;

import griefingutils.modules.BetterModule;
import griefingutils.modules.Categories;
import griefingutils.utils.CreativeUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ExplosiveHands extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<ExplosiveEntity> entity = sgGeneral.add(new EnumSetting.Builder<ExplosiveEntity>()
        .name("entity")
        .description("entity that explodes")
        .defaultValue(ExplosiveEntity.CREEPER)
        .build()
    );

    private final Setting<Integer> strength = sgGeneral.add(new IntSetting.Builder()
        .name("strength")
        .description("of the explosion")
        .defaultValue(10)
        .range(1, 127)
        .sliderRange(1, 127)
        .visible(() -> entity.get().hasRange)
        .build()
    );

    public ExplosiveHands() {
        super(Categories.DEFAULT, "explosive-hands", "Makes your hands explosive (requires creative mode)");
    }

    // TODO code style
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isCreative()) {
            warning("You're not in creative mode!");
            toggle();
            return;
        }

        if (!mc.options.attackKey.isPressed() || mc.currentScreen != null) return;
        HitResult hitResult = mc.cameraEntity.raycast(900, 0, false);
        if (hitResult.getType() == HitResult.Type.MISS) return;
        Vec3d p = hitResult.getPos();
        String nbt = entity.get().asEggNBT(p.offset(Direction.DOWN, 1), strength.get());
        ItemStack lastStack = mc.player.getMainHandStack();

        CreativeUtils.giveItemWithNbtToSelectedSlot(Items.MOOSHROOM_SPAWN_EGG, nbt, null, 1);
        BlockHitResult bhr = new BlockHitResult(
            mc.player.getPos().add(0, 1, 0),
            Direction.UP,
            new BlockPos(mc.player.getBlockPos().add(0, 1, 0)),
            false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        mc.interactionManager.clickCreativeStack(lastStack, 36 + mc.player.getInventory().selectedSlot);
    }

    private enum ExplosiveEntity {
        FIREBALL("minecraft:fireball", true, "power:[0.0, -10000.0, 0.0]", "ExplosionPower:%db"),
        TNT("minecraft:tnt",false,"fuse:0"),
        CREEPER("minecraft:creeper", true, "ignited:1b", "Health:4206969f", "Fuse:0", "ExplosionRadius:%db");

        private final String entityId;
        private final String extra;
        public final boolean hasRange;

        ExplosiveEntity(String entityId, boolean hasRange, String... extra) {
            this.hasRange = hasRange;
            this.extra = String.join(",", extra);
            this.entityId = entityId;
        }

        public String asEggNBT(Vec3d to, int strength){
            return "{EntityTag:{id:\"%s\",Pos:[%s],%s}}"
                .formatted(
                    entityId,
                    to.toString().substring(1, to.toString().length()-1),
                    hasRange ? extra.formatted(strength) : extra
                );
        }
    }
}
