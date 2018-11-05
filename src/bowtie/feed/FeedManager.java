package bowtie.feed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import bowt.bot.Bot;
import bowt.cons.Colors;
import bowt.prop.Properties;
import bowt.thread.Threads;
import bowtie.bot.util.Activation;
import bowtie.core.Main;
import bowtie.feed.exc.ChannelDeletedException;
import bowtie.feed.exc.FeedNotFoundException;
import bowtie.feed.exc.GuildNotFoundException;
import bowtie.feed.exc.NoValidEntriesException;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MissingPermissionsException;

/**
 * @author &#8904
 *
 */
public class FeedManager
{
    public static final int UPDATE_INTERVAL_1; // minutes
    public static final int UPDATE_INTERVAL_2; // minutes
    public static final int UPDATE_INTERVAL_3; // minutes
    public static final int UPDATE_INTERVAL_4; // minutes
    public static final int NON_PATRON_INTERVAL = 60; // minutes
    public static final int MAX_FREE_FEEDS = 5;
    private volatile static Map<String, Map<String, ManagedFeed>> feeds = new ConcurrentHashMap<>();
    private volatile static CopyOnWriteArrayList<ManagedFeed> list1 = new CopyOnWriteArrayList<>();
    private volatile static CopyOnWriteArrayList<ManagedFeed> list2 = new CopyOnWriteArrayList<>();
    private volatile static CopyOnWriteArrayList<ManagedFeed> list3 = new CopyOnWriteArrayList<>();
    private volatile static CopyOnWriteArrayList<ManagedFeed> list4 = new CopyOnWriteArrayList<>();
    private volatile static CopyOnWriteArrayList<ManagedFeed> list5 = new CopyOnWriteArrayList<>();
    private ExecutorService executor;
    private Main main;

    static
    {
        UPDATE_INTERVAL_1 = Integer.parseInt(Properties.getValueOf("rss_interval_1")); // minutes
        UPDATE_INTERVAL_2 = Integer.parseInt(Properties.getValueOf("rss_interval_2")); // minutes
        UPDATE_INTERVAL_3 = Integer.parseInt(Properties.getValueOf("rss_interval_3")); // minutes
        UPDATE_INTERVAL_4 = Integer.parseInt(Properties.getValueOf("rss_interval_4")); // minutes
    }

    public FeedManager(Main main)
    {
        this.executor = Executors.newFixedThreadPool(50);
        Threads.add(this.executor);
        this.main = main;
    }

