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
import org.objectledge.coral.script.parser.DefaultRMLVisitor;
import org.objectledge.coral.script.parser.ParseException;
import org.objectledge.coral.script.parser.RMLParser;
import org.objectledge.coral.script.parser.RMLVisitor;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;


/**
 * A common base class for {@link QueryService} implemnetations.
 *
 * @author <a href="rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: AbstractCoralQueryImpl.java,v 1.1 2004-08-27 11:31:23 rafal Exp $
 */
public abstract class AbstractCoralQueryImpl
    implements CoralQuery
{
    // instance variables ////////////////////////////////////////////////////

	/** The coral session. */
	private CoralCore coral;
	
    /** The entity resolver. */
    private RMLEntityResolver entities;

    // initailzation /////////////////////////////////////////////////////////

    /** 
     * Constructs an QueryService implementation instance.
     *
     */
    public AbstractCoralQueryImpl(CoralCore coral)
    {
    	this.coral = coral;
        entities = new RMLEntityResolver(coral.getSchema(), coral.getSecurity(), coral.getStore());
    }
    
    // public interface //////////////////////////////////////////////////////

    /**
     * Executes a query.
     *
     * @param query the query.
     * @return query resuls.
     * @throws MalformedQuery exception if the query has syntactic or semantic
     *         errors and thus cannot be executed.
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
     *
     * <p>Query may contain a number of <em>positional parameters</em>. The
     * parameters have form of "$"<NUMBER>, with no intervening
     * whitespace. The nuber must be greater or equal to 1. The positional
     * parameters can only be used on the right hand side of binary operators.
     * When the query is executed, all positional parameters must be set to
     * non-null values. To test a resource's property for being defined
     * (i.e. not null), use the DEFINED operator.</p>
     *
     * @throws MalformedQuery exception if the query has syntactic or semantic
     *         errors and thus cannot be executed.
     */
    public PreparedQuery prepareQuery(String query)
        throws MalformedQueryException
    {
        ASTfindResourceStatement statement = parseQuery(query);
        return prepareQuery(statement);
    }

    // methods to override in implemenation //////////////////////////////////

    /**
     * Executes a preparsed query.
     *
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return query resuls.
     * @throws MalformedQuery exception if the query has semantic errors and
     * thus cannot be executed. 
     */
    public abstract QueryResults executeQuery(ASTfindResourceStatement statement)
        throws MalformedQueryException;

    /**
     * Prepares a preparsed query.
     *
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return query resuls.
     * @throws MalformedQuery exception if the query has semantic errors and
     * thus cannot be executed. 
     */
    protected abstract PreparedQuery prepareQuery(ASTfindResourceStatement statement)
        throws MalformedQueryException;

    // common parts of the implementation ////////////////////////////////////

    /**
     * Parses an conditional expression operand.
     *
     * <p>If the lhs is <code>true</code> the operand must be an attribute
     * reference. Otherwise it may be also a resource class reference (using
     * query results column alias) or a literal value.</p>
     *
     * <p>If the operand is an attribute, AttributeDefinition will be
     * returned. If it is a colmn alias, ResultColumn will be returned.
     * Finally if it is a literal constans a String containing it's value will
     * be returned.</p> 
     *
     * @param operand the operand.
     * @param lhs <code>true</code> if the operand is on the left hand side of
     * the operator. 
     * @param columnMap map containg ResultColumn objects keyed by alias.
     * @returns an AttributeDefinition, ResultColumn, or String.
     */
    protected Object parseOperand(String operand, boolean lhs, Map columnMap)
        throws MalformedQueryException
    {
        ResultColumn rcm = null;
        ResourceClass rc;
        String an = null;
        if(columnMap.containsKey(operand))
        {
            if(lhs)
            {
                throw new MalformedQueryException("resource reference "+operand+
                                                  " is not allowed in this context");
            }
            {
                return columnMap.get(operand);
            }
        }
        int p = operand.lastIndexOf('.');
        if(p < 0)
        {
            if(columnMap.containsKey(null))
            {
                rcm = (ResultColumn)columnMap.get(null);
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
            rcm = (ResultColumn)columnMap.get(rca);
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
        rc = rcm.rClass;
        AttributeDefinition ad;
        try
        {
            if(rcm.rClass == null)
            {
                throw new UnknownAttributeException("BUILTIN attributes only");
            }
            ad = rc.getAttribute(an);
        }
        catch(UnknownAttributeException e)
        {
            try
            {
                rc = coral.getSchema().getResourceClass("node");
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
                throw new BackendException("'node' class is missing");
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
        if(!rcm.attributes.contains(ad))
        {
            rcm.attributes.add(ad);
            rcm.nameIndex.put(ad.getName(), new Integer(rcm.attributes.size()-1));
        }
        return new ResultColumnAttribute(rcm, ad);
    }

    private ASTclassAndAliasSpecifier[] getItems(ASTclassAndAliasSpecifierList list)
    {
        int count = list.jjtGetNumChildren();
        List items = new ArrayList();
        for(int i=0; i<count; i++)
        {
            items.add(list.jjtGetChild(i));
        }
        ASTclassAndAliasSpecifier[] result = new ASTclassAndAliasSpecifier[items.size()];
        items.toArray(result);
        return result;
    }
    
    private ASTorderBySpecifier[] getItems(ASTorderByList list)
    {
        int count = list.jjtGetNumChildren();
        List items = new ArrayList();
        for(int i=0; i<count; i++)
        {
            items.add(list.jjtGetChild(i));
        }
        ASTorderBySpecifier[] result = new ASTorderBySpecifier[items.size()];
        items.toArray(result);
        return result;
    }

    
    /**
     * Build a list of {@link ResultColumn} objects from the query data.
     *
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return a list of {@link ResultColumn} objects.
     */
    protected List getColumns(ASTfindResourceStatement statement)
        throws MalformedQueryException
    {
        ArrayList columns = new ArrayList();
        Map columnMap = new HashMap();
        // process FROM
        if(statement.getFrom() == null)
        {
            ResultColumn rc = new ResultColumn(null, null);
            columnMap.put(null, rc);
            columns.add(rc);
            rc.index = 1;
        }
        else
        {
            ASTclassAndAliasSpecifier[] items = getItems(statement.getFrom());
            for(int i=0; i<items.length; i++)
            {
                ResourceClass rClass;
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
                                               items[i].getResourceClass());
                }
                String alias = items[i].getAlias();
                if(alias == null)
                {
                    alias = rClass.getName();
                    ResultColumn rc = new ResultColumn(rClass, alias);
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
                    rc.index = columns.size();
                }
                else
                {
                    ResultColumn rc = new ResultColumn(rClass, alias);
                    columnMap.put(alias, rc);
                    columns.add(rc);
                    rc.index = columns.size();
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
                ResultColumnAttribute rcm = 
                    (ResultColumnAttribute)parseOperand(items[i].getAttribute(), true, columnMap);
                if((rcm.attribute.getFlags() & AttributeFlags.SYNTHETIC) != 0)
                {
                    throw new MalformedQueryException("SYNTHETIC attribute "+
                                                      rcm.attribute.getName()+
                                                      "cannot be used in the ORDER BY clause");
                }
            }
        }

        return columns;
    }

    // implementation ////////////////////////////////////////////////////////
    
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
        ASTfindResourceStatement statement;
        try
        {
            statement = parser.findResourceStatement();
        }
        catch(ParseException e)
        {
            throw new MalformedQueryException("Syntax error", e);
        }
        coral.getRMLParserFactory().recycle(parser);
        return statement;
    }

    /**
     * Gathers information about resource attributes used in a WHERE clause of
     * a query.
     *
     * <p>Warning. Seriously wacky stuff ahead.</p>
     *
     * @param expr the WHERE clause.
     * @param columnMap map containg ResultColumn objects keyed by alias.
     */
    private void gatherAttributes(ASTconditionalExpression expr, final Map columnMap)
        throws MalformedQueryException
    {
        RMLVisitor visitor = new DefaultRMLVisitor()
            {
                public Object visit(ASTdefinedCondition node, Object data)
                {
                    try
                    {
                        AttributeDefinition rhs = 
                            ((ResultColumnAttribute)parseOperand(node.getRHS(), true, columnMap)).
                            attribute;
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
                        AttributeDefinition lhs = 
                            ((ResultColumnAttribute)parseOperand(node.getLHS(), true, columnMap)).
                            attribute;
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
                            rhs = parseOperand(node.getRHS(), false, columnMap);
                        }
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            AttributeDefinition rhsa = ((ResultColumnAttribute)rhs).attribute;

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
                        if(rhs instanceof ResultColumn)
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
                                    ResourceClass req = coral.getSchema().
                                        getResourceClass(lhs.getDomain());
                                    if(!req.equals(((ResultColumn)rhs).rClass) &&
                                       !req.isParent(((ResultColumn)rhs).rClass))
                                    {
                                        throw new MalformedQueryException(((ResultColumn)rhs).
                                                                          rClass.getName()+
                                                                          " is not a subclass"+
                                                                          " of "+
                                                                          req.getName());
                                    }
                                }
                                catch(EntityDoesNotExistException e)
                                {
                                    throw new BackendException("invalid constraint on "+
                                                               "a Resource reference attribute #"+
                                                               lhs.getId()+": "+lhs.getDomain());
                                }
                            }
                        }
                        if(rhs instanceof String)
                        {
                            AttributeHandler h = lhs.getAttributeClass().getHandler();
                            try
                            {
                                Object value = h.toAttributeValue(rhs);
                                if(lhs.getDomain() != null && !lhs.getDomain().equals(""))
                                {
                                    h.checkDomain(lhs.getDomain(), value);
                                }
                            }
                            catch(IllegalArgumentException e)
                            {
                                throw new MalformedQueryException("Illegal literal value '"+
                                                                 rhs+"'", e);
                            }
                            catch(ConstraintViolationException e)
                            {
                                throw new MalformedQueryException("Literal value '"+rhs+"' "+
                                                                  " violates domain constraint "+
                                                                  lhs.getDomain()+" on "+
                                                                  lhs.getName());
                            }
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
                        AttributeDefinition lhs = 
                            ((ResultColumnAttribute)parseOperand(node.getLHS(), true, columnMap)).
                             attribute;
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
                            rhs = parseOperand(node.getRHS(), false, columnMap);
                        }
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            AttributeDefinition rhsa = ((ResultColumnAttribute)rhs).attribute;

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
                        if(rhs instanceof ResultColumn)
                        {
                            throw new MalformedQueryException("resource class reference "+
                                                              node.getRHS()+
                                                              " is not allowed in this context");
                        }
                        if(rhs instanceof String)
                        {
                            AttributeHandler h = lhs.getAttributeClass().getHandler();
                            try
                            {
                                Object value = h.toAttributeValue(rhs);
                                if(lhs.getDomain() != null && !lhs.getDomain().equals(""))
                                {
                                    h.checkDomain(lhs.getDomain(), value);
                                }
                            }
                            catch(IllegalArgumentException e)
                            {
                                throw new MalformedQueryException("Illegal literal value '"+
                                                                 rhs+"'", e);
                            }
                            catch(ConstraintViolationException e)
                            {
                                throw new MalformedQueryException("Literal value '"+rhs+"' "+
                                                                  " violates domain constraint "+
                                                                  lhs.getDomain()+" on "+
                                                                  lhs.getName());
                            }
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
                        AttributeDefinition lhs = 
                            ((ResultColumnAttribute)parseOperand(node.getLHS(), true, columnMap)).
                            attribute;
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
                            rhs = parseOperand(node.getRHS(), false, columnMap);
                        }
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            AttributeDefinition rhsa = ((ResultColumnAttribute)rhs).attribute;

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
                        if(rhs instanceof ResultColumn)
                        {
                            throw new MalformedQueryException("resource class reference "+
                                                              node.getRHS()+
                                                              " is not allowed in this context");
                        }
                        if(rhs instanceof String)
                        {
                            AttributeHandler h = lhs.getAttributeClass().getHandler();
                            try
                            {
                                Object value = h.toAttributeValue(rhs);
                                if(lhs.getDomain() != null && !lhs.getDomain().equals(""))
                                {
                                    h.checkDomain(lhs.getDomain(), value);
                                }
                            }
                            catch(IllegalArgumentException e)
                            {
                                throw new MalformedQueryException("Illegal literal value '"+
                                                                 rhs+"'", e);
                            }
                            catch(ConstraintViolationException e)
                            {
                                throw new MalformedQueryException("Literal value '"+rhs+"' "+
                                                                  " violates domain constraint "+
                                                                  lhs.getDomain()+" on "+
                                                                  lhs.getName());
                            }
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
     * Describes a column of the query results.
     */
    protected class ResultColumn
    {
        // instance variables ////////////////////////////////////////////////

        /** The resource class, or <code>null</code> for any. */
        public ResourceClass rClass;

        /** The 1-based index of the column. */
        public int index;
        
        /** The alias or <code>null</code> for none. */
        public String alias;
        
        /** The attributes used in WHERE and ORDER BY clauses. */
        public List attributes = new ArrayList();
        
        /** Mapping of attribute names to indices. */
        public Map nameIndex = new HashMap();

        // initialization ////////////////////////////////////////////////////
        
        /**
         * Constructs a ResultColumn object.
         *
         * @param rClass the resource class or null for any.
         * @param alias the alias or null for none.
         */
        public ResultColumn(ResourceClass rClass, String alias)
        {
            this.rClass = rClass;
            this.alias = alias;
        }
    }

    /**
     * Helper runtime exception class needed to bypass the exception contract
     * of the AST visitor.
     */
    protected class WrappedMalformedQueryException
        extends RuntimeException
    {
        private MalformedQueryException e;

        public WrappedMalformedQueryException(MalformedQueryException e)
        {
            this.e = e;
        }
        
        public MalformedQueryException getException()
        {
            return e;
        }
    }

    /**
     * Helper class binding attribute definition with a result column.
     */
    protected class ResultColumnAttribute
    {
        /**
         * Constructs a ResultColumnAttribute.
         *
         * @param column the column.
         * @param attribute the attribute.
         */
        public ResultColumnAttribute(ResultColumn column, 
                                     AttributeDefinition attribute)
        {
            this.column = column;
            this.attribute = attribute;
        }

        /** The column. */
        public ResultColumn column;
        
        /** The attribute. */
        public AttributeDefinition attribute;
    }
}
