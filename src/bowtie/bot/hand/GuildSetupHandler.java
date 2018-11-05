package bowtie.bot.hand;

import java.util.List;
import java.util.Map;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.guild.GuildObject;
import bowt.hand.impl.GuildCommandHandler;
import bowt.util.perm.UserPermissions;
import bowtie.bot.collections.group.GroupManager;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class GuildSetupHandler
{
    public static GuildObject setupGuildObject(IGuild guild, Main main, Bot bot)
    {
        GuildObject guildObject = new GuildObject(guild);
        GuildCommandHandler commandHandler = Main.commandHandler;

        Map<String, Integer> overrides = main.getDatabase().getOverridesForGuild(guild.getStringID());

        for (Command command : commandHandler.getCommands())
        {
            if (command.canOverride() && overrides.containsKey(command.getValidExpressions().get(0)))
            {
                command.overridePermission(overrides.get(command.getValidExpressions().get(0)), guildObject);
            }
        }

        if (!overrides.isEmpty())
        {
            Bot.log.print("Loaded " + overrides.size() + " command overrides for '" + guildObject.getGuild().getName()
                    + "'.");
        }

        guildObject.setCommandHandler(commandHandler);

        if (bot.addGuildObject(guildObject))
        {
            main.getDatabase().addMaster(guildObject.getGuild().getOwner().getStringID(), guild.getStringID(),
                    UserPermissions.OWNER);
            guildObject.addOwner(guildObject.getGuild().getOwner());
            List<String> ids = null;
            ids = main.getDatabase().getMasterIDs(guildObject, UserPermissions.OWNER);
            for (String id : ids)
            {
                IUser user = RequestBuffer.request(new IRequest<IUser>()
                {
                    @Override
                    public IUser request()
                    {
                        return bot.getClient().fetchUser(Long.parseLong(id));
                    }
                }).get();
                guildObject.addOwner(user);
            }
            ids = main.getDatabase().getMasterIDs(guildObject, UserPermissions.MASTER);
            for (String id : ids)
            {
                IUser user = RequestBuffer.request(new IRequest<IUser>()
                {
                    @Override
                    public IUser request()
                    {
                        return bot.getClient().fetchUser(Long.parseLong(id));
                    }
                }).get();
                guildObject.addMaster(user);
            }
            GroupManager.addManager(new GroupManager(guildObject));
            Bot.errorLog.print("Added new GuildObject after NullPointer.");
        }
        return guildObject;
    }
}