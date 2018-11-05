package bowtie.core.db;

import java.awt.Color;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.db.Database;
import bowt.guild.GuildObject;
import bowtie.bot.auto.AutomatedCommand;
import bowtie.bot.auto.DummyMessage;
import bowtie.bot.auto.SerializableDummyMessage;
import bowtie.bot.collections.CollectionTimer;
import bowtie.bot.collections.group.Group;
import bowtie.bot.collections.group.GroupManager;
import bowtie.bot.collections.list.ItemList;
import bowtie.bot.collections.list.ListManager;
import bowtie.bot.hand.GuildSetupHandler;
import bowtie.bot.sound.SoundManager;
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
public class DatabaseAccess extends Database
{
    /** Indicates that the given word is known to the bot. */
    public static final int KNOWN = 1;
    /** Indicates that something is unknown to the bot. */
    public static final int UNKNOWN = 2;
    /** Indicates that the database action was succesfully executed. */
    public static final int SUCCESS = 3;
    public static final int ACTIVE = 4;
    public static final int NOT_ACTIVE = 5;
    public static final int GLOBAL = 6;
    public static final int NOT_GLOBAL = 7;
    public static final int PLAIN = 8;
    public static final int NOT_PLAIN = 9;
    private Main main;

    public DatabaseAccess(Main main)
    {
        super();
        this.main = main;
    }

