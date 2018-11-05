package bowtie.core;

import java.io.File;
import java.sql.SQLException;

import bowt.bot.Bot;
import bowt.bot.exc.BowtieClientException;
import bowt.cmnd.PrefixLoader;
import bowt.cons.LibConstants;
import bowt.guild.GuildObject;
import bowt.hand.impl.GuildCommandHandler;
import bowt.hand.impl.OfflineHandler;
import bowt.log.Logger;
import bowt.prop.Properties;
import bowt.thread.Threads;
import bowt.util.perm.UserPermissions;
import bowtie.bot.bots.cmd.alias.SetAliasCommand;
import bowtie.bot.bots.cmd.auto.AutomateCommand;
import bowtie.bot.bots.cmd.auto.RemoveAutomatedCommand;
import bowtie.bot.bots.cmd.auto.ShowAutomatedCommandsCommand;
import bowtie.bot.bots.cmd.collections.AddToListCommand;
import bowtie.bot.bots.cmd.collections.CloseCollectionCommand;
import bowtie.bot.bots.cmd.collections.CreateGroupCommand;
import bowtie.bot.bots.cmd.collections.CreateListCommand;
import bowtie.bot.bots.cmd.collections.JoinGroupCommand;
import bowtie.bot.bots.cmd.collections.LeaveGroupCommand;
import bowtie.bot.bots.cmd.collections.RemoveFromCollectionCommand;
import bowtie.bot.bots.cmd.collections.ShowCollectionMembersCommand;
import bowtie.bot.bots.cmd.collections.ShowGroupsCommand;
import bowtie.bot.bots.cmd.collections.ShowListsCommand;
import bowtie.bot.bots.cmd.dict.AntonymLookUpCommand;
import bowtie.bot.bots.cmd.dict.DescribeWithLookUpCommand;
import bowtie.bot.bots.cmd.dict.DescribesLookUpCommand;
import bowtie.bot.bots.cmd.dict.RhymeLookUpCommand;
import bowtie.bot.bots.cmd.dict.SynonymLookUpCommand;
import bowtie.bot.bots.cmd.dict.UrbanLookUpCommand;
import bowtie.bot.bots.cmd.dict.WikipediaLookUpCommand;
import bowtie.bot.bots.cmd.dict.WordnetLookUpCommand;
import bowtie.bot.bots.cmd.feeds.AddAdditionalTextCommand;
import bowtie.bot.bots.cmd.feeds.AddFeedCommand;
import bowtie.bot.bots.cmd.feeds.ChangeKeepCountCommand;
import bowtie.bot.bots.cmd.feeds.CheckFeedCommand;
import bowtie.bot.bots.cmd.feeds.ExportFeedsCommand;
import bowtie.bot.bots.cmd.feeds.FeedSettingsCommand;
import bowtie.bot.bots.cmd.feeds.GetYoutubeChannelFeedCommand;
import bowtie.bot.bots.cmd.feeds.ImportFeedsCommand;
import bowtie.bot.bots.cmd.feeds.RemoveAdditionalTextCommand;
import bowtie.bot.bots.cmd.feeds.RemoveFeedCommand;
import bowtie.bot.bots.cmd.feeds.ShowFeedsCommand;
import bowtie.bot.bots.cmd.files.DiskSpaceCommand;
import bowtie.bot.bots.cmd.lines.AddGlobalLineCommand;
import bowtie.bot.bots.cmd.lines.AddLineCommand;
import bowtie.bot.bots.cmd.lines.DeleteAllLinesCommand;
import bowtie.bot.bots.cmd.lines.DeleteGlobalLineCommand;
import bowtie.bot.bots.cmd.lines.DeleteLineCommand;
import bowtie.bot.bots.cmd.lines.DeleteLineIndexCommand;
import bowtie.bot.bots.cmd.lines.ExportLinesCommand;
import bowtie.bot.bots.cmd.lines.ImportLinesCommand;
import bowtie.bot.bots.cmd.lines.ShowLinesCommand;
import bowtie.bot.bots.cmd.lines.ToggleGlobalCommand;
import bowtie.bot.bots.cmd.lines.TogglePlainTextCommand;
import bowtie.bot.bots.cmd.misc.GetPrefixCommand;
import bowtie.bot.bots.cmd.misc.HelpCommand;
import bowtie.bot.bots.cmd.misc.MemoryCommand;
import bowtie.bot.bots.cmd.misc.RebootCommand;
import bowtie.bot.bots.cmd.misc.SetPrefixCommand;
import bowtie.bot.bots.cmd.misc.ShutdownCommand;
import bowtie.bot.bots.cmd.misc.StatisticCommand;
import bowtie.bot.bots.cmd.misc.StatusCommand;
import bowtie.bot.bots.cmd.misc.ThreadCountCommand;
import bowtie.bot.bots.cmd.misc.VersionCommand;
import bowtie.bot.bots.cmd.random.M8Command;
import bowtie.bot.bots.cmd.random.PickAndRemoveFromCollectionCommand;
import bowtie.bot.bots.cmd.random.RandomAllCommand;
import bowtie.bot.bots.cmd.random.RandomChannelCommand;
import bowtie.bot.bots.cmd.random.RandomCommand;
import bowtie.bot.bots.cmd.random.RandomFromCollectionCommand;
import bowtie.bot.bots.cmd.random.RandomNumberCommand;
import bowtie.bot.bots.cmd.random.RandomOnlineCommand;
import bowtie.bot.bots.cmd.teams.TeamChannelCommand;
import bowtie.bot.bots.cmd.teams.TeamCommand;
import bowtie.bot.bots.cmd.teams.TeamFromGroupCommand;
import bowtie.bot.bots.cmd.updates.PatchCommand;
import bowtie.bot.bots.cmd.updates.ResetUpdateChannelCommand;
import bowtie.bot.bots.cmd.updates.SendUpdateCommand;
import bowtie.bot.bots.cmd.updates.UpdatesCommand;
import bowtie.bot.bots.cmd.users.AddMasterCommand;
import bowtie.bot.bots.cmd.users.AddOwnerCommand;
import bowtie.bot.bots.cmd.users.BanUserCommand;
import bowtie.bot.bots.cmd.users.DmAllCommand;
import bowtie.bot.bots.cmd.users.GetPermissionLevelCommand;
import bowtie.bot.bots.cmd.users.OverridePermissionCommand;
import bowtie.bot.bots.cmd.users.RemoveMasterCommand;
import bowtie.bot.bots.cmd.users.RemoveOwnerCommand;
import bowtie.bot.bots.cmd.users.ShowMastersCommand;
import bowtie.bot.bots.cmd.users.UnbanUserCommand;
import bowtie.bot.bots.cmd.voice.JoinVoiceChannelCommand;
import bowtie.bot.bots.cmd.voice.LeaveVoiceChannelCommand;
import bowtie.bot.bots.cmd.voice.ResetDefaultChannelCommand;
import bowtie.bot.bots.cmd.voice.SetDefaultChannelCommand;
import bowtie.bot.bots.cmd.voice.SetJoinTextCommand;
import bowtie.bot.bots.cmd.voice.SetLeaveTextCommand;
import bowtie.bot.bots.cmd.voice.SetVoiceNameCommand;
import bowtie.bot.collections.group.GroupManager;
import bowtie.bot.collections.list.ListManager;
import bowtie.bot.hand.GuildKickHandler;
import bowtie.bot.hand.GuildTransferOwnerHandler;
import bowtie.bot.hand.MessageHandler;
import bowtie.bot.hand.NewGuildHandler;
import bowtie.bot.hand.ReactionHandler;
import bowtie.bot.hand.ReadyHandler;
import bowtie.bot.hand.UserLeaveHandler;
import bowtie.bot.hand.VoiceChannelJoinHandler;
import bowtie.bot.hand.VoiceChannelLeaveHandler;
import bowtie.bot.hand.VoiceChannelMoveHandler;
import bowtie.bot.sound.SoundManager;
import bowtie.bot.util.Activation;
import bowtie.core.db.DatabaseAccess;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

