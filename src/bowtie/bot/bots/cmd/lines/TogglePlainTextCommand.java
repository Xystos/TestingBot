package bowtie.bot.bots.cmd.lines;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;
import bowtie.core.db.DatabaseAccess;

/**
 * @author &#8904
 *
 */
public class TogglePlainTextCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public TogglePlainTextCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission);
        this.bot = bot;
        this.main = main;
    }

    public TogglePlainTextCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @see bowt.cmnd.Command#execute(bowt.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        int global = this.main.getDatabase().isPlainTextActive(event.getGuildObject());
        if (global == DatabaseAccess.PLAIN)
        {
            this.main.getDatabase().setPlainTextActive(event.getGuildObject(), false);
            this.bot.sendMessage("Disabled plain text for your server!", event.getMessage().getChannel(), Colors.ORANGE);
            Bot.log.print(this, "Disabled plain text on "
                    + "'" + event.getGuild().getName() + "' (" + event.getGuildObject().getStringID()
                    + ")");
        }
        else
        {
            this.main.getDatabase().setPlainTextActive(event.getGuildObject(), true);
            this.bot.sendMessage("Enabled plain text for your server!", event.getMessage().getChannel(), Colors.GREEN);
            Bot.log.print(this, "Enabled plain text on "
                    + "'" + event.getGuild().getName() + "' (" + event.getGuildObject().getStringID()
                    + ")");
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Toggle Plain Text 8Ball Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#8Ball");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will enable/disable plain \n"
                + "|  text messages instead of the fancy bot embed messages\n"
                + "|  for answers of the 8Ball command.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "toggleplain\n"
                        + "|", false);

        builder.appendField(
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                        + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
                "[This command needs " + UserPermissions.getPermissionString(this.getPermissionOverride(guild))
                        + " permissions](http://www.bowtiebots.xyz/master-system.html)", false);
        builder.withFooterText("Click the title to read about this command on the website.");
        return builder.build();
    }
}