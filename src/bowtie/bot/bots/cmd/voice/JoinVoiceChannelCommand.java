package bowtie.bot.bots.cmd.voice;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;
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
public class JoinVoiceChannelCommand extends Command
{
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public JoinVoiceChannelCommand(String[] validExpressions, int permission, Main main)
    {
        super(validExpressions, permission);
        this.main = main;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        try
        {
            IUser user = event.getAuthor();
            IVoiceChannel wantedChannel = user.getVoiceStateForGuild(event.getGuild()).getChannel();

            if (wantedChannel != null)
            {
                RequestBuffer.request(() ->
                {
                    try
                    {
                        wantedChannel.join();
                    }
                    catch (Exception e)
                    {
                        Bot.errorLog.print(e);
                    }
                }).get();
            }
            else
            {
                main.getBot().sendMessage("You have to be in a voicechannel.", event.getMessage().getChannel(),
                        Colors.RED);
            }
        }
        catch (MissingPermissionsException e)
        {
            main.getBot().sendMessage("Please make sure that the bot has permissions to connect to a voicechannel.",
                    event
                            .getMessage().getChannel(), Colors.RED);
        }
    }

    /**
     * @see bowt.cmnd.Command#getHelp(bowt.guild.GuildObject)
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Join Voicechannel Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#voice");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Makes the bot join your voice channel.\n"
                + "|  \n"
                + "|  While the bot is in a voice channel it will announce people\n"
                + "|  who join and leave that channel.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "joinme\n"
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