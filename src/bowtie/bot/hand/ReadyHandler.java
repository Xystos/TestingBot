package bowtie.bot.hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.guild.GuildObject;
import bowt.hand.impl.BotReadyHandler;
import bowt.hand.impl.ChannelLogger;
import bowt.hand.impl.GuildCommandHandler;
import bowt.hand.impl.PresenceHandler;
import bowt.hand.presence.Presence;
import bowt.prop.Properties;
import bowt.thread.Threads;
import bowt.util.perm.UserPermissions;
import bowtie.bot.auto.AutomatedCommand;
import bowtie.bot.collections.CollectionTimer;
import bowtie.bot.collections.group.Group;
import bowtie.bot.collections.group.GroupManager;
import bowtie.bot.collections.list.ItemList;
import bowtie.bot.collections.list.ListManager;
import bowtie.bot.sound.SoundManager;
import bowtie.core.Main;
import bowtie.feed.FeedManager;
import bowtie.feed.ManagedFeed;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;

/**
 * @author &#8904
 *
 */
public class ReadyHandler extends BotReadyHandler
{
    private Main main;

    public ReadyHandler(Bot bot, Main main)
    {
        super(bot);
        this.main = main;
    }

    /**
     * @see bowt.hand.impl.BotReadyHandler#prepare()
     */
    @Override
    public void prepare()
    {
        int attempt = 0;
        while (this.bot.getGuildObjects().size() != this.bot.getClient().getGuilds().size())
        {
            attempt ++ ;
            this.bot.setPlayingText("Loading. Attempt: " + attempt);
            this.bot.createGuildObjects();
            loadMastersAndOwners();
            loadBannedUsers();
            loadCommandHandlers();
            checkGroupTimers();
            checkListTimers();
            loadAutomatedCommands();
            loadFeeds();
            joinDefaultChannels();
        }
        if (attempt > 1)
        {
            Bot.errorLog.print(this, "Needed " + attempt + " attempts to load guilds.");
        }
        this.bot.setPlayingText(Bot.getPrefix() + "help");
        PresenceHandler statusHandler = new PresenceHandler(this.bot, 20000);
        statusHandler.setDynamicUpdateHandler(() ->
        {
            List<Presence> playingTexts = new ArrayList<>();
            playingTexts.add(new Presence(StatusType.ONLINE, ActivityType.PLAYING, Bot.getPrefix() + "help"));
            statusHandler.setPresences(playingTexts);
        });
        statusHandler.start();

        ChannelLogger channelLog = new ChannelLogger(Bot.log, 300000, this.bot);
        if (channelLog.setLogChannel(Properties.getValueOf("logchannel")))
        {
            channelLog.start();
            Bot.log.print(this, "Started ChannelLogger.");
        }
        else
        {
            Bot.errorLog.print(this, "Failed to set channel for the ChannelLogger.");
        }

        ChannelLogger errrorLog = new ChannelLogger(Bot.errorLog, 300000, this.bot);
        if (errrorLog.setLogChannel(Properties.getValueOf("errorlogchannel")))
        {
            errrorLog.start();
            Bot.log.print(this, "Started error ChannelLogger.");
        }
        else
        {
            Bot.errorLog.print(this, "Failed to set channel for the error ChannelLogger.");
        }
    }

