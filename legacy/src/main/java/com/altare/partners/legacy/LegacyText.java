package com.altare.partners.legacy;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts the MiniMessage-style texts used by the modern build into legacy
 * section-code strings that Minecraft 1.8.8 understands.
 *
 * <p>1.8.8 has no RGB colour support, so hex colours are matched to the nearest
 * of the sixteen legacy colours.</p>
 */
public final class LegacyText {

    private static final Pattern SERVER_NAME = Pattern.compile("(?i)<server[ _-]?name>|%server[ _-]?name%");
    private static final Pattern MINI_HEX = Pattern.compile("(?i)<#([0-9a-f]{6})>");
    private static final Pattern AMP_HEX = Pattern.compile("(?i)&#([0-9a-f]{6})");
    private static final Pattern CLOSING_TAG = Pattern.compile("</[^>]+>");
    private static final Pattern LEGACY_CODE = Pattern.compile("(?i)[&\\u00a7]([0-9a-fk-or])");

    private static final String[] TAGS = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
            "yellow", "white", "obfuscated", "bold", "strikethrough", "underlined", "italic", "reset"
    };
    private static final char[] CODES = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'k', 'l', 'm', 'n', 'o', 'r'
    };

    /** RGB values Minecraft 1.8 renders for each of the sixteen chat colours. */
    private static final int[][] PALETTE = {
            {0x00, 0x00, 0x00}, {0x00, 0x00, 0xAA}, {0x00, 0xAA, 0x00}, {0x00, 0xAA, 0xAA},
            {0xAA, 0x00, 0x00}, {0xAA, 0x00, 0xAA}, {0xFF, 0xAA, 0x00}, {0xAA, 0xAA, 0xAA},
            {0x55, 0x55, 0x55}, {0x55, 0x55, 0xFF}, {0x55, 0xFF, 0x55}, {0x55, 0xFF, 0xFF},
            {0xFF, 0x55, 0x55}, {0xFF, 0x55, 0xFF}, {0xFF, 0xFF, 0x55}, {0xFF, 0xFF, 0xFF}
    };

    private static final String SECTION = "\u00a7";

    private LegacyText() {
    }

    /** Converts one configured text into a legacy-coloured string. */
    public static String parse(JavaPlugin plugin, String text) {
        if (text == null || text.isEmpty()) return "";

        String serverName = plugin.getConfig().getString("server-name", "Your Server");
        String out = SERVER_NAME.matcher(text).replaceAll(Matcher.quoteReplacement(serverName));

        out = replaceHex(MINI_HEX, out);
        for (int i = 0; i < TAGS.length; i++) {
            out = out.replace("<" + TAGS[i] + ">", SECTION + CODES[i]);
        }
        out = CLOSING_TAG.matcher(out).replaceAll(Matcher.quoteReplacement(SECTION + "r"));
        out = replaceHex(AMP_HEX, out);

        Matcher legacy = LEGACY_CODE.matcher(out);
        StringBuffer buffer = new StringBuffer();
        while (legacy.find()) {
            legacy.appendReplacement(buffer, Matcher.quoteReplacement(SECTION + legacy.group(1).toLowerCase(Locale.ROOT)));
        }
        legacy.appendTail(buffer);
        return buffer.toString();
    }

    private static String replaceHex(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(
                    SECTION + nearestColor(matcher.group(1))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /** Returns the legacy colour code closest to the given hex colour. */
    private static char nearestColor(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < PALETTE.length; i++) {
            int dr = r - PALETTE[i][0];
            int dg = g - PALETTE[i][1];
            int db = b - PALETTE[i][2];
            int distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return CODES[best];
    }
}
