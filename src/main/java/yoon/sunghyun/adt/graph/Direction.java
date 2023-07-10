package yoon.sunghyun.adt.graph;

/**
 *
 */
public enum Direction
{
    IN, OUT, BOTH;

    public Direction opposite()
    {
        if (this.equals(IN)) return OUT;
        if (this.equals(OUT)) return IN;
        return BOTH;
    }
}
