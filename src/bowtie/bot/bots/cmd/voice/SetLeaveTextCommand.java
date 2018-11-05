package bowtie.bot.bots.cmd.voice;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class SetLeaveTextCommand extends Command
{
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public SetLeaveTextCommand(String[] validExpressions, int permission, Main main)
    {
        super(validExpressions, permission);
        this.main = main;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        String text = event.getMessage().getContent().toLowerCase()
                .replace(event.getGuildObject().getPrefix() + event.getCommand(), "").trim();

        if (text.length() > 300)
        {
            this.main.getBot().sendMessage("The text can't be longer than 300 characters.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        if (text.isEmpty())
        {
            this.main.getBot().sendMessage("The text can't be empty.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        main.getDatabase().setLeaveText(event.getGuild().getStringID(), text);

        this.main.getBot().sendMessage("The new text will be played when someone leaves the "
                + "voicechannel that the bot is in.",
                event.getMessage().getChannel(), Colors.GREEN);
    }

    /**
     * @see bowt.cmnd.Command#getHelp(bowt.guild.GuildObject)
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Set Custom Voice Leave Text Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#voice");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Sets a custom text that the bot will say if someone \n"
                + "|  leaves its voicechannel.\n"
                + "|  \n"
                + "|  Special formatting can be used to randomize the text a bit further:\n"
                + "|  \n"
                + "|  You can add texts between '<' and '>', separated by '|'. The bot will \n"
                + "|  choose on of those texts randomly.\n"
                + "|  \n"
                + "|  For example:\n"
                + "|  \n"
                + "|  Goodbye you < monkey | rat | bird > .\n"
                + "|  \n"
                + "|  If the bot chooses that line it will randomly select either 'monkey', \n"
                + "|  'rat' or 'bird' and insert it instead of the bracket block. The final \n"
                + "|  line could look like this:\n"
                + "|  \n"
                + "|  Goodbye you monkey.\n"
                + "|  \n"
                + "|  Instead of having to put all of the different possibilities inside \n"
                + "|  those brackets you can also link lists, so that the bot will pick \n"
                + "|  one of the list members.\n"
                + "|  \n"
                + "|  You are a < list=animals > .\n"
                + "|  \n"
                + "|  In a similar way you can link groups. The bot will insert the name \n"
                + "|  of the picked user at the location of the brackets.\n"
                + "|  \n"
                + "|  < group=groupname > is a < list=animals > .\n"
                + "|  \n"
                + "|  If you want to use the username of the user that left you have\n"
                + "|  to insert < username > .\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "setleavetext < username > the < list=animals > left.\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "setleavetext Goodbye < username >.\n"
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