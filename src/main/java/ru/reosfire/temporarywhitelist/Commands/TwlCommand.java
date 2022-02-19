package ru.reosfire.temporarywhitelist.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.reosfire.temporarywhitelist.Configuration.Localization.MessagesConfig;
import ru.reosfire.temporarywhitelist.Data.IDataProvider;
import ru.reosfire.temporarywhitelist.Lib.Commands.CommandName;
import ru.reosfire.temporarywhitelist.Lib.Commands.CommandNode;
import ru.reosfire.temporarywhitelist.Lib.Commands.CommandPermission;
import ru.reosfire.temporarywhitelist.Lib.Text.Replacement;
import ru.reosfire.temporarywhitelist.Lib.Text.Text;
import ru.reosfire.temporarywhitelist.TemporaryWhiteList;
import ru.reosfire.temporarywhitelist.TimeConverter;

@CommandName("twl")
public class TwlCommand extends CommandNode
{
    private final IDataProvider _dataProvider;
    private final MessagesConfig _messages;
    private final TemporaryWhiteList _pluginInstance;
    private final TimeConverter _timeConverter;

    public TwlCommand(MessagesConfig messages, IDataProvider dataProvider, TemporaryWhiteList pluginInstance, TimeConverter timeConverter)
    {
        _dataProvider = dataProvider;
        _messages = messages;
        _pluginInstance = pluginInstance;
        _timeConverter = timeConverter;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        return true;
    }

    @CommandName("add")
    @CommandPermission("TemporaryWhiteList.Add")
    public class Add extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            try
            {
                if(args.length == 1)
                {
                    _dataProvider.Add(args[0]);
                    sender.sendMessage(args[0] + " success added to white list");
                    return true;
                }
                else if(args.length == 2)
                {
                    _dataProvider.Add(args[0], _timeConverter.ParseTime(args[1]));
                    sender.sendMessage(args[0] + " success added to white list for " + args[1]);
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @CommandName("remove")
    @CommandPermission("TemporaryWhiteList.Remove")
    public class Remove extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            try
            {
                _dataProvider.Remove(args[0]);
                sender.sendMessage(args[0] + " success removed from white list");
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    @CommandName("permanent")
    @CommandPermission("TemporaryWhiteList.Permanent")
    public class Permanent extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            return true;
        }

        @CommandName("set")
        @CommandPermission("TemporaryWhiteList.Permanent.Set")
        public class Set extends CommandNode
        {
            @Override
            public boolean execute(CommandSender sender, String[] args)
            {
                try
                {
                    _dataProvider.SetPermanent(args[0], true);
                    sender.sendMessage(args[0] + "'s subscribe set permanent");
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }

        }

        @CommandName("reset")
        @CommandPermission("TemporaryWhiteList.Permanent.Reset")
        public class Reset extends CommandNode
        {
            @Override
            public boolean execute(CommandSender sender, String[] args)
            {
                try
                {
                    _dataProvider.SetPermanent(args[0], false);
                    sender.sendMessage(args[0] + "'s subscribe set permanent");
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }

        }
    }

    @CommandName("check")
    @CommandPermission("TemporaryWhiteList.Check")
    public class Check extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            try
            {
                if (args.length == 0)
                {
                    if (sender instanceof Player)
                    {
                        Player playerSender = (Player)sender;

                        Replacement replacement = new Replacement("{status}", _dataProvider.Check(sender.getName()));
                        sender.sendMessage(Text.Colorize(playerSender, _messages.CheckMessageFormat, replacement));
                    }
                    else
                    {
                        //TODO for players only
                    }
                }
                else if (args.length == 1)
                {
                    if (!sender.hasPermission("WMWhiteList.Check.Other") && !sender.isOp())
                    {
                        noPermissionAction(sender);
                    }
                    else sender.sendMessage(_dataProvider.Check(args[0]));
                }
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    @CommandName("enable")
    @CommandPermission("TemporaryWhiteList.Administrate.Enable")
    public class Enable extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            try
            {
                _pluginInstance.Enable();
                sender.sendMessage("enabled");
            }
            catch (Exception e)
            {
                sender.sendMessage("Error! Watch console");
                e.printStackTrace();
            }
            return true;
        }
    }

    @CommandName("disable")
    @CommandPermission("TemporaryWhiteList.Administrate.Disable")
    public class Disable extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            try
            {
                _pluginInstance.Disable();
                sender.sendMessage("disabled");
            }
            catch (Exception e)
            {
                sender.sendMessage("Error! Watch console");
                e.printStackTrace();
            }
            return true;
        }
    }

    @CommandName("reload")
    @CommandPermission("TemporaryWhiteList.Administrate.Reload")
    public class Reload extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            _pluginInstance.Load();
            sender.sendMessage("reloaded");
            return true;
        }
    }

    @CommandName("list")
    @CommandPermission("TemporaryWhiteList.Administrate.List")
    public class List extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            try
            {
                sender.sendMessage(String.join(", ", _dataProvider.ActiveList()));
            }
            catch (Exception e)
            {
                return false;
            }
            return true;
        }
    }

    @CommandName("count")
    @CommandPermission("TemporaryWhiteList.Administrate.Count")
    public class Count extends CommandNode
    {
        @Override
        public boolean execute(CommandSender sender, String[] args)
        {
            try
            {
                sender.sendMessage(Integer.toString(_dataProvider.ActiveList().size()));
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }
}