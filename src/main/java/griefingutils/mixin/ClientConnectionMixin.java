package griefingutils.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientConnection.class, priority = Integer.MIN_VALUE)
public class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        // Somehow ac is null in prod but never in dev
        /*if (packet instanceof BundleS2CPacket p) {
            AntiCrash ac = Modules.get().get(AntiCrash.class);
            if (!ac.isActive()) return;
            if (AntiCrash.isInvalid(p)) {
                ac.alert("invalid bundle");
                ci.cancel();
            }
        }*/
    }
}
