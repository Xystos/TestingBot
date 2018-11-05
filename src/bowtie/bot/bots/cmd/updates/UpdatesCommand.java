package bowtie.bot.bots.cmd.updates;

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
public class UpdatesCommand extends Command
{
    public static final String INFO = "info";
    public static final String FEATURE = "feature";
    public static final String DOWNTIME = "downtime";
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public UpdatesCommand(String[] validExpressions, int permission, Main main)
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
        boolean info = event.getFixedContent().toLowerCase().contains(INFO);
        boolean feature = event.getFixedContent().toLowerCase().contains(FEATURE);
        boolean downtime = event.getFixedContent().toLowerCase().contains(DOWNTIME);

        String updates = "";
        boolean anyUpdates = false;

        if (info)
        {
            main.getDatabase().setUpdateChannel(event.getChannel().getStringID(), INFO);
            updates += INFO + "\n";
            anyUpdates = true;
        }
        else
        {
            main.getDatabase().resetUpdateChannel(event.getChannel().getStringID(), INFO);
        }

        if (feature)
        {
            main.getDatabase().setUpdateChannel(event.getChannel().getStringID(), FEATURE);
            updates += FEATURE + "\n";
            anyUpdates = true;
        }
        else
        {
            main.getDatabase().resetUpdateChannel(event.getChannel().getStringID(), FEATURE);
        }

        if (downtime)
        {
            main.getDatabase().setUpdateChannel(event.getChannel().getStringID(), DOWNTIME);
            updates += DOWNTIME + "\n";
            anyUpdates = true;
        }
        else
        {
            main.getDatabase().resetUpdateChannel(event.getChannel().getStringID(), DOWNTIME);
        }

        if (anyUpdates)
        {
            main.getBot().sendMessage("The following updates will be sent to this channel:\n\n" + updates,
                    event.getChannel());
        }
        else
        {
            main.getBot().sendMessage("Please add the updates that you wish to receive in this channel.\n\n"
                    + "Valid updates are: \n\n"
                    + "'" + INFO + "' for general information such as giveaways\n"
                    + "'" + FEATURE + "' for the announcement of new features\n"
                    + "'" + DOWNTIME + "' for the announcement and/or explanaition of downtimes\n\n"
                    + "Just add the name/s of the desired announcement/s after the command.",
                    event.getChannel(), Colors.RED);
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp(bowt.guild.GuildObject)
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Set Update Channel Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#updates");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Lets you receive specififc updates about the bot \n"
                + "|  in this channel.\n"
                + "|  \n"
                + "|  There are 3 different update categroies:\n"
                + "|  \n"
                + "|  - info\n"
                + "|     For general information such as giveaways\n"
                + "|  \n"
                + "|  - feature\n"
                + "|     For the announcement of new or changed features\n"
                + "|  \n"
                + "|  - downtime\n"
                + "|     For the announcement and/or explanaition of downtimes\n"
                + "|  \n"
                + "|  The channel that you use this command in will only receive \n"
                + "|  the given categories. All others will be disabled.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "updates info feature downtime \n"
                        + "|  " + guild.getPrefix() + "updates info downtime \n"
                        + "|  " + guild.getPrefix() + "updates downtime \n"
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