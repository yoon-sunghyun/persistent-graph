package yoon.sunghyun.adt.graph;

public interface Edge extends Element
{
    /**
     * Returns the vertex specified by {@code direction}.
     *
     * @param direction see {@link Direction} for more information
     * @return the vertex specified by {@code direction}
     * @throws IllegalArgumentException if {@code direction} is {@link Direction#BOTH}
     * @see Direction
     */
    Vertex getVertex(Direction direction) throws IllegalArgumentException;

    /**
     * Returns the label of this edge
     * @return the label of this edge
     */
    String getLabel();
}