/**
 * @author &#8904
 *
 */
public class Main
{
    public static final String BOT_VERSION = "X (Unsupported Selfhost Version)";
    public static final boolean NEEDS_ACTIVATION = false;
    public static final String BOWTIE_BOT_ICON =
            "https://cdn.discordapp.com/attachments/386235876625088522/471088502977724426/bowtie_dark__shadow__fill.png";
    public static final int ENTER_VOTES = 5;
    public static GuildCommandHandler commandHandler;
    private Bot bot;
    private OfflineHandler offlineHandler;
    private DatabaseAccess database;
    private IGuild homeGuild;
    private IChannel tmpPremiumChannel;

    public static void main(String[] args)
    {
        new Main();
    }

    public Main()
    {
        Bot.log.setLogToSystemOut(true);
        Bot.errorLog.setLogToSystemOut(true);

        Bot.errorLog.addFilterText("[Fatal Error] :"); // undefined html tags which are interpreted as xml
        Bot.errorLog.addFilterText("[sx.blah.discord.Discord4J] - Unable to process JSON!");
        Bot.errorLog.addFilterText("XML document structures must start and end within the same entity.");
        Bot.errorLog.addFilterText(" was referenced, but not declared.");

        this.database = new DatabaseAccess(this);

        Activation.setMain(this);
        SoundManager.setMain(this);

        GroupManager.setDatabase(this.database);
        GroupManager.startCleaner();

        ListManager.setDatabase(this.database);
        ListManager.startCleaner();

        this.bot = new Bot(Properties.getValueOf("token"), Properties.getValueOf("prefix"));

        this.bot.setPrefixLoader(new PrefixLoader()
        {
            @Override
            public String load(GuildObject guild)
            {
                String prefix = database.getPrefix(guild.getStringID());

                if (prefix == null)
                {
                    prefix = Bot.getPrefix();
                }

                return prefix;
            }
        });

        setupCommandHandler();
        new LibConstants();
        Bot.log.print(this, "Booting Bowtie Bot version " + BOT_VERSION);
        Bot.log.print(this, "Patcher version " + Properties.getValueOf("patcherversion"));
        Bot.log.print(this, "Rebooter version " + Properties.getValueOf("rebooterversion"));

        Properties.setValueOf("botversion", BOT_VERSION);
        IListener[] listeners =
        {
                new ReadyHandler(this.bot, this),
                new GuildKickHandler(this.bot, this),
                new NewGuildHandler(this.bot, this),
                new GuildTransferOwnerHandler(this),
                new MessageHandler(this.bot, this),
                new ReactionHandler(this.bot),
                new UserLeaveHandler(this.bot, this),
                new VoiceChannelLeaveHandler(this),
                new VoiceChannelJoinHandler(this),
                new VoiceChannelMoveHandler(this)
        };
        this.bot.addListeners(listeners, Threads.cachedPool);
        try
        {
            this.bot.login();
        }
        catch (BowtieClientException e)
        {
            Bot.errorLog.print(this, e);
        }

        offlineHandler = new OfflineHandler(this.bot)
        {
            @Override
            public void handle()
            {
                Bot.log.print(this, "Bot offline. Trying to reboot.");
                File jar = null;
                try
                {
                    jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    Process process = Runtime.getRuntime().exec(
                            "java -jar rebooter.jar command 10 nohup java -Xmx5g -d64 -jar bowtiebot.jar &");
                }
                catch (Exception e)
                {
                    Bot.errorLog.print(this, e);
                }
                kill(true);
            }
        };
        offlineHandler.getLogger().setLogToSystemOut(false);
        offlineHandler.setShouldLog(false);
        offlineHandler.start();
    }

