package bowtie.bot.bots.cmd.lines;

import java.awt.Color;
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
public class AddLineCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public AddLineCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission);
        this.bot = bot;
        this.main = main;
    }

    public AddLineCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        String line = event.getFixedContent().replace(event.getGuildObject().getPrefix() + event.getCommand(), "")
                .trim();
        if (line.length() >= 200)
        {
            this.bot.sendMessage("The line is too long. Custom answers may not be longer than 200 characters.",
                    event.getChannel(), new Color(255, 0, 0));
            return;
        }
        else if (line.length() == 0)
        {
            this.bot.sendMessage("The line is empty.", event.getChannel(), Colors.RED);
            return;
        }
        int result = this.main.getDatabase().addLine(line, event.getGuildObject());
        if (result == DatabaseAccess.SUCCESS)
        {
            this.bot.sendMessage("The line '" + line + "' was added to the list of available answers.",
                    event.getChannel(), new Color(0, 255, 0));
            Bot.log.print(this, "The line '" + line + "' was added to the list of available answers of "
                    + "'" + event.getGuild().getName() + "' (" + event.getGuildObject().getStringID()
                    + ")");
        }
        else
        {
            this.bot.sendMessage("The line '" + line + "' is already in the list of available answers.",
                    event.getChannel(), new Color(255, 0, 0));
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Add Line Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#8Ball");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Adds the text after the command as a custom line to the \n"
                + "|  8Ball command.\n"
                + "|  \n"
                + "|  Special formatting can be used to randomize the lines a bit further:\n"
                + "|  \n"
                + "|  You can add texts between '<' and '>', separated by '|'. The bot will \n"
                + "|  choose on of those texts randomly if it selects that line.\n"
                + "|  \n"
                + "|  For example:\n"
                + "|  \n"
                + "|  You are a < monkey | rat | bird > .\n"
                + "|  \n"
                + "|  If the bot chooses that line it will randomly select either 'monkey', \n"
                + "|  'rat' or 'bird' and insert it instead of the bracket block. The final \n"
                + "|  line could look like this:\n"
                + "|  \n"
                + "|  You are a monkey.\n"
                + "|  \n"
                + "|  Instead of having to put all of the different possibilities inside \n"
                + "|  those brackets you can also link lists, so that the bot will pick \n"
                + "|  one of the list members.\n"
                + "|  \n"
                + "|  You are a < list=animals > .\n"
                + "|  \n"
                + "|  In a similar way you can link groups. The bot will mention the picked \n"
                + "|  user at the location of th brackets.\n"
                + "|  \n"
                + "|  < group=groupname > is a < list=animals > .\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "addline This is the new line.\n"
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