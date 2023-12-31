package me.cubecrafter.xutils;

import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.messages.Titles;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    /**
     * Colorize the given string
     *
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String color(String text) {
        if (ReflectionUtil.VERSION >= 16) {
            Matcher matcher = HEX_PATTERN.matcher(text);
            while (matcher.find()) {
                String color = matcher.group();
                text = text.replace(color, ChatColor.of(color).toString());
                matcher = HEX_PATTERN.matcher(text);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Colorize the given list of strings
     *
     * @param text The list of strings to colorize
     * @return The colorized list of strings
     */
    public static List<String> color(List<String> text) {
        return text.stream().map(TextUtil::color).collect(Collectors.toList());
    }

    /**
     * Send a message to a player
     *
     * @param sender The player to send the message to
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        message = parsePlaceholders(sender instanceof Player ? (Player) sender : null, message);
        sender.sendMessage(color(message));
    }

    /**
     * Send a messages to a list of players
     *
     * @param senders The players to send the messages to
     * @param message The messages to send
     */
    public static void sendMessage(List<CommandSender> senders, String message) {
        senders.forEach(sender -> sendMessage(sender, message));
    }

    /**
     * Send a list of messages to a player
     *
     * @param sender The player to send the message to
     * @param messages The messages to send
     */
    public static void sendMessages(CommandSender sender, List<String> messages) {
        messages.forEach(message -> sendMessage(sender, message));
    }

    /**
     * Send a list of messages to a list of players
     *
     * @param senders The players to send the messages to
     * @param messages The messages to send
     */
    public static void sendMessages(List<CommandSender> senders, List<String> messages) {
        senders.forEach(sender -> sendMessages(sender, messages));
    }

    public static void broadcast(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> sendMessage(player, message));
    }

    public static void broadcast(List<String> messages) {
        Bukkit.getOnlinePlayers().forEach(player -> sendMessages(player, messages));
    }

    /**
     * Parse PlaceholderAPI placeholders
     *
     * @param player The player to parse the placeholders for
     * @param text The text to parse
     * @return The parsed text
     */
    public static String parsePlaceholders(OfflinePlayer player, String text) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    /**
     * Capitalize the given string
     *
     * @param text The text to capitalize
     * @return The capitalized text
     */
    public static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Parse PlaceholderAPI placeholders in the item's display name and lore
     *
     * @param player The player to parse the placeholders for
     * @param item The item to parse the placeholders in
     * @return The item with parsed placeholders
     */
    public static ItemStack parsePlaceholders(OfflinePlayer player, ItemStack item) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                meta.setDisplayName(PlaceholderAPI.setPlaceholders(player, meta.getDisplayName()));
            }
            if (meta.hasLore()) {
                meta.setLore(PlaceholderAPI.setPlaceholders(player, meta.getLore()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static String getCurrentDate(String format) {
        return getDate(format, Instant.now());
    }

    public static String getDate(String format, Instant instant) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter);
    }

    public void info(String message) {
        XUtils.getPlugin().getLogger().info(message);
    }

    public void warn(String message) {
        XUtils.getPlugin().getLogger().warning(message);
    }

    public void error(String message) {
        XUtils.getPlugin().getLogger().severe(message);
    }

    public String stripColor(String text) {
        return ChatColor.stripColor(color(text));
    }

    public String fromLocation(Location location) {
        StringBuilder builder = new StringBuilder();
        builder.append(location.getWorld().getName()).append(":").append(location.getX()).append(":").append(location.getY()).append(":").append(location.getZ());
        if (location.getYaw() != 0 && location.getPitch() != 0) {
            builder.append(":").append(location.getYaw()).append(":").append(location.getPitch());
        }
        return builder.toString();
    }

    public Location parseLocation(String location) {
        String[] split = location.split(":");
        if (split.length == 4) {
            return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
        }
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (ReflectionUtil.supports(11)) {
            player.sendTitle(color(title), color(subtitle), fadeIn, stay, fadeOut);
        } else {
            Titles.sendTitle(player, fadeIn, stay, fadeOut, color(title), color(subtitle));
        }
    }

    public void sendActionBar(Player player, String message) {
        ActionBar.sendActionBar(player, color(message));
    }

    public void sendActionBarWhile(Player player, String message, Callable<Boolean> condition) {
        ActionBar.sendActionBarWhile(XUtils.getPlugin(), player, color(message), condition);
    }

}
