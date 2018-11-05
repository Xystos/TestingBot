package bowtie.bot.hand;

import java.util.SplittableRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import bowt.guild.GuildObject;
import bowtie.bot.collections.group.Group;
import bowtie.bot.collections.group.GroupManager;
import bowtie.bot.collections.list.ItemList;
import bowtie.bot.collections.list.ListManager;
import bowtie.bot.sound.SoundManager;
import bowtie.bot.sound.Voice;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class VoiceChannelJoinHandler implements IListener<UserVoiceChannelJoinEvent>
{
    private Main main;
    private Pattern pat = Pattern.compile("(<[\\w|1-9\\s=]+>)");
    private SplittableRandom r;

    public VoiceChannelJoinHandler(Main main)
    {
        this.main = main;
        this.r = new SplittableRandom();
    }

    /**
     * @see sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.events.Event)
     */
    @Override
    public void handle(UserVoiceChannelJoinEvent event)
    {
        if (event.getVoiceChannel().isConnected()
                && !event.getUser().isBot())
        {
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
            }

            String name = main.getDatabase().getVoiceName(event.getUser().getStringID());

            if (name == null)
            {
                name = event.getUser().getNicknameForGuild(event.getGuild());
            }

            if (name == null)
            {
                name = event.getUser().getName();
            }

            String text = main.getDatabase().getJoinText(event.getGuild().getStringID());

            GuildObject guild = main.getBot().getGuildObjectByID(event.getGuild().getStringID());
            if (guild == null)
            {
                guild = GuildSetupHandler.setupGuildObject(event.getGuild(), main, main.getBot());
            }

            Matcher mat = pat.matcher(text);

            while (mat.find())
            {
                String match = mat.group();
                String cleanMatch = match.replace("<", "").replace(">", "");
                String[] parts = cleanMatch.split("\\|");
                int num = r.nextInt(parts.length);
                String picked = parts[num].trim();

                if (picked.contains("list=") || picked.contains("list ="))
                {
                    String listName = picked.replace("list=", "").replace("list =", "").trim();
                    ListManager manager = ListManager.getManagerForGuild(guild);
                    ItemList list = manager.getListByName(listName.toLowerCase());

                    if (list != null)
                    {
                        if (!list.getMembers().isEmpty())
                        {
                            picked = list.getMembers().get(r.nextInt(list.getMembers().size()));
                        }
                        else
                        {
                            picked = "list " + listName + " is empty";
                        }
                    }
                    else
                    {
                        picked = "list " + listName + " not found";
                    }
                }
                else if (picked.contains("group=") || picked.contains("group ="))
                {
                    String groupName = picked.replace("group=", "").replace("group =", "").trim();
                    GroupManager manager = GroupManager.getManagerForGuild(guild);
                    Group group = manager.getGroupByName(groupName.toLowerCase());

                    if (group != null)
                    {
                        if (!group.getMembers().isEmpty())
                        {
                            picked = group.getMembers().get(r.nextInt(group.getMembers().size())).getName();
                        }
                        else
                        {
                            picked = "group " + groupName + " is empty";
                        }
                    }
                    else
                    {
                        picked = "group " + groupName + " not found";
                    }
                }
                else if (picked.toLowerCase().equals("username"))
                {
                    picked = name;
                }

                Matcher secondaryMat = pat.matcher(text);
                text = secondaryMat.replaceFirst(picked);
            }

            SoundManager.playSound(
                    Voice.get().getAudio(text),
                    event.getGuild());
        }
    }
}
