package bowtie.bot.bots.cmd.misc;

import java.util.List;

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
public class SetPrefixCommand extends Command
{
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public SetPrefixCommand(String[] validExpressions, int permission, Main main)
    {
        super(validExpressions, permission, true);
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public SetPrefixCommand(List<String> validExpressions, int permission, Main main)
    {
        super(validExpressions, permission, true);
        this.main = main;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        String[] parts = event.getMessage().getContent().split(" ");

        if (parts.length < 2)
        {
            main.getBot().sendMessage("You have to add the new prefix after the command.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        String prefix = parts[1].trim();

        if (prefix.length() > 10)
        {
            main.getBot().sendMessage("The prefix may not be longer than 10 characters.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        main.getDatabase().addPrefix(event.getGuildObject().getStringID(), prefix);

        event.getGuildObject().setPrefix(prefix);

        main.getBot().sendMessage("The command prefix is now `" + prefix + "`.",
                event.getMessage().getChannel(), Colors.GREEN);
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Change Prefix Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#prefix");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command lets you change the command \n"
                + "|  prefix for your server.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "setprefix newPrefix\n"
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