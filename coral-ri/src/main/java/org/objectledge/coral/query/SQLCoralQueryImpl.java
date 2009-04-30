package org.objectledge.coral.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.script.parser.ASTandExpression;
import org.objectledge.coral.script.parser.ASTapproximationCondition;
import org.objectledge.coral.script.parser.ASTcomparisonCondition;
import org.objectledge.coral.script.parser.ASTconditionalExpression;
import org.objectledge.coral.script.parser.ASTdefinedCondition;
import org.objectledge.coral.script.parser.ASTequalityCondition;
import org.objectledge.coral.script.parser.ASTfindResourceStatement;
import org.objectledge.coral.script.parser.ASTnotExpression;
import org.objectledge.coral.script.parser.ASTorExpression;
import org.objectledge.coral.script.parser.ASTorderBySpecifier;
import org.objectledge.coral.script.parser.DefaultRMLVisitor;
import org.objectledge.coral.script.parser.RMLVisitor;
import org.objectledge.coral.script.parser.SimpleNode;
import org.objectledge.database.Database;

/**
 * A QueryService implementation that uses the underlying relational database.
 *
 * @author <a href="rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: SQLCoralQueryImpl.java,v 1.11 2008-06-05 17:17:03 rafal Exp $
 */
public class SQLCoralQueryImpl
    extends AbstractCoralQueryImpl
{
    /** the parser syntax */
    private static Map attributesMap = new HashMap();

    static
    {
        attributesMap.put("id", "resource_id");
        attributesMap.put("owner", "owned_by");
        //TODO http://objectledge.org/jira/browse/CORAL-72 add other attribute name mapping
    }
    
    
    // instance variables ////////////////////////////////////////////////////
    
    /** The database service */
    private Database database;
    
    private Logger logger;

    // initailzation /////////////////////////////////////////////////////////

    /** 
     * Constructs an SQLQueryService implementation instance.
     *
     * @param database the database to use.
     * @param coral the coral core.
     * @param logger the logger.
     */
    public SQLCoralQueryImpl(Database database, CoralCore coral, Logger logger)
    {
        super(coral);
        this.database = database;
        this.logger = logger;
    }

    // QueryService interface ////////////////////////////////////////////////

    /**
     * Executes a preparsed query.
     *
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return query resuls.
     * @throws MalformedQueryException if the query has semantic errors and
     * thus cannot be executed. 
     */
    public QueryResults executeQuery(ASTfindResourceStatement statement)
        throws MalformedQueryException
    {
        List columns = getColumns(statement);
        Map columnMap = new HashMap();
        Iterator it = columns.iterator();
        while(it.hasNext())
        {
            ResultColumn rcm = (ResultColumn)it.next();
            columnMap.put(rcm.getAlias(), rcm);
        }
        if(columns.size() == 1)
        {
            columnMap.put(null, columns.get(0));
        }

        StringBuilder query = new StringBuilder();
        // SELECT
        query.append("SELECT ");
        for(int i=0; i<columns.size(); i++)
        {
            query.append("r").append(i+1).append(".resource_id");
            if(i<columns.size()-1)
            {
                query.append(", ");
            }
        }
        // FROM
        for(int i=0; i<columns.size(); i++)
        {
            query.append(i==0 ? "\nFROM " : "\n  , ");
            query.append("coral_resource r").append(i+1);
            ResultColumn rcm = (ResultColumn)columns.get(i);
            for(int j=0; j<rcm.getAttributes().size(); j++)
            {
                AttributeDefinition ad = (AttributeDefinition)rcm.getAttributes().get(j);
                if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    query.append(", coral_generic_resource ");
                    query.append("r").append(i+1).append("g").append(j+1);
                    query.append(", ");
                    query.append(ad.getAttributeClass().getDbTable());
                    query.append(" ");
                    query.append("r").append(i+1).append("a").append(j+1);
                } 
            }
        }
        boolean whereStarted = false;
        // WHERE - resource classes
        for(int i=0; i<columns.size(); i++)
        {
            ResultColumn rcm = (ResultColumn)columns.get(i);
            if(rcm.getRClass() != null)
            {
                if(whereStarted)
                {
                    query.append("\n  AND ");
                }
                else
                {
                    query.append("\nWHERE ");
                    whereStarted = true;
                }
                ResourceClass[] children = rcm.getRClass().getChildClasses();
                query.append("r").append(i+1).append(".resource_class_id");
                if(children.length > 0)
                {
                    query.append(" IN (");
                    for(int j=0; j<children.length; j++)
                    {
                        query.append(children[j].getIdString());
                        query.append(", ");
                    }
                    query.append(rcm.getRClass().getIdString()).append(")");
                }
                else
                {
                    query.append(" = ");
                    query.append(rcm.getRClass().getIdString());
                }
            }
        }
        // WHERE - attribute glue
        for(int i=0; i<columns.size(); i++)
        {
            ResultColumn rcm = (ResultColumn)columns.get(i);
            for(int j=0; j<rcm.getAttributes().size(); j++)
            {
                AttributeDefinition ad = (AttributeDefinition)rcm.getAttributes().get(j);
                if((ad.getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    if(whereStarted)
                    {
                        query.append("\n  AND ");
                    }
                    else
                    {
                        query.append("\nWHERE ");
                        whereStarted = true;
                    }
                    query.append("r").append(i+1).append("g").append(j+1);
                    query.append(".resource_id = r").append(i+1).append(".resource_id");
                    query.append(" AND ");
                    query.append("r").append(i+1).append("g").append(j+1);
                    query.append(".attribute_definition_id = ");
                    query.append(ad.getIdString());
                    query.append(" AND ");
                    query.append("r").append(i+1).append("a").append(j+1).append(".data_key = ");
                    query.append("r").append(i+1).append("g").append(j+1).append(".data_key");
                }
            }
        }
        // WHERE - condition
        if(statement.getWhere() != null)
        {
            if(whereStarted)
            {
                query.append("\n  AND ");
            }
            else
            {
                query.append("\nWHERE ");
            }
            appendCondition(statement.getWhere(), columnMap, query);
        }
        // ORDER BY
        if(statement.getOrderBy() != null)
        {
            ASTorderBySpecifier[] items = getItems(statement.getOrderBy());
            query.append("\nORDER BY ");
            for(int i=0; i<items.length; i++)
            {
                appendAttribute(items[i].getAttribute(), columnMap, query);
                if(!items[i].getDirection())
                {
                    query.append(" DESC");
                }
                if(i<items.length-1)
                {
                    query.append(", ");
                }
            }
        }

        Connection conn = null;
        try
        {
            conn = database.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery(query.toString());
            String[][] from = new String[columns.size()][];
            for(int i=0; i<columns.size(); i++)
            {
                ResultColumn rcm = (ResultColumn)columns.get(i);
                from[i] = new String[2];
                from[i][0] = rcm.getRClass() != null ? rcm.getRClass().getName() : null;
                from[i][1] = rcm.getAlias();
            }
            String[] select = statement.getSelect() != null ? 
            		getItems(statement.getSelect()) : null;
            QueryResults queryResults = new SQLQueryResultsImpl(coral.getSchema(), 
                coral.getStore(), results, from, select );
            results.close();
            stmt.close();
            return queryResults;
        }
        catch(SQLException e)
        {
            throw new BackendException("failed to execute query", e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(SQLException e)
                {
                    logger.error("failed to close connection", e);
                }
            }
        }
    }

    /**
     * Prepares a preparsed query.
     *
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return query resuls.
     * @throws MalformedQueryException if the query has semantic errors and
     * thus cannot be executed. 
     */
    protected PreparedQuery prepareQuery(ASTfindResourceStatement statement)
        throws MalformedQueryException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    // implementation ////////////////////////////////////////////////////////

    /**
     * Appends an attribute reference to the query.
     *
     * @param attribute the attribute to append
     * @param columnMap the map containting ResultColum objectcs keyed by 
     *        alias.
     * @param out the buffer to write expresion to.
     */
    void appendAttribute(String attribute, 
                                 final Map columnMap,
                                 final StringBuilder out)
        throws MalformedQueryException
    {
        ResultColumnAttribute rca = (ResultColumnAttribute)
            parseOperand(attribute, true, columnMap);
        if((rca.getAttribute().getFlags() & AttributeFlags.BUILTIN) == 0)
        {
            out.append("r").append(rca.getColumn().getIndex()).
                append("a").append(((Integer)rca.getColumn().getNameIndex().
                                    get(rca.getAttribute().getName())).intValue()+1);
            if(Entity.class.isAssignableFrom(rca.getAttribute().
                                             getAttributeClass().getJavaClass()))
            {
                out.append(".ref");
            }
            else
            {
                out.append(".data");
            }
        }
        else
        {
            out.append("r").append(rca.getColumn().getIndex()).
                append(".");
            String columnName = (String)attributesMap.get(rca.getAttribute().getName());
            if(columnName == null)
            {
                columnName = rca.getAttribute().getName();
            }
            out.append(columnName);
        }
    }

    /**
     * Generates a conditional expression based on the query WHERE clause.
     *
     * @param expr the WHERE clause.
     * @param columnMap the map containting ResultColum objectcs keyed by 
     *        alias.
     * @param out the buffer to write expresion to.
     */
    private void appendCondition(ASTconditionalExpression expr, 
                                 final Map columnMap,
                                 final StringBuilder out)
        throws MalformedQueryException
    {
        RMLVisitor visitor = new DefaultRMLVisitor()
            {
                public Object visit(ASTnotExpression node, Object data)
                {
                    out.append("NOT ");
                    return visit((SimpleNode)node, data);
                }

                public Object visit(ASTandExpression node, Object data)
                {
                    boolean parens = node.jjtGetParent() instanceof ASTnotExpression;
                    
                    if(parens)
                    {
                        out.append("(");
                    }
                    for(int i=0; i<node.jjtGetNumChildren(); i++)
                    {
                        node.jjtGetChild(i).jjtAccept(this, data);
                        if(i < node.jjtGetNumChildren()-1)
                        {
                            out.append(" AND ");
                        }
                    }
                    if(parens)
                    {
                        out.append(")");
                    }
                    return data;
                }                
                
                public Object visit(ASTorExpression node, Object data)
                {
                    boolean parens = (node.jjtGetParent() instanceof ASTnotExpression) ||
                        (node.jjtGetParent() instanceof ASTandExpression) ;
                    if(parens)
                    {
                        out.append("(");
                    }
                    for(int i=0; i<node.jjtGetNumChildren(); i++)
                    {
                        node.jjtGetChild(i).jjtAccept(this, data);
                        if(i < node.jjtGetNumChildren()-1)
                        {
                            out.append(" OR ");
                        }
                    }
                    if(parens)
                    {
                        out.append(")");
                    }
                    return data;
                }

                public Object visit(ASTdefinedCondition node, Object data)
                {
                    try
                    {
                        appendAttribute(node.getRHS(), columnMap, out);
                        out.append(" NOTNULL");
                        return data;
                    }
                    catch(MalformedQueryException e)
                    {
                        throw new WrappedMalformedQueryException(e);
                    }
                }

                public Object visit(ASTequalityCondition node, Object data)
                {
                    try
                    {
                        AttributeDefinition lhs = 
                            ((ResultColumnAttribute)parseOperand(node.getLHS(), true, columnMap)).
                             getAttribute();
                        appendAttribute(node.getLHS(), columnMap, out);
                        String[] ops = { " <> ", " = " };
                        out.append(ops[node.getOperator()]);
                        Object rhs = parseOperand(node.getRHS(), false, columnMap);
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            appendAttribute(node.getRHS(), columnMap, out);
                        }
                        if(rhs instanceof ResultColumn)
                        {
                            out.append("r").append(((ResultColumn)rhs).getIndex()).
                                append(".resource_id");
                        }
                        if(rhs instanceof String)
                        {
                            AttributeHandler h = lhs.getAttributeClass().getHandler();
                            Object value = h.toAttributeValue(rhs);
                            out.append(h.toExternalString(value));
                        }                        
                        return data;
                    }
                    catch(MalformedQueryException e)
                    {
                        throw new WrappedMalformedQueryException(e);
                    }
                }

                public Object visit(ASTcomparisonCondition node, Object data)
                {
                    try
                    {
                        AttributeDefinition lhs = 
                            ((ResultColumnAttribute)parseOperand(node.getLHS(), true, columnMap)).
                             getAttribute();
                        appendAttribute(node.getLHS(), columnMap, out);
                        String[] ops = { " < ", " <= ", " >= ", " > " };
                        out.append(ops[node.getOperator()]);
                        Object rhs = parseOperand(node.getRHS(), false, columnMap);
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            appendAttribute(node.getRHS(), columnMap, out);
                        }
                        if(rhs instanceof String)
                        {
                            AttributeHandler h = lhs.getAttributeClass().getHandler();
                            Object value = h.toAttributeValue(rhs);
                            out.append(h.toExternalString(value));
                        }                        
                        return data;
                    }
                    catch(MalformedQueryException e)
                    {
                        throw new WrappedMalformedQueryException(e);
                    }
                }

                public Object visit(ASTapproximationCondition node, Object data)
                {
                    try
                    {
                        AttributeDefinition lhs = 
                        	((ResultColumnAttribute)parseOperand(node.getLHS(), true, columnMap)).
                        	 getAttribute();
                        boolean[] caseSensitive = { false, true };
                        if(caseSensitive[node.getOperator()]){
                            appendAttribute(node.getLHS(), columnMap, out);
                        }else{
                            out.append(" LOWER(");
                            appendAttribute(node.getLHS(), columnMap, out);
                            out.append(")");
                        }
                        out.append(" LIKE ");
                        Object rhs = parseOperand(node.getRHS(), false, columnMap);
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            appendAttribute(node.getRHS(), columnMap, out);
                        }
                        if(rhs instanceof String)
                        {
                            AttributeHandler h = lhs.getAttributeClass().getHandler();
                            Object value = h.toAttributeValue(rhs);
                            out.append(h.toExternalString(value));
                        }						 
                        return data;
                    }
                    catch(MalformedQueryException e)
                    {
                        throw new WrappedMalformedQueryException(e);
                    }
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
}
