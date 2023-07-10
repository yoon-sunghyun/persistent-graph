package yoon.sunghyun.adt.graph;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public abstract class PersistentElement implements Comparable<PersistentElement>, Element
{
    final PersistentGraph persistentGraph;
    final String id;

    public PersistentElement(PersistentGraph persistentGraph, String id)
    {
        this.persistentGraph = persistentGraph;
        this.id = id;
    }

    @Override
    public final boolean equals(Object other)
    {
        boolean equals = super.equals(other);

        if (!equals && (other instanceof PersistentElement otherElement))
            equals = (compareTo(otherElement) == 0);
        return equals;
    }

    @Override
    public final int compareTo(PersistentElement other)
    {
        return id.compareTo(other.id);
    }

    @Override
    public final String getId()
    {
        return id;
    }

    @Override
    public Object getProperty(String key)
    {
        String tableName = null;
        if (this instanceof PersistentVertex)   tableName = "vertex";
        if (this instanceof PersistentEdge)     tableName = "edge";
        String sql = String.format("SELECT JSON_TYPE(`value`), JSON_UNQUOTE(`value`) FROM (SELECT JSON_EXTRACT(`property`, '$.%s') AS `value` FROM `%s` WHERE `id`=?) AS `temp`;", key, tableName);

        try (PreparedStatement preparedStatement = persistentGraph.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, this.id);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    String valueType = resultSet.getString(1);
                    if (valueType != null)
                    {
                        switch (valueType.toLowerCase())
                        {
                            case ("boolean"):
                                return resultSet.getBoolean(2);
                            case ("integer"):
                                return resultSet.getInt(2);
                            case ("double"):
                                return resultSet.getDouble(2);
                            case ("string"):
                                return resultSet.getString(2);
                            case ("object"):
                                return new JSONObject(resultSet.getString(2));
                            case ("array"):
                                return new JSONArray(resultSet.getString(2));
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setProperty(String key, Object value)
    {
        String tableName = null;
        if (this instanceof PersistentVertex)   tableName = "vertex";
        if (this instanceof PersistentEdge)     tableName = "edge";
        String sql = String.format("UPDATE `%s` SET `property`=JSON_SET(`property`, '$.%s', ?) WHERE `id`=?;", tableName, key);

        // preventing conversion from boolean to integer
        if (value instanceof Boolean valueB)
            sql = sql.replaceFirst("\\?", (valueB ? "TRUE" : "FALSE"));

        try (PreparedStatement preparedStatement = persistentGraph.connection.prepareStatement(sql))
        {
            if (value instanceof Boolean)
            {
                preparedStatement.setString(1, this.id);
            }
            else
            {
                preparedStatement.setObject(1, value);
                preparedStatement.setString(2, this.id);
            }
            preparedStatement.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getPropertyKeys()
    {
        HashSet<String> propertyKeys = new HashSet<>();

        String tableName = null;
        if (this instanceof PersistentVertex)   tableName = "vertex";
        if (this instanceof PersistentEdge)     tableName = "edge";
        String sql = String.format("SELECT JSON_KEYS(`property`) FROM `%s` WHERE `id`=?;", tableName);

        try (PreparedStatement preparedStatement = persistentGraph.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, this.id);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    JSONArray jsonArray = new JSONArray(resultSet.getString(1));
                    for (Object value: jsonArray)
                        if (value instanceof String propertyKey)
                            propertyKeys.add(propertyKey);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return propertyKeys;
    }

    @Override
    public Object removeProperty(String key)
    {
        Object value = getProperty(key);

        if (value != null)
        {
            String tableName = null;
            if (this instanceof PersistentVertex)   tableName = "vertex";
            if (this instanceof PersistentEdge)     tableName = "edge";
            String sql = String.format("UPDATE `%s` SET `property`=JSON_REMOVE(`property`, '$.%s') WHERE `id`=?;", tableName, key);

            try (PreparedStatement preparedStatement = persistentGraph.connection.prepareStatement(sql))
            {
                preparedStatement.setString(1, this.id);
                preparedStatement.execute();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return value;
    }
}
