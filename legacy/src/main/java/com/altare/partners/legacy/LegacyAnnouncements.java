package com.altare.partners.legacy;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds the built-in sponsor announcements for the 1.8.8 build and broadcasts
 * them every 10 minutes, cycling through the list in order.
 */
public class LegacyAnnouncements {

    private static final String DEFAULT_PREFIX = "<#2fff6b><bold>ꜱᴘᴏɴꜱᴏʀ</bold></#2fff6b> <dark_gray>»</dark_gray>";
    private static final String DEFAULT_SOUND = "ENTITY_EXPERIENCE_ORB_PICKUP";

    /** Announcements rotate every 10 minutes. */
    private static final long INTERVAL_TICKS = 10L * 60L * 20L;

    /** Maps modern sound names to the Sound constants that exist in 1.8.8. */
    private static final Map<String, String> SOUNDS = new HashMap<String, String>();

    static {
        SOUNDS.put("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP");
        SOUNDS.put("ENTITY_ITEM_PICKUP", "ITEM_PICKUP");
        SOUNDS.put("ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
        SOUNDS.put("ENTITY_VILLAGER_YES", "VILLAGER_YES");
        SOUNDS.put("ENTITY_VILLAGER_NO", "VILLAGER_NO");
        SOUNDS.put("ENTITY_ENDER_DRAGON_GROWL", "ENDERDRAGON_GROWL");
        SOUNDS.put("BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING");
        SOUNDS.put("BLOCK_NOTE_BLOCK_HARP", "NOTE_PLING");
        SOUNDS.put("BLOCK_NOTE_BLOCK_CHIME", "NOTE_PLING");
        SOUNDS.put("BLOCK_AMETHYST_BLOCK_CHIME", "NOTE_PLING");
        SOUNDS.put("BLOCK_END_PORTAL_SPAWN", "PORTAL_TRAVEL");
        SOUNDS.put("UI_BUTTON_CLICK", "CLICK");
        SOUNDS.put("UI_TOAST_CHALLENGE_COMPLETE", "LEVEL_UP");
        SOUNDS.put("UI_TOAST_IN", "CLICK");
    }

    private final LegacyPlugin plugin;
    private final List<Announcement> announcements = new ArrayList<Announcement>();

    private BukkitTask task;
    private int currentIndex = 0;
    private String prefixStr = DEFAULT_PREFIX;
    private Sound sound;
    private String soundName;

    public LegacyAnnouncements(LegacyPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /** Reloads the branding from config.yml and restarts the timer. */
    public void reload() {
        loadAnnouncements();
        startTask();
    }

    /**
     * The two sponsor announcements are built in and always active; only the
     * prefix, sound and server name are configurable in config.yml.
     */
    public void loadAnnouncements() {
        announcements.clear();
        announcements.add(new Announcement(
                "altare-hosting",
                "<#2fff6b><bold>Powered by Altare.gg</bold></#2fff6b>",
                Arrays.asList(
                        "<white><ServerName> runs on <#2fff6b>Altare.gg</#2fff6b>, our hosting sponsor!</white>",
                        "<gray>Free Minecraft, bot and website hosting with 24/7 uptime.</gray>"
                ),
                "<#2fff6b><bold>» altare.gg</bold></#2fff6b>",
                "https://altare.gg",
                "<white>Click to visit <#2fff6b>Altare.gg</#2fff6b></white>"
        ));
        announcements.add(new Announcement(
                "free-minecraft-hosting",
                "<#2fff6b><bold>Host Your Own Server</bold></#2fff6b>",
                Arrays.asList(
                        "<white>Want a server for you and your friends? <#2fff6b>It's free!</#2fff6b></white>",
                        "<gray>Get 6GB RAM and 24/7 uptime with Altare.</gray>"
                ),
                "<#2fff6b><bold>» Get started</bold></#2fff6b>",
                "https://altare.gg",
                "<white>Click to claim your free server</white>"
        ));
    }

    public void startTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        currentIndex = 0;

        prefixStr = plugin.getConfig().getString("announcements.prefix", DEFAULT_PREFIX);
        resolveSound(plugin.getConfig().getString("announcements.sound", DEFAULT_SOUND));

        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) return;

                if (currentIndex >= announcements.size()) {
                    currentIndex = 0;
                }

                Announcement data = announcements.get(currentIndex++);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    sendTo(player, data);
                }
            }
        }, INTERVAL_TICKS, INTERVAL_TICKS);
    }

    /** Sends one announcement to a single player, including the configured sound. */
    public void sendTo(final Player player, Announcement data) {
        player.sendMessage("");
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } else if (soundName != null) {
            player.playSound(player.getLocation(), soundName, 1.0f, 1.0f);
        }

        player.sendMessage(LegacyText.parse(plugin, prefixStr) + " " + LegacyText.parse(plugin, data.title));

        for (String descLine : data.description) {
            player.sendMessage(LegacyText.parse(plugin, descLine));
        }

        if (data.linkText != null && data.linkUrl != null) {
            TextComponent link = new TextComponent(
                    TextComponent.fromLegacyText(LegacyText.parse(plugin, data.linkText)));
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, data.linkUrl));

            if (data.linkHover != null && !data.linkHover.isEmpty()) {
                link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        TextComponent.fromLegacyText(LegacyText.parse(plugin, data.linkHover))));
            }

            player.spigot().sendMessage(link);
        }

        player.sendMessage("");
    }

    public Announcement getById(String id) {
        for (Announcement announcement : announcements) {
            if (announcement.id.equalsIgnoreCase(id)) {
                return announcement;
            }
        }
        return null;
    }

    public Announcement getByIndex(int index) {
        if (index < 0 || index >= announcements.size()) return null;
        return announcements.get(index);
    }

    public Announcement getRandom() {
        if (announcements.isEmpty()) return null;
        return announcements.get(ThreadLocalRandom.current().nextInt(announcements.size()));
    }

    public List<Announcement> getAnnouncements() {
        return Collections.unmodifiableList(announcements);
    }

    /**
     * Accepts modern sound names, 1.8 enum names or vanilla 1.8 keys such as
     * "random.orb". Use "none" to disable the sound.
     */
    private void resolveSound(String raw) {
        sound = null;
        soundName = null;

        if (raw == null || raw.trim().isEmpty()
                || raw.equalsIgnoreCase("none") || raw.equalsIgnoreCase("off")) {
            return;
        }

        String value = raw.trim();
        if (value.indexOf('.') >= 0) {
            soundName = value;
            return;
        }

        String modern = value.toUpperCase(Locale.ROOT);
        String legacy = SOUNDS.get(modern);

        // 1.8 used ORB_PICKUP-style names; 1.9+ uses ENTITY_EXPERIENCE_ORB_PICKUP.
        sound = firstSound(legacy, modern);
        if (sound == null) {
            plugin.getLogger().warning("Unknown announcement sound '" + raw + "'; falling back to " + DEFAULT_SOUND + ".");
            sound = firstSound("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP");
        }
    }

    private Sound firstSound(String... names) {
        for (String name : names) {
            if (name == null) continue;
            try {
                return Sound.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                // Try the next name.
            }
        }
        return null;
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public static class Announcement {
        public String id;
        public String title;
        public List<String> description;
        public String linkText;
        public String linkUrl;
        public String linkHover;

        public Announcement(String id, String title, List<String> description,
                            String linkText, String linkUrl, String linkHover) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.linkText = linkText;
            this.linkUrl = linkUrl;
            this.linkHover = linkHover;
        }
    }
}
