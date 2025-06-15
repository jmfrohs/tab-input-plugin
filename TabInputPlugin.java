/*
 * MIT License
 *
 * Copyright (c) {2025} {jmfrohs}
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.jmfrohs.tabinputplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class TabInputPlugin extends JavaPlugin implements Listener {

  private Map<UUID, List<String>> playerTags = new HashMap();
  private Map<UUID, String> activeTag = new HashMap();
  private Map<UUID, Long> temporaryTags = new HashMap();
  private Map<String, String> groupTags = new HashMap();
  private Map<Player, Integer> attempts = new HashMap();
  private Map<Player, Integer> Material = new HashMap();
  private Map<Player, Integer> ghgCounter = new HashMap();

  public TabInputPlugin() {}

  public void onEnable() {
    this.getServer().getPluginManager().registerEvents(this, this);
    this.loadConfig();
    this.getLogger().info("TagPlugin has been enabled!");
  }

  public void onDisable() {
    this.getLogger().info("TagPlugin has been disabled!");
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    this.attempts.remove(player);
  }

  private void loadConfig() {
    this.saveDefaultConfig();
    FileConfiguration config = this.getConfig();
    if (config.contains("groups")) {
      for (String key : config.getConfigurationSection("groups").getKeys(false)) {
        this.groupTags.put(key, config.getString("groups." + key));
      }
    }
  }

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("tag")) {
      if (!(sender instanceof Player)) {
        sender.sendMessage("This command can only be used by players.");
        return true;
      } else {
        Player player = (Player) sender;
        if (args.length < 1) {
          this.sendUsage(player);
          return true;
        } else {
          switch (args[0].toLowerCase()) {
            case "add":
              return this.handleAddTag(player, args);
            case "remove":
              return this.handleRemoveTag(player, args);
            case "temp":
              return this.handleTempTag(player, args);
            default:
              this.sendUsage(player);
              return true;
          }
        }
      }
    } else {
      return false;
    }
  }

  private boolean handleAddTag(Player player, String[] args) {
    if (args.length < 3) {
      player.sendMessage(ChatColor.RED + "Usage: /tag add <color> <tag>");
      return true;
    } else {
      String colorName = args[1].toUpperCase();

      ChatColor color;
      try {
        color = ChatColor.valueOf(colorName);
      } catch (IllegalArgumentException var9) {
        player.sendMessage(ChatColor.RED + "Invalid color. Please use a valid color name.");
        return true;
      }

      StringBuilder tagBuilder = new StringBuilder();

      for (int i = 2; i < args.length; ++i) {
        tagBuilder.append(args[i]).append(" ");
      }

      String tag = tagBuilder.toString().trim();
      if (tag.length() > 5) {
        player.sendMessage(ChatColor.RED + "Tag cannot be longer than 5 characters.");
        return true;
      } else {
        this.addPlayerTag(player, color + tag);
        player.sendMessage(
            ChatColor.GREEN
                + "Tag added: "
                + color
                + ChatColor.GRAY
                + "["
                + color
                + tag
                + ChatColor.GRAY
                + "]");
        if (!this.attempts.containsKey(player)) {
          this.attempts.put(player, 0);
        }

        if (tag.equalsIgnoreCase("GHG")) {
          int count = (Integer) this.ghgCounter.getOrDefault(player, 0) + 1;
          this.ghgCounter.put(player, count);
          if (count % 5 == 0) {
            ItemStack allium = new ItemStack(org.bukkit.Material.ALLIUM);
            player.getInventory().addItem(new ItemStack[] {allium});
            player.sendMessage(ChatColor.GOLD + "#GHG");
          }
        }

        return true;
      }
    }
  }

  private Player getWorld() {
    return null;
  }

  private boolean handleRemoveTag(Player player, String[] args) {
    if (args.length < 2) {
      player.sendMessage(ChatColor.RED + "Usage: /tag remove <tag>");
      return true;
    } else {
      String tagToRemove = args[1];
      boolean removed = this.removePlayerTag(player, tagToRemove);
      if (removed) {
        player.sendMessage(ChatColor.GREEN + "Tag removed: " + tagToRemove);
      } else {
        player.sendMessage(ChatColor.RED + "Tag not found: " + tagToRemove);
      }

      return true;
    }
  }

  private boolean handleTempTag(final Player player, String[] args) {
    if (args.length < 4) {
      player.sendMessage(ChatColor.RED + "Usage: /tag temp <color> <tag> <duration>");
      return true;
    } else {
      String colorName = args[1].toUpperCase();

      ChatColor color;
      try {
        color = ChatColor.valueOf(colorName);
      } catch (IllegalArgumentException var10) {
        player.sendMessage(ChatColor.RED + "Invalid color. Please use a valid color name.");
        return true;
      }

      StringBuilder tagBuilder = new StringBuilder();

      for (int i = 2; i < args.length - 1; ++i) {
        tagBuilder.append(args[i]).append(" ");
      }

      final String tag = tagBuilder.toString().trim();

      int duration;
      try {
        duration = Integer.parseInt(args[args.length - 1]);
      } catch (NumberFormatException var9) {
        player.sendMessage(ChatColor.RED + "Invalid duration. Please use a number in seconds.");
        return true;
      }

      this.addPlayerTag(player, color + tag);
      player.sendMessage(
          ChatColor.GREEN
              + "Temporary tag added: "
              + ChatColor.GRAY
              + "["
              + color
              + tag
              + ChatColor.GRAY
              + "]"
              + ChatColor.GREEN
              + " for "
              + duration
              + "seconds.");
      this.temporaryTags.put(
          player.getUniqueId(), System.currentTimeMillis() + (long) (duration * 1000));
      (new BukkitRunnable() {
            public void run() {
              de.jmfrohs.tabinputplugin.TabInputPlugin.this.removePlayerTag(player, tag);
              player.sendMessage(ChatColor.YELLOW + "Your temporary tag has expired.");
            }
          })
          .runTaskLater(this, (long) (duration * 20));
      return true;
    }
  }

  private void sendUsage(Player player) {
    player.sendMessage(ChatColor.YELLOW + "Tag Plugin Usage:");
    player.sendMessage(ChatColor.YELLOW + "/tag add <color> <tag> - Add a new tag");
    player.sendMessage(ChatColor.YELLOW + "/tag remove <tag> - Remove a tag");
    player.sendMessage(
        ChatColor.YELLOW + "/tag temp <color> <tag> <duration> - Add a temporary tag");
  }

  private void addPlayerTag(Player player, String tag) {
    UUID playerId = player.getUniqueId();
    List<String> tags = this.playerTags.computeIfAbsent(playerId, k -> new ArrayList<>());
    if (!tags.isEmpty()) {
      tags.set(tags.size() - 1, tag);
    } else {
      tags.add(tag);
    }
    this.activeTag.put(playerId, tag);

    this.updatePlayerTag(player);
  }

  private boolean removePlayerTag(Player player, String tag) {
    UUID playerId = player.getUniqueId();
    List<String> tags = (List) this.playerTags.getOrDefault(playerId, new ArrayList());
    boolean removed = tags.removeIf((t) -> ChatColor.stripColor(t).equalsIgnoreCase(tag));
    if (removed) {
      this.playerTags.put(playerId, tags);
      if (this.activeTag.get(playerId) != null
          && ChatColor.stripColor((String) this.activeTag.get(playerId)).equalsIgnoreCase(tag)) {
        this.activeTag.put(playerId, tags.isEmpty() ? null : (String) tags.get(0));
      }

      this.updatePlayerTag(player);
    }

    return removed;
  }

  private void updatePlayerTag(Player player) {
    UUID playerId = player.getUniqueId();
    String tag = this.activeTag.get(playerId);
    if (tag != null) {
      String nameWithTag =
          player.getName() + " " + ChatColor.GRAY + "[" + tag + ChatColor.GRAY + "]";
      player.setPlayerListName(nameWithTag);
    } else {
      player.setPlayerListName(player.getName());
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    this.updatePlayerTag(player);

    for (Map.Entry<String, String> entry : this.groupTags.entrySet()) {
      if (player.hasPermission("group." + (String) entry.getKey())) {
        this.addPlayerTag(player, (String) entry.getValue());
        player.sendMessage(
            ChatColor.GREEN + "You received a group tag: " + (String) entry.getValue());
      }
    }
  }
}