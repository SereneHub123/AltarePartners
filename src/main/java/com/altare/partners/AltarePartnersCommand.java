package com.altare.partners;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Admin command: /altare <reload|list|preview [id|number]|test announce>. */
public class AltarePartnersCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "altarepartners.admin";

    private final AltarePartners plugin;
    private final AnnouncementManager announcementManager;

    public AltarePartnersCommand(AltarePartners plugin, AnnouncementManager announcementManager) {
        this.plugin = plugin;
        this.announcementManager = announcementManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Text.parse(plugin, "<red>You do not have permission to use this command.</red>"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload":
                handleReload(sender);
                break;
            case "list":
                handleList(sender);
                break;
            case "preview":
                handlePreview(sender, args);
                break;
            case "test":
                handleTest(sender, args);
                break;
            default:
                sendUsage(sender);
                break;
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(Text.parse(plugin, "<green>AltarePartners reloaded. Loaded <white>"
                + announcementManager.getAnnouncements().size() + "</white> announcement(s).</green>"));
    }

    private void handleList(CommandSender sender) {
        List<AnnouncementManager.Announcement> announcements = announcementManager.getAnnouncements();
        if (announcements.isEmpty()) {
            sender.sendMessage(Text.parse(plugin, "<yellow>No announcements configured.</yellow>"));
            return;
        }

        sender.sendMessage(Text.parse(plugin, "<#2fff6b><bold>Altare Partner Announcements</bold></#2fff6b> <gray>("
                + announcements.size() + ")</gray>"));
        int index = 1;
        for (AnnouncementManager.Announcement announcement : announcements) {
            sender.sendMessage(Text.parse(plugin, "<gray>" + index + ". <white>" + announcement.id
                    + "</white> <dark_gray>»</dark_gray> " + announcement.title + "</gray>"));
            index++;
        }
        sender.sendMessage(Text.parse(plugin, "<gray>Use <white>/altare preview <id|number></white> to view one.</gray>"));
    }

    private void handlePreview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.parse(plugin, "<red>Only players can preview announcements.</red>"));
            return;
        }
        Player player = (Player) sender;

        List<AnnouncementManager.Announcement> announcements = announcementManager.getAnnouncements();
        if (announcements.isEmpty()) {
            player.sendMessage(Text.parse(plugin, "<yellow>No announcements configured.</yellow>"));
            return;
        }

        AnnouncementManager.Announcement announcement;
        if (args.length >= 2) {
            announcement = resolve(args[1]);
            if (announcement == null) {
                player.sendMessage(Text.parse(plugin, "<red>Unknown announcement '<white>" + args[1] + "</white>'.</red>"));
                return;
            }
        } else {
            announcement = announcements.get(0);
        }

        announcementManager.sendTo(player, announcement);
    }

    /** Sends a random announcement right now. Players see it themselves; console broadcasts to everyone. */
    private void handleTest(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("announce")) {
            sender.sendMessage(Text.parse(plugin, "<gray>Usage: <white>/altare test announce</white></gray>"));
            return;
        }

        AnnouncementManager.Announcement announcement = announcementManager.getRandom();
        if (announcement == null) {
            sender.sendMessage(Text.parse(plugin, "<yellow>No announcements configured.</yellow>"));
            return;
        }

        if (sender instanceof Player) {
            announcementManager.sendTo((Player) sender, announcement);
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            announcementManager.sendTo(player, announcement);
        }
        sender.sendMessage(Text.parse(plugin, "<green>Sent random announcement '<white>" + announcement.id
                + "</white>' to " + plugin.getServer().getOnlinePlayers().size() + " player(s).</green>"));
    }

    @Nullable
    private AnnouncementManager.Announcement resolve(String input) {
        try {
            int number = Integer.parseInt(input);
            return announcementManager.getByIndex(number - 1);
        } catch (NumberFormatException ignored) {
            return announcementManager.getById(input);
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Text.parse(plugin, "<#2fff6b><bold>AltarePartners</bold></#2fff6b>"));
        sender.sendMessage(Text.parse(plugin, "<gray>/altare reload <dark_gray>-</dark_gray> reload config.yml</gray>"));
        sender.sendMessage(Text.parse(plugin, "<gray>/altare list <dark_gray>-</dark_gray> list announcements</gray>"));
        sender.sendMessage(Text.parse(plugin, "<gray>/altare preview [id|number] <dark_gray>-</dark_gray> show one to yourself</gray>"));
        sender.sendMessage(Text.parse(plugin, "<gray>/altare test announce <dark_gray>-</dark_gray> send a random announcement now</gray>"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filter(Arrays.asList("reload", "list", "preview", "test"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("preview")) {
            List<String> options = new ArrayList<String>();
            int index = 1;
            for (AnnouncementManager.Announcement announcement : announcementManager.getAnnouncements()) {
                options.add(String.valueOf(index++));
                options.add(announcement.id);
            }
            return filter(options, args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
            return filter(Collections.singletonList("announce"), args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String input) {
        List<String> result = new ArrayList<String>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))) {
                result.add(option);
            }
        }
        return result;
    }
}
