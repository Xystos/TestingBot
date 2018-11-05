package bowtie.bot.bots.cmd.misc;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowtie.bot.auto.AutomatedCommand;
import bowtie.core.Main;
import bowtie.feed.FeedManager;

/**
 * @author &#8904
 *
 */
public class StatisticCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public StatisticCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public StatisticCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        bot.sendMessage(getHelp(event.getGuildObject()), event.getChannel());
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Bot Statistics");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription(
                "|  Servers: " + this.bot.getGuildCount() + "\n"
                        + "|  Active servers: " + this.main.getDatabase().activationCount() + "\n"
                        + "|  \n"
                        + "|  Users: " + this.bot.getTotalUserCount() + "\n"
                        + "|  Owners: " + this.bot.getTotalOwnerCount() + "\n"
                        + "|  Masters: " + this.bot.getTotalMasterCount() + "\n"
                        + "|  Banned: " + this.bot.getBannedUsers().size() + "\n"
                        + "|  Open groups: " + this.main.getDatabase().getGroupCount() + "\n"
                        + "|  Open lists: " + this.main.getDatabase().getListCount() + "\n"
                        + "|  Custom lines: " + this.main.getDatabase().getTotalLineCount() + "\n"
                        + "|  Custom join texts: " + this.main.getDatabase().getJoinTextCount() + "\n"
                        + "|  Custom leave texts: " + this.main.getDatabase().getLeaveTextCount() + "\n"
                        + "|  Custom prefixes: " + this.main.getDatabase().getPrefixCount() + "\n"
                        + "|  Active automations: " + AutomatedCommand.size + "\n"
                        + "|  Commands: " + Main.commandHandler.getCommands().size() + "\n"
                        + "|  Active feeds: " + FeedManager.getFeedCount() + "\n"
                        + "|      " + FeedManager.UPDATE_INTERVAL_1 + " minutes: " + FeedManager.list1Count() + "\n"
                        + "|      " + FeedManager.UPDATE_INTERVAL_2 + " minutes: " + FeedManager.list2Count() + "\n"
                        + "|      " + FeedManager.UPDATE_INTERVAL_3 + " minutes: " + FeedManager.list3Count() + "\n"
                        + "|      " + FeedManager.UPDATE_INTERVAL_4 + " minutes: " + FeedManager.list4Count() + "\n"
                        + "|      " + FeedManager.NON_PATRON_INTERVAL + " minutes: " + FeedManager.list5Count() + "\n"
                        + "|  Voice connections: " + this.bot.getClient().getConnectedVoiceChannels().size());

        return builder.build();
    }
}