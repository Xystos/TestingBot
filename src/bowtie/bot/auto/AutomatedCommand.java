package bowtie.bot.auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import bowt.bot.Bot;
import bowt.cons.Colors;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.thread.Threads;
import bowtie.core.Main;

/**
 * @author &#8904
 *
 */
public class AutomatedCommand
{
    private static Map<String, Map<Long, AutomatedCommand>> automatedCommands = new ConcurrentHashMap<>();
    public volatile static long currentID = 1;
    public volatile static long size = 0;
    private DummyMessage message;
    private GuildObject guild;
    private Main main;
    private long interval;
    private long lastExecute;
    private String addText;
    private String commandString;
    private long ID;
    private boolean running = true;
    private ScheduledFuture future;

    public AutomatedCommand(long ID, DummyMessage message, GuildObject guild, Main main, long interval)
    {
        this.ID = ID;
        this.message = message;

        String[] parts = message.getContent().split("<>");
        this.commandString = parts[0].trim();
        if (!this.commandString.startsWith(guild.getPrefix()))
        {
            this.commandString = guild.getPrefix() + this.commandString;
        }

        if (parts.length > 1)
        {
            this.addText = parts[1].trim();
        }

        this.guild = guild;
        this.main = main;
        this.interval = interval;
        this.lastExecute = System.currentTimeMillis();
        run(true);
    }

    public AutomatedCommand(long ID, DummyMessage message, GuildObject guild, Main main, long interval, long lastExecute)
    {
        this.ID = ID;
        this.message = message;

        String[] parts = message.getContent().split("<>");
        this.commandString = parts[0].trim();
        if (!this.commandString.startsWith(guild.getPrefix()))
        {
            this.commandString = guild.getPrefix() + this.commandString;
        }

        if (parts.length > 1)
        {
            this.addText = parts[1].trim();
        }

        this.guild = guild;
        this.main = main;
        this.interval = interval;
        this.lastExecute = lastExecute;
        run(false);
    }

    public String getCommandString()
    {
        return this.commandString;
    }

    public String getAddText()
    {
        return this.addText;
    }

    public long getID()
    {
        return this.ID;
    }

    public void setID(long id)
    {
        this.ID = id;
    }

    public DummyMessage getMessage()
    {
        return this.message;
    }

    public GuildObject getGuild()
    {
        return this.guild;
    }

    public long getInterval()
    {
        return this.interval;
    }

    public long getLastExecuteTime()
    {
        return this.lastExecute;
    }

    private void run(boolean runNow)
    {
        long delay = 0;
        if (!runNow)
        {
            delay = this.interval + this.lastExecute - System.currentTimeMillis();
        }
        delay = delay < 0 ? 0 : delay;

        future = Threads.schedulerPool.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (running && message.getChannel() != null && !message.getChannel().isDeleted())
                    {
                        if (addText != null && !addText.trim().isEmpty())
                        {
                            main.getBot().sendPlainMessage(addText, message.getChannel());
                        }
                        if (main.getDatabase().isActivatedGuild(guild.getStringID()))
                        {
                            if (!Main.commandHandler.dispatch(new CommandEvent(guild, message)))
                            {
                                main.getBot().sendMessage("Failed to execute an automated command. \n" + commandString,
                                        message.getChannel(), Colors.RED);
                            }
                        }
                        lastExecute = System.currentTimeMillis();
                        main.getDatabase().updateAutomatedCommand(ID, lastExecute);
                    }
                    else if (message.getChannel() == null || message.getChannel().isDeleted())
                    {
                        stop();
                    }
                }
                catch (Exception e)
                {
                    Bot.errorLog.print(this, e);
                }
            }
        }, delay, this.interval, TimeUnit.MILLISECONDS);
    }

    public void stop()
    {
        if (future != null)
        {
            future.cancel(false);
        }
        running = false;
    }

    public synchronized static void add(AutomatedCommand command)
    {
        Map<Long, AutomatedCommand> commands = automatedCommands.get(command.getGuild().getStringID());

        if (commands == null)
        {
            commands = new ConcurrentHashMap<>();
            automatedCommands.put(command.getGuild().getStringID(), commands);
        }

        while (commands.get(command.getID()) != null)
        {
            command.setID(AutomatedCommand.currentID ++ );
        }

        if (!commands.values().contains(command))
        {
            commands.put(command.getID(), command);
            size ++ ;

            Bot.log.print("Added automated command '" + command.getCommandString() + "'.");
        }

    }

    public synchronized static boolean remove(AutomatedCommand command)
    {
        Map<Long, AutomatedCommand> commands = automatedCommands.get(command.getGuild().getStringID());

        if (commands != null)
        {
            AutomatedCommand nullCheck = commands.remove(command.getID());

            if (nullCheck != null)
            {
                size -- ;
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean remove(long id, String guildID)
    {
        Map<Long, AutomatedCommand> commands = automatedCommands.get(guildID);

        if (commands != null)
        {
            AutomatedCommand nullCheck = commands.remove(id);

            if (nullCheck != null)
            {
                size -- ;
                return true;
            }
        }
        return false;
    }

    public synchronized static AutomatedCommand get(long id, String guildID)
    {
        Map<Long, AutomatedCommand> commands = automatedCommands.get(guildID);

        if (commands != null)
        {
            return commands.get(id);
        }
        return null;
    }

    public synchronized static List<AutomatedCommand> get(String guildID)
    {
        Map<Long, AutomatedCommand> commands = automatedCommands.get(guildID);

        List<AutomatedCommand> commandList = new ArrayList<>();

        if (commands != null)
        {
            commandList.addAll(commands.values());
        }
        return commandList;
    }

    public synchronized static int getAutomatedCommandCount()
    {
        List<Map<Long, AutomatedCommand>> commandMaps = new ArrayList<>(automatedCommands.values());
        List<AutomatedCommand> commands = new ArrayList<>();

        for (Map<Long, AutomatedCommand> map : commandMaps)
        {
            commands.addAll(map.values());
        }

        return commands.size();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        else if (o instanceof AutomatedCommand
                && ((AutomatedCommand)o).getMessage().getStringID().equals(this.message.getStringID()))
        {
            return true;
        }

        return false;
    }
}