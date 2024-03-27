package griefingutils.modules.op;

import griefingutils.modules.BetterModule;
import griefingutils.modules.Categories;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

public class AutoScoreboard extends BetterModule {
    private final SettingGroup sgTitle = settings.createGroup("Title Options");
    private final SettingGroup sgContent = settings.createGroup("Content Options");

    private final Setting<String> title = sgTitle.add(new StringSetting.Builder()
        .name("title")
        .description("Title of the scoreboard to create. Supports Starscript.")
        .defaultValue("0x06's Griefing Utils")
        .wide()
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<String> titleColor = sgTitle.add(new StringSetting.Builder()
        .name("title-color")
        .description("Color of the title")
        .defaultValue("dark_red")
        .wide()
        .build()
    );

    private final Setting<List<String>> content = sgContent.add(new StringListSetting.Builder()
        .name("content")
        .description("Content of the scoreboard. Supports Starscript.")
        .defaultValue(List.of(
            "0x06 was here!",
            "{date}"
        ))
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<String> contentColor = sgContent.add(new StringSetting.Builder()
        .name("content-color")
        .description("Color of the content")
        .defaultValue("red")
        .build()
    );

    public AutoScoreboard() {
        super(Categories.DEFAULT, "auto-scoreboard", "Creates a scoreboard with some content. (requires OP)");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if(!hasOp()) {
            warning("You don't have OP");
            toggle();
            return;
        }

        String scoreboardName = RandomStringUtils.randomAlphabetic(10).toLowerCase();
        String parsedTitle = MeteorStarscript.run(MeteorStarscript.compile(title.get()));

        String command = "scoreboard objectives add %s dummy {\"text\":\"%s\",\"color\":\"%s\"}"
            .formatted(scoreboardName, parsedTitle, titleColor.get());

        if (command.length() <= 256) sendCommand(command);
        else {
            error("Title is too long. Shorten it by %d characters.", command.length() - 256);
            toggle();
            return;
        }

        sendCommand("scoreboard objectives setdisplay sidebar " + scoreboardName);

        int i = content.get().size();
        for (String content : content.get()) {
            String parsedContent = MeteorStarscript.run(MeteorStarscript.compile(content));
            String randomName = RandomStringUtils.randomAlphabetic(10).toLowerCase();
            sendCommand("team add " + randomName);
            String command2 = "team modify %s suffix {\"text\":\" %s\"}".formatted(randomName, parsedContent);
            if (command2.length() <= 256) sendCommand(command2);
            else {
                error(
                    "This content line is too long (%s). Shorten it by %d characters.",
                    parsedContent, command2.length() - 256
                );
                toggle();
                return;
            }
            sendCommand("team modify %s color %s".formatted(randomName, contentColor));
            sendCommand("team join %s %d".formatted(randomName, i));
            sendCommand("scoreboard players set %d %s %d".formatted(i, scoreboardName, i));
            i--;
        }
        info("Created scoreboard.");
        toggle();
    }
}