package com.akon.freload;

import com.shampaggon.crackshot.CSDirector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class FReload extends JavaPlugin {

	private CSDirector csInstance;

	@Override
	public void onEnable() {
		this.csInstance = (CSDirector)Bukkit.getPluginManager().getPlugin("CrackShot");
		if (this.csInstance == null) {
			return;
		}
		HandlerList handlers = PlayerDropItemEvent.getHandlerList();
		Arrays.stream(handlers.getRegisteredListeners()).filter(registeredListener -> registeredListener.getPlugin() == this.csInstance).forEach(handlers::unregister);
		Bukkit.getPluginManager().registerEvents(new Listener() {

			@EventHandler
			public void onSwapItems(PlayerSwapHandItemsEvent e) {
				ItemStack item = e.getOffHandItem();
				if (item == null) {
					return;
				}
				Player player = e.getPlayer();
				String[] parentNodes = FReload.this.csInstance.itemParentNode(item, player);
				if (parentNodes == null || !FReload.this.csInstance.getBoolean(parentNodes[0] + ".Reload.Enable") || FReload.this.csInstance.getBoolean(parentNodes[0] + ".Reload.Reload_With_Mouse")) {
					return;
				}
				if (player.hasMetadata("dr0p_authorised")) {
					return;
				}
				e.setCancelled(true);
				FReload.this.csInstance.delayedReload(player, parentNodes[0]);
			}

		}, this);
	}

	@Override
	public void onDisable() {
		if (this.csInstance == null) {
			return;
		}
		PlayerDropItemEvent.getHandlerList().register(new RegisteredListener(this.csInstance, (listener, event) -> {
			if (listener instanceof CSDirector && event instanceof PlayerDropItemEvent) {
				((CSDirector)listener).onGunThrow((PlayerDropItemEvent)event);
			}
		}, EventPriority.HIGHEST, this.csInstance, false));
	}
}
