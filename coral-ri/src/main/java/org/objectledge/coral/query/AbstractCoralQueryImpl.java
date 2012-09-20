package org.objectledge.coral.query;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.script.RMLEntityResolver;
import org.objectledge.coral.script.parser.ASTapproximationCondition;
import org.objectledge.coral.script.parser.ASTclassAndAliasSpecifier;
import org.objectledge.coral.script.parser.ASTclassAndAliasSpecifierList;
import org.objectledge.coral.script.parser.ASTcomparisonCondition;
import org.objectledge.coral.script.parser.ASTconditionalExpression;
import org.objectledge.coral.script.parser.ASTdefinedCondition;
import org.objectledge.coral.script.parser.ASTequalityCondition;
import org.objectledge.coral.script.parser.ASTfindResourceStatement;
import org.objectledge.coral.script.parser.ASTorderByList;
import org.objectledge.coral.script.parser.ASTorderBySpecifier;
import org.objectledge.coral.script.parser.ASTselectList;
import org.objectledge.coral.script.parser.ASTselectSpecifier;
import org.objectledge.coral.script.parser.DefaultRMLVisitor;
import org.objectledge.coral.script.parser.ParseException;
import org.objectledge.coral.script.parser.RMLParser;
import org.objectledge.coral.script.parser.RMLVisitor;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;


/**
 * A common base class for {@link CoralQuery} implementations.
 * 
 * @author <a href="rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: AbstractCoralQueryImpl.java,v 1.14 2006-03-03 11:41:12 rafal Exp $
 */
