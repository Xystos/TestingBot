package bowtie.bot.bots.cmd.feeds;

import java.net.MalformedURLException;
import java.net.URL;
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
public class RemoveAdditionalTextCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public RemoveAdditionalTextCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public RemoveAdditionalTextCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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

        ManagedFeed feed = FeedManager.getFeed(link, event.getGuildObject().getStringID());
        if (feed == null)
        {
            this.bot.sendMessage("There is no feed with that link on your server.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }
        feed.setAdditionalText(null);
        main.getDatabase().removeAdditionalText(feed);

        bot.sendMessage("The additional text for [" + feed.getTitle() + "](" + link + ") has been removed.",
                event.getChannel(), Colors.GREEN);
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Remove Additional Text From RSS Feed Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#rss");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command removes previously set additional \n"
                + "|  text from the feed with the given URL.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "removetext <feed url>\n"
                        + "|  " + guild.getPrefix() + "removetext https://www.reddit.com/r/discordapp/new/.rss \n"
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