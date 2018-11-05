package bowtie.bot.bots.cmd.updates;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;
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
public class SendUpdateCommand extends Command
{
    public static final String INFO_UPDATE = "New Informational Update";
    public static final String FEATURE_UPDATE = "New Feature Update";
    public static final String DOWNTIME_UPDATE = "New Downtime Update";

    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public SendUpdateCommand(String[] validExpressions, int permission, Main main)
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
        String type = event.getParameter("type");

        if (type == null)
        {
            main.getBot().sendMessage("Please add the 'type' parameter.", event.getChannel(), Colors.RED);
            return;
        }

        type = type.toLowerCase();

        String description = event.getParameter("description");
        String title = event.getParameter("title");

        if (title == null)
        {
            switch (type)
            {
            case UpdatesCommand.INFO:
                title = INFO_UPDATE;
                break;

            case UpdatesCommand.FEATURE:
                title = FEATURE_UPDATE;
                break;

            case UpdatesCommand.DOWNTIME:
                title = DOWNTIME_UPDATE;
                break;
            }
        }

        String image = event.getParameter("image");

        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Colors.DEFAULT);
        builder.withAuthorName(title);
        builder.withFooterText("Use the '" + event.getGuildObject().getPrefix()
                + "resetupdates' command to reset this channels update settings.");

        if (description != null)
        {
            builder.withDescription(description);
        }

        if (image != null)
        {
            builder.withImage(image);
        }

        int num = 1;

        while (true)
        {
            String fieldTitle = event.getParameter("head" + num);
            String fieldText = event.getParameter("text" + num);

            if (fieldTitle != null && fieldText != null)
            {
                builder.appendField(fieldTitle, fieldText, false);
                num ++ ;
            }
            else
            {
                break;
            }
        }
        EmbedObject embed = builder.build();
        List<String> channelIDs = main.getDatabase().getUpdateChannels(type);

        for (String id : channelIDs)
        {
            IChannel channel = RequestBuffer.request(new IRequest<IChannel>()
            {
                @Override
                public IChannel request()
                {
                    return main.getBot().getClient().getChannelByID(Long.parseLong(id));
                }
            }).get();

            if (channel != null)
            {
                main.getBot().sendMessage(embed, channel);
            }
            else
            {
                main.getDatabase().resetUpdateChannel(id);
            }
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp(bowt.guild.GuildObject)
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Send Update Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#updates");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  \n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "votes \n"
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