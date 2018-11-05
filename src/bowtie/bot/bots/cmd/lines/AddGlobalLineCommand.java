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
public class AddGlobalLineCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public AddGlobalLineCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission);
        this.bot = bot;
        this.main = main;
    }

    public AddGlobalLineCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        int result = this.main.getDatabase().addLine(line, "-1");
        if (result == DatabaseAccess.SUCCESS)
        {
            this.bot.sendMessage("The line '" + line + "' was added to the list of global answers.",
                    event.getChannel(), new Color(0, 255, 0));
        }
        else
        {
            this.bot.sendMessage("The line '" + line + "' is already in the list of global answers.",
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

        builder.withAuthorName("Add Global Line Command");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Soon\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "addgloballine Text\n"
                        + "|", false);

        builder.appendField(
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                        + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
                "[This command needs " + UserPermissions.getPermissionString(this.getPermissionOverride(guild))
                        + " permissions](http://www.bowtiebots.xyz/master-system.html)", false);
        return builder.build();
    }
}