package bowtie.bot.bots.cmd.collections;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
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
public class ShowCollectionMembersCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public ShowCollectionMembersCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public ShowCollectionMembersCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        String[] parts = event.getMessage().getContent().trim().toLowerCase().split(" ");
        String name = "";
        boolean usedGroup = true;

        if (parts.length > 1)
        {
            name = parts[1];
        }
        else
        {
            this.bot.sendMessage("You have to add the name of the group or list. Example: '"
                    + event.getGuildObject().getPrefix()
                    + "members groupname'.", event.getMessage().getChannel(), Colors.RED);
            return;
        }
        Group group = manager.getGroupByName(name.toLowerCase());
        ItemList list = null;
        ListManager listManager = null;

        if (group == null)
        {
            listManager = ListManager.getManagerForGuild(event.getGuildObject());
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
            List<String> mentions = new ArrayList<String>();

            for (IUser user : users)
            {
                mentions.add(user.mention() + " \n(" + user.getDisplayName(event.getGuild()) + ")");
            }

            bot.sendListMessage("Members of '" + group.getName() + "'", mentions, event.getChannel(), 15, true);
        }
        else
        {
            List<String> members = list.getMembers();

            bot.sendListMessage("Members of '" + list.getName() + "'", members, event.getChannel(), 15, false);
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Collection Members Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#collections");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will show all members of the collection\n"
                + "|  (group or list) with the given name.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "members groupname\n"
                        + "|  " + guild.getPrefix() + "members listname\n"
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