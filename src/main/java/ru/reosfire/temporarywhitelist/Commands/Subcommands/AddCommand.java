package ru.reosfire.temporarywhitelist.Commands.Subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import ru.reosfire.temporarywhitelist.Configuration.Localization.CommandResults.AddCommandResultsConfig;
import ru.reosfire.temporarywhitelist.Configuration.Localization.MessagesConfig;
import ru.reosfire.temporarywhitelist.Data.PlayerData;
import ru.reosfire.temporarywhitelist.Data.PlayerDatabase;
import ru.reosfire.temporarywhitelist.Lib.Commands.CommandName;
import ru.reosfire.temporarywhitelist.Lib.Commands.CommandNode;
import ru.reosfire.temporarywhitelist.Lib.Commands.CommandPermission;
import ru.reosfire.temporarywhitelist.Lib.Commands.ExecuteAsync;
import ru.reosfire.temporarywhitelist.Lib.Text.Replacement;
import ru.reosfire.temporarywhitelist.TimeConverter;

import java.util.ArrayList;
import java.util.Collections;

@CommandName("add")
@CommandPermission("TemporaryWhiteList.Add")
@ExecuteAsync
public class AddCommand extends CommandNode
{
    private final AddCommandResultsConfig _commandResults;
    private final PlayerDatabase _database;
    private final TimeConverter _timeConverter;

    public AddCommand(MessagesConfig messagesConfig, PlayerDatabase database, TimeConverter timeConverter)
    {
        super(messagesConfig.NoPermission);
        _commandResults = messagesConfig.CommandResults.Add;
        _database = database;
        _timeConverter = timeConverter;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args)
    {
        if (args.length != 2)
        {
            _commandResults.Usage.Send(sender);
            return true;
        }

        Replacement playerReplacement = new Replacement("{player}", args[0]);
        Replacement timeReplacement = new Replacement("{time}", args[1]);

        PlayerData playerData = _database.getPlayerData(args[0]);
        if (playerData != null && playerData.Permanent)
        {
            _commandResults.AlreadyPermanent.Send(sender, playerReplacement);
            return true;
        }

        if (args[1].equals("permanent"))
        {
            _database.SetPermanent(args[0]).whenComplete((changed, exception) ->
                    HandleCompletion(sender, exception, playerReplacement, timeReplacement));
        }
        else
        {
            long time;
            try
            {
                time = _timeConverter.ParseTime(args[1]);
            }
            catch (Exception e)
            {
                _commandResults.IncorrectTime.Send(sender);
                return true;
            }
            _database.Add(args[0], time).whenComplete((result, exception) ->
                    HandleCompletion(sender, exception, playerReplacement, timeReplacement));
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
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if (args.length == 1)
        {
            ArrayList<String> result = new ArrayList<>();

            for (PlayerData playerData : _database.AllList())
            {
                if (playerData.Name.startsWith(args[0])) result.add(playerData.Name);
            }

            return result;
        }
        else if (args.length == 2 && "permanent".startsWith(args[1])) return Collections.singletonList("permanent");

        return super.onTabComplete(sender, command, alias, args);
    }
}