package bowtie.bot.bots.cmd.collections;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
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
public class RemoveFromCollectionCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public RemoveFromCollectionCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public RemoveFromCollectionCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @see bowt.cmnd.Command#execute(bowt.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        GroupManager manager = GroupManager.getManagerForGuild(event.getGuildObject());
        String[] parts = event.getMessage().getContent().trim().split(" ");
        String name = "";
        boolean usedGroup = true;

        if (parts.length > 1)
        {
            name = parts[1];
        }

        Group group = manager.getGroupByName(name.toLowerCase());

        ItemList list = null;
        ListManager listManager = null;

        if (group == null)
        {
            listManager = ListManager.getManagerForGuild(event.getGuildObject());
            list = listManager.getListByName(name.toLowerCase());

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
            List<IUser> users = event.getMentions();

            for (IRole role : event.getMessage().getRoleMentions())
            {
                for (IUser user : event.getGuild().getUsersByRole(role))
                {
                    users.add(user);
                }
            }

            if (parts.length <= 1 || users.isEmpty())
            {
                this.bot.sendMessage("Usage: '" + event.getGuildObject().getPrefix() + "remove groupname @user'.",
                        event.getMessage()
                                .getChannel(), Colors.RED);
                return;
            }

            int count = 0;
            for (IUser user : users)
            {
                if (group.removeMember(user))
                {
                    count ++ ;
                }
            }

            this.bot.sendMessage("Removed " + count + " member/s from the group '" + group.getName() + "'.",
                    event.getMessage().getChannel(), count == 0 ? Colors.RED : Colors.GREEN);
        }
        else
        {
            String item = "";

            if (parts.length > 2)
            {
                for (int i = 2; i < parts.length; i ++ )
                {
                    item += parts[i] + " ";
                }
                item = item.trim();
            }
            else
            {
                item = null;
            }

            System.out.println(item);

            if (parts.length <= 1 || item == null)
            {
                this.bot.sendMessage("Usage: '" + event.getGuildObject().getPrefix() + "remove listname listitem'.",
                        event.getMessage()
                                .getChannel(), Colors.RED);
                return;
            }

            int count = 0;
            if (list.removeMember(item))
            {
                count ++ ;
            }

            this.bot.sendMessage("Removed " + count + " member/s from the list '" + list.getName() + "'.",
                    event.getMessage().getChannel(), count == 0 ? Colors.RED : Colors.GREEN);
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Remove From Collection Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#collections");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will remove members from the collection \n"
                + "|  (list or group) with the given name. You can either put the name of \n"
                + "|  a list item or one/multiple user mentions after the collection name.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "removemember groupname @user @user\n"
                        + "|  " + guild.getPrefix() + "removemember listname listitem\n"
                        + "|  " + guild.getPrefix() + "removemember animals monkey\n"
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