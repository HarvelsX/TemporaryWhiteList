package ru.reosfire.temporarywhitelist.Data;

import ru.reosfire.temporarywhitelist.Data.Exporters.IDataExporter;

import java.util.concurrent.CompletableFuture;

public interface IDataProvider extends IDataExporter, IUpdatable
{
    CompletableFuture<Void> remove(String playerName);
    PlayerData get(String playerName);
}