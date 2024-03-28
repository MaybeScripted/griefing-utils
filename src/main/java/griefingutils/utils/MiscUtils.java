package griefingutils.utils;

import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import org.jetbrains.annotations.Nullable;

public class MiscUtils {
    public static String hexifyColor(int i) {
        return hexifyColor(new Color(new java.awt.Color(i)));
    }

    public static String hexifyColor(Color color) {
        String r = Integer.toHexString(color.r);
        r = "0".repeat(2 - r.length()) + r;
        String g = Integer.toHexString(color.g);
        g = "0".repeat(2 - g.length()) + g;
        String b = Integer.toHexString(color.b);
        b = "0".repeat(2 - b.length()) + b;
        return "#" + r + g + b;
    }

    @Nullable
    public static Script compileSilently(String starScript) {
        Parser.Result result = Parser.parse(starScript);

        if (result.hasErrors()) return null;

        return Compiler.compile(result);
    }
}
