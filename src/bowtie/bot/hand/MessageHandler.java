package bowtie.bot.hand;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import bowt.bot.Bot;
import bowt.cmnd.Command;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.hand.impl.GuildCommandHandler;
import bowtie.bot.bots.cmd.dict.WikipediaLookUpCommand;
import bowtie.bot.bots.cmd.dict.WordnetLookUpCommand;
import bowtie.bot.bots.cmd.feeds.AddFeedCommand;
import bowtie.bot.bots.cmd.feeds.CheckFeedCommand;
import bowtie.bot.bots.cmd.feeds.FeedSettingsCommand;
import bowtie.bot.bots.cmd.feeds.GetYoutubeChannelFeedCommand;
import bowtie.bot.bots.cmd.feeds.RemoveFeedCommand;
import bowtie.bot.bots.cmd.feeds.ShowFeedsCommand;
import bowtie.bot.bots.cmd.misc.HelpCommand;
import bowtie.bot.bots.cmd.misc.MemoryCommand;
import bowtie.bot.bots.cmd.misc.RebootCommand;
import bowtie.bot.bots.cmd.misc.ShutdownCommand;
import bowtie.bot.bots.cmd.misc.ThreadCountCommand;
import bowtie.bot.bots.cmd.random.M8Command;
import bowtie.bot.bots.cmd.random.RandomAllCommand;
import bowtie.bot.bots.cmd.random.RandomNumberCommand;
import bowtie.bot.bots.cmd.random.RandomOnlineCommand;
import bowtie.bot.bots.cmd.teams.TeamCommand;
import bowtie.bot.bots.cmd.updates.PatchCommand;
import bowtie.bot.bots.cmd.updates.ResetUpdateChannelCommand;
import bowtie.bot.bots.cmd.updates.UpdatesCommand;
import bowtie.bot.bots.cmd.users.AddMasterCommand;
import bowtie.bot.bots.cmd.users.AddOwnerCommand;
import bowtie.bot.bots.cmd.users.BanUserCommand;
import bowtie.bot.bots.cmd.users.GetPermissionLevelCommand;
import bowtie.bot.bots.cmd.users.RemoveMasterCommand;
import bowtie.bot.bots.cmd.users.RemoveOwnerCommand;
import bowtie.bot.bots.cmd.users.ShowMastersCommand;
import bowtie.bot.bots.cmd.users.UnbanUserCommand;
import bowtie.bot.bots.cmd.voice.JoinVoiceChannelCommand;
import bowtie.bot.bots.cmd.voice.LeaveVoiceChannelCommand;
import bowtie.bot.util.Activation;
import bowtie.core.Main;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author &#8904
 *
 */
public class MessageHandler implements IListener<MessageReceivedEvent>
{
    private Bot bot;
    private Main main;
    private SplittableRandom r = new SplittableRandom();
    private ArrayList<Class<? extends Command>> freeCommands = new ArrayList<Class<? extends Command>>();

    public MessageHandler(Bot bot, Main main)
    {
        this.bot = bot;
        this.main = main;
        freeCommands.add(HelpCommand.class);
        freeCommands.add(M8Command.class);

        freeCommands.add(UpdatesCommand.class);
        freeCommands.add(ResetUpdateChannelCommand.class);

        freeCommands.add(RandomAllCommand.class);
        freeCommands.add(RandomOnlineCommand.class);
        freeCommands.add(RandomNumberCommand.class);

        freeCommands.add(AddFeedCommand.class);
        freeCommands.add(GetYoutubeChannelFeedCommand.class);
        freeCommands.add(RemoveFeedCommand.class);
        freeCommands.add(ShowFeedsCommand.class);
        freeCommands.add(FeedSettingsCommand.class);
        freeCommands.add(CheckFeedCommand.class);

        freeCommands.add(WordnetLookUpCommand.class);
        freeCommands.add(WikipediaLookUpCommand.class);

        freeCommands.add(GetPermissionLevelCommand.class);
        freeCommands.add(AddMasterCommand.class);
        freeCommands.add(AddOwnerCommand.class);
        freeCommands.add(RemoveMasterCommand.class);
        freeCommands.add(RemoveOwnerCommand.class);
        freeCommands.add(ShowMastersCommand.class);

        freeCommands.add(TeamCommand.class);

        freeCommands.add(JoinVoiceChannelCommand.class);
        freeCommands.add(LeaveVoiceChannelCommand.class);

        freeCommands.add(PatchCommand.class);
        freeCommands.add(ThreadCountCommand.class);
        freeCommands.add(UnbanUserCommand.class);
        freeCommands.add(BanUserCommand.class);
        freeCommands.add(RebootCommand.class);
        freeCommands.add(ShutdownCommand.class);
        freeCommands.add(MemoryCommand.class);
        freeCommands.add(ShutdownCommand.class);
    }

