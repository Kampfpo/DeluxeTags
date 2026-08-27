package me.clip.deluxetags.commands;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.tags.DeluxeTag;
import me.clip.deluxetags.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TagTabCompleter implements TabCompleter {
    private final DeluxeTags plugin;

    public TagTabCompleter(DeluxeTags instance) {
        plugin = instance;
    }

    private boolean hasAnyListPermission(CommandSender sender) {
        return sender.hasPermission(Permissions.LIST)
                || sender.hasPermission(Permissions.LIST_ALL)
                || sender.hasPermission(Permissions.LIST_PLAYER);
    }

    private List<String> getMatchingCompletions(String input, Collection<String> commands) {
        List<String> completions = new ArrayList<>();

        StringUtil.copyPartialMatches(input, commands, completions);

        return completions;
    }


    private List<String> getPlayerCompletions(String input) {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        if (input == null || input.isEmpty()) {
            return playerNames;
        }
        return getMatchingCompletions(input, playerNames);
    }

    private List<String> getPermittedTagCompletions(Player player, String input) {
        List<String> completions = new ArrayList<>();

        for (DeluxeTag tag : plugin.getTagsHandler().getAllTags()) {
            if (tag.hasPermissionToUse(player)) {
                completions.add(tag.getIdentifier());
            }
        }

        return getMatchingCompletions(input, completions);
    }

    private List<String> getListCompletions(Player player, String input, int argument) {
        if (argument != 1) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (player.hasPermission(Permissions.LIST_ALL)) {
            completions.add("all");
        }

        if (player.hasPermission(Permissions.LIST_PLAYER)) {
            completions.addAll(getPlayerCompletions(input));
        }

        return getMatchingCompletions(input, completions);
    }

    private List<String> getSelectCompletions(Player player, String input, int arguments) {
        if (arguments != 1) {
            return Collections.emptyList();
        }

        return getPermittedTagCompletions(player, input);
    }

    private List<String> getSetCompletions(String input, int arguments, String[] args) {
        if (arguments == 1) {
            return getPlayerCompletions(input);
        }

        if (arguments != 2) {
            return Collections.emptyList();
        }

        Player targetPlayer = Bukkit.getPlayer(args[1]);

        if (targetPlayer == null) {
            return Collections.emptyList();
        }

        return getPermittedTagCompletions(targetPlayer, input);
    }

    private List<String> getClearCompletions(String input, int arguments) {
        if (arguments != 1) {
            return Collections.emptyList();
        }

        return getPlayerCompletions(input);
    }

    private List<String> getTagCompletions(String input, int arguments) {
        if (arguments != 1) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        for (DeluxeTag tag : plugin.getTagsHandler().getAllTags()) {
            completions.add(tag.getIdentifier());
        }

        return getMatchingCompletions(input, completions);
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> commands = new ArrayList<>();

            commands.add("help");
            if (hasAnyListPermission(sender)) {
                commands.add("list");
            }
            if (sender.hasPermission(Permissions.SELECT)) {
                commands.add("select");
            }
            if (sender.hasPermission(Permissions.SET)) {
                commands.add("set");
            }
            if (sender.hasPermission(Permissions.CLEAR)) {
                commands.add("clear");
            }
            if (sender.hasPermission(Permissions.CREATE)) {
                commands.add("create");
            }
            if (sender.hasPermission(Permissions.DELETE)) {
                commands.add("delete");
            }
            if (sender.hasPermission(Permissions.SET_DESCRIPTION)) {
                commands.add("setdesc");
            }
            if (sender.hasPermission(Permissions.SET_ORDER)) {
                commands.add("setorder");
            }
            if (sender.hasPermission(Permissions.SET_DISPLAY)) {
                commands.add("setdisplay");
            }
            if (sender.hasPermission(Permissions.RELOAD)) {
                commands.add("reload");
            }
            if (sender.hasPermission(Permissions.VERSION)) {
                commands.add("version");
            }

            return getMatchingCompletions(args[0], commands);
        }

        int arguments = args.length - 1;
        String input = args[args.length - 1];

        if (args[0].equalsIgnoreCase("list") && hasAnyListPermission(sender)) {
            return getListCompletions(player, input, arguments);
        }
        if (args[0].equalsIgnoreCase("select") && sender.hasPermission(Permissions.SELECT)) {
            return getSelectCompletions(player, input, arguments);
        }
        if (args[0].equalsIgnoreCase("set") && sender.hasPermission(Permissions.SET)) {
            return getSetCompletions(input, arguments, args);
        }
        if (args[0].equalsIgnoreCase("clear") && sender.hasPermission(Permissions.CLEAR)) {
            return getClearCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("delete") && sender.hasPermission(Permissions.DELETE)) {
            return getTagCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("setdesc") && sender.hasPermission(Permissions.SET_DESCRIPTION)) {
            return getTagCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("setorder") && sender.hasPermission(Permissions.SET_ORDER)) {
            return getTagCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("setdisplay") && sender.hasPermission(Permissions.SET_DISPLAY)) {
            return getTagCompletions(input, arguments);
        }


        return Collections.emptyList();
    }
}