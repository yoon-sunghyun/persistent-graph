package yoon.sunghyun.adt.graph;

import java.util.Collection;

public interface Graph
{
    Vertex addVertex(String id) throws IllegalArgumentException;

    Vertex getVertex(String id);

    Collection<Vertex> getVertices();

    Collection<Vertex> getVertices(String key, Object value);

    void removeVertex(Vertex vertex);

    Edge addEdge(Vertex vertexO, Vertex vertexI, String label) throws IllegalArgumentException, NullPointerException;

    Edge getEdge(String id);

    Edge getEdge(Vertex vertexO, Vertex vertexI, String label);

    Collection<Edge> getEdges();

    Collection<Edge> getEdges(String key, Object value);

    void removeEdge(Edge edge);

    void shutdown();
}
