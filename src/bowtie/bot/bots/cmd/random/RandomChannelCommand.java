package bowtie.bot.bots.cmd.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.StatusType;
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
public class RandomChannelCommand extends Command
{
    private Bot bot;
    private Main main;
    SplittableRandom r;

    /**
     * @param validExpressions
     * @param permission
     */
    public RandomChannelCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
        r = new SplittableRandom();
    }

    public RandomChannelCommand(List<String> validExpressions, int permission, Bot bot, Main main)
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
        boolean exceptUser = false;
        String[] parts = event.getMessage().getContent().split(" ");
        if (parts.length > 1 && parts[1].trim().equals("n")
                || parts.length > 2 && parts[2].trim().equals("n"))
        {
            exceptUser = true;
        }

        int picks = 1;

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
            try
            {
                picks = Integer.parseInt(parts[2]);

                if (picks < 1)
                {
                    picks = 1;
                }
            }
            catch (Exception ex)
            {
                try
                {
                    picks = Integer.parseInt(parts[3]);

                    if (picks < 1)
                    {
                        picks = 1;
                    }
                }
                catch (Exception exc)
                {

                }
            }
        }

        List<IUser> allUsers = null;
        List<IUser> users = new ArrayList<IUser>();

        if (event.getMessage().getChannelMentions().isEmpty())
        {
            List<IVoiceChannel> channels = event.getGuild().getVoiceChannels();
            IVoiceChannel wantedChannel = null;
            for (IVoiceChannel voiceChannel : channels)
            {
                if (voiceChannel.getConnectedUsers().contains(event.getAuthor()))
                {
                    wantedChannel = voiceChannel;
                }
            }
            if (wantedChannel != null)
            {
                allUsers = wantedChannel.getConnectedUsers();
            }
            else
            {
                this.bot.sendMessage("You have to be in a voicechannel or tag a textchannel with a #.", event
                        .getMessage().getChannel(), Colors.RED);
                return;
            }
        }
        else
        {
            try
            {
                allUsers = event.getMessage().getChannelMentions().get(0).getUsersHere();
            }
            catch (Exception e)
            {
                Bot.errorLog.print(e);
            }
        }
        for (IUser user : allUsers)
        {
            if (event.getMessage().getContent().contains("online"))
            {
                if (!user.isBot() && !user.getPresence().getStatus().equals(StatusType.OFFLINE)
                        && !user.getPresence().getStatus().equals(StatusType.INVISIBLE))
                {
                    if (exceptUser && user.equals(event.getAuthor()))
                    {

                    }
                    else
                    {
                        users.add(user);
                    }
                }
            }
            else
            {
                if (!user.isBot())
                {
                    if (exceptUser && user.equals(event.getAuthor()))
                    {

                    }
                    else
                    {
                        users.add(user);
                    }
                }
            }
        }

        Collections.shuffle(users);

        int num;
        IUser user;

        if (picks == 1)
        {
            if (users.size() > 0)
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
                this.bot.sendMessage("There are not enough people in the channel.", event.getChannel(), Colors.RED);
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

        builder.withAuthorName("Random From Channel Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html/#random_user");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Picks a random user from the voicechannel that \n"
                + "|  you are in.\n"
                + "|  \n"
                + "|  If you mention a textchannel after the command the bot will \n"
                + "|  pick from all users that have read permissions in that channel.\n"
                + "|  \n"
                + "|  You can add a number after the command to pick multiple users.\n"
                + "|  \n"
                + "|  If you want to prevent the bot from picking you, you can add 'n'\n"
                + "|  to the command.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "channel\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "channel 5 n\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "channel #general\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "channel 3 #general\n"
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