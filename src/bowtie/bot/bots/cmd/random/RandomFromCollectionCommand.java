package bowtie.bot.bots.cmd.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;
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
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class RandomFromCollectionCommand extends Command
{
    private Bot bot;
    private Main main;
    SplittableRandom r;

    /**
     * @param validExpressions
     * @param permission
     */
    public RandomFromCollectionCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        r = new SplittableRandom();
    }

    public RandomFromCollectionCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        r = new SplittableRandom();
    }

    /**
     * @see bowt.cmnd.Command#execute(bowt.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        GroupManager groupManager = GroupManager.getManagerForGuild(event.getGuildObject());

        String[] parts = event.getMessage().getContent().trim().toLowerCase().split(" ");
        String name = "";
        boolean online = false;
        boolean usedGroup = true;

        if (parts.length > 1)
        {
            name = parts[1];
        }
        else
        {
            this.bot.sendMessage("You have to add the name of the group or the list. Example: '"
                    + event.getGuildObject().getPrefix()
                    + "pick groupname'.", event.getMessage().getChannel(), Colors.RED);
            return;
        }
        if (parts.length > 2)
        {
            online = parts[2].toLowerCase().trim().equals("online");
        }
        Group group = groupManager.getGroupByName(name);
        ItemList list = null;

        if (group == null)
        {
            ListManager listManager = ListManager.getManagerForGuild(event.getGuildObject());
            list = listManager.getListByName(name);

            if (list == null)
            {
                this.bot.sendMessage(
                        "Make sure to open a group or list with that name by using the 'group' or 'list' command first.",
                        event.getMessage().getChannel(), Colors.RED);
                return;
            }
            else
            {
                usedGroup = false;
            }
        }

        if (usedGroup)
        {
            List<IUser> users = group.getMembers();

            if (online)
            {
                List<IUser> onlineUsers = new ArrayList<IUser>();

                for (IUser user : users)
                {
                    if (!user.getPresence().getStatus().equals(StatusType.OFFLINE)
                            && !user.getPresence().getStatus().equals(StatusType.INVISIBLE))
                    {
                        onlineUsers.add(user);
                    }
                }
                users = onlineUsers;
            }

            if (!users.isEmpty())
            {
                Collections.shuffle(users);
                int num = r.nextInt(users.size());
                this.bot.sendMessage(
                        users.get(num).mention(false) + " \n("
                                + users.get(num).getDisplayName(event.getGuild()) + ")",
                        event.getChannel(), Colors.PURPLE);
            }
        }
        else
        {
            List<String> members = list.getMembers();

            if (!members.isEmpty())
            {
                Collections.shuffle(members);
                int num = r.nextInt(members.size());
                this.bot.sendMessage(members.get(num), event.getChannel(), Colors.PURPLE);
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

        builder.withAuthorName("Random Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html/#collections");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Picks a random user from either a group \n"
                + "|  or list with the given name.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "pick groupname\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "pick listname\n"
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