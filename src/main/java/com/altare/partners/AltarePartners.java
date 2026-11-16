package com.altare.partners;

import org.bukkit.plugin.java.JavaPlugin;

public class AltarePartners extends JavaPlugin {

    private AnnouncementManager announcementManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        announcementManager = new AnnouncementManager(this);

        AltarePartnersCommand command = new AltarePartnersCommand(this, announcementManager);
        getCommand("altare").setExecutor(command);
        getCommand("altare").setTabCompleter(command);

        SponsorGui sponsorGui = new SponsorGui(this);
        getServer().getPluginManager().registerEvents(sponsorGui, this);
        getCommand("sponser").setExecutor(new SponsorCommand(this, sponsorGui));

        getLogger().info("AltarePartners enabled with " + announcementManager.getAnnouncements().size() + " announcement(s).");
    }

    @Override
    public void onDisable() {
        if (announcementManager != null) {
            announcementManager.shutdown();
        }
        getLogger().info("AltarePartners disabled.");
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }

    /** Reloads config.yml and restarts the announcement rotation. */
    public void reload() {
        reloadConfig();
        announcementManager.reload();
    }
}
