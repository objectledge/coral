package org.objectledge.coral.schema;


/**
 * Thrown to indicate various schema integrity problems caused by adding
 * attributes, parent resource classes, and attribute flags modifications.
 *
 * @version $Id: SchemaIntegrityException.java,v 1.4 2005-05-25 07:55:47 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class SchemaIntegrityException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
    /** Value returned by {@link #getType()} for exceptions of unknown
     *  origin. */
    public static final int UNKNOWN = 0;

    /** Value returned by {@link #getType()} for attribute class clash
     *  exceptions. */
    public static final int CLASS_CLASH = 1;
    
    /** Value returned by {@link #getType()} for attribute flags clash
     *  exceptions. */
    public static final int FLAGS_CLASH = 2;
    
    /** Value returned by {@link #getType()} for exceptions caused by
     *  modification of flags of multiply-inherited attributes.  */ 
    public static final int MULTIPLE_INHERITANCE = 4;

    /**
     * Returns the type of the inconsistency introduced by the requested
     * operation. 
     *
     * @return one of {@link #CLASS_CLASH}, {@link #FLAGS_CLASH}, {@link
     * #MULTIPLE_INHERITANCE} 
     */
    public int getType()
    {
        return type;
    }

    /**
     * Returns the definition of the attribute that is causes the
     * inconsistency. 
     *
     * @return the offending attribute.
     */
    public AttributeDefinition<?> getAttribute()
    {
        return attribute;
    }
    
    /**
     * Returns the resource class that has the conflicting attribute.
     *
     * @return the offending class.
     */
    public ResourceClass<?> getConflictingClass()
    {
        return resourceClass;
    }

    /**
     * Returns <code>true</code> if the class returned by {@link
     * #getConflictingClass()} is the superclass of the class on which the
     * operation was performed.
     *
     * @return <code>true</code> if the class returned by {@link
     * #getConflictingClass()} is the superclass of the class on which the
     * operation was performed.
     */
    public boolean isSuperClass()
    {
        return superClass;
    }
    
    /**
     * Returns the other class involved in the conflict for multiple
     * inheritance exceptions.
     *
     * @return the other involved class.
     */
    public ResourceClass<?> getOtherClass()
    {
        return otherClass;
    }

    /** The exception type. */
    private int type = UNKNOWN;

    /** The involved attribute. */
    private AttributeDefinition<?> attribute = null;
    
    /** The involved resource class. */
    private ResourceClass<?> resourceClass = null;

    /** Is resourceClass a super class of the class on which the opertaion was
     *  performed? */
    private boolean superClass = false;
    
    /** The other involved class. */
    private ResourceClass<?> otherClass = null;

    /** 
     * Construcs a new exception object with the specified detail message.
     *
     * @param msg the detail message.
     */
    public SchemaIntegrityException(String msg)
    {
        super(msg);
    }
    
    /** 
     * Construcs a new exception object with the specified detail message
     * and background information.
     *
     * @param msg the detail message.
     * @param type the exception type.
     * @param attribute the involved attribute.
     * @param resourceClass the involved resource class.
     * @param superClass is the <code>resourceClass</code> a super class of
     *                   the ``current'' class?
     * @param otherClass the other involved resource class.
     */
    public SchemaIntegrityException(String msg, int type, 
                                   AttributeDefinition<?> attribute, 
                                   ResourceClass<?> resourceClass, 
                                   boolean superClass,
                                   ResourceClass<?> otherClass)
    {
        super(msg);
        this.type = type;
        this.attribute = attribute;
        this.resourceClass = resourceClass;
        this.otherClass = otherClass;
    }
}
