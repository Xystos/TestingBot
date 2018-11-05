package bowtie.bot.sound;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import bowt.bot.Bot;

/**
 * @author &#8904
 *
 */
public class Voice
{
    private static final String VOICE_NAME = "dfki-spike-hsmm";// "cmu-slt-hsmm";
    private MaryInterface marytts;
    private static Voice voiceInstance;

    private Voice()
    {
    }

    private Voice(String voiceName)
    {
        try
        {
            marytts = new LocalMaryInterface();
            marytts.setVoice(voiceName);
        }
        catch (MaryConfigurationException ex)
        {
            Bot.errorLog.print(this, ex);
        }
    }

    public AudioInputStream getAudio(String text)
    {
        try
        {
            return marytts.generateAudio(text);
        }
        catch (SynthesisException e)
        {
            Bot.errorLog.print(this, e);
        }
        return null;
    }

    public static synchronized Voice get()
    {
        if (voiceInstance == null)
        {
            voiceInstance = new Voice(VOICE_NAME);
        }
        return voiceInstance;
    }
}