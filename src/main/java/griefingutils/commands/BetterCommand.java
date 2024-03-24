package griefingutils.commands;

import griefingutils.GriefingUtils;
import griefingutils.utils.MCUtil;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.MinecraftClient;

public abstract class BetterCommand extends Command implements MCUtil {
    public final MinecraftClient mc = GriefingUtils.MC;

    public BetterCommand(String name, String description, String... aliases) {
        super(name, description, aliases);
    }
}
