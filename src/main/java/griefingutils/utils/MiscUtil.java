package griefingutils.utils;

import meteordevelopment.meteorclient.utils.render.color.Color;

public class MiscUtil {
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
}
