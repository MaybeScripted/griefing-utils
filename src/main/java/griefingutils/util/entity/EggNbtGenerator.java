package griefingutils.util.entity;

import net.minecraft.nbt.*;
import net.minecraft.util.math.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum EggNbtGenerator {
    FIREBALL("fireball", nbt -> {
        NbtList power = new NbtList();
        power.add(NbtDouble.of(0.0));
        power.add(NbtDouble.of(-10000.0));
        power.add(NbtDouble.of(0.0));
        nbt.put("power", power);
    }, (nbt, args) -> {
        NbtByte explosionPower = (NbtByte) args[0];
        nbt.put("ExplosionPower", explosionPower);
    }),

    TNT("tnt", nbt -> nbt.putInt("fuse", 0), (nbt, args) -> {}),

    CREEPER("creeper", nbt -> {
        nbt.putBoolean("ignited", true);
        nbt.putBoolean("Invulnerable", true);
        nbt.putInt("Fuse", 0);
    }, (nbt, args) -> {
        NbtByte explosionRadius = (NbtByte) args[0];
        nbt.put("ExplosionRadius", explosionRadius);
    }),

    WITHER("wither", nbt -> nbt.putBoolean("Invulnerable", true), (nbt, args) -> {
        NbtString customName = (NbtString) args[0];
        nbt.put("CustomName", customName);
    }),

    ARMOR_STAND("armor_stand", nbt -> {
        nbt.putString("id", "minecraft:armor_stand");
        nbt.putBoolean("CustomNameVisible", true);
        nbt.putBoolean("Marker", true);
        nbt.putBoolean("Invisible", true);
    }, (nbt, args) -> {
        NbtString customName = (NbtString) args[0];
        nbt.put("CustomName", customName);
    });

    private final NbtCompound baseNbt;
    private final BiConsumer<NbtCompound, NbtElement[]> nbtTransformer;

    EggNbtGenerator(
        String id,
        Consumer<NbtCompound> baseNbtCreator,
        BiConsumer<NbtCompound, NbtElement[]> nbtTransformer
    ) {
        NbtCompound baseNbt = new NbtCompound();
        NbtCompound entityTag = new NbtCompound();
        baseNbt.put("EntityTag", entityTag);
        entityTag.putString("id", id);
        baseNbtCreator.accept(entityTag);
        this.baseNbt = baseNbt;
        this.nbtTransformer = nbtTransformer;
    }

    public NbtCompound asEggNbt(Vec3d pos, NbtElement... args) {
        NbtCompound eggNbt = baseNbt.copy();
        NbtCompound entityTag = eggNbt.getCompound("EntityTag");
        nbtTransformer.accept(entityTag, args);
        NbtList doubleList = new NbtList();
        doubleList.add(NbtDouble.of(pos.x));
        doubleList.add(NbtDouble.of(pos.y));
        doubleList.add(NbtDouble.of(pos.z));
        entityTag.put("Pos", doubleList);
        return eggNbt;
    }
}
