package com.enderdevelopment.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Main extends JavaPlugin implements Listener {
    private static final long PASSIVE_COOLDOWN = 30L * 20L;
    private static final long HOOK_COOLDOWN = 25L * 20L;
    private static final long DOMAIN_COOLDOWN = 270L * 20L;
    private static final long DOMAIN_DURATION = 30L * 20L;
    private final Map<UUID, Long> passiveCooldowns = new HashMap<>();
    private final Map<UUID, Long> hookCooldowns = new HashMap<>();
    private final Map<UUID, Long> domainCooldowns = new HashMap<>();
    private final Map<UUID, Long> domains = new HashMap<>();
    private NamespacedKey spearKey;

    @Override
    public void onEnable() {
        spearKey = new NamespacedKey(this, "green_spear");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Green Spear enabled.");
    }

    @Override
    public void onDisable() {
        passiveCooldowns.clear();
        hookCooldowns.clear();
        domainCooldowns.clear();
        domains.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the spear.");
            return true;
        }
        player.getInventory().addItem(createSpear());
        player.sendMessage(Component.text("You received the Green Spear.").color(NamedTextColor.GREEN));
        return true;
    }

    private ItemStack createSpear() {
        ItemStack spear = new ItemStack(Material.TRIDENT);
        ItemMeta meta = spear.getItemMeta();
        meta.displayName(Component.text("Green Spear").color(NamedTextColor.GREEN));
        meta.lore(java.util.List.of(
                Component.text("Passive: 20% corruption on hit").color(NamedTextColor.DARK_GREEN),
                Component.text("Offhand: Hook strike").color(NamedTextColor.GREEN),
                Component.text("Sneak + offhand: Domain").color(NamedTextColor.GREEN)));
        meta.getPersistentDataContainer().set(spearKey, PersistentDataType.BYTE, (byte) 1);
        spear.setItemMeta(meta);
        return spear;
    }

    private boolean isSpear(ItemStack item) {
        if (item == null || item.getType() != Material.TRIDENT || !item.hasItemMeta()) {
            return false;
        }
        Byte value = item.getItemMeta().getPersistentDataContainer().get(spearKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player) || !isSpear(player.getInventory().getItemInMainHand())
                || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (Math.random() < 0.20 && cooldownReady(passiveCooldowns, player, PASSIVE_COOLDOWN)) {
            applyCorruption(living, 6);
            freeze(living, 60);
            greenScreen(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!isSpear(player.getInventory().getItemInOffHand())) {
            return;
        }
        event.setCancelled(true);
        if (player.isSneaking()) {
            activateDomain(player);
        } else {
            activateHook(player);
        }
    }

    private void activateHook(Player player) {
        if (!cooldownReady(hookCooldowns, player, HOOK_COOLDOWN)) {
            return;
        }
        Entity target = player.getTargetEntity(20);
        if (!(target instanceof LivingEntity living) || target == player) {
            player.sendMessage(Component.text("No target in range.").color(NamedTextColor.RED));
            return;
        }
        living.damage(8.0, player);
        applyCorruption(living, 10);
        freeze(living, 60);
        greenScreen(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 0.7f);
    }

    private void activateDomain(Player player) {
        if (!cooldownReady(domainCooldowns, player, DOMAIN_COOLDOWN)) {
            return;
        }
        domains.put(player.getUniqueId(), System.currentTimeMillis() + DOMAIN_DURATION * 50L);
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 80, 4, 1, 4,
                new Particle.DustOptions(Color.fromRGB(30, 220, 70), 1.4f));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.6f);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            Long expiry = domains.get(player.getUniqueId());
            if (expiry == null || expiry <= System.currentTimeMillis() || !player.isOnline()) {
                domains.remove(player.getUniqueId());
                return;
            }
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 20, 4, 0.3, 4,
                    new Particle.DustOptions(Color.fromRGB(30, 220, 70), 1.0f));
            for (Entity entity : player.getNearbyEntities(6, 3, 6)) {
                if (entity instanceof LivingEntity living && entity != player) {
                    applyCorruption(living, 2);
                }
            }
        }, 0L, 20L);
        Bukkit.getScheduler().runTaskLater(this, task::cancel, DOMAIN_DURATION + 1L);
        player.sendMessage(Component.text("Domain active for 30 seconds.").color(NamedTextColor.GREEN));
    }

    private void applyCorruption(LivingEntity target, int seconds) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, seconds * 20, 0, true, true, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, seconds * 20, 0, true, true, true));
    }

    private void freeze(LivingEntity target, int ticks) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 10, true, false, false));
    }

    private void greenScreen(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20, 0, true, false, false));
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 50, 0.5, 0.8, 0.5,
                new Particle.DustOptions(Color.fromRGB(40, 255, 60), 2.0f));
    }

    private boolean cooldownReady(Map<UUID, Long> cooldowns, Player player, long baseCooldown) {
        long now = System.currentTimeMillis();
        Long readyAt = cooldowns.get(player.getUniqueId());
        if (readyAt != null && readyAt > now) {
            return false;
        }
        long multiplier = domains.getOrDefault(player.getUniqueId(), 0L) > now ? 2L : 1L;
        cooldowns.put(player.getUniqueId(), now + baseCooldown * 50L * multiplier);
        return true;
    }
}
