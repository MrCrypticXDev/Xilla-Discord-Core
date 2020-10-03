package net.xilla.discordcore.command.cmd;

import net.dv8tion.jda.api.EmbedBuilder;
import net.xilla.core.library.manager.ManagerObject;
import net.xilla.discordcore.CoreObject;
import net.xilla.discordcore.DiscordCore;
import net.xilla.discordcore.command.CommandBuilder;
import net.xilla.discordcore.command.response.CoreCommandResponse;
import net.xilla.discordcore.core.command.Command;
import net.xilla.discordcore.core.command.CommandExecutor;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class HelpCommand extends CoreObject {

    public HelpCommand() {
        CommandBuilder commandBuilder = new CommandBuilder("Core", "Help");
        commandBuilder.setDescription("View the commands you can access.");
        commandBuilder.setCommandExecutor((data) -> {
            try {
            HashMap<String, ArrayList<String>> commands = new HashMap<>();

            for(ManagerObject object : new ArrayList<>(DiscordCore.getInstance().getCommandManager().getData().values())) {
                Command legacyCommand = (Command) object;
                if (getCommandSettings().isCommand(data, legacyCommand.getName())) {
                    String finalName = legacyCommand.getModule().substring(0, 1).toUpperCase() + legacyCommand.getModule().substring(1).toLowerCase();

                    if (data.getInputType().equalsIgnoreCase("commandline")) {
                        if (legacyCommand.isConsoleSupported()) {
                            if (!commands.containsKey(finalName)) {
                                commands.put(finalName, new ArrayList<>());
                            }

                            if (legacyCommand.getPermission() == null || data.getUser().hasPermission(legacyCommand.getPermission())) {
                                commands.get(finalName).add(getCoreSetting().getCommandPrefix() + legacyCommand.getUsage() + " - " + legacyCommand.getDescription());
                            }
                        }
                    } else {
                        if (!commands.containsKey(finalName)) {
                            commands.put(finalName, new ArrayList<>());
                        }

                        if (legacyCommand.getPermission() == null || data.getUser().hasPermission(legacyCommand.getPermission())) {
                            commands.get(finalName).add(getCoreSetting().getCommandPrefix() + legacyCommand.getUsage() + " - " + legacyCommand.getDescription());
                        }
                    }
                }
            }

            StringBuilder description = new StringBuilder();

            if(data.getArgs().length == 0) {
                int loop = 0;
                for (String module : commands.keySet()) {
                    loop++;
                    ArrayList<String> lines = commands.get(module);

                    if (lines.size() > 0) {
                        if (!getCoreSetting().isMinimizeHelp() && data.getArgs().length == 0) {

                            description.append("**").append(module).append(" Commands**\n");
                            for (String line : lines) {
                                description.append(line).append("\n");
                            }

                            if (loop != commands.size()) {
                                description.append("\n");
                            }
                        } else {
                            description.append("> ").append(module).append("\n");
                        }
                    }
                }
                if(getCoreSetting().isMinimizeHelp()) {
                    description.append("\n**").append(getCoreSetting().getCommandPrefix()).append("help <Category>**");
                }
            } else {
                StringBuilder category = new StringBuilder();

                for(int i = 0; i < data.getArgs().length; i++) {
                    category.append(data.getArgs()[i]);
                    if(i != data.getArgs().length - 1) {
                        category.append(" ");
                    }
                }
                String finalCategory = category.toString().toLowerCase();
                finalCategory = finalCategory.substring(0, 1).toUpperCase() + finalCategory.substring(1);
                if(commands.containsKey(finalCategory)) {
                    ArrayList<String> lines = commands.get(finalCategory);

                    description.append("**").append(finalCategory).append(" Commands**\n");
                    for (String line : lines) {
                        description.append(line).append("\n");
                    }

                } else {
                    description.append("That is not a valid category!" + finalCategory);
                }
            }

            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Help");
            embedBuilder.setDescription(description.toString());
            embedBuilder.setColor(Color.decode(getCoreSetting().getEmbedColor()));

            return new CoreCommandResponse(data).setEmbed(embedBuilder.build());

            } catch (Exception ex) {
                ex.printStackTrace();
                return new CoreCommandResponse(data).setDescription("Error" + ex.getMessage());
            }
        });

        commandBuilder.build();
    }

}
