package griefingutils.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @ModifyConstant(method = "getWindowTitle", constant = @Constant(stringValue = "Minecraft"))
    String modifyMinecraftConst(String constant) {
        return "0x06's Griefing Utils | Minecraft";
    }
}
