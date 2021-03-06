package net.xilla.discordcore.core.command;

import net.xilla.core.library.manager.Manager;
import net.xilla.core.log.LogLevel;
import net.xilla.core.log.Logger;
import net.xilla.discordcore.DiscordCore;
import net.xilla.discordcore.core.command.flag.FlagManager;
import net.xilla.discordcore.core.command.handler.ConsoleUser;
import net.xilla.discordcore.core.command.permission.user.PermissionUser;
import net.xilla.discordcore.core.command.response.BaseCommandResponder;
import net.xilla.discordcore.core.command.response.CommandResponder;
import net.xilla.discordcore.core.command.response.CommandResponse;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandManager extends Manager<String, Command> {

    private ExecutorService executor;
    private CommandWorker commandWorker;
    private FlagManager flagManager;
    private CommandResponder responder;
    private String welcome;
    private boolean commandLine;
    private CommandPermissionError permissionError;
    private CommandRunCheck commandRunCheck;

    private Map<String, List<Command>> commandCache;

    private Map<String, Command> activatorCache;

    public CommandManager(String welcome, boolean commandLine) {
        super("Commands");
        this.commandCache = new ConcurrentHashMap<>();
        this.activatorCache = new ConcurrentHashMap<>();
        this.welcome = welcome;
        this.commandLine = commandLine;
        this.flagManager = new FlagManager();
        this.commandRunCheck = (data) -> true;
        this.permissionError = (args, data) -> new CommandResponse(data).setTitle("Error!").setDescription("You do not have permission for that command.");
    }

    public FlagManager getFlagManager() {
        return flagManager;
    }

    public CommandResponder getResponder() {
        return responder;
    }

    public void setResponder(CommandResponder responder) {
        this.responder = responder;
    }

    public void setCommandRunCheck(CommandRunCheck commandRunCheck) {
        this.commandRunCheck = commandRunCheck;
    }

    public void runRawCommandInput(String input, String inputType, PermissionUser user) {
        String commandInput = input.split(" ")[0].toLowerCase();
        String[] args = Arrays.copyOfRange(input.split(" "), 1, input.split(" ").length);

        Command command = activatorCache.get(commandInput);

        runCommand(new CommandData(commandInput, args, null, inputType, user));

        if(user instanceof ConsoleUser && command == null) {
            Logger.log(LogLevel.WARN, "Unknown command, type \"?\" for a list of available commands.", getClass());
        }
    }

    public void runCommand(CommandData data) {
        if(activatorCache.containsKey(data.getCommand().toLowerCase())) {
            Command basicCommand = activatorCache.get(data.getCommand().toLowerCase());

            if(!basicCommand.isConsoleSupported() && data.getUser() instanceof ConsoleUser) {
                return;
            }

            if(!DiscordCore.getInstance().getServerSettings().canRunCommand(data)) {
                return;
            }

            if(commandRunCheck.check(data)) {
                Callable<Object> callableTask = () -> {
                    ArrayList<CommandResponse> responses = basicCommand.run(data);

                    for (CommandResponse response : responses) {
                        getResponder().send(response);
                    }
                    return null;
                };
                executor.submit(callableTask);
            }
        }
    }

    public List<Command> getCommandsByModule(String module) {
        return commandCache.get(module);
    }

    public CommandWorker getCommandWorker() {
        return commandWorker;
    }

    public boolean isCommandLine() {
        return commandLine;
    }

    public Command getCommand(String key) {
        return get(key);
    }

    public CommandPermissionError getPermissionError() {
        return permissionError;
    }

    public void setPermissionError(CommandPermissionError permissionError) {
        this.permissionError = permissionError;
    }

    public void reload() {
        //super.reload();
        this.executor = Executors.newFixedThreadPool(10);
        this.commandWorker = new CommandWorker(welcome);
        this.responder = new BaseCommandResponder();
    }

    @Override
    public void load() {

    }

    @Override
    public void objectAdded(Command command) {
        if(!commandCache.containsKey(command.getModule())) {
            commandCache.put(command.getModule(), new Vector<>());
        }
        commandCache.get(command.getModule()).add(command);

        for(String activator : command.getActivators())
            activatorCache.put(activator.toLowerCase(), command);
    }

    @Override
    public void objectRemoved(Command command) {
        for(String activator : command.getActivators())
            activatorCache.remove(activator.toLowerCase());
    }
}
