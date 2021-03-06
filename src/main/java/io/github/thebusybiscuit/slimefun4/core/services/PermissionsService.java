package io.github.thebusybiscuit.slimefun4.core.services;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import io.github.thebusybiscuit.cscorelib2.config.Config;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;

/**
 * This Service is responsible for handling the {@link Permission} of a
 * {@link SlimefunItem}.
 * 
 * You can set up these {@link Permission} nodes inside the {@code permissions.yml} file.
 * 
 * @author TheBusyBiscuit
 *
 */
public class PermissionsService {

    private final Map<String, String> permissions = new HashMap<>();
    private final Config config;

    public PermissionsService(SlimefunPlugin plugin) {
        config = new Config(plugin, "permissions.yml");
        config.getConfiguration().options().header("This file is used to assign permission nodes to items from Slimefun or any of its addons.\nTo assign an item a certain permission node you simply have to set the 'permission' attribute\nto your desired permission node. You can also customize the text that is displayed when a Player does not have that permission.");
        config.getConfiguration().options().copyHeader(true);
    }

    public void register(Iterable<SlimefunItem> items) {
        for (SlimefunItem item : items) {
            if (item != null && item.getID() != null && !migrate(item)) {
                config.setDefaultValue(item.getID() + ".permission", "none");
                config.setDefaultValue(item.getID() + ".lore", new String[] { "&rYou do not have the permission", "&rto access this item." });
                permissions.put(item.getID(), config.getString(item.getID() + ".permission"));
            }
        }

        config.save();
    }

    // Temporary migration method for the old system
    private boolean migrate(SlimefunItem item) {
        String permission = SlimefunPlugin.getItemCfg().getString(item.getID() + ".required-permission");

        if (permission != null) {
            config.setDefaultValue(item.getID() + ".permission", permission.length() == 0 ? "none" : permission);
            config.setDefaultValue(item.getID() + ".lore", SlimefunPlugin.getItemCfg().getString(item.getID() + ".no-permission-tooltip"));
            permissions.put(item.getID(), config.getString(item.getID() + ".permission"));

            SlimefunPlugin.getItemCfg().setValue(item.getID() + ".required-permission", null);
            SlimefunPlugin.getItemCfg().setValue(item.getID() + ".no-permission-tooltip", null);
            return true;
        }

        return false;
    }

    /**
     * This method checks whether the given {@link Permissible} has the {@link Permission}
     * to access the given {@link SlimefunItem}.
     * 
     * @param p
     *            The {@link Permissible} to check
     * @param item
     *            The {@link SlimefunItem} in question
     * 
     * @return Whether the {@link Permissible} has the required {@link Permission}
     */
    public boolean hasPermission(Permissible p, SlimefunItem item) {
        if (item == null) {
            // Failsafe
            return true;
        }

        String permission = permissions.get(item.getID());
        return permission == null || permission.equals("none") || p.hasPermission(permission);
    }

    /**
     * This method sets the {@link Permission} for a given {@link SlimefunItem}.
     * 
     * @param item
     *            The {@link SlimefunItem} to modify
     * @param permission
     *            The {@link Permission} to set
     */
    public void setPermission(SlimefunItem item, String permission) {
        permissions.put(item.getID(), permission != null ? permission : "none");
    }

    /**
     * This saves every configured {@link Permission} to the permissions {@link File}.
     */
    public void save() {
        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            config.setValue(entry.getKey() + ".permission", entry.getValue());
        }

        config.save();
    }

    public List<String> getLore(SlimefunItem item) {
        List<String> lore = config.getStringList(item.getID() + ".lore");
        return lore == null ? Arrays.asList("LORE NOT FOUND") : lore;
    }

}
