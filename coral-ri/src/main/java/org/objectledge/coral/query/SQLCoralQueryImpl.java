package org.objectledge.coral.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.query.ResourceQueryHandler.ResultColumn;
import org.objectledge.coral.query.ResourceQueryHandler.ResultColumnAttribute;
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
import org.objectledge.database.DatabaseUtils;

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
    private static Map<String, String> builtinAttrNames = new HashMap<String, String>();

    static
    {
        builtinAttrNames.put("id", "resource_id");
        builtinAttrNames.put("name", "name");
        builtinAttrNames.put("parent", "parent");
        builtinAttrNames.put("resource_class", "resource_class_id");
        builtinAttrNames.put("owner", "owned_by");
        builtinAttrNames.put("created_by", "created_by");
        builtinAttrNames.put("modified_by", "modified_by");
        builtinAttrNames.put("creation_time", "creation_time");
        builtinAttrNames.put("modification_time", "modification_time");
    }

    // instance variables ////////////////////////////////////////////////////

    /** The database service */
    private Database database;

    // initialization ////////////////////////////////////////////////////////

    /**
     * Constructs an SQLQueryService implementation instance.
     * 
     * @param database the database to use.
     * @param coral the coral core.
     * @param logger the logger.
     */
    public SQLCoralQueryImpl(Database database, CoralCore coral)
    {
        super(coral);
        this.database = database;
    }

    // QueryService interface ////////////////////////////////////////////////

    /**
     * Executes a pre-parsed query.
     * 
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return query results.
     * @throws MalformedQueryException if the query has semantic errors and thus cannot be executed.
     */
    public QueryResults executeQuery(ASTfindResourceStatement statement)
        throws MalformedQueryException
    {
        ResourceClass<?> rootRClass;
        try
        {
            rootRClass = coral.getSchema().getResourceClass(1);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("resource class #1 not available", e);
        }

        List<ResultColumn<?>> columns = getColumns(statement, rootRClass);
        Map<String, ResultColumn<?>> columnMap = new HashMap<String, ResultColumn<?>>();
        Iterator<ResultColumn<?>> it = columns.iterator();
        while(it.hasNext())
        {
            ResultColumn<?> rcm = it.next();
            columnMap.put(rcm.getAlias(), rcm);
        }
        if(columns.size() == 1)
        {
            columnMap.put(null, columns.get(0));
        }

        StringBuilder query = new StringBuilder();
        // SELECT
        for(int i = 0; i < columns.size(); i++)
        {
            query.append(i == 0 ? "SELECT " : ", ");
            appendResourceIdTerm(query, columns.get(i));
        }
        // FROM
        for(int i = 0; i < columns.size(); i++)
        {
            query.append(i == 0 ? "\nFROM " : "\n  , ");
            columns.get(i).getQHandler().appendFromClause(query, columns.get(i), builtinAttrNames, false);
        }
        boolean whereStarted = false;
        // WHERE - resource classes
        for(int i = 0; i < columns.size(); i++)
        {
            ResultColumn<?> rcm = columns.get(i);
            if(statement.getFrom() != null)
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
                ResourceClass<?>[] children = rcm.getRClass().getChildClasses();
                query.append("r").append(i + 1).append(".resource_class_id");
                if(children.length > 0)
                {
                    query.append(" IN (");
                    for(int j = 0; j < children.length; j++)
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
        for(int i = 0; i < columns.size(); i++)
        {
            ResultColumn<?> rcm = columns.get(i);
            whereStarted = rcm.getQHandler().appendWhereClause(query, whereStarted, rcm)
                || whereStarted;
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
            query.append("(");
            appendCondition(statement.getWhere(), columnMap, query);
            query.append(")");
        }
        // ORDER BY
        if(statement.getOrderBy() != null)
        {
            ASTorderBySpecifier[] items = getItems(statement.getOrderBy());
            query.append("\nORDER BY ");
            for(int i = 0; i < items.length; i++)
            {
                appendAttribute(items[i].getAttribute(), columnMap, query);
                if(!items[i].getDirection())
                {
                    query.append(" DESC");
                }
                if(i < items.length - 1)
                {
                    query.append(", ");
                }
            }
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet results = null;
        try
        {
            conn = database.getConnection();
            stmt = conn.createStatement();
            results = stmt.executeQuery(query.toString());
            String[][] from = new String[columns.size()][];
            for(int i = 0; i < columns.size(); i++)
            {
                ResultColumn<?> rcm = columns.get(i);
                from[i] = new String[2];
                from[i][0] = rcm.getRClass() != null ? rcm.getRClass().getName() : null;
                from[i][1] = rcm.getAlias();
            }
            String[] select = statement.getSelect() != null ? getItems(statement.getSelect())
                : null;
            QueryResults queryResults = new SQLQueryResultsImpl(coral.getSchema(),
                coral.getStore(), results, from, select);
            return queryResults;
        }
        catch(SQLException e)
        {
            throw new BackendException("failed to execute query", e);
        }
        finally
        {
            DatabaseUtils.close(results);
            DatabaseUtils.close(stmt);
            DatabaseUtils.close(conn);
        }
    }

    /**
     * Prepares a pre-parsed query.
     * 
     * @param statement the AST node representing a FIND RESOURCE statement.
     * @return query results.
     * @throws MalformedQueryException if the query has semantic errors and thus cannot be executed.
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
     * @param columnMap the map containing ResultColum objects keyed by alias.
     * @param out the buffer to write expression to.
     */
    void appendAttribute(String attribute, final Map<String, ResultColumn<?>> columnMap,
        final StringBuilder out)
        throws MalformedQueryException
    {
        ResultColumnAttribute<?, ?> rca = (ResultColumnAttribute<?, ?>)parseOperand(attribute,
            true, false, columnMap);
        if((rca.getAttribute().getFlags() & AttributeFlags.BUILTIN) == 0)
        {
            rca.getColumn().getQHandler().appendAttributeTerm(out, rca);
        }
        else
        {
            out.append("r").append(rca.getColumn().getIndex()).append(".");
            String columnName = (String)builtinAttrNames.get(rca.getAttribute().getName());
            if(columnName == null)
            {
                columnName = rca.getAttribute().getName();
            }
            out.append(columnName);
        }
    }

    <A> void appendLiteral(AttributeDefinition<A> lhs, String rhs, final StringBuilder out)
    {
        AttributeHandler<A> h = lhs.getAttributeClass().getHandler();
        A value = h.toAttributeValue(rhs);
        out.append(h.toExternalString(value));
    }

    void appendResourceIdTerm(StringBuilder query, ResultColumn<?> rcm)
    {
        query.append("r").append(rcm.getIndex()).append(".resource_id");
    }

    /**
     * Generates a conditional expression based on the query WHERE clause.
     * 
     * @param expr the WHERE clause.
     * @param columnMap the map containing ResultColum objects keyed by alias.
     * @param out the buffer to write expression to.
     */
    private void appendCondition(ASTconditionalExpression expr,
        final Map<String, ResultColumn<?>> columnMap, final StringBuilder out)
        throws MalformedQueryException
    {
        RMLVisitor visitor = new DefaultRMLVisitor()
            {
                public Object visit(ASTnotExpression node, Object data)
                {
                    if(!(node.jjtGetChild(0) instanceof ASTdefinedCondition))
                    {
                        out.append("NOT ");
                    }
                    return visit((SimpleNode)node, data);
                }

                public Object visit(ASTandExpression node, Object data)
                {
                    boolean parens = node.jjtGetParent() instanceof ASTnotExpression;

                    if(parens)
                    {
                        out.append("(");
                    }
                    for(int i = 0; i < node.jjtGetNumChildren(); i++)
                    {
                        node.jjtGetChild(i).jjtAccept(this, data);
                        if(i < node.jjtGetNumChildren() - 1)
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
                    boolean parens = (node.jjtGetParent() instanceof ASTnotExpression)
                        || (node.jjtGetParent() instanceof ASTandExpression);
                    if(parens)
                    {
                        out.append("(");
                    }
                    for(int i = 0; i < node.jjtGetNumChildren(); i++)
                    {
                        node.jjtGetChild(i).jjtAccept(this, data);
                        if(i < node.jjtGetNumChildren() - 1)
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
                        if(node.jjtGetParent() instanceof ASTnotExpression)
                        {
                            out.append(" IS NULL");
                        }
                        else
                        {
                            out.append(" IS NOT NULL");
                        }
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
                        AttributeDefinition<?> lhs = ((ResultColumnAttribute<?, ?>)parseOperand(
                            node.getLHS(), true, false, columnMap)).getAttribute();
                        appendAttribute(node.getLHS(), columnMap, out);
                        String[] ops = { " <> ", " = " };
                        out.append(ops[node.getOperator()]);
                        Object rhs = parseOperand(node.getRHS(), false, false, columnMap);
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            appendAttribute(node.getRHS(), columnMap, out);
                        }
                        if(rhs instanceof ResultColumn)
                        {
                            out.append("r").append(((ResultColumn<?>)rhs).getIndex())
                                .append(".resource_id");
                        }
                        if(rhs instanceof String)
                        {
                            appendLiteral(lhs, (String)rhs, out);
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
                        AttributeDefinition<?> lhs = ((ResultColumnAttribute<?, ?>)parseOperand(
                            node.getLHS(), true, false, columnMap)).getAttribute();
                        appendAttribute(node.getLHS(), columnMap, out);
                        String[] ops = { " < ", " <= ", " >= ", " > " };
                        out.append(ops[node.getOperator()]);
                        Object rhs = parseOperand(node.getRHS(), false, false, columnMap);
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            appendAttribute(node.getRHS(), columnMap, out);
                        }
                        if(rhs instanceof String)
                        {
                            appendLiteral(lhs, (String)rhs, out);
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
                        AttributeDefinition<?> lhs = ((ResultColumnAttribute<?, ?>)parseOperand(
                            node.getLHS(), true, false, columnMap)).getAttribute();
                        if(node.isCaseSensitive())
                        {
                            appendAttribute(node.getLHS(), columnMap, out);
                        }
                        else
                        {
                            out.append(" LOWER(");
                            appendAttribute(node.getLHS(), columnMap, out);
                            out.append(")");
                        }
                        out.append(" LIKE ");
                        Object rhs = parseOperand(node.getRHS(), false, false, columnMap);
                        if(rhs instanceof ResultColumnAttribute)
                        {
                            if(node.isCaseSensitive())
                            {
                                appendAttribute(node.getRHS(), columnMap, out);
                            }
                            else
                            {
                                out.append(" LOWER(");
                                appendAttribute(node.getRHS(), columnMap, out);
                                out.append(")");
                            }
                        }
                        if(rhs instanceof String)
                        {
                            String literal = node.isCaseSensitive() ? (String)rhs : ((String)rhs)
                                .toLowerCase();
                            appendLiteral(lhs, literal, out);
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
