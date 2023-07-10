package yoon.sunghyun.adt.graph;

import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;

public class PersistentGraph implements Graph
{
    final Connection connection;

    public PersistentGraph(String url, String username, String password, String dbName) throws SQLException
    {
        this.connection = DriverManager.getConnection(url, username, password);

        String sql;
        Statement statement = this.connection.createStatement();

        // creating database
        sql = String.format("CREATE DATABASE IF NOT EXISTS `%s`;", dbName);
        statement.execute(sql);

        // using database
        sql = String.format("USE `%s`;", dbName);
        statement.execute(sql);

        // creating vertex table
        sql = "CREATE TABLE IF NOT EXISTS `vertex` ("
                + "`id`       VARCHAR(64) PRIMARY KEY,"
                + "`property` JSON NOT NULL DEFAULT JSON_OBJECT()"
                + ");";
        statement.execute(sql);

        // creating edge table
        sql = "CREATE TABLE IF NOT EXISTS `edge` ("
                + "`id`       VARCHAR(64) PRIMARY KEY,"
                + "`id_vo`    VARCHAR(64) NOT NULL REFERENCES `vertex`(`id`) ON UPDATE CASCADE ON DELETE CASCADE,"
                + "`id_vi`    VARCHAR(64) NOT NULL REFERENCES `vertex`(`id`) ON UPDATE CASCADE ON DELETE CASCADE,"
                + "`label`    VARCHAR(64),"
                + "`property` JSON NOT NULL DEFAULT JSON_OBJECT()"
                + ");";
        statement.execute(sql);
    }

    @Override
    public Vertex addVertex(String id) throws IllegalArgumentException
    {
        if (id.contains("|"))
            throw new IllegalArgumentException("id cannot contain '|'");

        String sql = "INSERT INTO `vertex`(`id`) VALUE (?) ON DUPLICATE KEY UPDATE `id`=VALUE(`id`);";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, id);
            preparedStatement.execute();
            return new PersistentVertex(this, id);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Vertex getVertex(String id)
    {
        String sql = "SELECT `id` FROM `vertex` WHERE `id`=?;";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                    return new PersistentVertex(this, id);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Collection<Vertex> getVertices()
    {
        Collection<Vertex> vertices = new LinkedList<>();
        String sql = "SELECT `id` FROM `vertex`;";

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String vertexID = resultSet.getString(1);
                    vertices.add(new PersistentVertex(this, vertexID));
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
    public Collection<Vertex> getVertices(String key, Object value)
    {
        Collection<Vertex> vertices = new LinkedList<>();
        String sql = String.format("SELECT `id` FROM `vertex` WHERE JSON_VALUE(`property`, '$.%s')=?;", key);

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            preparedStatement.setObject(1, value);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String vertexID = resultSet.getString(1);
                    vertices.add(new PersistentVertex(this, vertexID));
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
    public void removeVertex(Vertex vertex)
    {
        String sql = "DELETE FROM `vertex` WHERE `id`=?;";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, vertex.getId());
            preparedStatement.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Edge addEdge(Vertex vertexO, Vertex vertexI, String label) throws IllegalArgumentException, NullPointerException
    {
        if (label.contains("|"))
            throw new IllegalArgumentException("label cannot contain '|'");
        if (vertexO == null)
            throw new NullPointerException("outVertex cannot be null");
        if (vertexI == null)
            throw new NullPointerException("inVertex cannot be null");

        String id_vo = vertexO.getId();
        String id_vi = vertexI.getId();
        String id = String.format("%s|%s|%s", id_vo, label, id_vi);

        String sql = "INSERT INTO `edge`(`id`, `id_vo`, `id_vi`, `label`) VALUE (?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE"
                + " `id`   =VALUE(`id`),"
                + " `id_vo`=VALUE(`id_vo`),"
                + " `id_vi`=VALUE(`id_vi`),"
                + " `label`=VALUE(`label`);";

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, id_vo);
            preparedStatement.setString(3, id_vi);
            preparedStatement.setString(4, label);
            preparedStatement.execute();
            return new PersistentEdge(this, id, vertexO, vertexI, label);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Edge getEdge(String id)
    {
        String sql = "SELECT `id_vo`, `id_vi`, `label` FROM `edge` WHERE `id`=?;";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                if (resultSet.next())
                {
                    String id_vo = resultSet.getString(1);
                    String id_vi = resultSet.getString(2);
                    String label = resultSet.getString(3);

                    Vertex vertexO = new PersistentVertex(this, id_vo);
                    Vertex vertexI = new PersistentVertex(this, id_vi);

                    return new PersistentEdge(this, id, vertexO, vertexI, label);
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
    public Edge getEdge(Vertex vertexO, Vertex vertexI, String label)
    {
        String id = String.format("%s|%s|%s", vertexO.getId(), label, vertexI.getId());
        return getEdge(id);
    }

    @Override
    public Collection<Edge> getEdges()
    {
        Collection<Edge> edges = new LinkedList<>();
        String sql = "SELECT `id`, `id_vo`, `id_vi`, `label` FROM `edge`;";

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String id = resultSet.getString(1);
                    String id_vo = resultSet.getString(2);
                    String id_vi = resultSet.getString(3);
                    String label = resultSet.getString(4);

                    PersistentVertex vertexO = new PersistentVertex(this, id_vo);
                    PersistentVertex vertexI = new PersistentVertex(this, id_vi);

                    edges.add(new PersistentEdge(this, id, vertexO, vertexI, label));
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
    public Collection<Edge> getEdges(String key, Object value)
    {
        Collection<Edge> edges = new LinkedList<>();
        String sql = String.format("SELECT `id`, `id_vo`, `id_vi`, `label` FROM `edge` WHERE JSON_VALUE(`property`, '$.%s')=?;", key);

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            preparedStatement.setObject(1, value);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    String id = resultSet.getString(1);
                    String id_vo = resultSet.getString(2);
                    String id_vi = resultSet.getString(3);
                    String label = resultSet.getString(4);

                    PersistentVertex vertexO = new PersistentVertex(this, id_vo);
                    PersistentVertex vertexI = new PersistentVertex(this, id_vi);

                    edges.add(new PersistentEdge(this, id, vertexO, vertexI, label));
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
    public void removeEdge(Edge edge)
    {
        String sql = "DELETE FROM `edge` WHERE `id`=?;";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql))
        {
            preparedStatement.setString(1, edge.getId());
            preparedStatement.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown()
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