    /**
     * @see sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.events.Event)
     */
    @Override
    public void handle(MessageReceivedEvent event)
    {
        if (this.bot.isReady() && !event.getAuthor().isBot())
        {
            IMessage message = event.getMessage();
            String text = message.getContent();

            if (!event.getChannel().isPrivate())
            {
                GuildObject guildObject = bot.getGuildObjectByID(event.getGuild().getStringID());
                if (guildObject == null)
                {
                    guildObject = GuildSetupHandler.setupGuildObject(event.getGuild(), main, bot);
                }

                if (text.toLowerCase().trim().startsWith(guildObject.getPrefix()))
                {
                    handleCommand(event, guildObject);
                }
                else
                {
                    String formattedContent = message.getFormattedContent();
                    // true if either everyone or here is tagged
                    boolean everyone = message.mentionsEveryone()
                            || message.mentionsHere()
                            || (formattedContent != null && formattedContent.contains("@everyone")) ? true : false;
                    List<IUser> mentions = message.getMentions();
                    if (mentions.contains(bot.getClient().getOurUser()) && !everyone)
                    {
                        handleMention(event, guildObject);
                    }
                }
            }
            else
            {
                handlePrivateMessage(event);
            }
        }
    }

    private void handleMention(MessageReceivedEvent event, GuildObject guildObject)
    {
        IMessage message = event.getMessage();
        String text = message.getContent();

        for (IUser mention : message.getMentions())
        {
            text = text.replace(mention.mention().replace("!", ""), "").trim();
        }

        if (text.toLowerCase().equals("help") || text.toLowerCase().equals("info")
                || text.toLowerCase().equals("commands"))
        {
            CommandEvent cmdEvent = new CommandEvent(guildObject, message);
            Command cmd = ((GuildCommandHandler)guildObject.getCommandHandler()).getCommand("help");

            cmd.execute(cmdEvent);
            return;
        }
        else if (text.toLowerCase().equals("prefix"))
        {
            CommandEvent cmdEvent = new CommandEvent(guildObject, message);
            Command cmd = ((GuildCommandHandler)guildObject.getCommandHandler()).getCommand("prefix");

            cmd.execute(cmdEvent);
            return;
        }
        else if (text.toLowerCase().equals("ur mom gay") || text.toLowerCase().equals("ur mom gay lol")
                || text.toLowerCase().equals("your mom gay lol") || text.toLowerCase().equals("your mom gay"))
        {
            bot.sendPlainMessage("no u", event.getChannel());
            return;
        }

        CommandEvent cmdEvent = new CommandEvent(guildObject, message);
        Command cmd = ((GuildCommandHandler)guildObject.getCommandHandler()).getCommand("8ball");
        cmd.execute(cmdEvent);
        Bot.log.print(this, "'" + cmdEvent.getCommand() + "' command was used.");
    }

    private void handleCommand(MessageReceivedEvent event, GuildObject guildObject)
    {
        if (!bot.isBanned(event.getAuthor()))
        {
            if (!hasMuteRole(event.getAuthor(), event.getGuild()))
            {
                IMessage message = event.getMessage();
                String text = message.getContent();

                CommandEvent cmdEvent = new CommandEvent(guildObject, message);

                GuildCommandHandler handler = (GuildCommandHandler)guildObject.getCommandHandler();

                Command usedCommand = handler.getCommand(cmdEvent.getCommand());

                if (usedCommand == null)
                {
                    usedCommand = handler.getCommandForAlias(
                            guildObject.getStringID(),
                            cmdEvent.getCommand());
                }

                if (usedCommand != null)
                {
                    if (freeCommands.contains(usedCommand.getClass()))
                    {
                        if (guildObject.getCommandHandler().dispatch(cmdEvent))
                        {
                            Bot.log.print(this, "'" + cmdEvent.getCommand() + "' command was used.\n"
                                    + "Input: " + text);
                        }
                    }
                    else if (main.getDatabase().isActivatedGuild(event.getGuild().getStringID()))
                    {
                        if (Activation.isActivated(event.getGuild().getStringID()))
                        {
                            if (guildObject.getCommandHandler().dispatch(cmdEvent))
                            {
                                Bot.log.print(this, "'" + cmdEvent.getCommand() + "' command was used.\n"
                                        + "Input: " + text);
                            }
                        }
                        else
                        {
                            String infoMessage =
                                    "**Bot not activated**\n\n\n"
                                            + "The bot is not activated for your server. On an unactivated server you wont be able "
                                            + "to use all commands. [Here](http://www.bowtiebots.xyz/_commands_non_patron_.html) "
                                            + "you can find a list of all free commands. \n\n"
                                            + "Visit the [Bowtie Bots Server](https://discord.gg/KRdQK8q) or the "
                                            + "[Bowtie Bots website](http://www.bowtiebots.xyz) "
                                            + "for more information on how to activate the hosted bot and testing of all features.\n\n"
                                            + "If you are supporting me on Patreon use the '" + guildObject.getPrefix()
                                            + "activate' command to enable the bot for this server.";

                            bot.sendMessage(infoMessage, event.getChannel(), Colors.RED);
                        }
                    }
                    else
                    {
                        String infoMessage =
                                "**Bot not activated**\n\n\n"
                                        + "The bot is not activated for your server. On an unactivated server you wont be able "
                                        + "to use all commands. [Here](http://www.bowtiebots.xyz/_commands_non_patron_.html) "
                                        + "you can find a list of all free commands. \n\n"
                                        + "Visit the [Bowtie Bots Server](https://discord.gg/KRdQK8q) or the "
                                        + "[Bowtie Bots website](http://www.bowtiebots.xyz) "
                                        + "for more information on how to activate the hosted bot and testing of all features.\n\n"
                                        + "If you are supporting me on Patreon use the '" + guildObject.getPrefix()
                                        + "activate' command to enable the bot for this server.";

                        bot.sendMessage(infoMessage, event.getChannel(), Colors.RED);
                    }
                }
            }
        }
    }

