package bowtie.bot.bots.cmd.feeds;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;
import bowtie.feed.FeedManager;
import bowtie.feed.ManagedFeed;

/**
 * @author &#8904
 *
 */
public class ExportFeedsCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public ExportFeedsCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public ExportFeedsCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        List<IChannel> channels = event.getMessage().getChannelMentions();
        IChannel channel = (channels.isEmpty() ? null : channels.get(0));

        List<ManagedFeed> feeds = FeedManager.getFeeds(event.getGuild().getStringID());

        if (feeds == null)
        {
            this.bot.sendMessage("Could not find any RSS feeds.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        if (channel != null)
        {
            feeds = feeds.stream()
                    .filter(f -> f.getChannelID() == channel.getLongID())
                    .collect(Collectors.toList());
        }

        if (feeds.isEmpty())
        {
            this.bot.sendMessage("Could not find any RSS feeds.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        JSONArray feedsArray = new JSONArray();

        for (ManagedFeed feed : feeds)
        {
            feedsArray.put(feed.toJSON());
        }

        JSONObject feedsJson = new JSONObject();
        feedsJson.put("Feeds", feedsArray);

        InputStream stream = new ByteArrayInputStream(feedsJson.toString(4).getBytes(StandardCharsets.UTF_8));

        RequestBuffer.request(
                () ->
                {
                    event.getChannel().sendFile(
                            "This is your file containing all information about the RSS feeds "
                                    + (channel == null ? "of this server." : "in " + channel.mention() + ".")
                                    + " You can use the 'importfeeds' command to import these to another server.",
                            stream,
                            event.getGuildObject().getStringID() + "_RssFeeds"
                                    + (channel == null ? "" : "_" + channel.getName()) + ".txt");
                }).get();
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Export RSS Feeds Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#rss");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will create a file containing all \n"
                + "|  information about the feeds on your server. This file can be \n"
                + "|  used to import those feeds on another server without loosing \n"
                + "|  the settings.\n"
                + "|  \n"
                + "|  If you mention a channel, the command will only collect the \n"
                + "|  feeds that are active in that channel.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "exportfeeds\n"
                        + "|  " + guild.getPrefix() + "exportfeeds #channel\n"
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
