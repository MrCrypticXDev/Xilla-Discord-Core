package net.xilla.discordcore.library.form.form.reaction;

import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.xilla.discordcore.library.form.form.Form;

public interface FormReactionEvent {

    boolean runEvent(Form form, GuildMessageReactionAddEvent event);

}