    private void handlePrivateMessage(MessageReceivedEvent event)
    {
        IMessage message = event.getMessage();
        String text = message.getContent();
        String answer = niceWords(text);

        if (answer == null)
        {
            if (text.toLowerCase().trim().contains("https://discord.gg/"))
            {
                answer = sendAnswer(
                        "If you want to invite me click "
                                + "[here](https://discordapp.com/api/oauth2/authorize?client_id=467705661745135629&permissions=0&scope=bot).",
                        false, message);
            }
            else if (text.toLowerCase().trim().startsWith(Bot.getPrefix()))
            {
                answer = sendAnswer(
                        "Commands don't work in private chats.",
                        false, message);
            }
            else
            {
                answer = sendAnswer(pickAnswer("-1"), false, message);
            }
        }
        else
        {
            sendAnswer(answer, false, message);
            bot.sendMessage(message.getAuthor().getName() + "#"
                    + message.getAuthor().getDiscriminator() + " sent '" + text + "'.", bot
                    .getClient().getApplicationOwner().getOrCreatePMChannel(), Colors.ORANGE);
        }
    }

    private String sendAnswer(String answer, boolean plain, IMessage message)
    {
        if (answer.startsWith("http"))
        {
            if (plain)
            {
                bot.sendPlainMessage(answer, message.getChannel());
            }
            else
            {
                bot.sendPlainMessage(message.getAuthor().mention() + " " + answer, message.getChannel());
            }
        }
        else
        {
            if (plain)
            {
                bot.sendPlainMessage(answer, message.getChannel());
            }
            else
            {
                bot.sendMessage(message.getAuthor().mention() + " " + answer, message.getChannel(),
                        Colors.PURPLE);
            }
        }
        return answer;
    }

    private String pickAnswer(String guildID)
    {
        List<String> lines = main.getDatabase().getLines(guildID);
        if (!lines.isEmpty())
        {
            int num = r.nextInt(lines.size());
            return lines.get(num);
        }
        return "";
    }

    private String niceWords(String input)
    {
        input = input.toLowerCase();
        if (input.contains("life") && input.contains("fall")
                && (input.contains("apart") || input.contains("appart")))
        {
            return "Sometimes life is divided into many pieces which drift into different directions, but there is always "
                    + "enough material around to build something beautiful with. If you need someone to talk to add Lukas&#8904.";
        }
        else if (((input.contains("kill") && input.contains("myself")) || input.contains("kms"))
                && input.contains("i"))
        {
            return "No, you should not kill yourself. There is people who love you. If you need someone to talk to add Lukas&#8904.";
        }
        else if (input.contains("i") && input.contains("die") && input.contains("alone"))
        {
            return "In the end everybody will die alone as death is something we have to face on our own, but you will never live alone "
                    + "because there is always people around you who care about you. If you need someone to talk to add Lukas&#8904.";
        }
        else if ((input.contains("my") && input.contains("life") || input.contains("i")
                && input.contains("am"))
                && input.contains("worthless"))
        {
            return "Nobody's life is worthless. If you need someone to talk to add Lukas&#8904.";
        }
        else if (input.contains("i") && input.contains("should") && input.contains("cut")
                && input.contains("myself"))
        {
            return "No. Cutting yourself might give you a good feeling, but only for a short time and only with severe physical consequences."
                    + " Wouldn't it be much better to find something that will give you a good feeling for a much longer period? "
                    + "If you need someone to talk to add Lukas&#8904, maybe we can find something that makes you happy.";
        }
        else if (input.contains("i") && input.contains("want") && input.contains("die"))
        {
            return "Sometimes life is divided into many pieces which drift into different directions, but there is always "
                    + "enough material around to build something beautiful with. If you need someone to talk to add Lukas&#8904.";
        }

        return null;
    }

    private boolean hasMuteRole(IUser user, IGuild guild)
    {
        List<IRole> roles = guild.getRolesByName("Bowtie Silenced");
        if (!roles.isEmpty())
        {
            return user.getRolesForGuild(guild).contains(roles.get(0));
        }
        return false;
    }
}