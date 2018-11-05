package bowtie.bot.bots.cmd.feeds;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.bot.util.Activation;
import bowtie.core.Main;
import bowtie.feed.FeedManager;
import bowtie.feed.ManagedFeed;
import bowtie.feed.RssFeed;
import bowtie.feed.exc.FeedNotFoundException;
import bowtie.feed.exc.NoValidEntriesException;
import bowtie.feed.util.FeedUtils;
import bowtie.json.JSON;

/**
 * @author &#8904
 *
 */
public class ImportFeedsCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public ImportFeedsCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public ImportFeedsCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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

        List<Attachment> attachments = event.getAttachments();

        if (attachments.isEmpty())
        {
            this.bot.sendMessage("You have to add the file to import as an attachment.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        String jsonString = downloadFile(attachments.get(0).getUrl());

        JSONObject json = JSON.parse(jsonString);

        if (json == null)
        {
            this.bot.sendMessage("The content of the file is not formatted correctly.\n\n"
                    + "Please do not change anything inside the files provided by the bot. "
                    + "If you did not change anything contact Lukas&#8904 and report this problem.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        JSONArray feeds = json.getJSONArray("Feeds");

        if (feeds == null)
        {
            this.bot.sendMessage("The content of the file is not formatted correctly.\n\n"
                    + "Please do not change anything inside the files provided by the bot. "
                    + "If you did not change anything contact Lukas&#8904 and report this problem.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        for (int i = 0; i < feeds.length(); i ++ )
        {
            JSONObject feed = feeds.getJSONObject(i);
            int keepCount = feed.getInt("KeepCount");
            Color color = new Color(feed.getInt("ColorR"), feed.getInt("ColorG"), feed.getInt("ColorB"));
            String link = feed.getString("Link");
            boolean withImage = feed.getBoolean("Image");
            boolean withTitle = feed.getBoolean("Title");
            boolean withIcon = feed.getBoolean("Icon");
            boolean withText = feed.getBoolean("Text");
            boolean withFooter = feed.getBoolean("Footer");

            IChannel feedChannel = channel;

            if (channel == null)
            {
                long channelID = feed.getLong("ChannelID");
                feedChannel = event.getGuild().getChannelByID(channelID);

                if (feedChannel == null)
                {
                    String channelName = feed.getString("ChannelName");

                    List<IChannel> foundChannels = event.getGuild().getChannelsByName(channelName);

                    if (!foundChannels.isEmpty())
                    {
                        feedChannel = foundChannels.get(0);
                    }
                }
            }

            if (feedChannel == null)
            {
                this.bot.sendMessage("You did not specify a channel for the feed " + link + " and the bot "
                        + "could not find the old channel that the feed was linked to before.",
                        event.getMessage().getChannel(), Colors.RED);
                continue;
            }

            if (main.getDatabase().getFeedCount(event.getGuildObject().getStringID()) < FeedManager.MAX_FREE_FEEDS)
            {

            }
            else if (!main.getDatabase().isActivatedGuild(event.getGuildObject().getStringID()))
            {
                this.bot.sendMessage(
                        "You can't subscribe to more than "
                                + FeedManager.MAX_FREE_FEEDS
                                + " feeds at once on an unactivated server.\n\n"
                                + "To activate the bot for this server you must become a "
                                + "[Patron](https://www.patreon.com/bowtiebots).\n\n"
                                + "Join the [Bowtie Bots Discord](https://discord.gg/KRdQK8q) server for more information.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }
            else if (!Activation.isActivated(event.getGuildObject().getStringID()))
            {
                this.bot.sendMessage(
                        "You can't subscribe to more than "
                                + FeedManager.MAX_FREE_FEEDS
                                + " feeds at once on an unactivated server.\n\n"
                                + "To activate the bot for this server you must become a "
                                + "[Patron](https://www.patreon.com/bowtiebots).\n\n"
                                + "Join the [Bowtie Bots Discord](https://discord.gg/KRdQK8q) server for more information.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }

            boolean exists = FeedManager.getFeed(link, event.getGuildObject().getStringID()) == null ? false : true;
            if (exists)
            {
                this.bot.sendMessage(
                        "The feed "
                                + link
                                + " is already active on your server. Remove and re-add it if you want to change the channel.",
                        event.getMessage().getChannel(), Colors.RED);
                continue;
            }

            RssFeed rssFeed = null;
            try
            {
                rssFeed = FeedUtils.getFeedFromUrl(link);
                rssFeed.setup();
            }
            catch (FeedNotFoundException e)
            {
                this.bot.sendMessage("Could not create a feed from " + link + " .",
                        event.getMessage().getChannel(), Colors.RED);
                continue;
            }
            catch (NoValidEntriesException e)
            {
                this.bot.sendMessage("The feed " + link + " does not contain any valid entries.",
                        event.getMessage().getChannel(), Colors.RED);
                continue;
            }
            ManagedFeed managedFeed = new ManagedFeed(rssFeed, main, event.getGuildObject(), feedChannel, link,
                    keepCount,
                    color);

            EnumSet<Permissions> needed = Permissions.getAllowedPermissionsForNumber(93184);
            List<Permissions> missing = UserPermissions.getMissingPermissions(
                    feedChannel.getModifiedPermissions(bot.getClient().getOurUser()), needed);
            String missingString = "The bot is missing the following permissions in " + feedChannel.mention()
                    + ": \n\n";

            for (Permissions p : missing)
            {
                missingString += p.name() + "\n";
            }

            if (!missing.isEmpty())
            {
                this.bot.sendMessage(missingString,
                        event.getMessage().getChannel(), Colors.RED);
                continue;
            }

            boolean active = false;

            if (main.getDatabase().isActivatedGuild(event.getGuildObject().getStringID()))
            {
                if (Activation.isActivated(event.getGuildObject().getStringID()))
                {
                    active = true;
                }
            }

            if (!active)
            {
                managedFeed.setUpdateInterval(FeedManager.NON_PATRON_INTERVAL);
            }

            FeedManager.add(managedFeed);
            this.main.getDatabase().addFeed(managedFeed);

            managedFeed.setFooterOn(withFooter);
            managedFeed.setImageOn(withImage);
            managedFeed.setTitleOn(withTitle);
            managedFeed.setIconOn(withIcon);
            managedFeed.setTextOn(withText);

            this.main.getDatabase().updateFeedSettings(managedFeed);

            bot.sendMessage("Future articles of the feed [" + rssFeed.getTitle() + "](" + link + ") will be sent to "
                    + feedChannel.mention() + ". "
                    + "This feed will be updated approximately every " + managedFeed.getUpdateInterval() + " minutes.",
                    event.getChannel(), Colors.GREEN);
        }
    }

    public String downloadFile(String url)
    {
        URL website;
        String text = null;
        try
        {
            website = new URL(url);
            URLConnection conn = website.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name()))
            {
                text = scanner.useDelimiter("\\A").next();
            }
        }
        catch (MalformedURLException e)
        {
        }
        catch (IOException e)
        {
        }
        return text;
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Import RSS Feeds Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#rss");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  For this command you should first use 'exportfeeds' \n"
                + "|  to get a file containing all feed information.\n"
                + "|  \n"
                + "|  Upload that file and put this command into the comment pop-up.\n"
                + "|  \n"
                + "|  The bot will now import all feeds from that file and tries to \n"
                + "|  restore them. All settings will be the same as the ones of the \n"
                + "|  original feed. \n"
                + "|  \n"
                + "|  If you do not tag a channel after the command the bot will first \n"
                + "|  try to find the channel that the old feed was in. If it does not \n"
                + "|  find it on the new server it will try to find a channel with the \n"
                + "|  same name. \n"
                + "|  \n"
                + "|  If you do tag a channel after the command the bot will set the \n"
                + "|  feeds to that channel. \n"
                + "|");

        builder.appendField(
                "|  Usage: ",
                "|  (always upload the file and put the command below into the \n"
                        + "|  comment pop-up)\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "importfeeds\n"
                        + "|  " + guild.getPrefix() + "importfeeds #channel\n"
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
