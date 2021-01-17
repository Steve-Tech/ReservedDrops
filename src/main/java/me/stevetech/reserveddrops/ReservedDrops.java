package me.stevetech.reserveddrops;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.UUID;

public class ReservedDrops extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info(getDescription().getName() + ' ' + getDescription().getVersion() + " has been Enabled");
    }

    @Override
    public void onDisable() {
        saveConfig();

        getLogger().info(getDescription().getName() + ' ' + getDescription().getVersion() + " has been Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ReservedDrops") && sender.hasPermission("ReservedDrops.reload")) {
            if (args.length == 1) {
                reloadConfig();
                sender.sendMessage("Reloaded Config");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onDeathEvent(EntityDeathEvent event) {
        Player player;
        if (event.getEntity() instanceof Player) {
            player = (Player) event.getEntity();
        } else if (getConfig().getBoolean("reserve-mob-drops")) {
            player = event.getEntity().getKiller();
        } else {
            return;
        }
        if (player != null) {
            event.getDrops().forEach(itemStack -> {
                NamespacedKey keyPlayer = new NamespacedKey(this, "player");
                NamespacedKey keyTime = new NamespacedKey(this, "time");
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.getPersistentDataContainer().set(keyPlayer, PersistentDataType.STRING, player.getUniqueId().toString());
                itemMeta.getPersistentDataContainer().set(keyTime, PersistentDataType.LONG, Instant.now().getEpochSecond());
                itemStack.setItemMeta(itemMeta);
            });
        }
    }

    @EventHandler
    public void onPickupEvent(EntityPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        NamespacedKey keyPlayer = new NamespacedKey(this, "player");
        NamespacedKey keyTime = new NamespacedKey(this, "time");
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        if (container.has(keyPlayer, PersistentDataType.STRING) && container.has(keyTime, PersistentDataType.LONG)) {
            UUID itemsPlayer = UUID.fromString(container.get(keyPlayer, PersistentDataType.STRING));
            long deathTime = container.get(keyTime, PersistentDataType.LONG);
            if (deathTime + getConfig().getInt("timeout") > Instant.now().getEpochSecond()) {
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (!(player.getUniqueId().equals(itemsPlayer) || player.hasPermission("ReservedDrops.bypass"))) {
                        event.setCancelled(true);
                    }
                } else if (!(getConfig().getBoolean("allow-entity-pickup"))) {
                    event.setCancelled(true);
                }
            }
            if (!(event.isCancelled())) {
                container.remove(keyPlayer);
                container.remove(keyTime);
                itemStack.setItemMeta(itemMeta);
            }
        }
    }
}
