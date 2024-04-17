package griefingutils.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.Toast;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MCUtil {
    MinecraftClient MC = MinecraftClient.getInstance();
    
    default ClientPlayNetworkHandler networkHandler() {
        return MC.getNetworkHandler();
    }

    default void sendPacket(Packet<?> packet) {
        networkHandler().sendPacket(packet);
    }

    default CustomPayloadC2SPacket createCustomPayloadPacket(Consumer<PacketByteBuf> writer, Identifier id) {
        return new CustomPayloadC2SPacket(new CustomPayload() {
            @Override
            public void write(PacketByteBuf buf) {
                writer.accept(buf);
            }

            @Override
            public Identifier id() {
                return id;
            }
        });
    }

    default boolean notCreative() {
        return !MC.player.isCreative();
    }

    default boolean isSpectator() {
        return MC.player.isSpectator();
    }

    default boolean notOp() {
        return !MC.player.hasPermissionLevel(4);
    }

    default boolean playerCollides(Vec3d pos, boolean blocks, boolean entities) {
        return playerCollides(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z, blocks, entities);
    }

    default boolean playerCollides(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, boolean blocks, boolean entities) {
        ClientPlayerEntity player = MC.player;
        EntityDimensions dimensions = player.getDimensions(player.getPose());
        Box box = dimensions.getBoxAt(fromX, fromY, fromZ).union(dimensions.getBoxAt(toX, toY, toZ));
        if (blocks && entities)
            return player.getWorld().getCollisions(null, box).iterator().hasNext();
        else if (blocks)
            return player.getWorld().getBlockCollisions(null, box).iterator().hasNext();
        else if (entities)
            return !player.getWorld().getEntityCollisions(null, box).isEmpty();
        else
            return false;
    }

    default void sendCommand(String command) {
        networkHandler().sendCommand(command);
    }

    default BlockHitResult bhrAtEyes() {
        Vec3d eyePos = MC.player.getPos();
        BlockPos blockPos = new BlockPos(
            MathHelper.floor(eyePos.x),
            MathHelper.floor(eyePos.y),
            MathHelper.floor(eyePos.z)
        );
        return new BlockHitResult(
            eyePos,
            Direction.UP,
            blockPos,
            false
        );
    }

    default void addToastWithLimit(Supplier<Toast> getter) {
        if (MC.getToastManager().toastQueue.size() >= 5) return;
        MC.getToastManager().add(getter.get());
    }
}
