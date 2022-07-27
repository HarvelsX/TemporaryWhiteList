package ru.reosfire.temporarywhitelist.Commands.Subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.reosfire.temporarywhitelist.Configuration.Localization.CommandResults.AddCommandResultsConfig;
import ru.reosfire.temporarywhitelist.Configuration.Localization.MessagesConfig;
import ru.reosfire.temporarywhitelist.Data.PlayerData;
import ru.reosfire.temporarywhitelist.Data.PlayerDatabase;
import ru.reosfire.temporarywhitelist.Lib.Commands.*;
import ru.reosfire.temporarywhitelist.Lib.Text.Replacement;
import ru.reosfire.temporarywhitelist.TemporaryWhiteList;
import ru.reosfire.temporarywhitelist.TimeConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@CommandName("add")
@CommandPermission("TemporaryWhitelist.Administrate.Add")
@ExecuteAsync
public class AddCommand extends CommandNode
{
    private final AddCommandResultsConfig _commandResults;
    private final PlayerDatabase _database;
    private final TimeConverter _timeConverter;
    private final boolean _forceSync;

    public AddCommand(TemporaryWhiteList pluginInstance, boolean forceSync)
    {
        super(pluginInstance.getMessages().NoPermission);
        _commandResults = pluginInstance.getMessages().CommandResults.Add;
        _database = pluginInstance.getDatabase();
        _timeConverter = pluginInstance.getTimeConverter();
        _forceSync = forceSync;
    }
    public AddCommand(TemporaryWhiteList pluginInstance)
    {
        this(pluginInstance, false);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (SendMessageIf(args.length != 2, _commandResults.Usage, sender)) return true;

        Replacement playerReplacement = new Replacement("{player}", args[0]);
        Replacement timeReplacement = new Replacement("{time}", args[1]);

        PlayerData playerData = _database.getPlayerData(args[0]);
        if (SendMessageIf(playerData != null && playerData.Permanent, _commandResults.AlreadyPermanent, sender, playerReplacement))
            return true;

        if (args[1].equals("permanent"))
        {
            _database.SetPermanent(args[0]).whenComplete((changed, exception) ->
                    HandleCompletion(sender, exception, playerReplacement, timeReplacement));
        }
        else
        {
            AtomicReference<Long> time = new AtomicReference<>();
            if (!TryParse(_timeConverter::ParseTime, args[1], time))
            {
                _commandResults.IncorrectTime.Send(sender);
                return true;
            }
            if (_forceSync)
            {
                try
                {
                    _database.Add(args[0], time.get()).join();
                    _commandResults.Success.Send(sender, playerReplacement, timeReplacement);
                }
                catch (Exception e)
                {
                    _commandResults.Error.Send(sender, playerReplacement, timeReplacement);
                    e.printStackTrace();
                }
            }
            else
            {
                _database.Add(args[0], time.get()).whenComplete((result, exception) ->
                        HandleCompletion(sender, exception, playerReplacement, timeReplacement));
            }
        }
        return true;
    }

    private void HandleCompletion(CommandSender sender, Throwable exception, Replacement... replacements)
    {
        if (exception == null)
            _commandResults.Success.Send(sender, replacements);
        else
        {
            _commandResults.Error.Send(sender, replacements);
            exception.printStackTrace();
        }
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args)
    {
        if (args.length == 1)
            return _database.AllList().stream().map(e -> e.Name).filter(e -> e.startsWith(args[0])).collect(Collectors.toList());
        else if (args.length == 2 && "permanent".startsWith(args[1])) return Collections.singletonList("permanent");

        return super.onTabComplete(sender, command, alias, args);
    }

    @Override
    public boolean isAsync()
    {
        if (_forceSync) return false;
        return super.isAsync();
    }
}