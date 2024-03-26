package griefingutils.screen;

import griefingutils.GriefingUtils;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.net.InetSocketAddress;

public class AccountsConfirmReconnectScreen extends ConfirmScreen {
    private static final Text CONFIRM_RECONNECT_TEXT = Text.of("Confirm Reconnect");
    private static final Text DESCRIPTION_TEXT = Text.of("Are you sure you want to reconnect with your new account?");
    private boolean openedAccountsScreen = false;

    public AccountsConfirmReconnectScreen(GameMenuExtrasScreen parent) {
        super(confirmed -> {
            if (confirmed) AccountsConfirmReconnectScreen.reconnect(parent);
            else GriefingUtils.MC.setScreen(parent);
        }, CONFIRM_RECONNECT_TEXT, DESCRIPTION_TEXT);
    }

    @Override
    protected void init() {
        super.init();
        if (!openedAccountsScreen) {
            client.setScreen(GuiThemes.get().accountsScreen());
            openedAccountsScreen = true;
        }
    }

    private static void reconnect(GameMenuExtrasScreen parent) {
        MinecraftClient client = GriefingUtils.MC;
        InetSocketAddress inetAddress = (InetSocketAddress) client.getNetworkHandler().getConnection().getAddress();
        ServerAddress address = new ServerAddress(inetAddress.getHostName(), inetAddress.getPort());

        ServerInfo server = client.getCurrentServerEntry();

        parent.parent.disconnect();
        ConnectScreen.connect(client.currentScreen, client, address, server, false);
    }
}
