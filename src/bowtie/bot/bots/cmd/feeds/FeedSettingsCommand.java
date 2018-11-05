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
public class FeedSettingsCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public FeedSettingsCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public FeedSettingsCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
            int index = -1;
            try
            {
                index = Integer.parseInt(link.replace("http://", "").replace("https://", "").trim());
            }
            catch (Exception e)
            {
                this.bot.sendMessage("There is no feed with that link on your server.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }

            List<ManagedFeed> feeds = FeedManager.getFeeds(event.getGuildObject().getStringID());

            if (index > 0 && index <= feeds.size())
            {
                feed = feeds.get(index - 1);
            }
            else
            {
                this.bot.sendMessage(
                        "There is no feed with that index on your server. Use the 'showfeeds' command to see all indexes.",
                        event.getMessage().getChannel(), Colors.RED);
            }
        }

        boolean changed = false;

        String withImageS = event.getParameter("image");
        String withTextS = event.getParameter("text");
        String withTitleS = event.getParameter("title");
        String withFooterS = event.getParameter("footer");
        String withIconS = event.getParameter("icon");

        boolean withImage = feed.isImageOn();
        boolean withText = feed.isTextOn();
        boolean withTitle = feed.isTitleOn();
        boolean withFooter = feed.isFooterOn();
        boolean withIcon = feed.isIconOn();

        if (withImageS != null && (withImageS.equals("on") || withImageS.equals("off")))
        {
            withImage = withImageS.equals("on");
            changed = true;
        }

        if (withTextS != null && (withTextS.equals("on") || withTextS.equals("off")))
        {
            withText = withTextS.equals("on");
            changed = true;
        }

        if (withTitleS != null && (withTitleS.equals("on") || withTitleS.equals("off")))
        {
            withTitle = withTitleS.equals("on");
            changed = true;
        }

        if (withFooterS != null && (withFooterS.equals("on") || withFooterS.equals("off")))
        {
            withFooter = withFooterS.equals("on");
            changed = true;
        }

        if (withIconS != null && (withIconS.equals("on") || withIconS.equals("off")))
        {
            withIcon = withIconS.equals("on");
            changed = true;
        }

        if (changed)
        {
            feed.setImageOn(withImage);
            feed.setIconOn(withIcon);
            feed.setTextOn(withText);
            feed.setTitleOn(withTitle);
            feed.setFooterOn(withFooter);

            main.getDatabase().updateFeedSettings(feed);
        }

        bot.sendMessage((changed ? "The settings for [" + feed.getTitle() + "](" + feed.getLink()
                + ") have been updated.\n\n"
                : "These are the current settings for [" + feed.getTitle() + "](" + feed.getLink() + ").\n"
                        + "If you want to change them take a look at '" + event.getGuildObject().getPrefix()
                        + "help settings'.\n\n")
                + "Image: \t" + (withImage ? "✅" : "❌") + "\n"
                + "Icon: \t\t" + (withIcon ? "✅" : "❌") + "\n"
                + "Title: \t\t" + (withTitle ? "✅" : "❌") + "\n"
                + "Text: \t\t" + (withText ? "✅" : "❌") + "\n"
                + "Footer: \t" + (withFooter ? "✅" : "❌") + "\n"
                + "Keepcount: " + (feed.getKeepCount() > 0 ? feed.getKeepCount() : "Not set") + "\n"
                + "Update interval: " + feed.getUpdateInterval() + " minutes\n"
                + (!feed.getFeed().getItemSet().getItemList().isEmpty() &&
                        feed.getFeed().getItemSet().getItemList().get(0).hasDateTags()
                        ? "(New articles every " + feed.getAvrgInterval() + " minutes on average)"
                        : ""),
                event.getChannel(), Colors.GREEN);
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("RSS FEed Settings Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#rss");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  With this command you are able to customize the information \n"
                + "|  that the bot sends with each article. You can disable or reenable certain \n"
                + "|  parts of an article such as the text, the image, the feed icon or the \n"
                + "|  footer. \n"
                + "|  \n"
                + "|  A list of things you can turn on or off: \n"
                + "|  \n"
                + "|  - **title**\n"
                + "|     The title of the feed (not the article headline).\n"
                + "|  \n"
                + "|  - **text**\n"
                + "|     The text of the article.\n"
                + "|  \n"
                + "|  - **footer**\n"
                + "|     The footer of the message which shows the timestamp of the article.\n"
                + "|  \n"
                + "|  - **image**\n"
                + "|     The image of the article if it has one.\n"
                + "|  \n"
                + "|  - **icon**\n"
                + "|     The icon of the feed shown on the right side of the message.\n"
                + "|  \n"
                + "|  \n"
                + "|  To toggle one part on or off you need to specify it as a paramter in the \n"
                + "|  command. Such a parameter is formatted like so \n"
                + "|  \n"
                + "|  -partName=on/off\n"
                + "|  \n"
                + "|  Example:  -title=off\n"
                + "|  \n"
                + "|  \n"
                + "|  "
                + guild.getPrefix()
                + "settings https://www.reddit.com/r/discordapp/new/.rss \n"
                + "|  -image=off -title=on\n"
                + "|  \n"
                + "|  This will turn images off and titles back on for the given feed. All \n"
                + "|  the other settings will not be changed because they were not added to the \n"
                + "|  command. An option can either be set to 'on' or 'off'. You have to \n"
                + "|  make sure that there are no spaces within a single setting, so '-icon = off' \n"
                + "|  would not work. You can modify as many settings for a single feed at once \n"
                + "|  as you like. If you don't add any settings the bot will simply show the \n"
                + "|  current ones.\n"
                + "|  \n"
                + "|  \n"
                + "|  The update interval can not be manually changed, the bot will calculate \n"
                + "|  it based on the average time between articles.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "settings <feed url> -partName=on/off\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix()
                        + "settings https://www.reddit.com/r/discordapp/new/.rss -icon=off \n"
                        + "|  -title=on\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "settings https://www.reddit.com/r/discordapp/new/.rss \n"
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