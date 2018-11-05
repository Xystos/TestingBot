package bowtie.bot.bots.cmd.users;

import java.awt.Color;
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
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class GetPermissionLevelCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public GetPermissionLevelCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    public GetPermissionLevelCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        Color color;

        IUser user;

        if (!event.getMentions().isEmpty())
        {
            user = event.getMentions().get(0);
        }
        else
        {
            user = event.getMessage().getAuthor();
        }

        String perm = UserPermissions.getPermissionString(UserPermissions.getPermissionLevel(user,
                event.getGuildObject()));

        switch (UserPermissions.getPermissionLevel(user, event.getGuildObject()))
        {
        case UserPermissions.CREATOR:
            color = Colors.RED;
            break;

        case UserPermissions.OWNER:
            color = Colors.ORANGE;
            break;

        case UserPermissions.MASTER:
            color = Colors.YELLOW;
            break;

        case UserPermissions.USER:
            color = Colors.DEFAULT;
            break;

        default:
            color = Color.GRAY;
            break;
        }

        this.bot.sendMessage(perm, event.getMessage().getChannel(), color);
    }

    /**
     * @see bowt.cmnd.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Get Permission Level Command");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Shows the permission level of yourself or\n"
                + "|  the mentioned user for this server.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "perms @user\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "perms @user\n"
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