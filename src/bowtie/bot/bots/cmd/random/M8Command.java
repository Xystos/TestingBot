package bowtie.bot.bots.cmd.random;

import java.util.List;
import java.util.SplittableRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.bot.collections.group.Group;
import bowtie.bot.collections.group.GroupManager;
import bowtie.bot.collections.list.ItemList;
import bowtie.bot.collections.list.ListManager;
import bowtie.bot.util.Activation;
import bowtie.core.Main;
import bowtie.core.db.DatabaseAccess;

/**
 * @author &#8904
 *
 */
public class M8Command extends Command
{
    private Bot bot;
    private Main main;
    private SplittableRandom r;
    private Pattern pat = Pattern.compile("(<[\\w|1-9\\s=]+>)");

    /**
     * @param validExpressions
     * @param permission
     */
    public M8Command(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        this.r = new SplittableRandom();
    }

    public M8Command(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        this.r = new SplittableRandom();
    }

    /**
     * @see bowt.cmnd.Command#execute(bowt.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        boolean active = false;

        if (main.getDatabase().isActivatedGuild(event.getGuildObject().getStringID()))
        {
            if (Activation.isActivated(event.getGuildObject().getStringID()))
            {
                active = true;
            }
        }

        List<String> lines = main.getDatabase().getLines((active ? event.getGuildObject().getStringID() : "-1"));
        String answer = "";

        if (!lines.isEmpty())
        {
            int num = r.nextInt(lines.size());
            answer = lines.get(num);

            Matcher mat = pat.matcher(answer);

            while (mat.find())
            {
                String match = mat.group();
                String cleanMatch = match.replace("<", "").replace(">", "");
                String[] parts = cleanMatch.split("\\|");
                num = r.nextInt(parts.length);
                String picked = parts[num].trim();

                if (picked.contains("list=") || picked.contains("list ="))
                {
                    String listName = picked.replace("list=", "").replace("list =", "").trim();
                    ListManager manager = ListManager.getManagerForGuild(event.getGuildObject());
                    ItemList list = manager.getListByName(listName.toLowerCase());

                    if (list != null)
                    {
                        if (!list.getMembers().isEmpty())
                        {
                            picked = list.getMembers().get(r.nextInt(list.getMembers().size()));
                        }
                        else
                        {
                            picked = "list is empty=" + listName;
                        }
                    }
                    else
                    {
                        picked = "list not found=" + listName;
                    }
                }
                else if (picked.contains("group=") || picked.contains("group ="))
                {
                    String groupName = picked.replace("group=", "").replace("group =", "").trim();
                    GroupManager manager = GroupManager.getManagerForGuild(event.getGuildObject());
                    Group group = manager.getGroupByName(groupName.toLowerCase());

                    if (group != null)
                    {
                        if (!group.getMembers().isEmpty())
                        {
                            picked = group.getMembers().get(r.nextInt(group.getMembers().size())).mention();
                        }
                        else
                        {
                            picked = "group is empty=" + groupName;
                        }
                    }
                    else
                    {
                        picked = "group not found=" + groupName;
                    }
                }

                Matcher secondaryMat = pat.matcher(answer);
                answer = secondaryMat.replaceFirst(picked);
            }
        }

        boolean plain = main.getDatabase().isPlainTextActive(event.getGuildObject()) == DatabaseAccess.PLAIN ? true
                : false;

        if (answer.startsWith("http"))
        {
            if (plain)
            {
                bot.sendPlainMessage(answer, event.getChannel());
            }
            else
            {
                bot.sendPlainMessage(event.getAuthor().mention() + " " + answer, event.getChannel());
            }
        }
        else
        {
            if (plain)
            {
                bot.sendPlainMessage(answer, event.getChannel());
            }
            else
            {
                bot.sendMessage(event.getAuthor().mention() + " " + answer, event.getChannel(), Colors.PURPLE);
            }
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("8Ball Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#8Ball");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Picks a random answer from its pool\n"
                + "|  of global and/or custom lines.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "8Ball Will I be rich?\n"
                        + "|  \n"
                        + "|  @Bowite Bot am I fat?\n"
                        + "|", false);

        builder.appendField(
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                        + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
                "[This command needs " + UserPermissions.getPermissionString(this.getPermissionOverride(guild))
                        + " permissions](http://www.bowtiebots.xyz/master-system.html)", false);
        builder.withFooterText("Click the title to read about this command on the website.");
        return builder.build();
    }
}