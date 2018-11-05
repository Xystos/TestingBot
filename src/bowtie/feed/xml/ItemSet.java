package bowtie.feed.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bowtie.feed.FeedManager;
import bowtie.feed.exc.DateUnknownException;

import com.rometools.rome.feed.synd.SyndEntry;

/**
 * @author &#8904
 *
 */
public class ItemSet
{
    private Map<String, Item> items;
    private int updateInterval;
    private int avrgInterval;

    public ItemSet(List<SyndEntry> entries)
    {
        this.items = new HashMap<>();
        for (SyndEntry entry : entries)
        {
            this.items.put(entry.getLink(), new Item(entry));
        }
    }

    public void distributeXml(Map<String, String> itemXmlMap)
    {
        List<Item> itemValues = new ArrayList<>(this.items.values());

        for (Item item : itemValues)
        {
            String link = item.getRawLink();
            if (link != null)
            {
                String xml = itemXmlMap.get(link);
                item.setXml(xml);
            }
        }
    }

    public void calcUpdateInterval()
    {
        List<Item> itemValues = new ArrayList<>(this.items.values());

        if (itemValues.isEmpty() || !itemValues.get(0).hasDateTags())
        {
            this.updateInterval = FeedManager.UPDATE_INTERVAL_2;
            return;
        }

        try
        {
            sort(itemValues);
        }
        catch (DateUnknownException e)
        {
            this.updateInterval = 30;
            return;
        }

        long totalTime = 0;

        for (int i = 0; i < itemValues.size() - 1; i ++ )
        {
            long interval = itemValues.get(i + 1).getDate().getTime() - itemValues.get(i).getDate().getTime();
            interval = interval < 0 ? interval *= -1 : interval;
            totalTime += interval;
        }
        long avrgInterval = FeedManager.UPDATE_INTERVAL_2;

        if (itemValues.size() - 1 > 0)
        {
            avrgInterval = (long)((totalTime / (itemValues.size() - 1)) / 1000 / 60); // avrg interval in minutes
        }
        else
        {
            avrgInterval = (long)(totalTime / 1000 / 60); // avrg interval in minutes
        }

        if (avrgInterval <= FeedManager.UPDATE_INTERVAL_2)
        {
            this.updateInterval = FeedManager.UPDATE_INTERVAL_1;
        }
        else if (avrgInterval <= FeedManager.UPDATE_INTERVAL_3)
        {
            this.updateInterval = FeedManager.UPDATE_INTERVAL_2;
        }
        else if (avrgInterval <= FeedManager.UPDATE_INTERVAL_4)
        {
            this.updateInterval = FeedManager.UPDATE_INTERVAL_3;
        }
        else
        {
            this.updateInterval = FeedManager.UPDATE_INTERVAL_4;
        }

        this.avrgInterval = (int)avrgInterval;
    }

    public int getUpdateInterval()
    {
        return this.updateInterval;
    }

    public void setUpdateInterval(int interval)
    {
        this.updateInterval = interval;
    }

    public int getAvrgInterval()
    {
        return this.avrgInterval;
    }

    public Item getItem(String link)
    {
        return this.items.get(link);
    }

    public List<Item> getItemList()
    {
        List<Item> itemList = new ArrayList<Item>(this.items.values());
        try
        {
            sort(itemList);
        }
        catch (DateUnknownException e)
        {
        }
        return itemList;
    }

    public List<Item> update(ItemSet itemset)
    {
        return update(itemset.getItemList());
    }

    public List<Item> update(List<Item> updateItems)
    {
        List<Item> newItems = new ArrayList<>();

        for (Item item : updateItems)
        {
            String itemlink1 = "https://" + item.getLink();
            String itemlink2 = "http://" + item.getLink();
            if (item.getLink().startsWith("https"))
            {
                itemlink1 = item.getLink();
                itemlink2 = "http" + item.getLink().substring(5);
            }
            else if (item.getLink().startsWith("http"))
            {
                itemlink1 = "https" + item.getLink().substring(4);
                itemlink2 = item.getLink();
            }
            Item checkItem1 = this.items.get(itemlink1);
            Item checkItem2 = this.items.get(itemlink2);

            if (checkItem1 == null && checkItem2 == null)
            {
                newItems.add(item);
            }
        }

        this.items.clear();

        for (Item item : updateItems)
        {
            this.items.put(item.getLink(), item);
        }

        calcUpdateInterval();

        try
        {
            sort(newItems);
        }
        catch (DateUnknownException e)
        {
        }

        return newItems;
    }

    private void sort(List<Item> sortItem) throws DateUnknownException
    {
        try
        {
            Collections.sort(sortItem, new Comparator<Item>()
            {
                @Override
                public int compare(Item i1, Item i2)
                {
                    return i1.getDate().compareTo(i2.getDate());
                }
            });
        }
        catch (NullPointerException e)
        {
            throw new DateUnknownException("Date tag is null, sorting is not possible.");
        }
    }
}