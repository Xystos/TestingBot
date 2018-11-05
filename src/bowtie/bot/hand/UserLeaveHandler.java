package bowtie.bot.hand;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import bowt.bot.Bot;
import bowt.guild.GuildObject;
import bowtie.bot.collections.group.GroupManager;
import bowtie.core.Main;

/**
 * @author &#8904
 */
public class UserLeaveHandler implements IListener<UserLeaveEvent>
{
    private Bot bot;
    private Main main;

    public UserLeaveHandler(Bot bot, Main main)
    {
        this.bot = bot;
        this.main = main;
    }

    @Override
    public void handle(UserLeaveEvent event)
    {
        try
        {
            IUser user = event.getUser();
            IGuild guild = event.getGuild();
            GuildObject guildObject = this.bot.getGuildObjectByID(guild.getStringID());

            GroupManager manager = GroupManager.getManagerForGuild(guildObject);
            manager.removeFromAll(user);

            if (guildObject.isMaster(user))
            {
                guildObject.removeMaster(user);
                this.main.getDatabase().removeMaster(user.getStringID(), guildObject.getStringID());
            }
            else if (guildObject.isOwner(user))
            {
                guildObject.removeOwner(user);
                this.main.getDatabase().removeMaster(user.getStringID(), guildObject.getStringID());
            }
        }
        catch (Exception e)
        {
        }
    }
}