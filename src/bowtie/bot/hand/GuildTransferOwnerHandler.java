package bowtie.bot.hand;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.GuildTransferOwnershipEvent;
import bowt.guild.GuildObject;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class GuildTransferOwnerHandler implements IListener<GuildTransferOwnershipEvent>
{
    private Main main;

    public GuildTransferOwnerHandler(Main main)
    {
        this.main = main;
    }

    /**
     * @see sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.events.Event)
     */
    @Override
    public void handle(GuildTransferOwnershipEvent event)
    {
        GuildObject guild = this.main.getBot().getGuildObjectByID(event.getGuild().getStringID());
        if (guild == null)
        {
            guild = GuildSetupHandler.setupGuildObject(event.getGuild(), this.main, this.main.getBot());
        }
        if (guild.addOwner(event.getNewOwner()))
        {
            if (guild.isMaster(event.getNewOwner()))
            {
                // if a master is being promoted
                guild.removeMaster(event.getNewOwner());
                this.main.getDatabase().removeMaster(event.getNewOwner().getStringID(), guild.getStringID());
                this.main.getDatabase().addMaster(event.getNewOwner().getStringID(),
                        guild.getStringID(), UserPermissions.OWNER);
            }
            else
            {
                this.main.getDatabase().addMaster(event.getNewOwner().getStringID(),
                        guild.getStringID(), UserPermissions.OWNER);
            }
        }
    }
}