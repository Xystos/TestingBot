package bowtie.bot.bots.cmd.lines;

import java.awt.Color;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;
import bowtie.core.db.DatabaseAccess;

/**
 * @author &#8904
 *
 */
public class DeleteLineIndexCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public DeleteLineIndexCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission);
        this.bot = bot;
        this.main = main;
    }

    public DeleteLineIndexCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        try
        {
            int index = Integer.parseInt(event.getMessage().getContent()
                    .replace(event.getGuildObject().getPrefix() + "delindex", "")
                    .trim());
            String line = this.main.getDatabase().getLines(event.getGuildObject()).get(index - 1);
            int result = this.main.getDatabase().removeLine(line, event.getGuildObject());
            if (result == DatabaseAccess.SUCCESS)
            {
                this.bot.sendMessage("The line '" + line + "' was removed from the list of available answers.",
                        event.getChannel(), new Color(0, 255, 0));
                Bot.log.print(this, "The line '" + line + "' was removed from the list of available answers of "
                        + "'" + event.getGuild().getName() + "' ("
                        + event.getGuildObject().getStringID() + ")");
            }
            else
            {
                this.bot.sendMessage("The line '" + line + "' is not in the list of available answers.",
                        event.getChannel(), new Color(255, 0, 0));
            }
        }
        catch (Exception e)
        {
            return;
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Delete Line by Index Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#8Ball");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will delete the custom line\n"
                + "|  with the given index from the 8Ball command.\n"
                + "|  \n"
                + "|  You can see the index of every line with '" + guild.getPrefix() + "showlines'.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "delindex 2\n"
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