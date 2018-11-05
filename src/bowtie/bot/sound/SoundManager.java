package bowtie.bot.sound;

import javax.sound.sampled.AudioInputStream;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.audio.AudioPlayer;
import bowt.bot.Bot;
import bowt.guild.GuildObject;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public final class SoundManager
{
    public static final String DEFAULT_JOIN_TEXT = "<username> joined.";
    public static final String DEFAULT_LEAVE_TEXT = "<username> left.";

    private static Main main;

    public static void setMain(Main main)
    {
        SoundManager.main = main;
    }

    public static boolean joinDefaultChannel(GuildObject guild)
    {
        String channelID = main.getDatabase().getDefaultVoiceChannel(guild.getStringID());

        if (channelID == null)
        {
            return false;
        }

        IVoiceChannel channel = guild.getGuild().getVoiceChannelByID(Long.parseLong(channelID));

        if (channel != null)
        {
            RequestBuffer.request(() ->
            {
                try
                {
                    channel.join();
                }
                catch (Exception e)
                {
                    Bot.errorLog.print(e);
                }
            }).get();

            return true;
        }
        else
        {
            main.getDatabase().resetDefaultVoiceChannel(guild.getStringID());
        }

        return false;
    }

    public static void playSound(AudioInputStream in, IGuild guild)
    {
        AudioPlayer audioPlayer = AudioPlayer.getAudioPlayerForGuild(guild);

        if (guild.getConnectedVoiceChannel() != null)
        {
            audioPlayer.queue(in);
        }
    }
}