package bowtie.bot.collections.list;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bowt.bot.Bot;
import bowt.thread.Threads;

/**
 * @author &#8904
 *
 */
public class ItemList
{
    private final String name;
    private List<String> members;
    private ListManager manager;
    private final String date;

    public ItemList(String name, ListManager manager, String date)
    {
        this.name = name;
        this.members = new ArrayList<String>();
        this.manager = manager;
        this.date = date;
    }

    public ItemList(String name, ListManager manager)
    {
        this(name, manager, null);
    }

    public void setMembers(List<String> members)
    {
        this.members = members;
    }

    public boolean addMember(String member)
    {
        if (!this.members.contains(member))
        {
            if (this.members.add(member))
            {
                ListManager.db.addListMember(manager.getGuild().getStringID(), name, member);
                return true;
            }
        }
        return false;
    }

    public boolean removeMember(String member)
    {
        if (this.members.contains(member))
        {
            if (this.members.remove(member))
            {
                ListManager.db.removeListMember(manager.getGuild().getStringID(), name, member);
                return true;
            }
        }
        return false;
    }

    public void destroyAt(long destroySystemTime)
    {
        long remaining = destroySystemTime - System.currentTimeMillis();
        Threads.schedulerPool.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                if (manager.remove(name))
                {
                    Bot.log.print("[" + "List-" + name + "-" + manager.getGuild().getStringID()
                            + "] List was closed after the timer ended.");
                }
            }
        }, remaining, TimeUnit.MILLISECONDS);
    }

    public List<String> getMembers()
    {
        return this.members;
    }

    public int size()
    {
        return this.members.size();
    }

    public String getName()
    {
        return this.name;
    }

    public String getDate()
    {
        return this.date;
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + "-" + this.name + "-" + this.manager.getGuild().getStringID();
    }
}