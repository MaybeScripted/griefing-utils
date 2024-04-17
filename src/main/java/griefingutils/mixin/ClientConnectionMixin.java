package griefingutils.mixin;

import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ClientConnection.class, priority = Integer.MIN_VALUE)
public class ClientConnectionMixin {
    /*@Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof BundleS2CPacket p) {
            AntiCrash ac = Modules.get().get(AntiCrash.class);
            if (ac == null) throw new IllegalStateException("Could not get module AntiCrash, this should never happen!");
            if (!ac.isActive()) return;
            if (AntiCrash.isInvalid(p)) {
                ac.alert("invalid bundle");
                ci.cancel();
            }
        }
    }*/
}
