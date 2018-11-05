package bowtie.bot.bots.cmd.feeds;

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
import bowtie.feed.FeedManager;
import bowtie.feed.ManagedFeed;

/**
 * @author &#8904
 *
 */
public class ChangeKeepCountCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public ChangeKeepCountCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public ChangeKeepCountCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        String[] parts = event.getMessage().getContent().trim().split(" ");

        if (parts.length < 3)
        {
            this.bot.sendMessage(
                    "You have to add the URL of the feed and the number of articles that you want to keep.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        int keepCount = -1;
        try
        {
            keepCount = Integer.parseInt(parts[2]);

            if (keepCount < 1)
            {
                keepCount = -1;
            }
            else if (keepCount > 50)
            {
                keepCount = 50;
            }
        }
        catch (NumberFormatException e)
        {
            this.bot.sendMessage(
                    "You have to add the number of articles that you want to keep after the URL of the feed.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        String link = parts[1].trim();

        if (!link.startsWith("https://"))
        {
            if (link.startsWith("http://") && link.length() >= 8)
            {
                link = link.substring(7);
            }
            link = "https://" + link;
        }

        ManagedFeed feed = FeedManager.getFeed(link, event.getGuildObject().getStringID());
        if (feed == null)
        {
            this.bot.sendMessage("There is no feed with that link on your server.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }
        feed.setKeepCount(keepCount);
        main.getDatabase().updateFeedKeepCount(feed);
        if (keepCount > 0)
        {
            bot.sendMessage(
                    "The bot will from now on only keep the " + keepCount + " newest messages of [" + feed.getTitle()
                            + "](" + link + ") "
                            + "and delete older ones.", event.getChannel(), Colors.GREEN);
        }
        else
        {
            bot.sendMessage("The bot will not delete any older articles of [" + feed.getTitle() + "](" + link + ").",
                    event.getChannel(), Colors.GREEN);
        }
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Change RSS Feed Keepcount Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#rss");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will allow you to specify the number \n"
                + "|  of messages the feed should keep in the channel. If the keep \n"
                + "|  limit is exceeded the bot will automatically delete older \n"
                + "|  messages of this specific feed.\n"
                + "|  \n"
                + "|  If you set it to 0 (which is the default) the bot will not \n"
                + "|  delete any articles. \n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "keep <feed url> 5\n"
                        + "|  " + guild.getPrefix() + "keep https://www.reddit.com/r/discordapp/new/.rss 1\n"
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