package griefingutils.mixin;

import griefingutils.GriefingUtils;
import griefingutils.modules.BetterPauseScreen;
import griefingutils.screen.GameMenuExtrasScreen;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin() {
        super(null);
    }

    @Shadow protected abstract ButtonWidget createButton(Text text, Supplier<Screen> screenSupplier);

    @Shadow public abstract void tick();

    @Unique private static final Text MORE_TEXT = Text.translatable("createWorld.tab.more.title");

    @Redirect(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 2))
    private Widget replaceSendFeedbackBtn(GridWidget.Adder instance, Widget sendFeedbackBtn) {
        if (Modules.get().isActive(BetterPauseScreen.class)) {
            ButtonWidget buttonWidget = createButton(MORE_TEXT, () -> new GameMenuExtrasScreen((GameMenuScreen) (Object) this));
            if (GriefingUtils.MC.isInSingleplayer()) {
                buttonWidget.active = false;
                buttonWidget.setTooltip(Tooltip.of(Text.of("Not available in singleplayer!")));
            }
            return instance.add(buttonWidget);
        } else return instance.add(sendFeedbackBtn);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.isCopy(keyCode) && Modules.get().isActive(BetterPauseScreen.class) && GameMenuExtrasScreen.copyServerIP()) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
