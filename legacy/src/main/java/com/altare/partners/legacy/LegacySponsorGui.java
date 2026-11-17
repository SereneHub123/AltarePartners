package com.altare.partners.legacy;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 27-slot sponsor menu for the 1.8.8 build. */
public class LegacySponsorGui implements Listener {

    public static final String PERMISSION = "altarepartners.sponsor";
    public static final String WEBSITE_URL = "https://altare.gg";

    private static final int WEBSITE_SLOT = 4;
    private static final int FREE_SLOT = 11;
    private static final int PLUS_SLOT = 15;

    /** Data value of gray stained glass pane in 1.8.8. */
    private static final short GRAY_PANE_DATA = 7;

    private final LegacyPlugin plugin;

    public LegacySponsorGui(LegacyPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        SponsorHolder holder = new SponsorHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27,
                LegacyText.parse(plugin, "<#2fff6b><bold>ꜱᴘᴏɴꜱᴏʀ</bold></#2fff6b> <dark_gray>»</dark_gray> <white>Altare.gg</white>"));
        holder.setInventory(inventory);

        ItemStack filler = createItem("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", GRAY_PANE_DATA, " ",
                Collections.<String>emptyList());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(WEBSITE_SLOT, websiteItem());
        inventory.setItem(FREE_SLOT, freeItem());
        inventory.setItem(PLUS_SLOT, plusItem());

        player.openInventory(inventory);
    }

    private ItemStack websiteItem() {
        return createItem(Material.BOOK, (short) 0,
                "<#2fff6b><bold>altare.gg</bold></#2fff6b>",
                Arrays.asList(
                        "<white>Click to open altare.gg</white>",
                        "<gray>Free hosting for Minecraft,</gray>",
                        "<gray>bots and websites.</gray>"
                ));
    }

    private ItemStack freeItem() {
        return createItem("GRASS_BLOCK", "GRASS", (short) 0,
                "<#2fff6b><bold>Free Tier</bold></#2fff6b>",
                Arrays.asList(
                        "<white>6 GB RAM</white>",
                        "<white>150% CPU</white>",
                        "<white>10 GB Disk</white>",
                        "",
                        "<gray>Host any software you wish for:</gray>",
                        "<white>• Minecraft servers</white>",
                        "<white>• Discord bots</white>",
                        "<white>• Websites</white>",
                        "<white>• Anything else</white>",
                        "",
                        "<#2fff6b><bold>100% FREE</bold></#2fff6b>",
                        "<gray>24/7 uptime at no cost.</gray>"
                ));
    }

    private ItemStack plusItem() {
        return createItem(Material.NETHER_STAR, (short) 0,
                "<#2fff6b><bold>Altare Plus</bold></#2fff6b>",
                Arrays.asList(
                        "<white><bold>$7.99 / month</bold></white>",
                        "<gray>Billed monthly · Cancel anytime</gray>",
                        "",
                        "<white>+4 GiB RAM · +20 GiB storage</white>",
                        "<white>+100% CPU · 2 extra app slots</white>",
                        "<white>10× storage & bigger databases</white>",
                        "<white>Plus-only regions · AI Token Plan</white>",
                        "<white>14-day logs · Plus badge</white>",
                        "",
                        "<#2fff6b>Click the book to visit altare.gg</#2fff6b>"
                ));
    }

    /**
     * Resolves a material by its flattened (1.13+) name first and falls back to
     * the pre-1.13 name plus data value, so one jar covers 1.8.8 through 1.15.
     */
    private ItemStack createItem(String modernMaterial, String legacyMaterial, short legacyData,
                                 String name, List<String> loreLines) {
        Material material = Material.matchMaterial(modernMaterial);
        short data = 0;

        if (material == null) {
            material = Material.matchMaterial(legacyMaterial);
            data = legacyData;
        }
        if (material == null) {
            material = Material.BOOK;
        }

        return createItem(material, data, name, loreLines);
    }

    private ItemStack createItem(Material material, short data, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LegacyText.parse(plugin, name));
            List<String> lore = new ArrayList<String>();
            for (String line : loreLines) {
                lore.add(LegacyText.parse(plugin, line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getInventory();
        if (top == null || !(top.getHolder() instanceof SponsorHolder)) return;

        event.setCancelled(true);

        if (event.getRawSlot() != WEBSITE_SLOT) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        sendWebsiteLink(player);
    }

    private void sendWebsiteLink(Player player) {
        TextComponent message = new TextComponent(TextComponent.fromLegacyText(
                LegacyText.parse(plugin, "<gray>Altare.gg <dark_gray>»</dark_gray> </gray>")));
        TextComponent link = new TextComponent(TextComponent.fromLegacyText(
                LegacyText.parse(plugin, "<#2fff6b><bold><u>Click here to open the website</u></bold></#2fff6b>")));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, WEBSITE_URL));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                LegacyText.parse(plugin, "<white>Click to visit <#2fff6b>Altare.gg</#2fff6b></white>"))));
        message.addExtra(link);

        player.spigot().sendMessage(message);
    }

    private static final class SponsorHolder implements InventoryHolder {

        private Inventory inventory;

        void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
