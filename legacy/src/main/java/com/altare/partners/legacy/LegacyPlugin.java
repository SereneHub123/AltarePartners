package com.altare.partners.legacy;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * AltarePartners built for Spigot/Paper 1.8.8. Uses legacy colour codes and
 * Bungee chat components instead of Adventure.
 */
public class LegacyPlugin extends JavaPlugin {

    private LegacyAnnouncements announcements;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        announcements = new LegacyAnnouncements(this);

        LegacyAdminCommand admin = new LegacyAdminCommand(this, announcements);
        if (getCommand("altare") != null) {
            getCommand("altare").setExecutor(admin);
            getCommand("altare").setTabCompleter(admin);
        }

        LegacySponsorGui gui = new LegacySponsorGui(this);
        getServer().getPluginManager().registerEvents(gui, this);
        if (getCommand("sponser") != null) {
            getCommand("sponser").setExecutor(new LegacySponsorCommand(this, gui));
        }

        getLogger().info("AltarePartners (1.8.8 build) enabled with "
                + announcements.getAnnouncements().size() + " announcement(s).");
    }

    @Override
    public void onDisable() {
        if (announcements != null) {
            announcements.shutdown();
        }
        getLogger().info("AltarePartners disabled.");
    }

    public LegacyAnnouncements getAnnouncements() {
        return announcements;
    }

    /** Reloads config.yml and restarts the announcement rotation. */
    public void reload() {
        reloadConfig();
        announcements.reload();
    }
}
