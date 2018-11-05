package bowtie.bot.collections.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import sx.blah.discord.handle.obj.IUser;
import bowt.guild.GuildObject;
import bowt.thread.Threads;
import bowtie.core.db.DatabaseAccess;

/**
 * @author &#8904
 *
 */
public class GroupManager
{
    private static ConcurrentHashMap<Long, GroupManager> groupManagers = new ConcurrentHashMap<Long, GroupManager>();
    private Map<String, Group> groups;
    private final GuildObject guild;
    protected static DatabaseAccess db;

    public GroupManager(GuildObject guild)
    {
        this.guild = guild;
        this.groups = new HashMap<String, Group>();
    }

    public GuildObject getGuild()
    {
        return this.guild;
    }

    public synchronized Group getGroupByName(String name)
    {
        Group group = groups.get(name);
        if (group != null)
        {
            return group;
        }
        if (db.existingGroup(guild.getStringID(), name))
        {
            group = db.getGroup(guild.getStringID(), name);
            groups.put(group.getName(), group);
            return group;
        }
        return null;
    }

    public synchronized boolean add(Group group)
    {
        if (db.addGroup(guild.getStringID(), group.getName(), group.getDate()))
        {
            this.groups.put(group.getName(), group);
            return true;
        }
        return false;
    }

    public synchronized boolean remove(String name)
    {
        Group group = groups.remove(name);

        if (group != null)
        {
            if (db.removeGroup(guild.getStringID(), name))
            {
                db.removeGroupTimer(guild.getStringID(), name);
                return true;
            }
        }
        return false;
    }

    public synchronized void removeFromAll(IUser user)
    {
        this.groups = db.getGroups(this.guild.getStringID(), true);

        for (Group g : new ArrayList<Group>(this.groups.values()))
        {
            g.removeMember(user);
        }
    }

    public synchronized void clearGroups()
    {
        this.groups.clear();
    }

    public static void setDatabase(DatabaseAccess db)
    {
        GroupManager.db = db;
    }

    public static void addManager(GroupManager manager)
    {
        groupManagers.put(manager.getGuild().getLongID(), manager);
    }

    /**
     * Returns either an existing manager or creates a new one.
     * 
     * @param guild
     * @return
     */
    public synchronized static GroupManager getManagerForGuild(GuildObject guild)
    {
        GroupManager manager = groupManagers.get(guild.getLongID());
        if (manager != null)
        {
            return manager;
        }
        manager = new GroupManager(guild);
        addManager(manager);
        return manager;
    }

    public static void startCleaner()
    {
        Threads.schedulerPool.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                for (GroupManager manager : new ArrayList<GroupManager>(groupManagers.values()))
                {
                    manager.clearGroups();
                }
                groupManagers.clear();
                System.gc();
            }
        }, 30, 30, TimeUnit.MINUTES);
    }
}