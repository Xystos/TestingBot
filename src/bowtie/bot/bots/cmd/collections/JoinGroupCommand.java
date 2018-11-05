package bowtie.bot.bots.cmd.collections;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.bot.collections.group.Group;
import bowtie.bot.collections.group.GroupManager;
import bowtie.core.Main;

import com.vdurmont.emoji.EmojiManager;

/**
 * @author &#8904
 *
 */
public class JoinGroupCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public JoinGroupCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public JoinGroupCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        if (parts.length > 1)
        {
            name = parts[1];
        }
        else
        {
            this.bot.sendMessage("You have to add the name of the group. Example: '"
                    + event.getGuildObject().getPrefix()
                    + "join groupname'.", event.getMessage().getChannel(), Colors.RED);
            return;
        }
        Group group = manager.getGroupByName(name.toLowerCase());
        if (group == null)
        {
            this.bot.sendMessage("Make sure to open a group by using the 'create' command first.", event.getMessage()
                    .getChannel(), Colors.RED);
            return;
        }

        if (!event.getMessage().getMentions().isEmpty())
        {
            int count = 0;
            for (IUser user : event.getMessage().getMentions())
            {
                if (group.addMember(user))
                {
                    count ++ ;
                }
            }
            this.bot.sendMessage("Added " + count + " user/s to the group '" + group.getName() + "'.", event
                    .getMessage().getChannel(), count == 0 ? Colors.RED : Colors.GREEN);
        }
        else if (!event.getMessage().getRoleMentions().isEmpty())
        {
            int count = 0;
            for (IRole role : event.getMessage().getRoleMentions())
            {
                for (IUser user : event.getGuild().getUsersByRole(role))
                {
                    if (group.addMember(user))
                    {
                        count ++ ;
                    }
                }
            }
            this.bot.sendMessage("Added " + count + " user/s to the group '" + group.getName() + "'.", event
                    .getMessage().getChannel(), count == 0 ? Colors.RED : Colors.GREEN);
        }
        else if (group.addMember(event.getAuthor()))
        {
            RequestBuffer.request(() -> event.getMessage().addReaction(EmojiManager.getForAlias("white_check_mark")));
        }
        else
        {
            this.bot.sendMessage("You are already a member of that group.", event.getMessage().getChannel(), Colors.RED);
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Join Group Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#collections");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  This command will make you join the group with \n"
                + "|  the given name. You can mention users and/or roles after the command\n"
                + "|  to put them into the group instead of yourself. \n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "joingroup groupname\n"
                        + "|  " + guild.getPrefix() + "joingroup groupname @user @user @role\n"
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