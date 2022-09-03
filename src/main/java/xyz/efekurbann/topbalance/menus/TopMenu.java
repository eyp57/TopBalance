package xyz.efekurbann.topbalance.menus;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.efekurbann.topbalance.TopBalancePlugin;
import xyz.efekurbann.topbalance.objects.TopPlayer;
import xyz.efekurbann.topbalance.utils.ConfigManager;
import xyz.efekurbann.topbalance.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TopMenu {

    private final TopBalancePlugin plugin;
    private final FileConfiguration config = ConfigManager.get("config.yml");
    private final Gui gui;
    public TopMenu(String title, int size) {
        this.plugin = TopBalancePlugin.getInstance();
        this.gui = Gui.gui()
                        .rows(size)
                        .title(Component.text(title))
                        .disableAllInteractions()
                        .create();
        create();
    }
    public void create() {
        if (config.getBoolean("Gui.items.fill.enabled")){
            for (int i = 0; i < gui.getRows() * 9; i++){
                gui.setItem(i, new GuiItem(new ItemBuilder(
                        XMaterial.matchXMaterial(config.getString("Gui.items.fill.material").toUpperCase(Locale.ENGLISH)).get().parseItem()).withName(" ").build()));
            }
        }

        for (String key : config.getConfigurationSection("Tops").getKeys(false)) {
            int rank = config.getInt("Tops." + key + ".rank");
            TopPlayer player = plugin.getPlayersMap().get(rank-1);
            int slot = config.getInt("Tops." + key + ".slot");
            ItemStack item;
            if (player != null) {
                if (config.getBoolean("Gui.custom-item")) {
                    List<String> lore = new ArrayList<>();

                    for (String str : config.getStringList("Gui.items.player-item.lore")) {
                        lore.add(str.replace("{rank}", String.valueOf(rank))
                                .replace("{name}", player.getName())
                                .replace("{balance_raw}", String.valueOf(player.getBalance()))
                                .replace("{balance}", Tools.formatMoney(player.getBalance())));
                    }
                    item = new ItemBuilder(XMaterial.valueOf(config.getString("Tops." + key + ".material").toUpperCase(Locale.ENGLISH)).parseMaterial())
                            .withName(config.getString("Gui.items.player-item.name")
                                    .replace("{rank}", String.valueOf(rank))
                                    .replace("{name}", player.getName()))
                            .withLore(lore).build();
                } else {
                    item = getSkull(rank-1, "player-item");
                }
            } else {
                item = new ItemBuilder(XMaterial.valueOf(config.getString("Gui.items.player-not-found.material").toUpperCase(Locale.ENGLISH)).parseMaterial())
                        .withName(config.getString("Gui.items.player-not-found.name"))
                        .withLore(config.getStringList("Gui.items.player-not-found.lore")).build();
            }
            gui.setItem(slot, new GuiItem(item));
        }


        gui.setItem(
                config.getInt("Gui.items.close-menu.slot"),
                new GuiItem(new ItemBuilder(XMaterial.valueOf(config.getString("Gui.items.close-menu.material").toUpperCase(Locale.ENGLISH)).parseMaterial())
                        .withName(config.getString("Gui.items.close-menu.name"))
                        .withLore(config.getStringList("Gui.items.close-menu.lore")).build(),
                        (event) -> Bukkit.getScheduler().runTaskLater(plugin,
                                () -> event.getWhoClicked().closeInventory(), 2))
        );
    }
    public void open(HumanEntity player) {
        this.gui.open(player);
    }
    public ItemStack getSkull(Integer number, String path) {
        ItemStack item = XMaterial.PLAYER_HEAD.parseItem();
        ItemMeta meta = item.getItemMeta();
        TopPlayer player = plugin.getPlayersMap().get(number);

        //long start = System.currentTimeMillis();
        //long startNano = System.nanoTime();
        SkullMeta skullMeta = SkullUtils.applySkin(meta, player.getUUID());
        //System.out.println("[DEBUG] Took " + (System.currentTimeMillis() - start) + "ms");
        //System.out.println("[DEBUG] Took " + (System.nanoTime() - startNano) + " nano sec");

        //start = System.currentTimeMillis();
        //startNano = System.nanoTime();
        skullMeta.setDisplayName(Tools.colored(config.getString("Gui.items." + path + ".name")
                .replace("{rank}", String.valueOf(number + 1))
                .replace("{name}", player.getName())
                .replace("{balance_raw}", String.valueOf(player.getBalance()))
                .replace("{balance}", Tools.formatMoney(player.getBalance()))));
        List<String> lore = new ArrayList<>();
        for (String str : config.getStringList("Gui.items." + path + ".lore")) {
            lore.add(str.replace("{rank}", String.valueOf(number + 1))
                    .replace("{name}", player.getName())
                    .replace("{balance_raw}", String.valueOf(player.getBalance()))
                    .replace("{balance}", Tools.formatMoney(player.getBalance())));
        }
        skullMeta.setLore(Tools.colored(lore));

        item.setItemMeta(skullMeta);
        //System.out.println("[DEBUG] (2) Took " + (System.currentTimeMillis() - start) + "ms");
        //System.out.println("[DEBUG] (2) Took " + (System.nanoTime() - startNano) + " nano sec");
        return item;
    }

}
