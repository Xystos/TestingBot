package bowtie.bot.bots.cmd.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

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
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class RandomCommand extends Command
{
    private Bot bot;
    private Main main;
    SplittableRandom r;

    /**
     * @param validExpressions
     * @param permission
     */
    public RandomCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        r = new SplittableRandom();
    }

    public RandomCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        List<IUser> users = new ArrayList<IUser>();

        boolean hasMentions = false;

        int picks = 1;
        String[] parts = event.getFixedContent().split(" ");

        try
        {
            picks = Integer.parseInt(parts[1]);

            if (picks < 1)
            {
                picks = 1;
            }
        }
        catch (Exception e)
        {

        }

        if (!event.getMessage().getMentions().isEmpty())
        {
            hasMentions = true;
            for (IUser user : event.getMessage().getMentions())
            {
                if (!users.contains(user))
                {
                    users.add(user);
                }
            }
        }
        if (!event.getMessage().getRoleMentions().isEmpty())
        {
            hasMentions = true;
            for (IRole role : event.getMessage().getRoleMentions())
            {
                for (IUser user : event.getGuild().getUsersByRole(role))
                {
                    if (!users.contains(user))
                    {
                        users.add(user);
                    }
                }
            }
        }

        if (!hasMentions)
        {
            this.bot.sendMessage("Make sure to mention either users or at least one role for the bot to pick from.",
                    event.getMessage().getChannel(), Colors.RED);
            return;
        }

        Collections.shuffle(users);

        int num;
        IUser user;

        if (picks == 1)
        {
            if (!users.isEmpty())
            {
                num = r.nextInt(users.size());
                user = users.get(num);
                this.bot.sendMessage(
                        user.mention(false) + " \n(" + user.getDisplayName(event.getGuild()) + ")",
                        event.getChannel(), Colors.PURPLE);
            }
        }
        else
        {
            if (picks > users.size())
            {
                this.bot.sendMessage("Not enough people to pick from.", event.getChannel(), Colors.RED);
                return;
            }

            List<String> mentions = new ArrayList<>();

            while (mentions.size() < picks)
            {
                if (!users.isEmpty())
                {
                    num = r.nextInt(users.size());
                    user = users.get(num);
                    mentions.add(user.mention(false) + " \n(" + user.getDisplayName(event.getGuild())
                            + ")");
                    users.remove(num);
                }
            }

            this.bot.sendListMessage("Picked users", mentions, event.getChannel(), 10, false);
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
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html/#random_user");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Picks a random user from either mentioned users\n"
                + "|  or mentioned roles.\n"
                + "|  \n"
                + "|  You can add a number after the command to pick multiple users.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "random @user @user\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "random 5 @role\n"
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