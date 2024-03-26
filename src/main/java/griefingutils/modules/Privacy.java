package griefingutils.modules;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Privacy extends BetterModule {
    public SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> hideIPs = sgGeneral.add(new BoolSetting.Builder()
        .name("Hide IPs")
        .description("Tries to hide IPv4 Addresses")
        .defaultValue(false)
        .build()
    );

    public Privacy() {
        super(Categories.DEFAULT, "privacy", "Hides sensitive information.");
    }

    public String transform(String s) {
        if (!isActive()) return s;
        if (hideIPs.get()) s = censorIPs(s);
        return s;
    }

    // Modified version of first comment from https://stackoverflow.com/q/31178400
    private final Pattern IPv4Pattern = Pattern.compile("(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(2[0-4][0-9]|25[0-5]|1[0-9]{2}|[1-9][0-9]|[0-9])");
    public String censorIPs(String s) {
        Matcher matcher = IPv4Pattern.matcher(s);
        return matcher.replaceAll("<IPv4 address>");
    }
}
