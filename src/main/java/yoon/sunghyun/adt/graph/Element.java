package yoon.sunghyun.adt.graph;

import java.util.Set;

public interface Element
{
    /**
     * Returns this element's identifier.
     * @return this element's identifier
     */
    String getId();

    /**
     * Returns this element's property keys.
     * @return this element's property keys
     */
    Set<String> getPropertyKeys();

    /**
     * Returns this element's property value mapped to specified key.
     * @param key the property key whose associated value is to be returned
     * @return this element's property value mapped to specified key
     */
    Object getProperty(String key);

    /**
     * Associates the specific property value with specific key in this element.
     * @param key property key to be associated with the specified value
     * @param value property value to be associated with the specified key
     */
    void setProperty(String key, Object value);

    /**
     * Removes this element's property value mapped to specified key.
     * @param key the property key whose associated value is to be removed
     * @return the removed property value associated with specified key, or {@code null}
     */
    Object removeProperty(String key);

    /**
     * Removes this element from its graph.
     */
    void remove();
}
