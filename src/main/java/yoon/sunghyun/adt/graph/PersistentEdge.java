package yoon.sunghyun.adt.graph;

public final class PersistentEdge extends PersistentElement implements Edge
{
    final Vertex vertexO;
    final Vertex vertexI;
    final String label;

    public PersistentEdge(final PersistentGraph persistentGraph, final String id, final Vertex vertexO, final Vertex vertexI, final String label)
    {
        super(persistentGraph, id);
        this.vertexO = vertexO;
        this.vertexI = vertexI;
        this.label = label;
    }

    @Override
    public String toString()
    {
        return String.format("e[%s]", id);
    }

    @Override
    public Vertex getVertex(final Direction direction) throws IllegalArgumentException
    {
        if (direction == Direction.OUT)
            return vertexO;
        if (direction == Direction.IN)
            return vertexI;
        throw new IllegalArgumentException("Direction.BOTH is not allowed");
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public void remove()
    {
        persistentGraph.removeEdge(this);
    }
}
