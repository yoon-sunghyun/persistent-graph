package yoon.sunghyun.adt.graph;

import java.util.Collection;

public interface Vertex extends Element
{
    /**
     * Returns the incident edges that satisfies the provided parameters.
     *
     * @param direction see {@link Direction} for more information
     * @param labels the labels that the edges must contain
     * @return an iterable of incident edges
     * @throws IllegalArgumentException if {@code direction} is {@link Direction#BOTH}
     * @see Direction
     */
    Collection<Edge> getEdges(Direction direction, String... labels) throws IllegalArgumentException;

    /**
     * Returns the adjacent vertices that satisfies the provided parameters.
     * This method does not remove duplicate vertices.
     *
     * @param direction see {@link Direction} for more information
     * @param labels the labels that the edges must contain
     * @return an iterable of adjacent vertices
     * @throws IllegalArgumentException if {@code direction} is {@link Direction#BOTH}
     * @see Direction
     */
    Collection<Vertex> getVertices(Direction direction, String... labels) throws IllegalArgumentException;

    /**
     * Returns the adjacent vertices that satisfies the provided parameters.
     * This method does not remove duplicate vertices.
     *
     * @param direction see {@link Direction} for more information
     * @param key the key-value pair property that the edges must contain
     * @param value the key-value pair property that the edges must contain
     * @param labels the labels that the edges must contain
     * @return an iterable of adjacent vertices
     * @throws IllegalArgumentException if {@code direction} is {@link Direction#BOTH}
     * @see Direction
     */
    Collection<Vertex> getVertices(Direction direction, String key, Object value, String... labels) throws IllegalArgumentException;

    /**
     * Returns the two-hop adjacent vertices that satisfies the provided parameters.
     * This method does not remove duplicate vertices.
     *
     * @param direction see {@link Direction} for more information
     * @param labels the labels that the edges must contain
     * @return an iterable of two-hop adjacent vertices
     * @throws IllegalArgumentException if {@code direction} is {@link Direction#BOTH}
     * @see Direction
     */
    Collection<Vertex> getTwoHopVertices(Direction direction, String... labels) throws IllegalArgumentException;
}
