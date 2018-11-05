package bowtie.bot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bowtie.core.Main;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;

/**
 * @author &#8904
 *
 */
public class Activation
{
    private static final long CHECK_COOLDOWN = 300000;
    private static Main main;
    private static Map<String, Long> lastChecked = new ConcurrentHashMap<>();

    public static void setMain(Main main)
    {
        Activation.main = main;
    }

    /**
     * Checks if the guild with the given id is still backed by a Patron supporter.
     * 
     * <p>
     * This method will check all users that have activated the guild to see if they still have a premium role on the
     * home guild.
     * </p>
     * <p>
     * If the check returns true this method will always return true for the next 5 minutes to avoid unneccessary API
     * calls.
     * </p>
     * 
     * @param guildID
     * @return true if the guilds activation is still backed by a valid Patreon supporter or if this method returned
     *         true within the last 5 minutes. false otherwise.
     */
    public static boolean isActivated(String guildID)
    {
        if (!Main.NEEDS_ACTIVATION)
        {
            return true;
        }

        if (!needsCheck(guildID))
        {
            return true;
        }

        List<String> userIDs = main.getDatabase().getUsersForGuild(guildID);
        List<IUser> users = new ArrayList<IUser>();

        for (String id : userIDs)
        {
            IUser user = RequestBuffer.request(new IRequest<IUser>()
            {
                @Override
                public IUser request()
                {
                    return main.getBot().getClient().fetchUser(Long.parseLong(id));
                }
            }).get();

            if (user != null)
            {
                users.add(user);
            }
        }

        for (IUser user : users)
        {
            int allowedServers = getAllowedServers(user);
            int activatedServers = main.getDatabase().activationCount(user.getStringID());

            if (activatedServers <= allowedServers)
            {
                lastChecked.put(guildID, System.currentTimeMillis());
                return true;
            }

            main.getDatabase().clearUserActivations(user.getStringID());
        }
        return false;
    }

    public static int getAllowedServers(IUser user)
    {
        return Integer.MAX_VALUE;
    }

    public static List<IRole> getPremiumRoles()
    {
        return null;
    }

    public static IRole getPremiumRole(String name)
    {
        return getPremiumRoles().stream()
                .filter(role -> role.getName().contains(name))
                .findFirst()
                .orElse(null);
    }

    public static synchronized boolean needsCheck(String guildID)
    {
        Long lastCheck = lastChecked.get(guildID);

        if (lastCheck != null)
        {
            return System.currentTimeMillis() - lastCheck >= CHECK_COOLDOWN;
        }

        return true;
    }
}