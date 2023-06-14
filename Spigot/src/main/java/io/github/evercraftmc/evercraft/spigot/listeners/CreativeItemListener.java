package io.github.evercraftmc.evercraft.spigot.listeners;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class CreativeItemListener extends SpigotListener {
    @EventHandler
    public void onItemCreate(InventoryClickEvent event) {
        if (!event.getWhoClicked().hasPermission("evercraft.commands.gamemode.creative")) {
            ItemStack itemStack = CraftItemStack.asNMSCopy(event.getCursor());
            CompoundTag nbtCompound = itemStack.getOrCreateTag();

            if (nbtCompound.contains("BlockTag") || nbtCompound.contains("EntityTag") || nbtCompound.contains("BlockState")) {
                event.setCancelled(true);

                event.getCursor().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onItemCreate(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Player && ((Player) event.getSource().getHolder()).hasPermission("evercraft.commands.gamemode.creative")) && !(event.getDestination().getHolder() instanceof Player && ((Player) event.getDestination().getHolder()).hasPermission("evercraft.commands.gamemode.creative"))) {
            ItemStack itemStack = CraftItemStack.asNMSCopy(event.getItem());
            CompoundTag nbtCompound = itemStack.getOrCreateTag();

            if (nbtCompound.contains("BlockTag") || nbtCompound.contains("EntityTag") || nbtCompound.contains("BlockState")) {
                event.setCancelled(true);

                event.getItem().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onItemCreate(PlayerDropItemEvent event) {
        if (!event.getPlayer().hasPermission("evercraft.commands.gamemode.creative")) {
            ItemStack itemStack = CraftItemStack.asNMSCopy(event.getItemDrop().getItemStack());
            CompoundTag nbtCompound = itemStack.getOrCreateTag();

            if (nbtCompound.contains("BlockTag") || nbtCompound.contains("EntityTag") || nbtCompound.contains("BlockState")) {
                event.setCancelled(true);

                event.getItemDrop().remove();
            }
        }
    }

    @EventHandler
    public void onItemCreate(InventoryPickupItemEvent event) {
        if (!(event.getInventory().getHolder() instanceof Player && ((Player) event.getInventory().getHolder()).hasPermission("evercraft.commands.gamemode.creative"))) {
            ItemStack itemStack = CraftItemStack.asNMSCopy(event.getItem().getItemStack());
            CompoundTag nbtCompound = itemStack.getOrCreateTag();

            if (nbtCompound.contains("BlockTag") || nbtCompound.contains("EntityTag") || nbtCompound.contains("BlockState")) {
                event.setCancelled(true);

                event.getItem().remove();
            }
        }
    }

    @EventHandler
    public void onItemCreate(PlayerItemHeldEvent event) {
        if (!event.getPlayer().hasPermission("evercraft.commands.gamemode.creative")) {
            ItemStack itemStack = CraftItemStack.asNMSCopy(event.getPlayer().getInventory().getItem(event.getNewSlot()));
            CompoundTag nbtCompound = itemStack.getOrCreateTag();

            if (nbtCompound.contains("BlockTag") || nbtCompound.contains("EntityTag") || nbtCompound.contains("BlockState")) {
                event.setCancelled(true);

                event.getPlayer().getInventory().setItem(event.getNewSlot(), null);
            }
        }
    }

    @EventHandler
    public void onItemCreate(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && event.getItem() != null && !event.getPlayer().hasPermission("evercraft.commands.gamemode.creative")) {
            ItemStack itemStack = CraftItemStack.asNMSCopy(event.getItem());
            CompoundTag nbtCompound = itemStack.getOrCreateTag();

            if (nbtCompound.contains("BlockTag") || nbtCompound.contains("EntityTag") || nbtCompound.contains("BlockState")) {
                event.getItem().setType(Material.AIR);

                event.setCancelled(true);
            }
        }
    }
}