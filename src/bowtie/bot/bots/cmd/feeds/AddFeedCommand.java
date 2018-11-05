package bowtie.bot.bots.cmd.feeds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
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

/**
 * @author &#8904
 *
 */
public class AddFeedCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public AddFeedCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public AddFeedCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        if (main.getDatabase().getFeedCount(event.getGuildObject().getStringID()) < FeedManager.MAX_FREE_FEEDS)
        {

        }
        else if (!main.getDatabase().isActivatedGuild(event.getGuildObject().getStringID()))
        {
            this.bot.sendMessage(
                    "You can't subscribe to more than " + FeedManager.MAX_FREE_FEEDS
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
                    "You can't subscribe to more than " + FeedManager.MAX_FREE_FEEDS
                            + " feeds at once on an unactivated server.\n\n"
                            + "To activate the bot for this server you must become a "
                            + "[Patron](https://www.patreon.com/bowtiebots).\n\n"
                            + "Join the [Bowtie Bots Discord](https://discord.gg/KRdQK8q) server for more information.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        if (event.getMessage().getChannelMentions().isEmpty())
        {
            this.bot.sendMessage("You have to mention the channel in which the feed should be posted.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        IChannel channel = event.getMessage().getChannelMentions().get(0);

        String content = event.getMessage().getContent();

        for (IChannel ch : event.getMessage().getChannelMentions())
        {
            content = content.replace(ch.mention(), "");
        }
        content = content.trim().replaceAll(" +", " ");
        String[] parts = content.trim().split(" ");

        if (parts.length < 2)
        {
            this.bot.sendMessage("You have to add the URL of the feed.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        String link = parts[1].trim();

        if (!link.startsWith("https://"))
        {
            if (link.startsWith("http://") && link.length() >= 8)
            {
                try
                {
                    new URL(link);
                }
                catch (MalformedURLException e)
                {
                    link = link.substring(7);
                    link = "https://" + link;
                }
            }
            else if (link.startsWith("https://") && link.length() >= 9)
            {
                try
                {
                    new URL(link);
                }
                catch (MalformedURLException e)
                {
                    link = link.substring(8);
                    link = "http://" + link;
                }
            }
            else
            {
                try
                {
                    new URL("http://" + link);
                    link = "http://" + link;
                }
                catch (MalformedURLException e)
                {
                    link = "https://" + link;
                }
            }
        }

        boolean exists = FeedManager.getFeed(link, event.getGuildObject().getStringID()) == null ? false : true;
        if (exists)
        {
            this.bot.sendMessage(
                    "This feed is already active on your server. Remove and re-add it if you want to change the channel.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        RssFeed feed = null;
        try
        {
            feed = FeedUtils.getFeedFromUrl(link);
            feed.setup();
        }
        catch (FeedNotFoundException e)
        {
            this.bot.sendMessage("Could not create a feed from the given URL.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }
        catch (NoValidEntriesException e)
        {
            this.bot.sendMessage("The feed does not contain any valid entries.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }
        ManagedFeed managedFeed = new ManagedFeed(feed, main, event.getGuildObject(), channel, link, -1,
                Colors.random());

        EnumSet<Permissions> needed = Permissions.getAllowedPermissionsForNumber(93184);
        List<Permissions> missing = UserPermissions.getMissingPermissions(
                channel.getModifiedPermissions(bot.getClient().getOurUser()), needed);
        String missingString = "The bot is missing the following permissions in that channel: \n\n";

        for (Permissions p : missing)
        {
            missingString += p.name() + "\n";
        }

        if (!missing.isEmpty())
        {
            this.bot.sendMessage(missingString,
                    event.getMessage().getChannel(), Colors.RED);
            return;
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
        this.main.getDatabase().updateFeedSettings(managedFeed);
        bot.sendMessage("Future articles of the feed [" + feed.getTitle() + "](" + link + ") will be sent to "
                + channel.mention() + ". "
                + "This feed will be updated approximately every " + managedFeed.getUpdateInterval() + " minutes.",
                event.getChannel(), Colors.GREEN);
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Subscribe to an RSS Feed Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#rss");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will subscribe to the given RSS feed.\n"
                + "|  Future articles of that feed will be sent to the given channel.\n"
                + "|  The update interval will vary depending on the average time between articles. \n"
                + "|  \n"
                + "|  The normal intervals are " + FeedManager.UPDATE_INTERVAL_1 + ", " + FeedManager.UPDATE_INTERVAL_2
                + ", "
                + FeedManager.UPDATE_INTERVAL_3 + " and " + FeedManager.UPDATE_INTERVAL_4 + ".\n"
                + "|  The interval of a feed can change if the average article frequency changes.\n"
                + "|  \n"
                + "|  On unactivated servers you are only able to subscribe to " + FeedManager.MAX_FREE_FEEDS
                + " RSS feeds and \n"
                + "|  the update interval will be set to " + FeedManager.NON_PATRON_INTERVAL + ".\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "sub <feed url> #channel\n"
                        + "|  " + guild.getPrefix() + "sub https://www.reddit.com/r/discordapp/new/.rss #rss\n"
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