public abstract class AbstractCoralQueryImpl
    implements CoralQuery
{
    // instance variables ////////////////////////////////////////////////////

	/** The coral session. */
	protected CoralCore coral;
	
    /** The entity resolver. */
    private RMLEntityResolver entities;

    // initialization ////////////////////////////////////////////////////////

    /** 
     * Constructs an QueryService implementation instance.
     *
     * @param coral coral core instance.
     */
    public AbstractCoralQueryImpl(CoralCore coral)
    {
        this.coral = coral;
        entities = new RMLEntityResolver(coral.getSchema(), coral.getSecurity(), coral.getStore(),
            coral.getRelationManager());
    }
    
    // public interface //////////////////////////////////////////////////////

    /**
     * Executes a query.
     * 
     * @param query the query.
     * @return query results.
     * @throws MalformedQueryException if the query has syntactic or semantic errors and thus cannot
     *         be executed.
     */
    public QueryResults executeQuery(String query)
        throws MalformedQueryException
    {
        int i = query.indexOf("FIND");
        if(i >= 0)
        {
            i = query.indexOf("RESOURCE", i);
            if(i >= 0)
            {
                query = query.substring(i+8);
                ASTfindResourceStatement statement = parseQuery(query);
                return executeQuery(statement);
            }
        }
        throw new MalformedQueryException("Queries must start with FIND RESOURCE");
    }
    
    /**
     * Prepares a query.
     * <p>
     * Query may contain a number of <em>positional parameters</em>. The parameters have form of
     * $&lt;NUMBER&gt;, with no intervening whitespace. The number must be greater or equal to 1.
     * The positional parameters can only be used on the right hand side of binary operators. When
     * the query is executed, all positional parameters must be set to non-null values. To test a
     * resource's property for being defined (i.e. not null), use the DEFINED operator.
     * </p>
     * 
     * @param query the query.
     * @return prepared query object.
     * @throws MalformedQueryException if the query has syntactic or semantic errors and thus cannot
     *         be executed.
     */
    public PreparedQuery prepareQuery(String query)
        throws MalformedQueryException
    {
        ASTfindResourceStatement statement = parseQuery(query);
        return prepareQuery(statement);
    }

    // methods to override in implementation /////////////////////////////////

    /**
     * Executes a preparsed query.
     *
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return query resuls.
     * @throws MalformedQueryException if the query has semantic errors and
     * thus cannot be executed. 
     */
    public abstract QueryResults executeQuery(ASTfindResourceStatement statement)
        throws MalformedQueryException;

    /**
     * Prepares a pre-parsed query.
     * 
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return query results.
     * @throws MalformedQueryException if the query has semantic errors and thus cannot be executed.
     */
    protected abstract PreparedQuery prepareQuery(ASTfindResourceStatement statement)
        throws MalformedQueryException;

    // common parts of the implementation ////////////////////////////////////

    /**
     * Parses an conditional expression operand.
     * <p>
     * If the lhs is <code>true</code> the operand must be an attribute reference. Otherwise it may
     * be also a resource class reference (using query results column alias) or a literal value.
     * </p>
     * <p>
     * If the operand is an attribute, AttributeDefinition will be returned. If it is a column
     * alias, ResultColumn will be returned. Finally if it is a literal constants a String
     * containing it's value will be returned.
     * </p>
     * 
     * @param operand the operand.
     * @param lhs <code>true</code> if the operand is on the left hand side of the operator.
     * @param outer indicates that NULL value of the attribute needs to be accounted for, which
     *        means the resource class needs to be outer joined into the query
     * @param columnMap map containing ResultColumn objects keyed by alias.
     * @return AttributeDefinition, ResultColumn, or String literal value.
     * @throws MalformedQueryException if the query has semantic errors and thus cannot be executed.
     */
    protected Object parseOperand(String operand, boolean lhs, boolean outer,
        Map<String, ResourceQueryHandler.ResultColumn<?>> columnMap)
        throws MalformedQueryException
    {
        ResourceQueryHandler.ResultColumn<?> rcm = null;
        ResourceClass<?> rc;
        String an = null;
        if(columnMap.containsKey(operand))
        {
            if(lhs)
            {
                throw new MalformedQueryException("resource reference "+operand+
                                                  " is not allowed in this context");
            }
            else
            {
                return columnMap.get(operand);
            }
        }
        int p = operand.lastIndexOf('.');
        if(p < 0)
        {
            if(columnMap.containsKey(null))
            {
                rcm = columnMap.get(null);
                an = operand;
            }
            else
            {
                if(lhs)
                {
                    throw new MalformedQueryException("unqualified attribute "+operand+
                                                      " is not allowed"+
                                                      " in a multi column query");
                }
                else
                {
                    return operand;
                }
            }
        }
        else
        {
            String rca = operand.substring(0,p);
            rcm = columnMap.get(rca);
            if(rcm == null)
            {
                if(lhs)
                {
                    throw new MalformedQueryException("unknown resource class "+rca);
                }
                else
                {
                    return operand;
                }
            }
            an = operand.substring(p+1);
        }
        rc = rcm.getRClass();
        AttributeDefinition<?> ad;
        try
        {
            if(rc == null)
            {
                throw new UnknownAttributeException("BUILTIN attributes only");
            }
            ad = rc.getAttribute(an);
        }
        catch(UnknownAttributeException e)
        {
            try
            {
                rc = coral.getSchema().getResourceClass("coral.Node");
                ad = rc.getAttribute(an);
                if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    if(lhs)
                    {
                        throw new MalformedQueryException(rc.getName()+" does not have "+an+
                                                          " attribute");
                    }
                    else
                    {
                        return operand;
                    }
                }
            }
            catch(EntityDoesNotExistException ee)
            {
                throw new BackendException("'coral.Node' class is missing", e);
            }
            catch(UnknownAttributeException ee)
            {
                if(lhs)
                {
                    throw new MalformedQueryException(rc.getName()+" does not have "+an+
                                                      " attribute");
                }
                else
                {
                    return operand;
                }
            }
        }
        rcm.addAttribute(ad, false);
        return ResourceQueryHandler.ResultColumnAttribute.newInstance(rcm, ad);
    }

    /**
     * Extract AST nodes from a list.
     * 
     * @param list ASTclassAndAliasSpecifierList
     * @return array of ASTclassAndAliasSpecifier objects.
     */
    protected ASTclassAndAliasSpecifier[] getItems(ASTclassAndAliasSpecifierList list)
    {
        int count = list.jjtGetNumChildren();
        ASTclassAndAliasSpecifier[] result = new ASTclassAndAliasSpecifier[count];
        for(int i=0; i<count; i++)
        {
            result[i] = (ASTclassAndAliasSpecifier)list.jjtGetChild(i);
        }
        return result;
    }
    
    /**
     * Extract AST nodes from a list.
     * 
     * @param list ASTorderBySpecifierList
     * @return array of ASTorderBySpecifier objects.
     */
    protected ASTorderBySpecifier[] getItems(ASTorderByList list)
    {
        int count = list.jjtGetNumChildren();
        ASTorderBySpecifier[] result = new ASTorderBySpecifier[count];
        for(int i=0; i<count; i++)
        {
        	result[i] = (ASTorderBySpecifier)list.jjtGetChild(i);
        }
        return result;
    }
    
    /**
     * Extract AST nodes from a list.
     * 
     * @param list ASTselectList
     * @return array of String objects.
     */
    protected String[] getItems(ASTselectList list)
    {
    	int count = list.jjtGetNumChildren();
    	String[] result = new String[count];
    	for(int i=0; i<count; i++)
    	{
    		ASTselectSpecifier specifier = (ASTselectSpecifier)list.jjtGetChild(i);
    		result[i] = specifier.getAttribute();
    	}
    	return result;
    }

    /**
     * Build a list of {@link ResourceQueryHandler.ResultColumn} objects from the query data.
     *
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return a list of {@link ResourceQueryHandler.ResultColumn} objects.
     * @throws MalformedQueryException if the query has syntactic or semantic
     *         errors and thus cannot be executed.
     */
    protected List<ResourceQueryHandler.ResultColumn<?>> getColumns(ASTfindResourceStatement statement)
        throws MalformedQueryException
    {
        ArrayList<ResourceQueryHandler.ResultColumn<?>> columns = new ArrayList<ResourceQueryHandler.ResultColumn<?>>();
        Map<String, ResourceQueryHandler.ResultColumn<?>> columnMap = new HashMap<String, ResourceQueryHandler.ResultColumn<?>>();
        // process FROM
        if(statement.getFrom() == null)
        {
            ResourceQueryHandler.ResultColumn<?> rc = new ResourceQueryHandler.ResultColumn<Resource>(null, null);
            columnMap.put(null, rc);
            columns.add(rc);
            rc.setIndex(1);
        }
        else
        {
            ASTclassAndAliasSpecifier[] items = getItems(statement.getFrom());
            for(int i=0; i<items.length; i++)
            {
                ResourceClass<?> rClass;
                try
                {
                    rClass = entities.resolve(items[i].getResourceClass());
                }
                catch(EntityDoesNotExistException e)
                {
                    throw new MalformedQueryException("resource class does not exist", e);
                }
                catch(AmbigousEntityNameException e)
                {
                    throw new BackendException("non-unique resource class name "+
                                               items[i].getResourceClass(), e);
                }
                String alias = items[i].getAlias();
                if(alias == null)
                {
                    alias = rClass.getName();
                    ResourceQueryHandler.ResultColumn<?> rc = ResourceQueryHandler.ResultColumn.newInstance(rClass, alias);
                    if(columnMap.isEmpty())
                    {
                        columnMap.put(null, rc);
                    }
                    else
                    {
                        columnMap.remove(null);
                    }
                    columnMap.put(alias, rc);
                    columns.add(rc);
                    rc.setIndex(columns.size());
                }
                else
                {
                    ResourceQueryHandler.ResultColumn<?> rc = ResourceQueryHandler.ResultColumn.newInstance(rClass, alias);
                    columnMap.put(alias, rc);
                    columns.add(rc);
                    rc.setIndex(columns.size());
                }
            }
        }
        // process WHERE
        if(statement.getWhere() != null)
        {
            gatherAttributes(statement.getWhere(), columnMap);
        }
        // process ORDER BY
        if(statement.getOrderBy() != null)
        {
            ASTorderBySpecifier[] items = getItems(statement.getOrderBy());
            for(int i=0; i<items.length; i++)
            {
                ResourceQueryHandler.ResultColumnAttribute<?, ?> rcm = (ResourceQueryHandler.ResultColumnAttribute<?, ?>)parseOperand(
                    items[i].getAttribute(), true, false, columnMap);
                if((rcm.getAttribute().getFlags() & AttributeFlags.SYNTHETIC) != 0)
                {
                    throw new MalformedQueryException("SYNTHETIC attribute "+
 rcm.getAttribute().getName() +
                                                      "cannot be used in the ORDER BY clause");
                }
            }
        }

        return columns;
    }

    // implementation ////////////////////////////////////////////////////////


    protected static <A> void checkConstraint(AttributeDefinition<A> ad, String literal)
        throws MalformedQueryException
    {
        AttributeHandler<A> h = ad.getAttributeClass().getHandler();
        try
        {
            A value = h.toAttributeValue(literal);
            if(ad.getDomain() != null && !ad.getDomain().equals(""))
            {
                h.checkDomain(ad.getDomain(), value);
            }
        }
        catch(IllegalArgumentException e)
        {
            throw new MalformedQueryException("Illegal literal value '" + literal + "'", e);
        }
        catch(ConstraintViolationException e)
        {
            throw new MalformedQueryException("Literal value '" + literal + "' "
                + " violates domain constraint " + ad.getDomain() + " on " + ad.getName());
        }
    }

    /**
     * Parses the query.
     *
     * @param query the query
     * @return a parsed FIND RESOURCE statement.
     */
    private ASTfindResourceStatement parseQuery(String query)
        throws MalformedQueryException
    {
        RMLParser parser = coral.getRMLParserFactory().getParser(new StringReader(query));
        try
        {
            return parser.findResourceStatement();
        }
        catch(ParseException e)
        {
            throw new MalformedQueryException("Syntax error", e);
        }
        finally
		{
            coral.getRMLParserFactory().recycle(parser);        	
		}
    }

    /**
     * Gathers information about resource attributes used in a WHERE clause of a query.
     * <p>
     * Warning. Seriously wacky stuff ahead.
     * </p>
     * 
     * @param expr the WHERE clause.
     * @param columnMap map containing ResultColumn objects keyed by alias.
     */
    private void gatherAttributes(ASTconditionalExpression expr,
        final Map<String, ResourceQueryHandler.ResultColumn<?>> columnMap)
        throws MalformedQueryException
    {
        RMLVisitor visitor = new DefaultRMLVisitor()
            {
                public Object visit(ASTdefinedCondition node, Object data)
                {
                    try
                    {
                        AttributeDefinition<?> rhs = ((ResourceQueryHandler.ResultColumnAttribute<?, ?>)parseOperand(
                            node.getRHS(), true, true, columnMap)).getAttribute();
                        if((rhs.getFlags() & AttributeFlags.SYNTHETIC) != 0)
                        {
                            throw new MalformedQueryException("SYNTHETIC attribute "+
                                                              rhs.getName()+
                                                              "cannot be used in WHERE clause");
                        }
                    }
                    catch(MalformedQueryException e)
                    {
                        throw new WrappedMalformedQueryException(e);
                    }
                    return data;
                }
                
                public Object visit(ASTequalityCondition node, Object data)
                {
                    try
                    {
                        AttributeDefinition<?> lhs = ((ResourceQueryHandler.ResultColumnAttribute<?, ?>)parseOperand(
                            node.getLHS(), true, false, columnMap)).getAttribute();
                        if((lhs.getFlags() & AttributeFlags.SYNTHETIC) != 0)
                        {
                            throw new MalformedQueryException("SYNTHETIC attribute "+
                                                              lhs.getName()+
                                                              "cannot be used in WHERE clause");
                        }
                        if((lhs.getAttributeClass().getHandler().getSupportedConditions() & 
                            AttributeHandler.CONDITION_EQUALITY) == 0)
                        {
                            throw new MalformedQueryException("attribute type "+
                                                              lhs.getAttributeClass().getName()+
                                                              " does not support"+
                                                              " equality conditions");
                        }
                        Object rhs;
                        if(node.isRHSLiteral())
                        {
                            rhs = node.getRHS();
                        }
                        else
                        {
                            rhs = parseOperand(node.getRHS(), false, false, columnMap);
                        }
                        if(rhs instanceof ResourceQueryHandler.ResultColumnAttribute)
                        {
                            AttributeDefinition<?> rhsa = ((ResourceQueryHandler.ResultColumnAttribute<?, ?>)rhs)
                                .getAttribute();

                            if((rhsa.getFlags() & AttributeFlags.SYNTHETIC) != 0)
                            {
                                throw new MalformedQueryException("SYNTHETIC attribute "+
                                                                  rhsa.getName()+
                                                                  "cannot be used "+
                                                                  "in WHERE clause");
                            }

                            if(!lhs.getAttributeClass().getJavaClass().
                               isAssignableFrom(rhsa.getAttributeClass().getJavaClass()))
                            {
                                throw new MalformedQueryException(lhs.getAttributeClass().
                                                                  getJavaClass().getName()+
                                                                  " cannot be compared with "+
                                                                  rhsa.getAttributeClass()
                                                                  .getJavaClass().getName());
                            }                                     
                        }
                        if(rhs instanceof ResourceQueryHandler.ResultColumn)
                        {
                            if(!(Resource.class.
                                 isAssignableFrom(lhs.getAttributeClass().getJavaClass())))
                            {
                                throw new MalformedQueryException(lhs.getName()+
                                                                  " is not a Resource reference");
                            }
                            if(lhs.getDomain() != null && !lhs.getDomain().equals(""))
                            {
                                try
                                {
                                    ResourceClass<?> req = coral.getSchema().getResourceClass(
                                        lhs.getDomain());
                                    if(!req.equals(((ResourceQueryHandler.ResultColumn<?>)rhs)
                                        .getRClass())
                                        && !req
                                            .isParent(((ResourceQueryHandler.ResultColumn<?>)rhs)
                                                .getRClass()))
                                    {
                                        throw new MalformedQueryException(
                                            ((ResourceQueryHandler.ResultColumn<?>)rhs).getRClass()
                                                .getName()
                                                + " is not a subclass" + " of " + req.getName());
                                    }
                                }
                                catch(EntityDoesNotExistException e)
                                {
                                    throw new BackendException("invalid constraint on "+
                                        "a Resource reference attribute #" + lhs.getIdString()
                                        + ": " + lhs.getDomain(), e);
                                }
                            }
                        }
                        if(rhs instanceof String)
                        {
                            checkConstraint(lhs, (String)rhs);
                        }   
                    }
                    catch(MalformedQueryException e)
                    {
                        throw new WrappedMalformedQueryException(e);
                    }
                    return data;
                }
                
                public Object visit(ASTcomparisonCondition node, Object data)
                {
                    try
                    {
                        AttributeDefinition<?> lhs = ((ResourceQueryHandler.ResultColumnAttribute<?, ?>)parseOperand(
                            node.getLHS(), true, false, columnMap)).getAttribute();
                        if((lhs.getFlags() & AttributeFlags.SYNTHETIC) != 0)
                        {
                            throw new MalformedQueryException("SYNTHETIC attribute "+
                                                              lhs.getName()+
                                                              "cannot be used in WHERE clause");
                        }
                        if((lhs.getAttributeClass().getHandler().getSupportedConditions() & 
                            AttributeHandler.CONDITION_COMPARISON) == 0)
                        {
                            throw new MalformedQueryException("attribute type "+
                                                              lhs.getAttributeClass().getName()+
                                                              " does not suport"+
                                                              " comparison conditions");
                        }
                        Object rhs;
                        if(node.isRHSLiteral())
                        {
                            rhs = node.getRHS();
                        }
                        else
                        {
                            rhs = parseOperand(node.getRHS(), false, false, columnMap);
                        }
                        if(rhs instanceof ResourceQueryHandler.ResultColumnAttribute)
                        {
                            AttributeDefinition<?> rhsa = ((ResourceQueryHandler.ResultColumnAttribute<?, ?>)rhs)
                                .getAttribute();

                            if((rhsa.getFlags() & AttributeFlags.SYNTHETIC) != 0)
                            {
                                throw new MalformedQueryException("SYNTHETIC attribute "+
                                                                  rhsa.getName()+
                                                                  "cannot be used "+
                                                                  "in WHERE clause");
                            }

                            if(!lhs.getAttributeClass().getJavaClass().
                               isAssignableFrom(rhsa.getAttributeClass().getJavaClass()))
                            {
                                throw new MalformedQueryException(lhs.getAttributeClass().
                                                                  getJavaClass().getName()+
                                                                  " cannot be compared with "+
                                                                  rhsa.getAttributeClass()
                                                                  .getJavaClass().getName());
                            }                                     
                        }
                        if(rhs instanceof ResourceQueryHandler.ResultColumn)
                        {
                            throw new MalformedQueryException("resource class reference "+
                                                              node.getRHS()+
                                                              " is not allowed in this context");
                        }
                        if(rhs instanceof String)
                        {
                            checkConstraint(lhs, (String)rhs);
                        }   
                    }
                    catch(MalformedQueryException e)
                    {
                        throw new WrappedMalformedQueryException(e);
                    }
                    return data;
                }

                public Object visit(ASTapproximationCondition node, Object data)
                {
                    try
                    {
                        AttributeDefinition<?> lhs = ((ResourceQueryHandler.ResultColumnAttribute<?, ?>)parseOperand(
                            node.getLHS(), true, false, columnMap)).getAttribute();
                        if((lhs.getFlags() & AttributeFlags.SYNTHETIC) != 0)
                        {
                            throw new MalformedQueryException("SYNTHETIC attribute "+
                                                              lhs.getName()+
                                                              "cannot be used in WHERE clause");
                        }
                        if((lhs.getAttributeClass().getHandler().getSupportedConditions() & 
                            AttributeHandler.CONDITION_APPROXIMATION) == 0)
                        {
                            throw new MalformedQueryException("attribute type "+
                                                              lhs.getAttributeClass().getName()+
                                                              " does not suport"+
                                                              " approximation conditions");
                        }
                        Object rhs;
                        if(node.isRHSLiteral())
                        {
                            rhs = node.getRHS();
                        }
                        else
                        {
                            rhs = parseOperand(node.getRHS(), false, false, columnMap);
                        }
                        if(rhs instanceof ResourceQueryHandler.ResultColumnAttribute)
                        {
                            AttributeDefinition<?> rhsa = ((ResourceQueryHandler.ResultColumnAttribute<?, ?>)rhs)
                                .getAttribute();

                            if((rhsa.getFlags() & AttributeFlags.SYNTHETIC) != 0)
                            {
                                throw new MalformedQueryException("SYNTHETIC attribute "+
                                                                  rhsa.getName()+
                                                                  "cannot be used "+
                                                                  "in WHERE clause");
                            }

                            if(!lhs.getAttributeClass().getJavaClass().
                               isAssignableFrom(rhsa.getAttributeClass().getJavaClass()))
                            {
                                throw new MalformedQueryException(lhs.getAttributeClass().
                                                                  getJavaClass().getName()+
                                                                  " cannot be compared with "+
                                                                  rhsa.getAttributeClass()
                                                                  .getJavaClass().getName());
                            }                                     
                        }
                        if(rhs instanceof ResourceQueryHandler.ResultColumn)
                        {
                            throw new MalformedQueryException("resource class reference "+
                                                              node.getRHS()+
                                                              " is not allowed in this context");
                        }
                        if(rhs instanceof String)
                        {
                            checkConstraint(lhs, (String)rhs);
                        }   
                    }
                    catch(MalformedQueryException e)
                    {
                        throw new WrappedMalformedQueryException(e);
                    }
                    return data;
                }
            };
       
        try
        {
            visitor.visit(expr, null);
        }
        catch(WrappedMalformedQueryException e)
        {
            throw e.getException();
        }
    }

    // inner classes /////////////////////////////////////////////////////////
    
    /**
     * Helper runtime exception class needed to bypass the exception contract
     * of the AST visitor.
     */
    protected class WrappedMalformedQueryException
        extends RuntimeException
    {
		public static final long serialVersionUID = 0L;
		
        private MalformedQueryException e;

        /**
         * Creates new WrappedMalformedQueryException instance.
         * 
         * @param e a MalformedQueryException.
         */
        public WrappedMalformedQueryException(MalformedQueryException e)
        {
            this.e = e;
        }
        
        /**
         * Returns the wrapped exception.
         * 
         * @return the wrapped exception.
         */
        public MalformedQueryException getException()
        {
            return e;
        }
    }
}
