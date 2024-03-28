package griefingutils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import griefingutils.GriefingUtils;
import griefingutils.utils.CreativeUtils;
import griefingutils.utils.MiscUtils;
import griefingutils.utils.entity.EggNbtGenerator;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class Hologram extends BetterCommand {
    private String lastImagePath = null;

    public Hologram() {
        super("hologram", "Loads an image into the world. (requires creative mode)", "holo");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes((ctx) -> execute(false))
            .then(
                literal("last")
                    .executes((ctx) -> execute(true))
            );
    }

    // TODO maybe rewrite?
    private int execute(boolean last) {
        if (!isCreative()) {
            warning("You're not in creative mode!");
            return SUCCESS;
        }
        if (last && lastImagePath == null) {
            warning("There is no last image!");
            return SUCCESS;
        }
        PointerBuffer filter;
        if(!last) filter = BufferUtils.createPointerBuffer(1)
            .put(MemoryUtil.memASCII("*jpg;*jpeg;*.png;*.bmp;*.gif")).rewind();
        else filter = null;
        CompletableFuture.runAsync(() -> {
            String imagePath;
            if (!last) {
                imagePath = TinyFileDialogs.tinyfd_openFileDialog(
                    "Select Image", null, filter, null, false);
                if (imagePath == null) {
                    info("Canceled");
                    return;
                }
                lastImagePath = imagePath;
            } else imagePath = lastImagePath;

            try {
                info("Selected: " + imagePath);
                File file = new File(imagePath);
                BufferedImage image = ImageIO.read(file);
                info("Original Resolution: %sx%s", image.getWidth(), image.getHeight());
                if (image.getWidth() * image.getHeight() > 128*128) {
                    warning("Image is too big, scaling down to 128x128");
                    image = scaleImage(image, BufferedImage.TYPE_INT_RGB, 128, 128);
                }

                int width = image.getWidth();
                int height = image.getHeight();

                ItemStack lastStack = mc.player.getMainHandStack();
                for (int y = 0; y < height; y++) {
                    BlockHitResult bhr = bhrAtEyes();

                    StringBuilder JSON = new StringBuilder("[");

                    int lastColor = image.getRGB(0, y);
                    StringBuilder tmp = new StringBuilder();
                    for (int x = 0; x < width; x++) {
                        int color = image.getRGB(x, y);
                        if (color == lastColor && (x != width - 1)) {
                            tmp.append("█");
                            continue;
                        }
                        appendToBuilder(JSON, lastColor, tmp, true);
                        tmp = new StringBuilder("█");
                        lastColor = color;
                    }
                    appendToBuilder(JSON, lastColor, tmp, false);

                    Vec3d pos = mc.player.getPos().offset(Direction.UP, (height - y) * 0.23);
                    NbtCompound nbt = EggNbtGenerator.ARMOR_STAND.asEggNbt(pos, NbtString.of(JSON + "]"));
                    ItemStack hologram = new ItemStack(Items.COD_SPAWN_EGG);
                    hologram.setNbt(nbt);

                    CreativeUtils.giveToSelectedSlot(hologram);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                }
                CreativeUtils.giveToSelectedSlot(lastStack);
                info("Loaded Image");

            } catch (Exception e) {
                info("exception: %s", e);
                GriefingUtils.LOG.error("", e);
            }
        });
        return SUCCESS;
    }

    public static BufferedImage scaleImage(BufferedImage image, int imageType, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, imageType);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return resized;
    }

    public static void appendToBuilder(StringBuilder JSON, int lastColor, StringBuilder tmp, boolean appendComma) {
        String colorString = MiscUtils.hexifyColor(lastColor);
        JSON.append("{\"text\":\"")
            .append(tmp)
            .append("\", \"color\": \"")
            .append(colorString)
            .append("\"}");
        if(appendComma) JSON.append(", ");
    }
}
