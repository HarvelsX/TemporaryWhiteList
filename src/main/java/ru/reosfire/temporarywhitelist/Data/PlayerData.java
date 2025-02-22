package ru.reosfire.temporarywhitelist.Data;

import org.bukkit.configuration.ConfigurationSection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Objects;

public class PlayerData
{
    public final String Name;
    public final boolean Permanent;
    public final long StartTime;
    public final long TimeAmount;

    public PlayerData(String name, long startTime, long timeAmount, boolean permanent)
    {
        Name = name;
        StartTime = startTime;
        TimeAmount = timeAmount;
        Permanent = permanent;
    }

    public PlayerData(ResultSet resultSet) throws SQLException
    {
        Name = resultSet.getString("Player");
        Permanent = resultSet.getBoolean("Permanent");
        StartTime = resultSet.getLong("LastStartTime");
        TimeAmount = resultSet.getLong("TimeAmount");
    }

    public PlayerData(ConfigurationSection section)
    {
        Name = section.getName();
        Permanent = section.getBoolean("permanent");
        StartTime = section.getLong("lastStartTime");
        TimeAmount = section.getLong("timeAmount");
    }

    public long endTime()
    {
        return StartTime + TimeAmount;
    }
    public boolean isTimedOut()
    {
        return endTime() <= Instant.now().getEpochSecond();
    }
    public long timeLeft()
    {
        return endTime() - Instant.now().getEpochSecond();
    }

    public boolean isSame(PlayerData other)
    {
        if (!Name.equals(other.Name)) return false;
        if (Permanent && other.Permanent) return true;
        return Permanent == other.Permanent && timeLeft() == other.timeLeft();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PlayerData)) return false;
        PlayerData that = (PlayerData) o;

        return Permanent == that.Permanent && TimeAmount == that.TimeAmount && StartTime == that.StartTime && Name.equals(that.Name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(Name, Permanent, StartTime, TimeAmount);
    }

    public boolean canJoin()
    {
        return Permanent || !isTimedOut();
    }
}