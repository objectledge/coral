package org.objectledge.coral.store;

/**
 * Thrown when an attempt to create a resource with invalid name is made.
 */
public class InvalidResourceNameException
    extends Exception
{
    private final String characters;

    /**
     * Creates a new InvalidResourceNameException instance.
     * 
     * @param message the detail message.
     * @param characters the invalid characters.
     */
    public InvalidResourceNameException(String message, String characters)
    {
        super(message);
        this.characters = characters;
    }

    /**
     * Returns the invalid chracters in the resource name.
     * 
     * @return the invalid chracters in the resource name.
     */
    public String getInvalidChracters()
    {
        return characters;
    }
}
