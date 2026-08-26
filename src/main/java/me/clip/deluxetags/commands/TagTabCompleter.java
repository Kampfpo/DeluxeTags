package me.clip.deluxetags.commands;

import me.clip.deluxetags.DeluxeTags;
import me.clip.deluxetags.tags.DeluxeTag;
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

    private List<String> getListCompletions(String input, int argument) {
        if (argument != 1) {
            return Collections.emptyList();
        }

        return getPlayerCompletions(input);
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
            if (sender.hasPermission("deluxetags.list")) {
                commands.add("list");
            }
            if (sender.hasPermission("deluxetags.select")) {
                commands.add("select");
            }
            if (sender.hasPermission("deluxetags.set")) {
                commands.add("set");
            }
            if (sender.hasPermission("deluxetags.clear")) {
                commands.add("clear");
            }
            if (sender.hasPermission("deluxetags.create")) {
                commands.add("create");
            }
            if (sender.hasPermission("deluxetags.delete")) {
                commands.add("delete");
            }
            if (sender.hasPermission("deluxetags.setdescription")) {
                commands.add("setdesc");
            }
            if (sender.hasPermission("deluxetags.setorder")) {
                commands.add("setorder");
            }
            if (sender.hasPermission("deluxetags.setdisplay")) {
                commands.add("setdisplay");
            }
            if (sender.hasPermission("deluxetags.reload")) {
                commands.add("reload");
            }
            if (sender.hasPermission("deluxetags.version")) {
                commands.add("version");
            }

            return getMatchingCompletions(args[0], commands);
        }

        int arguments = args.length - 1;
        String input = args[args.length - 1];

        if (args[0].equalsIgnoreCase("list") && sender.hasPermission("deluxetags.list")) {
            return getListCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("select") && sender.hasPermission("deluxetags.select")) {
            return getSelectCompletions(player, input, arguments);
        }
        if (args[0].equalsIgnoreCase("set") && sender.hasPermission("deluxetags.set")) {
            return getSetCompletions(input, arguments, args);
        }
        if (args[0].equalsIgnoreCase("clear") && sender.hasPermission("deluxetags.clear")) {
            return getClearCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("delete") && sender.hasPermission("deluxetags.delete")) {
            return getTagCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("setdesc") && sender.hasPermission("deluxetags.setdescription")) {
            return getTagCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("setorder") && sender.hasPermission("deluxetags.setorder")) {
            return getTagCompletions(input, arguments);
        }
        if (args[0].equalsIgnoreCase("setdisplay") && sender.hasPermission("deluxetags.setdisplay")) {
            return getTagCompletions(input, arguments);
        }


        return Collections.emptyList();
    }
}