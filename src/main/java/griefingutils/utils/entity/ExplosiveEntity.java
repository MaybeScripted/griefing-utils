package griefingutils.utils.entity;

public enum ExplosiveEntity {
    FIREBALL(EggNbtGenerator.FIREBALL, true),
    TNT(EggNbtGenerator.TNT, false),
    CREEPER(EggNbtGenerator.CREEPER, true);

    public final EggNbtGenerator generator;
    public final boolean hasCustomExplosionSize;

    ExplosiveEntity(EggNbtGenerator generator, boolean hasCustomExplosionSize) {
        this.generator = generator;
        this.hasCustomExplosionSize = hasCustomExplosionSize;
    }
}
