package bowtie.bot.bots.cmd.misc;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.cmnd.Command;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class GetPrefixCommand extends Command
{
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public GetPrefixCommand(String[] validExpressions, int permission, Main main)
    {
        super(validExpressions, permission, true);
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public GetPrefixCommand(List<String> validExpressions, int permission, Main main)
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
        String prefix = event.getGuildObject().getPrefix();

        main.getBot().sendMessage("The current prefix for this server is `" + prefix + "`.\n\n"
                + "You can change it with the '" + event.getGuildObject().getPrefix() + "setprefix' command.",
                event.getMessage().getChannel());
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Get Prefix Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#prefix");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Shows the current prefix for this server.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "prefix\n"
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