    private void run1Timer()
    {
        Threads.schedulerPool.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                CopyOnWriteArrayList<ManagedFeed> feed10 = (CopyOnWriteArrayList<ManagedFeed>)list1.clone();

                for (ManagedFeed feed : feed10)
                {
                    if (main.getDatabase().getFeedCount(feed.getGuildObject().getStringID()) <= MAX_FREE_FEEDS)
                    {

                    }
                    else if (!main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }
                    else if (!Activation.isActivated(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }

                    executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int oldInterval = feed.getUpdateInterval();

                            try
                            {
                                feed.update();

                                if (main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                                {
                                    if (Activation.isActivated(feed.getGuildObject().getStringID()))
                                    {
                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            int interval = feed.getUpdateInterval();
                                            
                                            if (interval == UPDATE_INTERVAL_2)
                                            {
                                                list1.remove(feed);
                                                list2.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_3)
                                            {
                                                list1.remove(feed);
                                                list3.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_4)
                                            {
                                                list1.remove(feed);
                                                list4.add(feed);
                                            }
                                            else if (interval == NON_PATRON_INTERVAL)
                                            {
                                                list1.remove(feed);
                                                list5.add(feed);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        feed.setUpdateInterval(NON_PATRON_INTERVAL);

                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            list1.remove(feed);
                                            list5.add(feed);
                                        }
                                    }
                                }
                                else
                                {
                                    feed.setUpdateInterval(NON_PATRON_INTERVAL);

                                    if (oldInterval != feed.getUpdateInterval())
                                    {
                                        list1.remove(feed);
                                        list5.add(feed);
                                    }
                                }
                            }
                            catch (ChannelDeletedException ce)
                            {
                                remove(feed);
                                list1.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                main.getBot().sendMessage(ce.getMessage(), owner.getOrCreatePMChannel(), Colors.RED);
                                return;
                            }
                            catch (MissingPermissionsException pe)
                            {
                                remove(feed);
                                list1.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                String missing = "\n\n\nIt needs the following permissions: \n\n";
                                for (Permissions p : pe.getMissingPermissions())
                                {
                                    missing += p.name() + "\n";
                                }
                                main.getBot().sendMessage(pe.getMessage() + missing, owner.getOrCreatePMChannel(),
                                        Colors.RED);
                                return;
                            }
                            catch (FeedNotFoundException fe)
                            {
                                return;
                            }
                            catch (NoValidEntriesException nve)
                            {
                                return;
                            }
                            catch (GuildNotFoundException e)
                            {
                                remove(feed);
                                list1.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                return;
                            }
                        }
                    });
                }
                System.gc();
            }
        }, UPDATE_INTERVAL_1, UPDATE_INTERVAL_1, TimeUnit.MINUTES);
    }

    private void run2Timer()
    {
        Threads.schedulerPool.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                CopyOnWriteArrayList<ManagedFeed> feed10 = (CopyOnWriteArrayList<ManagedFeed>)list2.clone();

                for (ManagedFeed feed : feed10)
                {
                    if (main.getDatabase().getFeedCount(feed.getGuildObject().getStringID()) <= MAX_FREE_FEEDS)
                    {

                    }
                    else if (!main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }
                    else if (!Activation.isActivated(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }

                    executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int oldInterval = feed.getUpdateInterval();

                            try
                            {
                                feed.update();

                                if (main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                                {
                                    if (Activation.isActivated(feed.getGuildObject().getStringID()))
                                    {
                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            int interval = feed.getUpdateInterval();

                                            if (interval == UPDATE_INTERVAL_1)
                                            {
                                                list2.remove(feed);
                                                list1.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_3)
                                            {
                                                list2.remove(feed);
                                                list3.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_4)
                                            {
                                                list2.remove(feed);
                                                list4.add(feed);
                                            }
                                            else if (interval == NON_PATRON_INTERVAL)
                                            {
                                                list2.remove(feed);
                                                list5.add(feed);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        feed.setUpdateInterval(NON_PATRON_INTERVAL);

                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            list2.remove(feed);
                                            list5.add(feed);
                                        }
                                    }
                                }
                                else
                                {
                                    feed.setUpdateInterval(NON_PATRON_INTERVAL);

                                    if (oldInterval != feed.getUpdateInterval())
                                    {
                                        list2.remove(feed);
                                        list5.add(feed);
                                    }
                                }
                            }
                            catch (ChannelDeletedException ce)
                            {
                                remove(feed);
                                list2.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                main.getBot().sendMessage(ce.getMessage(), owner.getOrCreatePMChannel(), Colors.RED);
                                return;
                            }
                            catch (MissingPermissionsException pe)
                            {
                                remove(feed);
                                list2.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                String missing = "\n\n\nIt needs the following permissions: \n\n";
                                for (Permissions p : pe.getMissingPermissions())
                                {
                                    missing += p.name() + "\n";
                                }
                                main.getBot().sendMessage(pe.getMessage() + missing, owner.getOrCreatePMChannel(),
                                        Colors.RED);
                                return;
                            }
                            catch (FeedNotFoundException fe)
                            {
                                return;
                            }
                            catch (NoValidEntriesException nve)
                            {
                                return;
                            }
                            catch (GuildNotFoundException e)
                            {
                                remove(feed);
                                list2.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                return;
                            }
                        }
                    });
                }
                System.gc();
            }
        }, UPDATE_INTERVAL_2, UPDATE_INTERVAL_2, TimeUnit.MINUTES);
    }

    private void run3Timer()
    {
        Threads.schedulerPool.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                CopyOnWriteArrayList<ManagedFeed> feed10 = (CopyOnWriteArrayList<ManagedFeed>)list3.clone();

                for (ManagedFeed feed : feed10)
                {
                    if (main.getDatabase().getFeedCount(feed.getGuildObject().getStringID()) <= MAX_FREE_FEEDS)
                    {

                    }
                    else if (!main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }
                    else if (!Activation.isActivated(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }

                    executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int oldInterval = feed.getUpdateInterval();

                            try
                            {
                                feed.update();

                                if (main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                                {
                                    if (Activation.isActivated(feed.getGuildObject().getStringID()))
                                    {
                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            int interval = feed.getUpdateInterval();

                                            if (interval == UPDATE_INTERVAL_2)
                                            {
                                                list3.remove(feed);
                                                list2.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_1)
                                            {
                                                list3.remove(feed);
                                                list1.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_4)
                                            {
                                                list3.remove(feed);
                                                list4.add(feed);
                                            }
                                            if (interval == NON_PATRON_INTERVAL)
                                            {
                                                list3.remove(feed);
                                                list5.add(feed);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        feed.setUpdateInterval(NON_PATRON_INTERVAL);

                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            list3.remove(feed);
                                            list5.add(feed);
                                        }
                                    }
                                }
                                else
                                {
                                    feed.setUpdateInterval(NON_PATRON_INTERVAL);

                                    if (oldInterval != feed.getUpdateInterval())
                                    {
                                        list3.remove(feed);
                                        list5.add(feed);
                                    }
                                }
                            }
                            catch (ChannelDeletedException ce)
                            {
                                remove(feed);
                                list3.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                main.getBot().sendMessage(ce.getMessage(), owner.getOrCreatePMChannel(), Colors.RED);
                                return;
                            }
                            catch (MissingPermissionsException pe)
                            {
                                remove(feed);
                                list3.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                String missing = "\n\n\nIt needs the following permissions: \n\n";
                                for (Permissions p : pe.getMissingPermissions())
                                {
                                    missing += p.name() + "\n";
                                }
                                main.getBot().sendMessage(pe.getMessage() + missing, owner.getOrCreatePMChannel(),
                                        Colors.RED);
                                return;
                            }
                            catch (FeedNotFoundException fe)
                            {
                                return;
                            }
                            catch (NoValidEntriesException nve)
                            {
                                return;
                            }
                            catch (GuildNotFoundException e)
                            {
                                remove(feed);
                                list3.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                return;
                            }
                        }
                    });
                }
                System.gc();
            }
        }, UPDATE_INTERVAL_3, UPDATE_INTERVAL_3, TimeUnit.MINUTES);
    }

    private void run4Timer()
    {
        Threads.schedulerPool.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                CopyOnWriteArrayList<ManagedFeed> feed10 = (CopyOnWriteArrayList<ManagedFeed>)list4.clone();

                for (ManagedFeed feed : feed10)
                {
                    if (main.getDatabase().getFeedCount(feed.getGuildObject().getStringID()) <= MAX_FREE_FEEDS)
                    {

                    }
                    else if (!main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }
                    else if (!Activation.isActivated(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }

                    executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int oldInterval = feed.getUpdateInterval();

                            try
                            {
                                feed.update();

                                if (main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                                {
                                    if (Activation.isActivated(feed.getGuildObject().getStringID()))
                                    {
                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            int interval = feed.getUpdateInterval();

                                            if (interval == UPDATE_INTERVAL_2)
                                            {
                                                list4.remove(feed);
                                                list2.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_1)
                                            {
                                                list4.remove(feed);
                                                list1.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_3)
                                            {
                                                list4.remove(feed);
                                                list3.add(feed);
                                            }
                                            else if (interval == NON_PATRON_INTERVAL)
                                            {
                                                list4.remove(feed);
                                                list5.add(feed);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        feed.setUpdateInterval(NON_PATRON_INTERVAL);

                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            list4.remove(feed);
                                            list5.add(feed);
                                        }
                                    }
                                }
                                else
                                {
                                    feed.setUpdateInterval(NON_PATRON_INTERVAL);

                                    if (oldInterval != feed.getUpdateInterval())
                                    {
                                        list4.remove(feed);
                                        list5.add(feed);
                                    }
                                }
                            }
                            catch (ChannelDeletedException ce)
                            {
                                remove(feed);
                                list4.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                main.getBot().sendMessage(ce.getMessage(), owner.getOrCreatePMChannel(), Colors.RED);
                                return;
                            }
                            catch (MissingPermissionsException pe)
                            {
                                remove(feed);
                                list4.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                String missing = "\n\n\nIt needs the following permissions: \n\n";
                                for (Permissions p : pe.getMissingPermissions())
                                {
                                    missing += p.name() + "\n";
                                }
                                main.getBot().sendMessage(pe.getMessage() + missing, owner.getOrCreatePMChannel(),
                                        Colors.RED);
                                return;
                            }
                            catch (FeedNotFoundException fe)
                            {
                                return;
                            }
                            catch (NoValidEntriesException nve)
                            {
                                return;
                            }
                            catch (GuildNotFoundException e)
                            {
                                remove(feed);
                                list4.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                return;
                            }
                        }
                    });
                }
                System.gc();
            }
        }, UPDATE_INTERVAL_4, UPDATE_INTERVAL_4, TimeUnit.MINUTES);
    }

    private void run5Timer()
    {
        Threads.schedulerPool.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                CopyOnWriteArrayList<ManagedFeed> feed10 = (CopyOnWriteArrayList<ManagedFeed>)list5.clone();

                for (ManagedFeed feed : feed10)
                {
                    if (main.getDatabase().getFeedCount(feed.getGuildObject().getStringID()) <= MAX_FREE_FEEDS)
                    {
                        System.out.println("running");
                    }
                    else if (!main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }
                    else if (!Activation.isActivated(feed.getGuildObject().getStringID()))
                    {
                        continue;
                    }

                    executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int oldInterval = feed.getUpdateInterval();

                            try
                            {
                                feed.update();

                                if (main.getDatabase().isActivatedGuild(feed.getGuildObject().getStringID()))
                                {
                                    if (Activation.isActivated(feed.getGuildObject().getStringID()))
                                    {
                                        if (oldInterval != feed.getUpdateInterval())
                                        {
                                            int interval = feed.getUpdateInterval();

                                            if (interval == UPDATE_INTERVAL_2)
                                            {
                                                list5.remove(feed);
                                                list2.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_1)
                                            {
                                                list5.remove(feed);
                                                list1.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_3)
                                            {
                                                list5.remove(feed);
                                                list3.add(feed);
                                            }
                                            else if (interval == UPDATE_INTERVAL_4)
                                            {
                                                list5.remove(feed);
                                                list4.add(feed);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        feed.setUpdateInterval(NON_PATRON_INTERVAL);
                                    }
                                }
                                else
                                {
                                    feed.setUpdateInterval(NON_PATRON_INTERVAL);
                                }
                            }
                            catch (ChannelDeletedException ce)
                            {
                                remove(feed);
                                list5.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                main.getBot().sendMessage(ce.getMessage(), owner.getOrCreatePMChannel(), Colors.RED);
                                return;
                            }
                            catch (MissingPermissionsException pe)
                            {
                                remove(feed);
                                list5.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                IUser owner = feed.getGuildObject().getGuild().getOwner();
                                String missing = "\n\n\nIt needs the following permissions: \n\n";
                                for (Permissions p : pe.getMissingPermissions())
                                {
                                    missing += p.name() + "\n";
                                }
                                main.getBot().sendMessage(pe.getMessage() + missing, owner.getOrCreatePMChannel(),
                                        Colors.RED);
                                return;
                            }
                            catch (FeedNotFoundException fe)
                            {
                                return;
                            }
                            catch (NoValidEntriesException nve)
                            {
                                return;
                            }
                            catch (GuildNotFoundException e)
                            {
                                remove(feed);
                                list5.remove(feed);
                                main.getDatabase().removeFeed(feed);
                                return;
                            }
                        }
                    });
                }
                System.gc();
            }
        }, NON_PATRON_INTERVAL, NON_PATRON_INTERVAL, TimeUnit.MINUTES);
    }

    public void run()
    {
        run1Timer();
        run2Timer();
        run3Timer();
        run4Timer();
        run5Timer();
    }

    public synchronized static boolean add(ManagedFeed feed)
    {
        Map<String, ManagedFeed> guildFeeds = feeds.get(feed.getGuildObject().getStringID());
        boolean added = false;

        if (guildFeeds == null)
        {
            guildFeeds = new ConcurrentHashMap<>();
            feeds.put(feed.getGuildObject().getStringID(), guildFeeds);
        }
        if (!guildFeeds.containsKey(feed.getLink()))
        {
            guildFeeds.put(feed.getLink(), feed);
            int interval = feed.getUpdateInterval();

            if (interval == UPDATE_INTERVAL_1)
            {
                list1.add(feed);
            }
            else if (interval == UPDATE_INTERVAL_2)
            {
                list2.add(feed);
            }
            else if (interval == UPDATE_INTERVAL_3)
            {
                list3.add(feed);
            }
            else if (interval == UPDATE_INTERVAL_4)
            {
                list4.add(feed);
            }
            else if (interval == NON_PATRON_INTERVAL)
            {
                list5.add(feed);
            }

            added = true;
            Bot.log.print("Added a new feed to group " + feed.getUpdateInterval() + " with an average interval of "
                    + feed.getAvrgInterval() + ".");
        }
        return added;
    }

    public synchronized static boolean remove(ManagedFeed feed)
    {
        return remove(feed.getGuildObject().getStringID(), feed.getLink());
    }

    public synchronized static boolean remove(String guildID, String link)
    {
        Map<String, ManagedFeed> guildFeeds = feeds.get(guildID);

        if (guildFeeds != null && guildFeeds.containsKey(link))
        {
            ManagedFeed feed = guildFeeds.remove(link);

            if (feed != null)
            {
                list1.remove(feed);
                list2.remove(feed);
                list3.remove(feed);
                list4.remove(feed);
                list5.remove(feed);
            }

            return true;
        }
        return false;
    }

    public synchronized static void remove(String guildID)
    {
        feeds.remove(guildID);

        list1.removeIf(feed -> feed.getGuildObject().getStringID().equals(guildID));
        list2.removeIf(feed -> feed.getGuildObject().getStringID().equals(guildID));
        list3.removeIf(feed -> feed.getGuildObject().getStringID().equals(guildID));
        list4.removeIf(feed -> feed.getGuildObject().getStringID().equals(guildID));
        list5.removeIf(feed -> feed.getGuildObject().getStringID().equals(guildID));
    }

    public synchronized static ManagedFeed getFeed(String link, String guildID)
    {
        Map<String, ManagedFeed> guildFeeds = feeds.get(guildID);

        if (guildFeeds != null)
        {
            return guildFeeds.get(link);
        }
        return null;
    }

    public synchronized static List<ManagedFeed> getFeeds(String guildID)
    {
        Map<String, ManagedFeed> guildFeeds = feeds.get(guildID);

        if (guildFeeds != null)
        {
            return new ArrayList<ManagedFeed>(guildFeeds.values());
        }
        return null;
    }

    public synchronized static int getFeedCount()
    {
        int count = 0;

        for (Map<String, ManagedFeed> guildFeeds : feeds.values())
        {
            count += guildFeeds.values().size();
        }

        return count;
    }

    public static int list1Count()
    {
        return list1.size();
    }

    public static int list2Count()
    {
        return list2.size();
    }

    public static int list3Count()
    {
        return list3.size();
    }

    public static int list4Count()
    {
        return list4.size();
    }

    public static int list5Count()
    {
        return list5.size();
    }
}