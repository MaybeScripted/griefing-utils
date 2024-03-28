package griefingutils.utils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import griefingutils.GriefingUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.InvalidNbtException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class CreativeUtils {
    public static void giveToEmptySlot(Item item, @Nullable String snbt, @Nullable Text customName, int count) {
        giveToEmptySlot(item, fromSnbt(snbt), customName, count);
    }

    public static void giveToEmptySlot(Item item, @Nullable NbtCompound nbt, @Nullable Text customName, int count) {
        ItemStack stack = item.getDefaultStack();
        stack.setCount(count);
        if (nbt != null)
            stack.setNbt(nbt);

        if(customName != null) stack.setCustomName(customName);
        giveToEmptySlot(stack);
    }

    public static void giveToEmptySlot(ItemStack stack) {
        if (GriefingUtils.MC.player.getMainHandStack().isEmpty())
            GriefingUtils.MC.interactionManager.clickCreativeStack(stack, 36 + GriefingUtils.MC.player.getInventory().selectedSlot);
        else {
            int nextEmptySlot = GriefingUtils.MC.player.getInventory().getEmptySlot();
            if (nextEmptySlot < 9) GriefingUtils.MC.interactionManager.clickCreativeStack(stack, 36 + nextEmptySlot);
            else
                GriefingUtils.MC.interactionManager.clickCreativeStack(stack, 36 + GriefingUtils.MC.player.getInventory().selectedSlot);
        }
    }

    public static void giveToSelectedSlot(Item item, @Nullable String snbt, @Nullable Text customName, int count) {
        giveToSelectedSlot(item, fromSnbt(snbt), customName, count);
    }

    public static void giveToSelectedSlot(Item item, @Nullable NbtCompound nbt, @Nullable Text customName, int count) {
        ItemStack stack = item.getDefaultStack();
        stack.setCount(count);
        if (nbt != null)
            stack.setNbt(nbt);

        if(customName != null) stack.setCustomName(customName);
        giveToSelectedSlot(stack);
    }

    public static void giveToSelectedSlot(ItemStack stack) {
        GriefingUtils.MC.interactionManager.clickCreativeStack(stack, 36 + GriefingUtils.MC.player.getInventory().selectedSlot);
    }

    public static NbtCompound fromSnbt(String snbt) throws InvalidNbtException {
        if (snbt == null) return null;
        try {
            return StringNbtReader.parse(snbt);
        } catch (CommandSyntaxException e) {
            throw new InvalidNbtException(e.getMessage());
        }
    }
}
