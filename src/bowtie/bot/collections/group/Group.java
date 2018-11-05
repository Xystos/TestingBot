package bowtie.bot.collections.group;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import sx.blah.discord.handle.obj.IUser;
import bowt.bot.Bot;
import bowt.thread.Threads;

/**
 * @author &#8904
 *
 */
public class Group
{
    private final String name;
    private List<IUser> members;
    private GroupManager manager;
    private final String date;

    public Group(String name, GroupManager manager, String date)
    {
        this.name = name;
        this.members = new ArrayList<IUser>();
        this.manager = manager;
        this.date = date;
    }

    public Group(String name, GroupManager manager)
    {
        this(name, manager, null);
    }

    public void setMembers(List<IUser> members)
    {
        this.members = members;
    }

    public boolean addMember(IUser user)
    {
        if (!this.members.contains(user))
        {
            if (this.members.add(user))
            {
                GroupManager.db.addGroupMember(manager.getGuild().getStringID(), name, user.getStringID());
                Bot.log.print(this, user.getName() + " joined.");
                return true;
            }
        }
        return false;
    }

    public boolean removeMember(IUser user)
    {
        if (this.members.contains(user))
        {
            if (this.members.remove(user))
            {
                GroupManager.db.removeGroupMember(manager.getGuild().getStringID(), name, user.getStringID());
                Bot.log.print(this, user.getName() + " left.");
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
                    Bot.log.print("[" + "Group-" + name + "-" + manager.getGuild().getStringID()
                            + "] Group was closed after the timer ended.");
                }
            }
        }, remaining, TimeUnit.MILLISECONDS);
    }

    public List<IUser> getMembers()
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