    private void loadCommandHandlers()
    {
        for (GuildObject guild : this.bot.getGuildObjects())
        {
            Threads.fixedPool.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    GuildCommandHandler commandHandler = Main.commandHandler;

                    Map<String, Integer> overrides = main.getDatabase().getOverridesForGuild(guild.getStringID());

                    if (!overrides.isEmpty())
                    {
                        List<String> commandStrings = new ArrayList<String>(overrides.keySet());

                        for (String commandString : commandStrings)
                        {
                            Command command = commandHandler.getCommand(commandString);

                            if (command != null)
                            {
                                command.overridePermission(overrides.get(commandString), guild);
                            }
                        }

                        Bot.log.print(this, "Loaded " + overrides.size() + " command overrides for '"
                                + guild.getGuild().getName() + "'.");
                    }

                    guild.setCommandHandler(commandHandler);
                }
            });
        }
        Bot.log.print(this, "Loaded command handlers.");
    }

    private void loadMastersAndOwners()
    {
        List<String> ids = null;
        for (GuildObject guild : this.bot.getGuildObjects())
        {
            IUser owner = RequestBuffer.request(new IRequest<IUser>()
            {
                @Override
                public IUser request()
                {
                    return guild.getGuild().getOwner();
                }
            }).get();

            main.getDatabase().addMaster(owner.getStringID(), guild.getStringID(), UserPermissions.OWNER);
            ids = main.getDatabase().getMasterIDs(guild, UserPermissions.OWNER);
            for (String id : ids)
            {
                guild.addOwner(id);
            }
            ids = main.getDatabase().getMasterIDs(guild, UserPermissions.MASTER);
            for (String id : ids)
            {
                guild.addMaster(id);
            }
        }
        Bot.log.print(this, "Registered " + this.bot.getTotalOwnerCount() + " owners.");
        Bot.log.print(this, "Registered " + this.bot.getTotalMasterCount() + " masters.");
    }

    private void loadBannedUsers()
    {
        List<String> ids = main.getDatabase().getBannedUsers();
        for (String id : ids)
        {
            try
            {
                this.bot.banUser(id);
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        Bot.log.print(this, "Loaded " + ids.size() + " banned users.");
    }

    private void checkGroupTimers()
    {
        int closeCount = 0;
        int resumeCount = 0;
        for (GuildObject guild : this.bot.getGuildObjects())
        {
            List<CollectionTimer> timers = this.main.getDatabase().getGroupTimersForGuild(guild.getStringID());

            if (!timers.isEmpty())
            {
                GroupManager manager = GroupManager.getManagerForGuild(guild);
                for (CollectionTimer timer : timers)
                {
                    Group group = manager.getGroupByName(timer.getName());
                    if (System.currentTimeMillis() >= timer.getTime())
                    {
                        if (manager.remove(timer.getName()))
                        {
                            closeCount ++ ;
                        }
                    }
                    else
                    {
                        group.destroyAt(timer.getTime());
                        resumeCount ++ ;
                    }
                }
            }
        }
        if (closeCount != 0)
        {
            Bot.log.print(this, "Closed " + closeCount + " groups because their timer expired.");
        }
        if (resumeCount != 0)
        {
            Bot.log.print(this, "Resumed " + resumeCount + " grouptimers.");
        }
    }

    private void checkListTimers()
    {
        int closeCount = 0;
        int resumeCount = 0;
        for (GuildObject guild : this.bot.getGuildObjects())
        {
            List<CollectionTimer> timers = this.main.getDatabase().getListTimersForGuild(guild.getStringID());

            if (!timers.isEmpty())
            {
                ListManager manager = ListManager.getManagerForGuild(guild);
                for (CollectionTimer timer : timers)
                {
                    ItemList list = manager.getListByName(timer.getName());
                    if (System.currentTimeMillis() >= timer.getTime())
                    {
                        if (manager.remove(timer.getName()))
                        {
                            closeCount ++ ;
                        }
                    }
                    else
                    {
                        list.destroyAt(timer.getTime());
                        resumeCount ++ ;
                    }
                }
            }
        }
        if (closeCount != 0)
        {
            Bot.log.print(this, "Closed " + closeCount + " lists because their timer expired.");
        }
        if (resumeCount != 0)
        {
            Bot.log.print(this, "Resumed " + resumeCount + " listtimers.");
        }
    }

    private void loadFeeds()
    {
        Bot.log.print(this, "Loading feeds.");
        int count = 0;
        for (GuildObject guild : this.bot.getGuildObjects())
        {
            count += main.getDatabase().getFeedCount(guild.getStringID());

            Threads.cachedPool.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    List<ManagedFeed> feeds = main.getDatabase().getFeeds(guild);

                    if (!main.getDatabase().isActivatedGuild(guild.getStringID()))
                    {
                        for (ManagedFeed managedFeed : feeds)
                        {
                            managedFeed.setUpdateInterval(FeedManager.NON_PATRON_INTERVAL);
                            FeedManager.remove(managedFeed);
                            FeedManager.add(managedFeed);
                            Bot.log.print("Updated interval to NON_PATRON.");
                        }
                    }
                }
            });
        }

        int currentCount = 0;
        int counter = 0;

        while (count > (currentCount = FeedManager.getFeedCount()))
        {
            this.bot.setPlayingText("Loading " + (count - currentCount) + " RSS feeds.");
            counter ++ ;
            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
            }

            if (counter > 10)
            {
                break;
            }
        }

        Bot.log.print(this, FeedManager.getFeedCount() + " / " + count + " feeds loaded.");

        new FeedManager(main).run();
    }

    private void joinDefaultChannels()
    {
        this.bot.setPlayingText("Joining default voice channels.");
        Bot.log.print(this, "Joining default voice channels.");
        int count = 0;
        for (GuildObject guild : this.bot.getGuildObjects())
        {
            if (SoundManager.joinDefaultChannel(guild))
            {
                count ++ ;
            }
        }

        Bot.log.print(this, "Joined " + count + " default voice channels.");
    }

    private void loadAutomatedCommands()
    {
        List<AutomatedCommand> commands = this.main.getDatabase().getAutomatedCommands();
        for (AutomatedCommand command : commands)
        {
            AutomatedCommand.add(command);
        }
        Bot.log.print(this, "Loaded " + commands.size() + " automated commands.");
    }
}