package griefingutils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import griefingutils.utils.CreativeUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ClipboardGive extends BetterCommand {
    public ClipboardGive() {
        super("clipboard-give", "Gives an item from a copied give command.", "clip-give", "cgive", "give-clip");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(this::run);
    }

    private int run(CommandContext<CommandSource> ctx) {
        try {
            String clipboard = mc.keyboard.getClipboard();
            if (!clipboard.startsWith("/give")) {
                ChatUtils.warning("Clipboard content is not a give command");
                return SUCCESS;
            }
            String command = clipboard.substring(clipboard.indexOf(" ", 6) + 1);
            Item item = getItem(command);
            String nbt = getNbtString(command);
            ChatUtils.info(String.valueOf(nbt));
            int count = getCount(command);

            CreativeUtils.giveToEmptySlot(item, nbt, null, count);

            return SUCCESS;
        } catch (Exception e) {
            ChatUtils.warning("Give command is malformed");
            return SUCCESS;
        }
    }

    private static Item getItem(String command) {
        boolean hasNbt = command.contains("{");
        String identifierString;
        if (hasNbt) identifierString = command.substring(0, command.indexOf("{"));
        else identifierString = command.substring(0, command.indexOf(" "));
        return Registries.ITEM.get(new Identifier(identifierString));
    }

    private static String getNbtString(String command) {
        boolean hasNbt = command.contains("{");
        if (!hasNbt) return null;
        return command.substring(command.indexOf("{"), command.lastIndexOf("}") + 1);
    }

    private static int getCount(String command) {
        if (command.substring(command.lastIndexOf("}")).isBlank()) return 1;
        return MathHelper.clamp(Integer.parseInt(command.substring(command.lastIndexOf("} ") + 2)), 1, 64);
    }
}
