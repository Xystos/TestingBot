package bowtie.bot.bots.cmd.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

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
public class RandomAllCommand extends Command
{
    private Bot bot;
    private Main main;
    SplittableRandom r;

    /**
     * @param validExpressions
     * @param permission
     */
    public RandomAllCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        r = new SplittableRandom();
    }

    public RandomAllCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        List<IUser> allusers = event.getGuild().getUsers();
        List<IUser> users = new ArrayList<IUser>();
        for (IUser user : allusers)
        {
            if (!user.isBot())
            {
                users.add(user);
            }
        }
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

        Collections.shuffle(users);

        int num;
        IUser user;

        if (picks == 1)
        {
            num = r.nextInt(users.size());
            user = users.get(num);
            this.bot.sendMessage(user.mention(false) + " \n(" + user.getDisplayName(event.getGuild())
                    + ")", event.getChannel(), Colors.PURPLE);
        }
        else
        {
            if (picks > users.size())
            {
                this.bot.sendMessage("There are not enough people on this server.", event.getChannel(), Colors.RED);
                return;
            }

            List<String> mentions = new ArrayList<>();

            while (mentions.size() < picks)
            {
                num = r.nextInt(users.size());
                user = users.get(num);
                mentions.add(user.mention(false) + " \n(" + user.getDisplayName(event.getGuild())
                        + ")");
                users.remove(num);
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

        builder.withAuthorName("Random All Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html/#random_user");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Picks a random non-bot user out of the entire \n"
                + "|  list of the server.\n"
                + "|  \n"
                + "|  You can add a number after the command to pick multiple users.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "all\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "all 5\n"
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