    public void kill(boolean exit)
    {
        try
        {
            this.bot.logout();
        }
        catch (BowtieClientException e)
        {
            Bot.errorLog.print(this, e);
        }
        if (exit)
        {
            try
            {
                database.closeConnection();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
            System.exit(0);
            this.offlineHandler.stop();
            Threads.kill();
            Logger.closeAll();
        }
    }

    private void setupCommandHandler()
    {
        commandHandler = new GuildCommandHandler();

        commandHandler.setAliasLoader((command) ->
        {
            database.loadCommandAliases(command);
        });

        commandHandler.addCommand(new HelpCommand(new String[]
        {
                "help", "info", "commands"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new UpdatesCommand(new String[]
        {
                "updates"
        }, UserPermissions.MASTER, this));

        commandHandler.addCommand(new SendUpdateCommand(new String[]
        {
                "sendupdate"
        }, UserPermissions.CREATOR, this));

        commandHandler.addCommand(new ResetUpdateChannelCommand(new String[]
        {
                "resetupdates"
        }, UserPermissions.MASTER, this));

        commandHandler.addCommand(new SetAliasCommand(new String[]
        {
                "alias"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new SetDefaultChannelCommand(new String[]
        {
                "setdefaultchannel"
        }, UserPermissions.MASTER, this));

        commandHandler.addCommand(new ResetDefaultChannelCommand(new String[]
        {
                "resetdefaultchannel"
        }, UserPermissions.MASTER, this));

        commandHandler.addCommand(new SetVoiceNameCommand(new String[]
        {
                "voicename"
        }, UserPermissions.USER, this));

        commandHandler.addCommand(new SetJoinTextCommand(new String[]
        {
                "setjointext"
        }, UserPermissions.MASTER, this));

        commandHandler.addCommand(new JoinVoiceChannelCommand(new String[]
        {
                "joinme"
        }, UserPermissions.MASTER, this));

        commandHandler.addCommand(new SetLeaveTextCommand(new String[]
        {
                "setleavetext"
        }, UserPermissions.MASTER, this));

        commandHandler.addCommand(new LeaveVoiceChannelCommand(new String[]
        {
                "leaveme"
        }, UserPermissions.MASTER));

        commandHandler.addCommand(new SetPrefixCommand(new String[]
        {
                "setprefix"
        }, UserPermissions.OWNER, this));

        commandHandler.addCommand(new GetPrefixCommand(new String[]
        {
                "prefix"
        }, UserPermissions.USER, this));

        commandHandler.addCommand(new UrbanLookUpCommand(new String[]
        {
                "urbandictionary", "urban"
        }, UserPermissions.USER, bot, this));
        commandHandler.addCommand(new DescribesLookUpCommand(new String[]
        {
                "describes", "describe"
        }, UserPermissions.USER, bot, this));
        commandHandler.addCommand(new DescribeWithLookUpCommand(new String[]
        {
                "describewith", "descwith"
        }, UserPermissions.USER, bot, this));
        commandHandler.addCommand(new SynonymLookUpCommand(new String[]
        {
                "synonym", "syn"
        }, UserPermissions.USER, bot, this));
        commandHandler.addCommand(new AntonymLookUpCommand(new String[]
        {
                "antonym", "ant"
        }, UserPermissions.USER, bot, this));
        commandHandler.addCommand(new RhymeLookUpCommand(new String[]
        {
                "rhyme", "rhymes"
        }, UserPermissions.USER, bot, this));
        commandHandler.addCommand(new WordnetLookUpCommand(new String[]
        {
                "define", "wordnet"
        }, UserPermissions.USER, bot, this));
        commandHandler.addCommand(new WikipediaLookUpCommand(new String[]
        {
                "wikipedia", "wiki"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new AddAdditionalTextCommand(new String[]
        {
                "addtext"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new RemoveAdditionalTextCommand(new String[]
        {
                "removetext"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ChangeKeepCountCommand(new String[]
        {
                "keep"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new CheckFeedCommand(new String[]
        {
                "check"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new FeedSettingsCommand(new String[]
        {
                "settings"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ExportFeedsCommand(new String[]
        {
                "exportfeeds"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ImportFeedsCommand(new String[]
        {
                "importfeeds"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new RemoveFeedCommand(new String[]
        {
                "unsubscribe", "unsub"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new AddFeedCommand(new String[]
        {
                "subscribe", "sub"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new GetYoutubeChannelFeedCommand(new String[]
        {
                "youtubefeed"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ShowFeedsCommand(new String[]
        {
                "showfeeds"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new AutomateCommand(new String[]
        {
                "auto", "automate"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new RemoveAutomatedCommand(new String[]
        {
                "stopauto"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ShowAutomatedCommandsCommand(new String[]
        {
                "showauto", "showautomated"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new RandomAllCommand(new String[]
        {
                "randomall", "all"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new RandomChannelCommand(new String[]
        {
                "randomchannel", "channel"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new RandomOnlineCommand(new String[]
        {
                "randomonline", "online"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new RandomCommand(new String[]
        {
                "random"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new RandomNumberCommand(new String[]
        {
                "roll"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new TeamCommand(new String[]
        {
                "team"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new TeamChannelCommand(new String[]
        {
                "teamchannel"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new TeamFromGroupCommand(new String[]
        {
                "teamgroup"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new ShowGroupsCommand(new String[]
        {
                "showgroups"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new ShowListsCommand(new String[]
        {
                "showlists"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new JoinGroupCommand(new String[]
        {
                "joingroup"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new LeaveGroupCommand(new String[]
        {
                "leavegroup"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new CreateListCommand(new String[]
        {
                "list"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new RandomFromCollectionCommand(new String[]
        {
                "pick"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new PickAndRemoveFromCollectionCommand(new String[]
        {
                "pickremove"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new CreateGroupCommand(new String[]
        {
                "group"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new CloseCollectionCommand(new String[]
        {
                "close"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ShowCollectionMembersCommand(new String[]
        {
                "members"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new AddToListCommand(new String[]
        {
                "addmember"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new RemoveFromCollectionCommand(new String[]
        {
                "removemember"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new M8Command(new String[]
        {
                "8ball"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new OverridePermissionCommand(new String[]
        {
                "override"
        }, UserPermissions.OWNER, bot, this));

        commandHandler.addCommand(new GetPermissionLevelCommand(new String[]
        {
                "permissions", "perms"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new StatisticCommand(new String[]
        {
                "stats", "statistics"
        }, UserPermissions.USER, bot, this));

        commandHandler.addCommand(new ShowMastersCommand(new String[]
        {
                "showmasters", "showowners"
        }, UserPermissions.USER, bot));

        commandHandler.addCommand(new AddLineCommand(new String[]
        {
                "addline"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new DeleteLineCommand(new String[]
        {
                "delline"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new DeleteAllLinesCommand(new String[]
        {
                "delalllines"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ExportLinesCommand(new String[]
        {
                "exportlines"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ImportLinesCommand(new String[]
        {
                "importlines"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new DeleteLineIndexCommand(new String[]
        {
                "delindex"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new AddGlobalLineCommand(new String[]
        {
                "addgloballine"
        }, UserPermissions.CREATOR, bot, this));

        commandHandler.addCommand(new DeleteGlobalLineCommand(new String[]
        {
                "delgloballine"
        }, UserPermissions.CREATOR, bot, this));

        commandHandler.addCommand(new ToggleGlobalCommand(new String[]
        {
                "toggleglobal"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new TogglePlainTextCommand(new String[]
        {
                "toggleplain"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new ShowLinesCommand(new String[]
        {
                "showlines"
        }, UserPermissions.MASTER, bot, this));

        commandHandler.addCommand(new AddMasterCommand(new String[]
        {
                "master", "addmaster"
        }, UserPermissions.OWNER, bot, this));

        commandHandler.addCommand(new RemoveMasterCommand(new String[]
        {
                "nomaster", "removemaster"
        }, UserPermissions.OWNER, bot, this));

        commandHandler.addCommand(new AddOwnerCommand(new String[]
        {
                "owner", "addowner"
        }, UserPermissions.OWNER, bot, this));

        commandHandler.addCommand(new RemoveOwnerCommand(new String[]
        {
                "noowner", "removeowner"
        }, UserPermissions.OWNER, bot, this));

        commandHandler.addCommand(new BanUserCommand(new String[]
        {
                "ban"
        }, UserPermissions.CREATOR, bot, this));

        commandHandler.addCommand(new UnbanUserCommand(new String[]
        {
                "unban"
        }, UserPermissions.CREATOR, bot, this));

        commandHandler.addCommand(new DmAllCommand(new String[]
        {
                "dmall"
        }, UserPermissions.CREATOR, bot, this));

        commandHandler.addCommand(new DiskSpaceCommand(new String[]
        {
                "disk"
        }, UserPermissions.CREATOR, bot));

        commandHandler.addCommand(new MemoryCommand(new String[]
        {
                "ram"
        }, UserPermissions.CREATOR, bot));

        commandHandler.addCommand(new PatchCommand(new String[]
        {
                "patch"
        }, UserPermissions.CREATOR, bot, this));

        commandHandler.addCommand(new RebootCommand(new String[]
        {
                "reboot", "restart"
        }, UserPermissions.CREATOR, this));

        commandHandler.addCommand(new ShutdownCommand(new String[]
        {
                "shutdown", "off"
        }, UserPermissions.CREATOR, bot, this));

        commandHandler.addCommand(new StatusCommand(new String[]
        {
                "status"
        }, UserPermissions.CREATOR, bot));

        commandHandler.addCommand(new ThreadCountCommand(new String[]
        {
                "threads", "thread"
        }, UserPermissions.CREATOR, bot));

        commandHandler.addCommand(new VersionCommand(new String[]
        {
                "version", "v"
        }, UserPermissions.CREATOR, bot));
    }

    public Bot getBot()
    {
        return this.bot;
    }

    public DatabaseAccess getDatabase()
    {
        return this.database;
    }
}