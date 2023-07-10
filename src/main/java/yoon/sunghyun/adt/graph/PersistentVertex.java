package yoon.sunghyun.adt.graph;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class PersistentVertex extends PersistentElement implements Vertex
{
    public PersistentVertex(PersistentGraph persistentGraph, String id)
    {
        super(persistentGraph, id);
    }

    @Override
    public String toString()
    {
        return String.format("v[%s]", id);
    }

    @Override
    public Collection<Edge> getEdges(Direction direction, String... labels) throws IllegalArgumentException
    {
        if (direction == Direction.BOTH)
            throw new IllegalArgumentException("Direction.BOTH is not allowed");

        String sql = null;
        StringJoiner stringJoiner = new StringJoiner(" OR ", " AND (", ")").setEmptyValue("");
        ArrayList<Edge> edges = new ArrayList<>();

        for (int i = 0; i < labels.length; i++)
            stringJoiner.add("`label`=?");

        if (direction == Direction.OUT)
            sql = String.format("SELECT `id`, `id_vo`, `id_vi`, `label` FROM `edge` WHERE `id_vo`=?%s;", stringJoiner);
        if (direction == Direction.IN)
            sql = String.format("SELECT `id`, `id_vo`, `id_vi`, `label` FROM `edge` WHERE `id_vi`=?%s;", stringJoiner);

        try (PreparedStatement preparedStatement = persistentGraph.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, this.id);
            for (int i = 0; i < labels.length; i++)
                preparedStatement.setString(2 + i, labels[i]);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String id = resultSet.getString(1);
                    String id_vo = resultSet.getString(2);
                    String id_vi = resultSet.getString(3);
                    String label = resultSet.getString(4);

                    PersistentVertex vertexO = new PersistentVertex(persistentGraph, id_vo);
                    PersistentVertex vertexI = new PersistentVertex(persistentGraph, id_vi);

                    edges.add(new PersistentEdge(persistentGraph, id, vertexO, vertexI, label));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return edges;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String... labels) throws IllegalArgumentException
    {
        if (direction == Direction.BOTH)
            throw new IllegalArgumentException("Direction.BOTH is not allowed");

        String sql = null;
        StringJoiner stringJoiner = new StringJoiner(" OR ", " AND (", ")").setEmptyValue("");
        Collection<Vertex> vertices = new LinkedList<>();

        for (int i = 0; i < labels.length; i++)
            stringJoiner.add("`label`=?");

        if (direction == Direction.OUT)
            sql = String.format("SELECT `id_vi` FROM `edge` WHERE `id_vo`=?%s;", stringJoiner);
        if (direction == Direction.IN)
            sql = String.format("SELECT `id_vo` FROM `edge` WHERE `id_vi`=?%s;", stringJoiner);

        try (PreparedStatement preparedStatement = persistentGraph.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, this.id);
            for (int i = 0; i < labels.length; i++)
                preparedStatement.setString(2 + i, labels[i]);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String vertexID = resultSet.getString(1);
                    vertices.add(new PersistentVertex(persistentGraph, vertexID));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return vertices;
    }

    @Override
    public Collection<Vertex> getTwoHopVertices(Direction direction, String... labels) throws IllegalArgumentException
    {
        // TODO: optimize query
        if (direction == Direction.BOTH)
            throw new IllegalArgumentException("Direction.BOTH is not allowed");

        String sql = null;
        StringJoiner stringJoiner = new StringJoiner(" OR ", " AND (", ")").setEmptyValue("");
        Collection<Vertex> vertices = new LinkedList<>();

        for (int i = 0; i < labels.length; i++)
            stringJoiner.add("`label`=?");

        if (direction == Direction.OUT)
            sql = String.format("SELECT `v3` FROM ("
                    + "(SELECT `id_vi` AS `v1` FROM `edge` WHERE `id_vo`=?%s) AS `t1` NATURAL JOIN "
                    + "(SELECT `id_vo` AS `v1`, `id_vi` AS `v2` FROM `edge`)  AS `t2` NATURAL JOIN "
                    + "(SELECT `id_vo` AS `v2`, `id_vi` AS `v3` FROM `edge`)  AS `t3`);", stringJoiner);
        if (direction == Direction.IN)
            sql = String.format("SELECT `v3` FROM ("
                    + "(SELECT `id_vo` AS `v1` FROM `edge` WHERE `id_vi`=?%s) AS `t1` NATURAL JOIN "
                    + "(SELECT `id_vi` AS `v1`, `id_vo` AS `v2` FROM `edge`)  AS `t2` NATURAL JOIN "
                    + "(SELECT `id_vi` AS `v2`, `id_vo` AS `v3` FROM `edge`)  AS `t3`);", stringJoiner);

        try (PreparedStatement preparedStatement = persistentGraph.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, this.id);
            for (int i = 0; i < labels.length; i++)
                preparedStatement.setString(2 + i, labels[i]);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String vertexID = resultSet.getString(1);
                    vertices.add(new PersistentVertex(persistentGraph, vertexID));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return vertices;
    }

    @Override
    public Collection<Vertex> getVertices(Direction direction, String key, Object value, String... labels) throws IllegalArgumentException
    {
        if (direction == Direction.BOTH)
            throw new IllegalArgumentException("Direction.BOTH is not allowed");

        String sql = null;
        StringJoiner stringJoiner = new StringJoiner(" OR ", " AND (", ")").setEmptyValue("");
        Collection<Vertex> vertices = new LinkedList<>();

        for (int i = 0; i < labels.length; i++)
            stringJoiner.add("`label`=?");

        if (direction == Direction.OUT)
            sql = String.format("SELECT `id_vi` FROM `edge` WHERE `id_vo`=? AND JSON_VALUE(`property`, '$.%s')=?%s;", key, stringJoiner);
        if (direction == Direction.IN)
            sql = String.format("SELECT `id_vo` FROM `edge` WHERE `id_vi`=? AND JSON_VALUE(`property`, '$.%s')=?%s;", key, stringJoiner);

        try (PreparedStatement preparedStatement = persistentGraph.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, this.id);
            preparedStatement.setObject(2, value);
            for (int i = 0; i < labels.length; i++)
                preparedStatement.setString(3 + i, labels[i]);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String vertexID = resultSet.getString(1);
                    vertices.add(new PersistentVertex(persistentGraph, vertexID));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return vertices;
    }

    @Override
    public void remove()
    {
        persistentGraph.removeVertex(this);
    }
}
