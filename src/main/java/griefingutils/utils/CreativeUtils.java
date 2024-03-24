package griefingutils.utils;

import griefingutils.GriefingUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class CreativeUtils {
    public static void giveItemWithNbtToEmptySlot(Item item, @Nullable String nbt, @Nullable Text customName, int count) {
        ItemStack stack = item.getDefaultStack();
        stack.setCount(count);
        if (nbt != null) {
            try {
                stack.setNbt(StringNbtReader.parse(nbt));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        if(customName != null) stack.setCustomName(customName);
        if (GriefingUtils.MC.player.getMainHandStack().isEmpty())
            GriefingUtils.MC.interactionManager.clickCreativeStack(stack, 36 + GriefingUtils.MC.player.getInventory().selectedSlot);
        else {
            int nextEmptySlot = GriefingUtils.MC.player.getInventory().getEmptySlot();
            if (nextEmptySlot < 9) GriefingUtils.MC.interactionManager.clickCreativeStack(stack, 36 + nextEmptySlot);
            else
                GriefingUtils.MC.interactionManager.clickCreativeStack(stack, 36 + GriefingUtils.MC.player.getInventory().selectedSlot);
        }
    }

    public static void giveItemWithNbtToSelectedSlot(Item item, @Nullable String nbt, @Nullable Text customName, int count) {
        ItemStack stack = item.getDefaultStack();
        stack.setCount(count);
        if (nbt != null) {
            try {
                stack.setNbt(StringNbtReader.parse(nbt));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        if(customName != null) stack.setCustomName(customName);
        GriefingUtils.MC.interactionManager.clickCreativeStack(stack, 36 + GriefingUtils.MC.player.getInventory().selectedSlot);
    }
}
