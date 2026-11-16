package com.altare.partners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses text with the &lt;ServerName&gt; variable, MiniMessage tags (when the server
 * provides MiniMessage) and classic '&amp;' codes.
 *
 * <p>Paper only bundles MiniMessage from 1.18.2 onward. On older servers this falls
 * back to legacy section-code conversion, so the same texts keep working.</p>
 */
public final class Text {

    /** Matches &lt;ServerName&gt; and %server_name% in any casing. */
    private static final Pattern SERVER_NAME = Pattern.compile("(?i)<server[ _-]?name>|%server[ _-]?name%");
    private static final Pattern HEX_LEGACY = Pattern.compile("(?i)&#([0-9a-f]{6})");
    private static final Pattern LEGACY_CODE = Pattern.compile("(?i)[&\u00a7]([0-9a-fk-or])");
    private static final Pattern MINI_HEX = Pattern.compile("(?i)<#([0-9a-f]{6})>");
    private static final Pattern MINI_CLOSING = Pattern.compile("</[^>]+>");

    private static final String[] MINI_TAGS = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
            "yellow", "white", "obfuscated", "bold", "strikethrough", "underlined", "italic", "reset"
    };
    private static final String[] SECTION_CODES = {
            "\u00a70", "\u00a71", "\u00a72", "\u00a73", "\u00a74", "\u00a75",
            "\u00a76", "\u00a77", "\u00a78", "\u00a79", "\u00a7a", "\u00a7b", "\u00a7c", "\u00a7d",
            "\u00a7e", "\u00a7f", "\u00a7k", "\u00a7l", "\u00a7m", "\u00a7n", "\u00a7o", "\u00a7r"
    };

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private static boolean miniMessageChecked;
    private static Object miniMessage;
    private static Method miniMessageDeserialize;

    private Text() {
    }

    public static Component parse(JavaPlugin plugin, @Nullable String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        String serverName = plugin.getConfig().getString("server-name", "Your Server");
        String processed = SERVER_NAME.matcher(text).replaceAll(Matcher.quoteReplacement(serverName));

        Object parser = getMiniMessage();
        if (parser != null) {
            processed = HEX_LEGACY.matcher(processed).replaceAll("<#$1>");
            processed = replaceLegacyCodes(processed);
            try {
                return (Component) miniMessageDeserialize.invoke(parser, processed);
            } catch (ReflectiveOperationException ignored) {
                // Fall through to the legacy parser below.
            }
        }

        return LEGACY.deserialize(toLegacy(processed));
    }

    /**
     * MiniMessage is optional: it is bundled by Paper 1.18.2+ but absent on 1.16.5/1.17.
     * Reflection keeps this class loadable on every version.
     */
    private static Object getMiniMessage() {
        if (!miniMessageChecked) {
            miniMessageChecked = true;
            try {
                Class<?> type = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
                try {
                    miniMessage = type.getMethod("miniMessage").invoke(null);
                } catch (NoSuchMethodException olderApi) {
                    miniMessage = type.getMethod("get").invoke(null);
                }
                miniMessageDeserialize = type.getMethod("deserialize", String.class);
            } catch (LinkageError | ReflectiveOperationException unavailable) {
                miniMessage = null;
                miniMessageDeserialize = null;
            }
        }
        return miniMessage;
    }

    private static String replaceLegacyCodes(String input) {
        Matcher matcher = LEGACY_CODE.matcher(input);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(out, Matcher.quoteReplacement("<" + legacyTag(matcher.group(1)) + ">"));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static String toLegacy(String input) {
        String text = input;

        Matcher hex = MINI_HEX.matcher(text);
        StringBuffer out = new StringBuffer();
        while (hex.find()) {
            hex.appendReplacement(out, Matcher.quoteReplacement(sectionHex(hex.group(1))));
        }
        hex.appendTail(out);
        text = out.toString();

        for (int i = 0; i < MINI_TAGS.length; i++) {
            text = text.replace("<" + MINI_TAGS[i] + ">", SECTION_CODES[i]);
        }
        text = MINI_CLOSING.matcher(text).replaceAll(Matcher.quoteReplacement("\u00a7r"));

        Matcher ampHex = HEX_LEGACY.matcher(text);
        out = new StringBuffer();
        while (ampHex.find()) {
            ampHex.appendReplacement(out, Matcher.quoteReplacement(sectionHex(ampHex.group(1))));
        }
        ampHex.appendTail(out);
        text = out.toString();

        Matcher codes = Pattern.compile("[&\u00a7]([0-9a-fk-orA-FK-OR])").matcher(text);
        out = new StringBuffer();
        while (codes.find()) {
            codes.appendReplacement(out, Matcher.quoteReplacement("\u00a7" + codes.group(1).toLowerCase(Locale.ROOT)));
        }
        codes.appendTail(out);
        return out.toString();
    }

    private static String sectionHex(String hex) {
        StringBuilder builder = new StringBuilder("\u00a7x");
        for (int i = 0; i < hex.length(); i++) {
            builder.append('\u00a7').append(Character.toLowerCase(hex.charAt(i)));
        }
        return builder.toString();
    }

    private static String legacyTag(String code) {
        switch (code.toLowerCase(Locale.ROOT)) {
            case "0": return "black";
            case "1": return "dark_blue";
            case "2": return "dark_green";
            case "3": return "dark_aqua";
            case "4": return "dark_red";
            case "5": return "dark_purple";
            case "6": return "gold";
            case "7": return "gray";
            case "8": return "dark_gray";
            case "9": return "blue";
            case "a": return "green";
            case "b": return "aqua";
            case "c": return "red";
            case "d": return "light_purple";
            case "e": return "yellow";
            case "f": return "white";
            case "k": return "obfuscated";
            case "l": return "bold";
            case "m": return "strikethrough";
            case "n": return "underlined";
            case "o": return "italic";
            case "r": return "reset";
            default: return "white";
        }
    }
}
