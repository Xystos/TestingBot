package bowtie.bot.hand;

import bowt.bot.Bot;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;

/**
 * Handles {@link GuildCreateEvent}s which is received when the bot is added to a new guild.
 * 
 * @author &#8904
 */
public class NewGuildHandler implements IListener<GuildCreateEvent>
{
    private Bot bot;
    private Main main;

    /**
     * Creates a new instance for the given bot.
     * 
     * @param bot
     */
    public NewGuildHandler(Bot bot, Main main)
    {
        this.bot = bot;
        this.main = main;
    }

    /**
     * Creates the {@link GuildObject} and registeres the guild owner as the first master.
     * 
     * @see sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.events.Event)
     */
    @Override
    public void handle(GuildCreateEvent event)
    {
        if (this.bot.isReady())
        { // to make sure its a new guild. else this happens after every boot
            GuildObject guild = new GuildObject(event.getGuild());
            guild.setCommandHandler(Main.commandHandler);
            // BotListAPI.updateServerCount(bot.getClient().getGuilds().size());
            if (this.bot.addGuildObject(guild))
            {
                main.getDatabase().addMaster(guild.getGuild().getOwner().getStringID(), guild.getStringID(),
                        UserPermissions.OWNER);
                guild.addOwner(guild.getGuild().getOwner());
                this.main.getDatabase().setGlobalLinesActive(guild, true);
                this.main.getDatabase().setPlainTextActive(guild, false);
                Bot.log.print(this, "Bot was added to '" + event.getGuild().getName() + "' ("
                        + event.getGuild().getStringID() + ")");
            }
        }
    }
}