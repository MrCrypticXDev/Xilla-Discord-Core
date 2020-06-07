package net.xilla.discordcore;

import net.md_5.bungee.api.plugin.Plugin;
import net.xilla.discordcore.platform.Platform;
import net.xilla.discordcore.platform.cmd.BungeeCommand;

public class DiscordCoreBungee extends Plugin {

    @Override
    public void onEnable(){
        new DiscordCore(Platform.getPlatform.BUNGEE.name, getDataFolder().toString());
        getProxy().getPluginManager().registerCommand(this, new BungeeCommand());
    }

    @Override
    public void onDisable(){
        DiscordCore.getInstance().getLog().sendMessage(1, "This plugin is not able to be reloaded! If this is a reload it will continue as normal in the background until a full restart.");
    }

    private static DiscordCoreBungee instance;

    public static DiscordCoreBungee getInstance() {
        return instance;
    }
}