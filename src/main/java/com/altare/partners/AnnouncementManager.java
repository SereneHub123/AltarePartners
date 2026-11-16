package com.altare.partners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds the built-in sponsor announcements and broadcasts them every 10 minutes,
 * cycling through the list in order.
 */
public class AnnouncementManager {

    private static final String DEFAULT_PREFIX = "<#2fff6b><bold>ꜱᴘᴏɴꜱᴏʀ</bold></#2fff6b> <dark_gray>»</dark_gray>";
    private static final String DEFAULT_SOUND = "ENTITY_EXPERIENCE_ORB_PICKUP";

    /**
     * Maps Bukkit enum names to the vanilla sound key that never changes between
     * versions. Keys are what {@code Player#playSound(Location, String, ...)}
     * expects, and unlike the Sound enum they survive renames in newer servers.
     */
    private static final Map<String, String> SOUND_KEYS = new HashMap<>();

    static {
        SOUND_KEYS.put("ENTITY_EXPERIENCE_ORB_PICKUP", "entity.experience_orb.pickup");
        SOUND_KEYS.put("ENTITY_ITEM_PICKUP", "entity.item.pickup");
        SOUND_KEYS.put("ENTITY_PLAYER_LEVELUP", "entity.player.levelup");
        SOUND_KEYS.put("ENTITY_VILLAGER_YES", "entity.villager.yes");
        SOUND_KEYS.put("ENTITY_VILLAGER_NO", "entity.villager.no");
        SOUND_KEYS.put("ENTITY_ENDER_DRAGON_GROWL", "entity.ender_dragon.growl");
        SOUND_KEYS.put("BLOCK_NOTE_BLOCK_PLING", "block.note_block.pling");
        SOUND_KEYS.put("BLOCK_NOTE_BLOCK_HARP", "block.note_block.harp");
        SOUND_KEYS.put("BLOCK_NOTE_BLOCK_CHIME", "block.note_block.chime");
        SOUND_KEYS.put("BLOCK_AMETHYST_BLOCK_CHIME", "block.amethyst_block.chime");
        SOUND_KEYS.put("BLOCK_END_PORTAL_SPAWN", "block.end_portal.spawn");
        SOUND_KEYS.put("UI_BUTTON_CLICK", "ui.button.click");
        SOUND_KEYS.put("UI_TOAST_CHALLENGE_COMPLETE", "ui.toast.challenge_complete");
        SOUND_KEYS.put("UI_TOAST_IN", "ui.toast.in");
    }

    /** Announcements rotate every 10 minutes. */
    private static final long INTERVAL_TICKS = 10L * 60L * 20L;

    private final AltarePartners plugin;
    private final List<Announcement> announcements = new ArrayList<>();

    private BukkitTask task;
    private int currentIndex = 0;
    private String prefixStr = DEFAULT_PREFIX;
    private String sound = DEFAULT_SOUND;

    public AnnouncementManager(AltarePartners plugin) {
        this.plugin = plugin;
        reload();
    }

    /** Reloads the announcement list from config.yml and restarts the timer. */
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
        sound = resolveSound(plugin.getConfig().getString("announcements.sound", DEFAULT_SOUND));

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;

            if (currentIndex >= announcements.size()) {
                currentIndex = 0;
            }

            Announcement data = announcements.get(currentIndex++);

            for (Player player : Bukkit.getOnlinePlayers()) {
                sendTo(player, data);
            }
        }, INTERVAL_TICKS, INTERVAL_TICKS);
    }

    /** Sends one announcement to a single player, including the configured sound. */
    public void sendTo(Player player, Announcement data) {
        Component prefix = Text.parse(plugin, prefixStr);

        player.sendMessage(Component.empty());
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        player.sendMessage(prefix.append(Component.text(" ")).append(Text.parse(plugin, data.title)));

        for (String descLine : data.description) {
            player.sendMessage(Text.parse(plugin, descLine));
        }

        if (data.linkText != null && data.linkUrl != null) {
            Component linkComp = Text.parse(plugin, data.linkText)
                    .clickEvent(ClickEvent.openUrl(data.linkUrl));

            if (data.linkHover != null && !data.linkHover.isEmpty()) {
                linkComp = linkComp.hoverEvent(HoverEvent.showText(Text.parse(plugin, data.linkHover)));
            }

            player.sendMessage(linkComp);
        }

        player.sendMessage(Component.empty());
    }

    /** Returns an announcement by its config key (case-insensitive) or null. */
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

    /** Returns a random loaded announcement, or null when the list is empty. */
    @Nullable
    public Announcement getRandom() {
        if (announcements.isEmpty()) return null;
        return announcements.get(ThreadLocalRandom.current().nextInt(announcements.size()));
    }

    public List<Announcement> getAnnouncements() {
        return Collections.unmodifiableList(announcements);
    }

    public String getPrefixStr() {
        return prefixStr;
    }

    /** Turns a config value such as ENTITY_EXPERIENCE_ORB_PICKUP into a vanilla sound key. */
    private String resolveSound(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.equalsIgnoreCase("none") || raw.equalsIgnoreCase("off")) {
            return null;
        }

        String value = raw.trim();

        // Already a namespaced key such as "entity.experience_orb.pickup".
        if (value.indexOf('.') >= 0 || value.indexOf(':') >= 0) {
            return value.toLowerCase(Locale.ROOT);
        }

        String mapped = SOUND_KEYS.get(value.toUpperCase(Locale.ROOT));
        if (mapped != null) {
            return mapped;
        }

        plugin.getLogger().warning("Unknown announcement sound '" + raw + "'; falling back to " + DEFAULT_SOUND + ".");
        return SOUND_KEYS.get(DEFAULT_SOUND);
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

        public Announcement() {
        }

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
