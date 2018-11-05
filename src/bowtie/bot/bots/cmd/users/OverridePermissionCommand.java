package bowtie.bot.bots.cmd.users;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;
import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.hand.impl.GuildCommandHandler;
import bowt.util.perm.UserPermissions;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class OverridePermissionCommand extends Command
{
    private Bot bot;
    private Main main;

    /**
     * @param validExpressions
     * @param permission
     */
    public OverridePermissionCommand(String[] validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @param validExpressions
     * @param permission
     */
    public OverridePermissionCommand(List<String> validExpressions, int permission, Bot bot, Main main)
    {
        super(validExpressions, permission, true);
        this.bot = bot;
        this.main = main;
    }

    /**
     * @see bowtie.bot.obj.Command#execute(bowtie.evnt.impl.CommandEvent)
     */
    @Override
    public void execute(CommandEvent event)
    {
        String[] parts = event.getMessage().getContent().split(" ");
        if (parts.length < 3)
        {
            this.bot.sendMessage(
                    "Usage: " + event.getGuildObject().getPrefix() + "override commandName permissionName",
                    event.getChannel(), Colors.RED);
            return;
        }
        String commandText = parts[1];
        Command command = ((GuildCommandHandler)event.getGuildObject().getCommandHandler()).getCommand(commandText);
        if (command != null)
        {
            // desired level
            int permissionLevel = UserPermissions.getPermissionLevelForString(parts[2]);

            if (permissionLevel == -1)
            {
                this.bot.sendMessage("Unknown permission level.", event.getChannel(), Colors.RED);
                return;
            }

            // user level
            int userPerm = UserPermissions.getPermissionLevel(event.getAuthor(), event.getGuildObject());

            if (permissionLevel == UserPermissions.NONE && userPerm != UserPermissions.CREATOR)
            {
                this.bot.sendMessage("Can't use NONE for overriding.", event.getChannel(), Colors.RED);
                return;
            }

            if (!command.isValidPermission(userPerm, event.getGuildObject()))
            {
                this.bot.sendMessage("Your permission level is too low to override this command.", event.getChannel(),
                        Colors.RED);
            }
            else if (userPerm < permissionLevel)
            {
                this.bot.sendMessage("You can't change the permission level to anything higher than your own.",
                        event.getChannel(), Colors.RED);
            }
            else
            {
                int addResp = command.overridePermission(permissionLevel, event.getGuildObject());
                if (addResp == Command.DEFAULT_PERMISSION)
                {
                    this.main.getDatabase().removeOverride(event.getGuildObject().getStringID(),
                            command.getValidExpressions().get(0));

                    this.bot.sendMessage("Changed the needed permission level of the '"
                            + command.getValidExpressions().get(0) + "' command "
                            + "to " + UserPermissions.getPermissionString(permissionLevel) + ".", event.getChannel(),
                            Colors.GREEN);

                    Bot.log.print(
                            this,
                            "Changed the needed permission level of the '" + command.getValidExpressions().get(0)
                                    + "' command "
                                    + " on '" + event.getGuild().getName() + "' to "
                                    + UserPermissions.getPermissionString(permissionLevel) + ".");
                }
                else if (addResp == Command.NEW_PERMISSION)
                {
                    this.main.getDatabase().addOverride(event.getGuildObject().getStringID(),
                            command.getValidExpressions().get(0), permissionLevel);

                    this.bot.sendMessage("Changed the needed permission level of the '"
                            + command.getValidExpressions().get(0) + "' command "
                            + "to " + UserPermissions.getPermissionString(permissionLevel) + ".", event.getChannel(),
                            Colors.GREEN);

                    Bot.log.print(
                            this,
                            "Changed the needed permission level of the '" + command.getValidExpressions().get(0)
                                    + "' command "
                                    + " on '" + event.getGuild().getName() + "' to "
                                    + UserPermissions.getPermissionString(permissionLevel) + ".");
                }
                else if (addResp == Command.CANT_OVERRIDE)
                {
                    this.bot.sendMessage("The permission level of this command can't be overriden.",
                            event.getChannel(), Colors.RED);
                }
            }
            return;
        }

        this.bot.sendMessage("That is not a valid command.", event.getChannel(), Colors.RED);
    }

    /**
     * @see bowtie.bot.obj.Command#getHelp()
     */
    @Override
    public EmbedObject getHelp(GuildObject guild)
    {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("Override Permissions Command");
        builder.withAuthorUrl("http://www.bowtiebots.xyz/_commands_.html#masters");
        builder.withAuthorIcon(Main.BOWTIE_BOT_ICON);

        builder.withDescription("|  Allows you to change the needed permission level for a command.\n"
                + "|  \n"
                + "|  The valid permission level are:\n"
                + "|  \n"
                + "|  - OWNER\n"
                + "|  - MASTER\n"
                + "|  - USER\n"
                + "|  \n"
                + "|  You can't set a sppecific permission level to something higehr than \n"
                + "|  your own level for that server.\n"
                + "|  \n"
                + "|  For example if you have master permissions then you can't set any \n"
                + "|  commands to owner permissions.\n"
                + "|  \n"
                + "|  You can also not change the level of commands that require a higher\n"
                + "|  permission level than you have on that server.\n"
                + "|  \n"
                + "|  So if you have master permissions you can't change the needed \n"
                + "|  permissions of owner commands.\n"
                + "|  \n"
                + "|  To change the level simply put the name of the command and the \n"
                + "|  desired permission level after the 'override' word.\n"
                + "|");

        builder.appendField(
                "|  Usage:",
                "|  " + guild.getPrefix() + "override commandName permissionLevel\n"
                        + "|  \n"
                        + "|  " + guild.getPrefix() + "override joingroup master\n"
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