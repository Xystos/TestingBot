package bowtie.bot.collections.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import bowt.guild.GuildObject;
import bowt.thread.Threads;
import bowtie.core.db.DatabaseAccess;

/**
 * @author &#8904
 *
 */
public class ListManager
{
    private static ConcurrentHashMap<Long, ListManager> listManagers = new ConcurrentHashMap<Long, ListManager>();
    private Map<String, ItemList> lists;
    private final GuildObject guild;
    protected static DatabaseAccess db;

    public ListManager(GuildObject guild)
    {
        this.guild = guild;
        this.lists = new HashMap<String, ItemList>();
    }

    public GuildObject getGuild()
    {
        return this.guild;
    }

    public synchronized ItemList getListByName(String name)
    {
        ItemList list = lists.get(name);
        if (list != null)
        {
            return list;
        }
        if (db.existingList(guild.getStringID(), name))
        {
            list = db.getList(guild.getStringID(), name);
            lists.put(list.getName(), list);
            return list;
        }
        return null;
    }

    public synchronized boolean add(ItemList list)
    {
        if (db.addList(guild.getStringID(), list.getName(), list.getDate()))
        {
            this.lists.put(list.getName(), list);
            return true;
        }
        return false;
    }

    public synchronized boolean remove(String name)
    {
        ItemList list = lists.remove(name);

        if (list != null)
        {
            if (db.removeList(guild.getStringID(), name))
            {
                db.removeListTimer(guild.getStringID(), name);
                return true;
            }
        }
        return false;
    }

    public synchronized void clearLists()
    {
        this.lists.clear();
    }

    public static void setDatabase(DatabaseAccess db)
    {
        ListManager.db = db;
    }

    public static void addManager(ListManager manager)
    {
        listManagers.put(manager.getGuild().getLongID(), manager);
    }

    /**
     * Returns either an existing manager or creates a new one.
     * 
     * @param guild
     * @return
     */
    public synchronized static ListManager getManagerForGuild(GuildObject guild)
    {
        ListManager manager = listManagers.get(guild.getLongID());
        if (manager != null)
        {
            return manager;
        }
        manager = new ListManager(guild);
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
                for (ListManager manager : new ArrayList<ListManager>(listManagers.values()))
                {
                    manager.clearLists();
                }
                listManagers.clear();
                System.gc();
            }
        }, 30, 30, TimeUnit.MINUTES);
    }
}