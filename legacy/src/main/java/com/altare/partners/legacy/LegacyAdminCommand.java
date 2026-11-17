package com.altare.partners.legacy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Admin command for the 1.8.8 build: /altare <reload|list|preview|test>. */
public class LegacyAdminCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "altarepartners.admin";

    private final LegacyPlugin plugin;
    private final LegacyAnnouncements announcements;

    public LegacyAdminCommand(LegacyPlugin plugin, LegacyAnnouncements announcements) {
        this.plugin = plugin;
        this.announcements = announcements;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(LegacyText.parse(plugin, "<red>You do not have permission to use this command.</red>"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            handleReload(sender);
        } else if (sub.equals("list")) {
            handleList(sender);
        } else if (sub.equals("preview")) {
            handlePreview(sender, args);
        } else if (sub.equals("test")) {
            handleTest(sender, args);
        } else {
            sendUsage(sender);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(LegacyText.parse(plugin, "<green>AltarePartners reloaded. Loaded <white>"
                + announcements.getAnnouncements().size() + "</white> announcement(s).</green>"));
    }

    private void handleList(CommandSender sender) {
        List<LegacyAnnouncements.Announcement> list = announcements.getAnnouncements();
        if (list.isEmpty()) {
            sender.sendMessage(LegacyText.parse(plugin, "<yellow>No announcements configured.</yellow>"));
            return;
        }

        sender.sendMessage(LegacyText.parse(plugin, "<#2fff6b><bold>Altare Partner Announcements</bold></#2fff6b> <gray>("
                + list.size() + ")</gray>"));
        int index = 1;
        for (LegacyAnnouncements.Announcement announcement : list) {
            sender.sendMessage(LegacyText.parse(plugin, "<gray>" + index + ". <white>" + announcement.id
                    + "</white> <dark_gray>»</dark_gray> " + announcement.title + "</gray>"));
            index++;
        }
        sender.sendMessage(LegacyText.parse(plugin,
                "<gray>Use <white>/altare preview <id|number></white> to view one.</gray>"));
    }

    private void handlePreview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LegacyText.parse(plugin, "<red>Only players can preview announcements.</red>"));
            return;
        }
        Player player = (Player) sender;

        List<LegacyAnnouncements.Announcement> list = announcements.getAnnouncements();
        if (list.isEmpty()) {
            player.sendMessage(LegacyText.parse(plugin, "<yellow>No announcements configured.</yellow>"));
            return;
        }

        LegacyAnnouncements.Announcement announcement;
        if (args.length >= 2) {
            announcement = resolve(args[1]);
            if (announcement == null) {
                player.sendMessage(LegacyText.parse(plugin, "<red>Unknown announcement '<white>" + args[1]
                        + "</white>'.</red>"));
                return;
            }
        } else {
            announcement = list.get(0);
        }

        announcements.sendTo(player, announcement);
    }

    /** Sends a random announcement right now. Players see it themselves; console broadcasts to everyone. */
    private void handleTest(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("announce")) {
            sender.sendMessage(LegacyText.parse(plugin, "<gray>Usage: <white>/altare test announce</white></gray>"));
            return;
        }

        LegacyAnnouncements.Announcement announcement = announcements.getRandom();
        if (announcement == null) {
            sender.sendMessage(LegacyText.parse(plugin, "<yellow>No announcements configured.</yellow>"));
            return;
        }

        if (sender instanceof Player) {
            announcements.sendTo((Player) sender, announcement);
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            announcements.sendTo(player, announcement);
        }
        sender.sendMessage(LegacyText.parse(plugin, "<green>Sent random announcement '<white>" + announcement.id
                + "</white>' to " + plugin.getServer().getOnlinePlayers().size() + " player(s).</green>"));
    }

    private LegacyAnnouncements.Announcement resolve(String input) {
        try {
            int number = Integer.parseInt(input);
            return announcements.getByIndex(number - 1);
        } catch (NumberFormatException ignored) {
            return announcements.getById(input);
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(LegacyText.parse(plugin, "<#2fff6b><bold>AltarePartners</bold></#2fff6b>"));
        sender.sendMessage(LegacyText.parse(plugin,
                "<gray>/altare reload <dark_gray>-</dark_gray> reload config.yml</gray>"));
        sender.sendMessage(LegacyText.parse(plugin,
                "<gray>/altare list <dark_gray>-</dark_gray> list announcements</gray>"));
        sender.sendMessage(LegacyText.parse(plugin,
                "<gray>/altare preview [id|number] <dark_gray>-</dark_gray> show one to yourself</gray>"));
        sender.sendMessage(LegacyText.parse(plugin,
                "<gray>/altare test announce <dark_gray>-</dark_gray> send a random announcement now</gray>"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return filter(Arrays.asList("reload", "list", "preview", "test"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("preview")) {
            List<String> options = new ArrayList<String>();
            int index = 1;
            for (LegacyAnnouncements.Announcement announcement : announcements.getAnnouncements()) {
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