    /**
     * @see bowt.db.Database#createTables()
     */
    @Override
    protected void createTables()
    {
        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE voteCount ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "userID VARCHAR(100), "
                    + "votes INTEGER)");
            Bot.log.print(this, "Created voteCount table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table voteCount does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE voiceName ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "command VARCHAR(50), "
                    + "userID VARCHAR(100), "
                    + "userName VARCHAR(30))");
            Bot.log.print(this, "Created voiceName table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table voiceName does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE commandAlias ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "command VARCHAR(50), "
                    + "alias VARCHAR(50), "
                    + "guildID VARCHAR(100))");
            Bot.log.print(this, "Created commandAlias table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table commandAlias does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE guildPrefix ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "prefix VARCHAR(200), "
                    + "guildID VARCHAR(100))");
            Bot.log.print(this, "Created guildPrefix table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table guildPrefix does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE updateChannel ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "channelID VARCHAR(100), "
                    + "updateType VARCHAR(20))");
            Bot.log.print(this, "Created updateChannel table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table updateChannel does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE defaultChannel ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "channelID VARCHAR(100), "
                    + "guildID VARCHAR(100))");
            Bot.log.print(this, "Created defaultChannel table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table defaultChannel does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE voiceJoinText ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "joinText VARCHAR(300), "
                    + "guildID VARCHAR(100))");
            Bot.log.print(this, "Created voiceJoinText table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table voiceJoinText does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE voiceLeaveText ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "leaveText VARCHAR(300), "
                    + "guildID VARCHAR(100))");
            Bot.log.print(this, "Created voiceLeaveText table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table voiceLeaveText does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE activatedGuild ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "userID VARCHAR(100), "
                    + "guildID VARCHAR(100))");
            Bot.log.print(this, "Created activatedGuild table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table activatedGuild does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE tempPremium ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "userID VARCHAR(100), "
                    + "expDate BIGINT)");
            Bot.log.print(this, "Created tempPremium table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table tempPremium does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE additionalTexts ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "feedLink VARCHAR(2000), "
                    + "addText VARCHAR(2000))");
            Bot.log.print(this, "Created additionalTexts table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table additionalTexts does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE links ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "channelID VARCHAR(100), "
                    + "feedLink VARCHAR(2000), "
                    + "itemLink VARCHAR(2000))");
            Bot.log.print(this, "Created links table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table links does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE sentMessages ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "channelID VARCHAR(100), "
                    + "feedLink VARCHAR(2000), "
                    + "itemLink VARCHAR(2000), "
                    + "messageID VARCHAR(100), "
                    + "sendTime TIMESTAMP)");
            Bot.log.print(this, "Created sentMessages table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table sentMessages does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE feeds ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "channelID VARCHAR(100), "
                    + "link VARCHAR(2000), "
                    + "red varchar(3), "
                    + "green varchar(3), "
                    + "blue varchar(3), "
                    + "keepCount VARCHAR(10), "
                    + "withImage boolean, "
                    + "withText boolean, "
                    + "withIcon boolean, "
                    + "withTitle boolean, "
                    + "withFooter boolean)");
            Bot.log.print(this, "Created feeds table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table feeds does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE automatedCommand ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "autoID VARCHAR(100), "
                    + "guildID VARCHAR(100), "
                    + "interval VARCHAR(100), "
                    + "lastExecute VARCHAR(100), "
                    + "message BLOB)");
            Bot.log.print(this, "Created automatedCommand table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table automatedCommand does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE groupTimers ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "destroyTime VARCHAR(100), "
                    + "groupName VARCHAR(100))");
            Bot.log.print(this, "Created groupTimers table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table groupTimers does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE userGroups ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "date VARCHAR(15), "
                    + "groupName VARCHAR(100))");
            Bot.log.print(this, "Created userGroups table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table userGroups does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE groupMembers ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "userID VARCHAR(100), "
                    + "groupName VARCHAR(100))");
            Bot.log.print(this, "Created groupMembers table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table groupMembers does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE listTimers ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "destroyTime VARCHAR(100), "
                    + "listName VARCHAR(100))");
            Bot.log.print(this, "Created listTimers table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table listTimers does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE itemLists ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "date VARCHAR(15), "
                    + "listName VARCHAR(100))");
            Bot.log.print(this, "Created itemLists table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table itemLists does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE listMembers ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "item VARCHAR(2000), "
                    + "listName VARCHAR(100))");
            Bot.log.print(this, "Created listMembers table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table listMembers does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE lines ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "text VARCHAR(500),"
                    + "guildID VARCHAR(100))");
            Bot.log.print(this, "Created lines table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table lines does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE globalActive ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100),"
                    + "globalAct VARCHAR(1))");
            Bot.log.print(this, "Created globalActive table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table globalActive does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE plaintextActive ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100),"
                    + "plainAct VARCHAR(1))");
            Bot.log.print(this, "Created globalActive table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table plaintextActive does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE commandOverrides ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100), "
                    + "command VARCHAR(100), "
                    + "permissionLevel VARCHAR(10))");
            Bot.log.print(this, "Created commandOverrides table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table commandOverrides does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE masters ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "guildID VARCHAR(100),"
                    + "masterID VARCHAR(100),"
                    + "level VARCHAR(1))");
            Bot.log.print(this, "Created masters table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table masters does already exist.");
        }

        try (Statement statement = this.getConnection().createStatement())
        {
            statement.execute("CREATE TABLE banned ("
                    + "ID INTEGER NOT NULL "
                    + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                    + "(START WITH 1, INCREMENT BY 1),"
                    + "userID VARCHAR(100))");
            Bot.log.print(this, "Created banned table.");
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, "Table banned does already exist.");
        }
    }

    public List<ManagedFeed> getFeeds(GuildObject guild)
    {
        List<ManagedFeed> feeds = new ArrayList<>();
        List<String[]> delete = new ArrayList<>();

        String sql = "SELECT "
                + "channelID, "
                + "link,"
                + "red, "
                + "blue, "
                + "green,"
                + "keepCount, "
                + "id, "
                + "withImage, "
                + "withTitle, "
                + "withText, "
                + "withIcon, "
                + "withFooter "
                + "FROM feeds WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guild.getStringID());

            try (ResultSet result = statement.executeQuery())
            {
                int count = 0;
                while (result.next())
                {
                    long channelID = result.getLong("channelID");
                    IChannel channel = main.getBot().getClient().getChannelByID(channelID);

                    if (channel != null)
                    {
                        RssFeed rssFeed;
                        try
                        {
                            rssFeed = FeedUtils.getFeedFromUrl(result.getString("link"));
                            rssFeed.setup();
                        }
                        catch (FeedNotFoundException e1)
                        {
                            continue;
                        }
                        catch (NoValidEntriesException e2)
                        {
                            continue;
                        }

                        int keepCount = result.getInt("keepCount");
                        ManagedFeed managedFeed = new ManagedFeed(rssFeed, main, guild, channel,
                                result.getString("link"),
                                keepCount == 0 ? -1 : keepCount,
                                new Color(result.getInt("red"), result.getInt("green"), result.getInt("blue")));
                        if (FeedManager.add(managedFeed))
                        {
                            feeds.add(managedFeed);
                            managedFeed.setImageOn(result.getBoolean("withImage"));
                            managedFeed.setIconOn(result.getBoolean("withIcon"));
                            managedFeed.setTextOn(result.getBoolean("withText"));
                            managedFeed.setTitleOn(result.getBoolean("withTitle"));
                            managedFeed.setFooterOn(result.getBoolean("withFooter"));
                            loadAdditionalText(managedFeed);
                        }
                    }
                    else
                    {
                        delete.add(new String[]
                        {
                                guild.getStringID(), result.getString("link")
                        });
                    }
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        for (String[] sAr : delete)
        {
            removeFeed(sAr[0], sAr[1]);
        }

        return feeds;
    }

    public void addMessage(GuildObject guild, String feedLink, String itemLink, long channelID, long messageID)
    {
        String sql = "INSERT INTO sentMessages (guildID, channelID, messageID, feedLink, itemLink, sendTime) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guild.getStringID());
            statement.setLong(2, channelID);
            statement.setLong(3, messageID);
            statement.setString(4, feedLink);
            statement.setString(5, itemLink);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void loadCommandAliases(Command command)
    {
        String sql = "SELECT alias, guildID FROM commandAlias WHERE command = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, command.getValidExpressions().get(0));
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    command.addAlias(result.getString("guildID"), result.getString("alias"));
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void addCommandAlias(String guildID, String command, String alias)
    {
        String sql = "INSERT INTO commandAlias (alias, command, guildID) VALUES (?, ?, ?)";

        if (commandAliasExists(guildID, command))
        {
            sql = "UPDATE commandAlias SET alias = ? WHERE command = ? AND guildID = ?";
        }

        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, alias);
            statement.setString(2, command);
            statement.setString(3, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean commandAliasExists(String guildID, String command)
    {
        String sql = "SELECT alias FROM commandAlias WHERE command = ? AND guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, command);
            statement.setString(2, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public String getVoiceName(String userID)
    {
        String sql = "SELECT userName FROM voiceName WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    return result.getString("userName");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public void setVoiceName(String userID, String name)
    {
        String sql = "INSERT INTO voiceName (userName, userID) VALUES (?, ?)";

        if (getVoiceName(userID) != null)
        {
            sql = "UPDATE voiceName SET userName = ? WHERE userID = ?";
        }

        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, name);
            statement.setString(2, userID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean isExistingUpdateChannel(String channelID, String type)
    {
        String sql = "SELECT channelID FROM updateChannel WHERE channelID = ? AND updateType = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, channelID);
            statement.setString(2, type);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public List<String> getUpdateChannels(String type)
    {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT channelID FROM updateChannel WHERE updateType = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, type);
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    ids.add(result.getString("channelID"));
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return ids;
    }

    public void resetUpdateChannel(String channelID)
    {
        String sql = "DELETE FROM updateChannel WHERE channelID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, channelID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void resetUpdateChannel(String channelID, String type)
    {
        String sql = "DELETE FROM updateChannel WHERE channelID = ? AND updateType = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, channelID);
            statement.setString(2, type);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void setUpdateChannel(String channelID, String type)
    {
        if (!isExistingUpdateChannel(channelID, type))
        {
            String sql = "INSERT INTO updateChannel (channelID, updateType) VALUES (?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, channelID);
                statement.setString(2, type);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
    }

    public void resetVoteCounts()
    {
        String sql = "DELETE FROM voteCount";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void setVoteCount(String userID, int count)
    {
        String sql = "INSERT INTO voteCount (votes, userID) VALUES (?, ?)";

        if (getVoteCount(userID) != -1)
        {
            sql = "UPDATE voteCount SET votes = ? WHERE userID = ?";
        }

        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setInt(1, count);
            statement.setString(2, userID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public int getVoteCount(String userID)
    {
        String sql = "SELECT votes FROM voteCount WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    return result.getInt("votes");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return -1;
    }

    public int getJoinTextCount()
    {
        int count = 0;
        String sql = "SELECT COUNT(joinText) as counter FROM voiceJoinText WHERE joinText <> ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, SoundManager.DEFAULT_JOIN_TEXT);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    count = result.getInt("counter");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return count;
    }

    public String getJoinText(String guildID)
    {
        if (!isActivatedGuild(guildID))
        {
            return SoundManager.DEFAULT_JOIN_TEXT;
        }

        String text = null;

        String sql = "SELECT joinText FROM voiceJoinText WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    text = result.getString("joinText");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        if (text == null)
        {
            text = SoundManager.DEFAULT_JOIN_TEXT;

            sql = "INSERT INTO voiceJoinText (guildID, joinText) VALUES (?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, text);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }

        return text;
    }

    public void setJoinText(String guildID, String text)
    {
        getJoinText(guildID);

        String sql = "UPDATE voiceJoinText SET joinText = ? WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, text);
            statement.setString(2, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public int getLeaveTextCount()
    {
        int count = 0;
        String sql = "SELECT COUNT(leaveText) as counter FROM voiceLeaveText WHERE leaveText <> ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, SoundManager.DEFAULT_LEAVE_TEXT);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    count = result.getInt("counter");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return count;
    }

    public String getLeaveText(String guildID)
    {
        if (!isActivatedGuild(guildID))
        {
            return SoundManager.DEFAULT_LEAVE_TEXT;
        }

        String text = null;

        String sql = "SELECT leaveText FROM voiceLeaveText WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    text = result.getString("leaveText");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        if (text == null)
        {
            text = SoundManager.DEFAULT_LEAVE_TEXT;

            sql = "INSERT INTO voiceLeaveText (guildID, leaveText) VALUES (?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, text);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }

        return text;
    }

    public void setLeaveText(String guildID, String text)
    {
        getLeaveText(guildID);

        String sql = "UPDATE voiceLeaveText SET leaveText = ? WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, text);
            statement.setString(2, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void setDefaultVoiceChannel(String guildID, String channelID)
    {
        if (getDefaultVoiceChannel(guildID) != null)
        {
            updateDefaultVoiceChannel(guildID, channelID);
            return;
        }

        String sql = "INSERT INTO defaultChannel (guildID, channelID) VALUES (?, ?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, channelID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void resetDefaultVoiceChannel(String guildID)
    {
        String sql = "DELETE FROM defaultChannel WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public String getDefaultVoiceChannel(String guildID)
    {
        String channelID = null;

        String sql = "SELECT channelID FROM defaultChannel WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    channelID = result.getString("channelID");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return channelID;
    }

    public void updateDefaultVoiceChannel(String guildID, String channelID)
    {
        String sql = "UPDATE defaultChannel SET channelID = ? WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, channelID);
            statement.setString(2, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public int getPrefixCount()
    {
        int count = 0;
        String sql = "SELECT COUNT(prefix) as counter FROM guildPrefix WHERE prefix <> ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, Bot.getPrefix());
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    count = result.getInt("counter");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return count;
    }

    public void addPrefix(String guildID, String prefix)
    {
        if (getPrefix(guildID) != null)
        {
            updatePrefix(guildID, prefix);
            return;
        }

        String sql = "INSERT INTO guildPrefix (guildID, prefix) VALUES (?, ?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, prefix);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public String getPrefix(String guildID)
    {
        String prefix = null;

        String sql = "SELECT prefix FROM guildPrefix WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    prefix = result.getString("prefix");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return prefix;
    }

    public void updatePrefix(String guildID, String prefix)
    {
        String sql = "UPDATE guildPrefix SET prefix = ? WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, prefix);
            statement.setString(2, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void removeMessage(GuildObject guild, String feedLink, String itemLink, long messageID)
    {
        String sql = "DELETE FROM sentMessages WHERE guildID = ? AND feedLink = ? AND messageID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guild.getStringID());
            statement.setString(2, feedLink);
            statement.setLong(3, messageID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public List<Long> removeOldMessages(GuildObject guild, String feedLink, int remain)
    {
        List<Long> messageIDs = new ArrayList<>();
        String sql = "SELECT messageID, itemLink FROM sentMessages WHERE guildID = ? AND feedLink = ? ORDER BY sendTime asc";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guild.getStringID());
            statement.setString(2, feedLink);
            try (ResultSet result = statement.executeQuery())
            {
                List<String[]> rowList = new ArrayList<>();

                while (result.next())
                {
                    rowList.add(0, new String[]
                    {
                            Long.toString(result.getLong("messageID")), result.getString("itemLink")
                    });
                }

                for (int i = remain; i < rowList.size(); i ++ )
                {
                    long id = Long.parseLong(rowList.get(i)[0]);
                    messageIDs.add(id);
                    removeMessage(guild, feedLink, rowList.get(i)[1], id);
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return messageIDs;
    }

    public void addLink(GuildObject guild, String itemLink, String feedLink, long channelID)
    {
        String sql = "INSERT INTO links (guildID, channelID, itemLink, feedLink) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guild.getStringID());
            statement.setLong(2, channelID);
            statement.setString(3, itemLink);
            statement.setString(4, feedLink);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void removeLink(GuildObject guild, String itemLink, String feedLink, long channelID)
    {
        String sql = "DELETE FROM links WHERE guildID = ? AND itemLink = ? AND feedLink = ? and channelID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guild.getStringID());
            statement.setString(2, itemLink);
            statement.setString(3, feedLink);
            statement.setLong(4, channelID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean isExistingLink(GuildObject guild, String itemLink, String feedLink, long channelID)
    {
        String sql = "SELECT itemLink FROM links WHERE guildID = ? AND channelID = ? AND itemLink = ? AND feedLink = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guild.getStringID());
            statement.setLong(2, channelID);
            statement.setString(3, itemLink);
            statement.setString(4, feedLink);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public int getFeedCount()
    {
        String sql = "SELECT COUNT(guildID) AS counter FROM feeds";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    return result.getInt("counter");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return -1;
    }

    public int getFeedCount(String guildID)
    {
        String sql = "SELECT COUNT(guildID) AS counter FROM feeds WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    return result.getInt("counter");
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return -1;
    }

    public boolean addFeed(ManagedFeed feed)
    {
        String sql = "INSERT INTO feeds (guildID, channelID, link, keepCount, red, green, blue) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, feed.getGuildObject().getStringID());
            statement.setLong(2, feed.getChannelID());
            statement.setString(3, feed.getLink());
            statement.setInt(4, feed.getKeepCount());
            statement.setInt(5, feed.getColor().getRed());
            statement.setInt(6, feed.getColor().getGreen());
            statement.setInt(7, feed.getColor().getBlue());
            statement.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public void updateFeedKeepCount(ManagedFeed feed)
    {
        String sql = "UPDATE feeds SET keepCount = ? WHERE guildID = ? AND link = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setInt(1, feed.getKeepCount());
            statement.setString(2, feed.getGuildObject().getStringID());
            statement.setString(3, feed.getLink());
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void updateFeedSettings(ManagedFeed feed)
    {
        String sql = "UPDATE feeds SET withImage = ?, withIcon = ?, withText = ?, withTitle = ?, "
                + "withFooter = ? WHERE guildID = ? AND link = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setBoolean(1, feed.isImageOn());
            statement.setBoolean(2, feed.isIconOn());
            statement.setBoolean(3, feed.isTextOn());
            statement.setBoolean(4, feed.isTitleOn());
            statement.setBoolean(5, feed.isFooterOn());
            statement.setString(6, feed.getGuildObject().getStringID());
            statement.setString(7, feed.getLink());
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void loadAdditionalText(ManagedFeed feed)
    {
        String sql = "SELECT addText FROM additionalTexts WHERE guildID = ? AND feedLink = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, feed.getGuildObject().getStringID());
            statement.setString(2, feed.getLink());
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    feed.setAdditionalText(result.getString("addText"));
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean hasAdditionalText(ManagedFeed feed)
    {
        String sql = "SELECT addText FROM additionalTexts WHERE guildID = ? AND feedLink = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, feed.getGuildObject().getStringID());
            statement.setString(2, feed.getLink());
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    return true;
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public void addAdditionalText(ManagedFeed feed, String text)
    {
        String sql = "";
        if (hasAdditionalText(feed))
        {
            sql = "UPDATE additionalTexts SET addText = ? WHERE guildID = ? AND feedLink = ?";
        }
        else
        {
            sql = "INSERT INTO additionalTexts (addText, guildID, feedLink) VALUES (?, ?, ?)";
        }
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, text);
            statement.setString(2, feed.getGuildObject().getStringID());
            statement.setString(3, feed.getLink());
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void removeAdditionalText(ManagedFeed feed)
    {
        String sql = "DELETE FROM additionalTexts WHERE guildID = ? AND feedLink = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, feed.getGuildObject().getStringID());
            statement.setString(2, feed.getLink());
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void clearLinks()
    {
        String sql = "DELETE FROM links";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean removeFeed(ManagedFeed feed)
    {
        return removeFeed(feed.getGuildObject().getStringID(), feed.getLink());
    }

    public boolean removeFeed(String guildID, String link)
    {
        String sql = "DELETE FROM feeds WHERE guildID = ? AND link = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, link);
            statement.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        return false;
    }

    public void clearFeeds(String guildID)
    {
        String sql = "DELETE FROM feeds WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        sql = "DELETE FROM links WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        sql = "DELETE FROM sentMessages WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        sql = "DELETE FROM additionalTexts WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean isKnownLine(String line, IGuild guild)
    {
        return this.isKnownLine(line, guild.getStringID());
    }

    public boolean isKnownLine(String line, GuildObject guild)
    {
        return this.isKnownLine(line, guild.getStringID());
    }

    public boolean isKnownLine(String line, String guildID)
    {

        String sql = "SELECT guildID FROM lines WHERE text = ? AND guildID = ?";
        boolean globalAct = false;
        if (this.isGlobalLinesActive(guildID) == GLOBAL)
        {
            globalAct = true;
            sql = "SELECT guildID FROM lines WHERE text = ? AND (guildID = ? OR guildID = ?)";
        }
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, line);
            statement.setString(2, guildID);
            if (globalAct)
            {
                statement.setString(3, "-1");
            }
            try (ResultSet results = statement.executeQuery())
            {
                return results.next();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
            ;
        }
        return false;
    }

    public int addLine(String line, IGuild guild)
    {
        return this.addLine(line, guild.getStringID());
    }

    public int addLine(String line, GuildObject guild)
    {
        return this.addLine(line, guild.getStringID());
    }

    public int addLine(String line, String guildID)
    {
        if (!this.isKnownLine(line, guildID))
        {
            String sql = "INSERT INTO lines (text, guildID) VALUES (?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, line);
                statement.setString(2, guildID);
                statement.executeUpdate();
                return SUCCESS;
            }
            catch (SQLException e)
            {
                Bot.log.print(this, e);
            }
        }
        return KNOWN;
    }

    public int removeLine(String line, IGuild guild)
    {
        return this.removeLine(line, guild.getStringID());
    }

    public int removeLine(String line, GuildObject guild)
    {
        return this.removeLine(line, guild.getStringID());
    }

    public int removeLine(String line, String guildID)
    {
        if (this.isKnownLine(line, guildID))
        {
            String sql = "DELETE FROM lines WHERE text = ? AND guildID = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, line);
                statement.setString(2, guildID);
                if (statement.executeUpdate() != 0)
                {
                    return SUCCESS;
                }
            }
            catch (SQLException e)
            {
                Bot.log.print(this, e);
                ;
            }
        }
        return UNKNOWN;
    }

    public void removeLines(GuildObject guild)
    {
        this.removeLines(guild.getStringID());
    }

    public void removeLines(IGuild guild)
    {
        this.removeLines(guild.getStringID());
    }

    public void removeLines(String guildID)
    {
        String sql = "DELETE FROM lines WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
            ;
        }
    }

    public List<String> getLines(IGuild guild)
    {
        return this.getLines(guild.getStringID());
    }

    public List<String> getLines(GuildObject guild)
    {
        return this.getLines(guild.getStringID());
    }

    public List<String> getLines(String guildID)
    {
        String sql = "SELECT text FROM lines WHERE guildID = ?";
        boolean globalAct = false;
        if (this.isGlobalLinesActive(guildID) == GLOBAL)
        {
            globalAct = true;
            sql = "SELECT text FROM lines WHERE guildID = ? OR guildID = ?";
        }
        List<String> lines = new ArrayList<String>();
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            if (globalAct)
            {
                statement.setString(2, "-1");
            }
            try (ResultSet results = statement.executeQuery())
            {
                while (results.next())
                {
                    lines.add(results.getString("text"));
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
            ;
        }
        return lines;
    }

    public int getLineCount(IGuild guild)
    {
        return this.getLineCount(guild.getStringID());
    }

    public int getLineCount(GuildObject guild)
    {
        return this.getLineCount(guild.getStringID());
    }

    public int getLineCount(String guildID)
    {
        String sql = "SELECT COUNT(text) as lineCount FROM lines WHERE guildID = ?";
        int count = 0;
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                result.next();
                count = result.getInt("lineCount");
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
        }
        return count;
    }

    public int getTotalLineCount()
    {
        String sql = "SELECT COUNT(text) as lineCount FROM lines";
        int count = 0;
        try (Statement statement = this.getConnection().createStatement())
        {
            try (ResultSet result = statement.executeQuery(sql))
            {
                result.next();
                count = result.getInt("lineCount");
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
        }
        return count;
    }

    public int isPlainTextActive(GuildObject guild)
    {
        return this.isPlainTextActive(guild.getStringID());
    }

    public int isPlainTextActive(IGuild guild)
    {
        return this.isPlainTextActive(guild.getStringID());
    }

    public int isPlainTextActive(String guildID)
    {
        String sql = "SELECT plainAct FROM plaintextActive where guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    int plainAct = result.getInt("plainAct");
                    if (plainAct == 1)
                    {
                        return PLAIN;
                    }
                    else
                    {
                        return NOT_PLAIN;
                    }
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
            ;
        }
        return UNKNOWN;
    }

    public void setPlainTextActive(GuildObject guild, boolean active)
    {
        this.setPlainTextActive(guild.getStringID(), active);
    }

    public void setPlainTextActive(IGuild guild, boolean active)
    {
        this.setPlainTextActive(guild.getStringID(), active);
    }

    public void setPlainTextActive(String guildID, boolean active)
    {
        String sql = "";
        if (this.isPlainTextActive(guildID) == UNKNOWN)
        {
            sql = "INSERT INTO plaintextActive (plainAct, guildID) VALUES (?, ?)";
        }
        else
        {
            sql = "UPDATE plaintextActive SET plainAct = ? WHERE guildID = ?";
        }
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setInt(1, active ? 1 : 0);
            statement.setString(2, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
            ;
        }
    }

    public int isGlobalLinesActive(GuildObject guild)
    {
        return this.isGlobalLinesActive(guild.getStringID());
    }

    public int isGlobalLinesActive(IGuild guild)
    {
        return this.isGlobalLinesActive(guild.getStringID());
    }

    public int isGlobalLinesActive(String guildID)
    {
        String sql = "SELECT globalAct FROM globalActive where guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    int globalAct = result.getInt("globalAct");
                    if (globalAct == 1)
                    {
                        return GLOBAL;
                    }
                    else
                    {
                        return NOT_GLOBAL;
                    }
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
            ;
        }
        return UNKNOWN;
    }

    public void setGlobalLinesActive(GuildObject guild, boolean active)
    {
        this.setGlobalLinesActive(guild.getStringID(), active);
    }

    public void setGlobalLinesActive(IGuild guild, boolean active)
    {
        this.setGlobalLinesActive(guild.getStringID(), active);
    }

    public void setGlobalLinesActive(String guildID, boolean active)
    {
        String sql = "";
        if (this.isGlobalLinesActive(guildID) == UNKNOWN)
        {
            sql = "INSERT INTO globalActive (globalAct, guildID) VALUES (?, ?)";
        }
        else
        {
            sql = "UPDATE globalActive SET globalAct = ? WHERE guildID = ?";
        }
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setInt(1, active ? 1 : 0);
            statement.setString(2, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.log.print(this, e);
            ;
        }
    }

    public void grantTempPremium(String userID, long expDate)
    {
        String sql = "INSERT INTO tempPremium (userID, expDate) VALUES (?, ?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            statement.setLong(2, expDate);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean isTempPremium(String userID)
    {
        String sql = "SELECT userID FROM tempPremium WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public long getExpDate(String userID)
    {
        String sql = "SELECT expDate FROM tempPremium WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    return result.getLong("expDate");
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return -1;
    }

    public void revokeTempPremium(String userID)
    {
        String sql = "DELETE FROM tempPremium WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean isActivatedGuild(String guildID)
    {
        if (!Main.NEEDS_ACTIVATION)
        {
            return true;
        }
        String sql = "SELECT guildID FROM activatedGuild WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public int activationCount(String userID)
    {
        int count = 0;
        String sql = "SELECT COUNT(guildID) as guilds FROM activatedGuild WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            try (ResultSet result = statement.executeQuery())
            {
                result.next();
                count = result.getInt("guilds");
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return count;
    }

    public int activationCount()
    {
        int count = 0;
        String sql = "SELECT COUNT(guildID) as guilds FROM activatedGuild";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            try (ResultSet result = statement.executeQuery())
            {
                result.next();
                count = result.getInt("guilds");
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return count;
    }

    public List<String> getUsersForGuild(String guildID)
    {
        List<String> users = new ArrayList<String>();
        String sql = "SELECT userID FROM activatedGuild WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    users.add(result.getString("userID"));
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return users;
    }

    public List<String> getGuildsForUser(String userID)
    {
        List<String> guilds = new ArrayList<String>();
        String sql = "SELECT guildID FROM activatedGuild WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    guilds.add(result.getString("guildID"));
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return guilds;
    }

    public boolean activateGuild(String guildID, String userID)
    {
        if (getUsersForGuild(guildID).contains(userID))
        {
            return false;
        }

        String sql = "INSERT INTO activatedGuild (guildID, userID) VALUES (?, ?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, userID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return true;
    }

    public boolean hasActivated(String guildID, String userID)
    {
        String sql = "SELECT userID FROM activatedGuild WHERE guildID = ? AND userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, userID);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public List<String[]> getActivatedGuilds()
    {
        List<String[]> ids = new ArrayList<>();
        String sql = "SELECT userID, guildID FROM activatedGuild";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    String[] pair = new String[2];

                    pair[0] = result.getString("guildID");
                    pair[1] = result.getString("userID");

                    ids.add(pair);
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return ids;
    }

    public boolean deactivateGuild(String guildID, String userID)
    {
        String sql = "DELETE FROM activatedGuild WHERE guildID = ? AND userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, userID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
            return false;
        }
        return true;
    }

    public boolean deactivateGuild(String guildID)
    {
        String sql = "DELETE FROM activatedGuild WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
            return false;
        }
        return true;
    }

    public void clearUserActivations(String userID)
    {
        String sql = "DELETE FROM activatedGuild WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean existingOverride(String guildID, String command)
    {
        String sql = "SELECT command FROM commandOverrides WHERE guildID = ? AND command = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, command);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public boolean addOverride(String guildID, String command, int permission)
    {
        if (!existingOverride(guildID, command))
        {
            String sql = "INSERT INTO commandOverrides (guildID, command, permissionLevel) VALUES (?, ?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, command);
                statement.setInt(3, permission);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public boolean removeOverride(String guildID, String command)
    {
        if (existingOverride(guildID, command))
        {
            String sql = "DELETE FROM commandOverrides WHERE guildID = ? AND command = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, command);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public int getOverride(String guildID, String command)
    {
        if (existingOverride(guildID, command))
        {
            String sql = "SELECT permissionLevel FROM commandOverrides WHERE guildID = ? AND command = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, command);
                try (ResultSet result = statement.executeQuery())
                {
                    return result.getInt("permissionLevel");
                }
                catch (Exception e)
                {
                    Bot.errorLog.print(this, e);
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return -1;
    }

    public Map<String, Integer> getOverridesForGuild(String guildID)
    {
        String sql = "SELECT command, permissionLevel FROM commandOverrides WHERE guildID = ?";
        Map<String, Integer> overrides = new HashMap<String, Integer>();
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    overrides.put(result.getString("command"), result.getInt("permissionLevel"));
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return overrides;
    }

    public boolean existingAutomatedCommand(long id)
    {
        String sql = "SELECT autoID FROM automatedCommand WHERE autoID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public boolean addAutomatedCommand(AutomatedCommand auto)
    {
        if (!existingAutomatedCommand(auto.getID()))
        {
            String sql = "INSERT INTO automatedCommand (autoID, guildID, interval, lastExecute, message) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = this.getConnection();
                    PreparedStatement statement = con.prepareStatement(sql))
            {
                statement.setLong(1, auto.getID());
                statement.setString(2, auto.getGuild().getStringID());
                statement.setLong(3, auto.getInterval());
                statement.setLong(4, auto.getLastExecuteTime());
                Blob blob = con.createBlob();
                blob.setBytes(1, SerializationUtils.serialize(new SerializableDummyMessage(auto.getMessage())));
                statement.setBlob(5, blob);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public List<AutomatedCommand> getAutomatedCommands()
    {
        List<AutomatedCommand> autoCommands = new ArrayList<>();
        String sql = "SELECT * FROM automatedCommand";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    long id = result.getLong("autoID");

                    if (id > AutomatedCommand.currentID)
                    {
                        AutomatedCommand.currentID = id + 1;
                    }

                    GuildObject guild = main.getBot().getGuildObjectByID(result.getString("guildID"));

                    if (guild == null)
                    {
                        IGuild iGuild = main.getBot().getClient()
                                .getGuildByID(Long.parseLong(result.getString("guildID")));

                        if (iGuild == null)
                        {
                            continue;
                        }
                        else
                        {
                            guild = GuildSetupHandler.setupGuildObject(iGuild, main, main.getBot());
                        }
                    }

                    Blob blob = result.getBlob("message");
                    SerializableDummyMessage ms = SerializationUtils.deserialize(blob.getBytes(1, (int)blob.length()));
                    DummyMessage message = new DummyMessage(main.getBot().getClient(), ms);

                    long interval = result.getLong("interval");
                    long lastExecute = result.getLong("lastExecute");
                    autoCommands.add(new AutomatedCommand(id, message, guild, main, interval, lastExecute));
                }
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return autoCommands;
    }

    public boolean removeAutomatedCommand(long id)
    {
        if (existingAutomatedCommand(id))
        {
            String sql = "DELETE FROM automatedCommand WHERE autoID = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setLong(1, id);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public boolean updateAutomatedCommand(long id, long lastExecute)
    {
        if (existingAutomatedCommand(id))
        {
            String sql = "UPDATE automatedCommand SET lastExecute = ? WHERE autoID = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setLong(1, lastExecute);
                statement.setLong(2, id);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public void clearAutomatedCommands(String guildID)
    {
        String sql = "DELETE FROM automatedCommand WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean existingGroup(String guildID, String groupName)
    {
        String sql = "SELECT groupName FROM userGroups WHERE guildID = ? AND groupName = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, groupName);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public boolean addGroup(String guildID, String groupName, String date)
    {
        if (!existingGroup(guildID, groupName))
        {
            String sql = "INSERT INTO userGroups (guildID, groupName, date) VALUES (?, ?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, groupName);
                statement.setString(3, date);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public boolean removeGroup(String guildID, String groupName)
    {
        if (existingGroup(guildID, groupName))
        {
            String sql = "DELETE FROM userGroups WHERE guildID = ? AND groupName = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, groupName);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }

            sql = "DELETE FROM groupMembers WHERE guildID = ? AND groupName = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, groupName);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
            return true;
        }
        return false;
    }

    public boolean isInGroup(String guildID, String groupName, String userID)
    {
        String sql = "SELECT userID FROM groupMembers WHERE guildID = ? AND groupName = ? AND userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, groupName);
            statement.setString(3, userID);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public boolean addGroupMember(String guildID, String groupName, String userID)
    {
        if (existingGroup(guildID, groupName))
        {
            String sql = "INSERT INTO groupMembers (guildID, groupName, userID) VALUES (?, ?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, groupName);
                statement.setString(3, userID);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public boolean removeGroupMember(String guildID, String groupName, String userID)
    {
        String sql = "DELETE FROM groupMembers WHERE guildID = ? AND groupName = ? AND userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, groupName);
            statement.setString(3, userID);
            statement.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public List<IUser> getGroupMembers(String guildID, String groupName)
    {
        String sql = "SELECT userID FROM groupMembers WHERE guildID = ? AND groupName = ?";
        List<IUser> members = new ArrayList<IUser>();
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, groupName);
            try (ResultSet result = statement.executeQuery())
            {
                GuildObject guild = main.getBot().getGuildObjectByID(guildID);

                while (result.next())
                {
                    IUser member = RequestBuffer.request(new IRequest<IUser>()
                    {
                        @Override
                        public IUser request()
                        {
                            try
                            {
                                return guild.getGuild().getUserByID(Long.parseLong(result.getString("userID")));
                            }
                            catch (NumberFormatException e)
                            {
                                e.printStackTrace();
                            }
                            catch (SQLException e)
                            {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }).get();

                    if (member != null)
                    {
                        members.add(member);
                    }
                    else
                    {
                        removeGroupMember(guildID, groupName, result.getString("userID"));
                    }
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return members;
    }

    public Group getGroup(String guildID, String groupName)
    {
        String sql = "SELECT groupName, date FROM userGroups WHERE guildID = ? AND groupName = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, groupName);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    List<IUser> members = getGroupMembers(guildID, groupName);
                    Group group = new Group(result.getString("groupName"),
                            GroupManager.getManagerForGuild(main.getBot().getGuildObjectByID(guildID)),
                            result.getString("date"));
                    group.setMembers(members);
                    return group;
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public Map<String, Group> getGroups(String guildID, boolean withMembers)
    {
        Map<String, Group> groups = new HashMap<String, Group>();
        String sql = "SELECT groupName, date FROM userGroups WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                GroupManager manager = GroupManager.getManagerForGuild(main.getBot().getGuildObjectByID(guildID));

                while (result.next())
                {
                    String groupName = result.getString("groupName");

                    Group group = new Group(groupName,
                            manager,
                            result.getString("date"));

                    if (withMembers)
                    {
                        List<IUser> members = getGroupMembers(guildID, groupName);
                        group.setMembers(members);
                    }

                    groups.put(groupName, group);
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return groups;
    }

    public int getGroupCount()
    {
        String sql = "SELECT COUNT(groupName) as groupCount FROM userGroups";
        int count = 0;
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            try (ResultSet result = statement.executeQuery())
            {
                result.next();
                count = result.getInt("groupCount");
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return count;
    }

    public List<CollectionTimer> getGroupTimersForGuild(String guildID)
    {
        List<CollectionTimer> timers = new ArrayList<CollectionTimer>();
        String sql = "SELECT groupName, destroyTime FROM groupTimers WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    timers.add(new CollectionTimer(result.getString("groupName"), result.getLong("destroyTime")));
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return timers;
    }

    public void addGroupTimer(String guildID, String groupName, long time)
    {
        String sql = "INSERT INTO groupTimers (guildID, groupName, destroyTime) VALUES (?, ?, ?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, groupName);
            statement.setLong(3, time);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void removeGroupTimer(String guildID, String groupName)
    {
        String sql = "DELETE FROM groupTimers WHERE guildID = ? AND groupName = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, groupName);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean existingList(String guildID, String listName)
    {
        String sql = "SELECT listName FROM itemLists WHERE guildID = ? AND listName = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, listName);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public boolean addList(String guildID, String listName, String date)
    {
        if (!existingList(guildID, listName))
        {
            String sql = "INSERT INTO itemLists (guildID, listName, date) VALUES (?, ?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, listName);
                statement.setString(3, date);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public boolean removeList(String guildID, String listName)
    {
        if (existingList(guildID, listName))
        {
            String sql = "DELETE FROM itemLists WHERE guildID = ? AND listName = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, listName);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }

            sql = "DELETE FROM listMembers WHERE guildID = ? AND listName = ?";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, listName);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
            return true;
        }
        return false;
    }

    public boolean isInList(String guildID, String listName, String item)
    {
        String sql = "SELECT item FROM listMembers WHERE guildID = ? AND listName = ? AND item = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, listName);
            statement.setString(3, item);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public boolean addListMember(String guildID, String listName, String item)
    {
        if (existingList(guildID, listName))
        {
            String sql = "INSERT INTO listMembers (guildID, listName, item) VALUES (?, ?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, guildID);
                statement.setString(2, listName);
                statement.setString(3, item);
                statement.executeUpdate();
                return true;
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        return false;
    }

    public boolean removeListMember(String guildID, String listName, String member)
    {
        String sql = "DELETE FROM listMembers WHERE guildID = ? AND listName = ? AND item = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, listName);
            statement.setString(3, member);
            statement.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public List<String> getListMembers(String guildID, String listName)
    {
        String sql = "SELECT item FROM listMembers WHERE guildID = ? AND listName = ?";
        List<String> members = new ArrayList<String>();
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, listName);
            try (ResultSet result = statement.executeQuery())
            {
                GuildObject guild = main.getBot().getGuildObjectByID(guildID);

                while (result.next())
                {
                    members.add(result.getString("item"));
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return members;
    }

    public ItemList getList(String guildID, String listName)
    {
        String sql = "SELECT listName, date FROM itemLists WHERE guildID = ? AND listName = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, listName);
            try (ResultSet result = statement.executeQuery())
            {
                if (result.next())
                {
                    List<String> members = getListMembers(guildID, listName);
                    ItemList list = new ItemList(result.getString("listName"),
                            ListManager.getManagerForGuild(main.getBot().getGuildObjectByID(guildID)),
                            result.getString("date"));
                    list.setMembers(members);
                    return list;
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public Map<String, ItemList> getLists(String guildID, boolean withMembers)
    {
        Map<String, ItemList> lists = new HashMap<String, ItemList>();
        String sql = "SELECT listName, date FROM itemLists WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                ListManager manager = ListManager.getManagerForGuild(main.getBot().getGuildObjectByID(guildID));

                while (result.next())
                {
                    String listName = result.getString("listName");

                    ItemList list = new ItemList(listName,
                            manager,
                            result.getString("date"));

                    if (withMembers)
                    {
                        List<String> members = getListMembers(guildID, listName);
                        list.setMembers(members);
                    }

                    lists.put(listName, list);
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return lists;
    }

    public int getListCount()
    {
        String sql = "SELECT COUNT(listName) as listCount FROM itemLists";
        int count = 0;
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            try (ResultSet result = statement.executeQuery())
            {
                result.next();
                count = result.getInt("listCount");
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return count;
    }

    public List<CollectionTimer> getListTimersForGuild(String guildID)
    {
        List<CollectionTimer> timers = new ArrayList<CollectionTimer>();
        String sql = "SELECT listName, destroyTime FROM listTimers WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    timers.add(new CollectionTimer(result.getString("listName"), result.getLong("destroyTime")));
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return timers;
    }

    public void addListTimer(String guildID, String listName, long time)
    {
        String sql = "INSERT INTO listTimers (guildID, listName, destroyTime) VALUES (?, ?, ?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, listName);
            statement.setLong(3, time);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void removeListTimer(String guildID, String listName)
    {
        String sql = "DELETE FROM listTimers WHERE guildID = ? AND listName = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setString(2, listName);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void clearLists(String guildID)
    {
        String sql = "DELETE FROM listTimers WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        sql = "DELETE FROM itemLists WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        sql = "DELETE FROM listMembers WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void clearGroups(String guildID)
    {
        String sql = "DELETE FROM groupTimers WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        sql = "DELETE FROM userGroups WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }

        sql = "DELETE FROM groupMembers WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public boolean isMaster(String masterID, String guildID)
    {
        String sql = "SELECT masterID FROM masters WHERE masterID = ? AND guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, masterID);
            statement.setString(2, guildID);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return false;
    }

    public List<String> getMasterIDs(GuildObject guild, int level)
    {
        return this.getMasterIDs(guild.getStringID(), level);
    }

    public List<String> getMasterIDs(IGuild guild, int level)
    {
        return this.getMasterIDs(guild.getStringID(), level);
    }

    public List<String> getMasterIDs(String guildID, int level)
    {
        List<String> ids = new ArrayList<String>();
        String sql = "SELECT masterID FROM masters WHERE guildID = ? AND level = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.setInt(2, level);
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    ids.add(result.getString("masterID"));
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return ids;
    }

    public void addMaster(String masterID, String guildID, int level)
    {
        if (!this.isMaster(masterID, guildID))
        {
            String sql = "INSERT INTO masters (masterID, guildID, level) VALUES (?, ?, ?)";
            try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
            {
                statement.setString(1, masterID);
                statement.setString(2, guildID);
                statement.setInt(3, level);
                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                Bot.errorLog.print(this, e);
            }
        }
    }

    public void removeMaster(String masterID, String guildID)
    {
        String sql = "DELETE FROM masters WHERE masterID = ? AND guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, masterID);
            statement.setString(2, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void removeMasters(GuildObject guild)
    {
        this.removeMasters(guild.getStringID());
    }

    public void removeMasters(IGuild guild)
    {
        this.removeMasters(guild.getStringID());
    }

    public void removeMasters(String guildID)
    {
        String sql = "DELETE FROM masters WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guildID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void removeGuildInformation(GuildObject guild)
    {
        removeMasters(guild);
        removeLines(guild);
        clearLists(guild.getStringID());
        clearGroups(guild.getStringID());
        clearAutomatedCommands(guild.getStringID());
        clearFeeds(guild.getStringID());

        String sql = "DELETE FROM guildPrefix WHERE guildID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, guild.getStringID());
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public List<String> getBannedUsers()
    {
        String sql = "SELECT userID FROM banned";
        List<String> ids = new ArrayList<String>();
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    ids.add(result.getString("userID"));
                }
            }
            catch (Exception e)
            {
                Bot.errorLog.print(this, e);
            }
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
        return ids;
    }

    public void banUser(String userID)
    {
        String sql = "INSERT INTO banned (userID) VALUES (?)";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }

    public void unbanUser(String userID)
    {
        String sql = "DELETE FROM banned WHERE userID = ?";
        try (PreparedStatement statement = this.getConnection().prepareStatement(sql))
        {
            statement.setString(1, userID);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            Bot.errorLog.print(this, e);
        }
    }
}