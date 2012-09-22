package org.objectledge.coral.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;

/**
 * Supplements ResourceHandler with methods necessary to transform RML queries to SQL queries
 * 
 * @author rafal
 */
public interface ResourceQueryHandler
{
    void appendResourceIdTerm(StringBuilder query, ResultColumn<?> rcm);

    void appendFromClause(StringBuilder query, ResultColumn<?> rcm,
        Map<String, String> bulitinAttrNames);

    boolean appendWhereClause(StringBuilder query, boolean whereStarted, ResultColumn<?> rcm);

    void appendAttributeTerm(StringBuilder query, ResultColumnAttribute<?, ?> rca);

    /**
     * Describes a column of the query results.
     */
    public class ResultColumn<R extends Resource>
    {
        // instance variables ////////////////////////////////////////////////

        /** The resource class, or <code>null</code> for any. */
        private final ResourceClass<R> rClass;

        private final ResourceQueryHandler queryHandler;

        /** The 1-based index of the column. */
        private int index;

        /** The alias or <code>null</code> for none. */
        private final String alias;

        /** The attributes used in WHERE and ORDER BY clauses. */
        private final List<AttributeDefinition<?>> attributes = new ArrayList<AttributeDefinition<?>>();

        /** Mapping of attribute names to indices. */
        private final Map<AttributeDefinition<?>, Integer> nameIndex = new HashMap<AttributeDefinition<?>, Integer>();

        private final Set<AttributeDefinition<?>> outerAttrs = new HashSet<AttributeDefinition<?>>();

        // initialization ////////////////////////////////////////////////////

        /**
         * Constructs a ResultColumn object.
         * 
         * @param rClass the resource class or null for any.
         * @param alias the alias or null for none.
         */
        public ResultColumn(ResourceClass<R> rClass, String alias)
        {
            this.rClass = rClass;
            this.queryHandler = rClass.getHandler().getQueryHandler();
            this.alias = alias;
        }

        /**
         * Returns the alias.
         * 
         * @return the alias.
         */
        public String getAlias()
        {
            return alias;
        }

        /**
         * Adds an attribute.
         * 
         * @param ad attribute definition
         * @param outer should NULL values of the attribute be accounted for.
         */
        public void addAttribute(AttributeDefinition<?> ad, boolean outer)
        {
            if(outer)
            {
                outerAttrs.add(ad);
            }
            if(!attributes.contains(ad))
            {
                attributes.add(ad);
                nameIndex.put(ad, attributes.size());
            }
        }

        /**
         * Returns the attributes.
         * 
         * @return the attributes.
         */
        public List<AttributeDefinition<?>> getAttributes()
        {
            return attributes;
        }

        /**
         * Returns the index.
         * 
         * @return the index.
         */
        public int getIndex()
        {
            return index;
        }

        /**
         * Sets the index of the column.
         * 
         * @param index
         */
        public void setIndex(int index)
        {
            this.index = index;
        }

        /**
         * Should null value of this attribute be accounted for.
         * 
         * @param ad
         * @return
         */
        public boolean isOuter(AttributeDefinition<?> ad)
        {
            return outerAttrs.contains(ad);
        }

        public int getNameIndex(AttributeDefinition<?> ad)
        {
            return nameIndex.get(ad);
        }

        /**
         * Returns the rClass.
         * 
         * @return the rClass.
         */
        public ResourceClass<R> getRClass()
        {
            return rClass;
        }

        public ResourceQueryHandler getQHandler()
        {
            return queryHandler;
        }

        public static <R extends Resource> ResultColumn<R> newInstance(ResourceClass<R> rc,
            String alias)
        {
            return new ResultColumn<R>(rc, alias);
        }

        @Override
        public String toString()
        {
            StringBuilder attrNames = new StringBuilder();
            Iterator<AttributeDefinition<?>> i = attributes.iterator();
            while(i.hasNext())
            {
                attrNames.append(i.next().getName());
                if(i.hasNext())
                {
                    attrNames.append(", ");
                }
            }
            return alias + "=" + rClass.getName() + "[" + attrNames.toString() + "]";
        }
    }

    /**
     * Helper class binding attribute definition with a result column.
     */
    public class ResultColumnAttribute<R extends Resource, A>
    {
        /**
         * Constructs a ResultColumnAttribute.
         * 
         * @param column the column.
         * @param attribute the attribute.
         */
        public ResultColumnAttribute(ResultColumn<R> column, AttributeDefinition<A> attribute)
        {
            this.column = column;
            this.attribute = attribute;
        }

        /** The column. */
        private final ResultColumn<R> column;

        /** The attribute. */
        private final AttributeDefinition<A> attribute;

        /**
         * Returns the attribute.
         * 
         * @return the attribute.
         */
        public AttributeDefinition<A> getAttribute()
        {
            return attribute;
        }

        /**
         * Returns the column.
         * 
         * @return the column.
         */
        public ResultColumn<R> getColumn()
        {
            return column;
        }

        public static <R extends Resource, A> ResultColumnAttribute<R, A> newInstance(
            ResultColumn<R> rcm, AttributeDefinition<A> ad)
        {
            return new ResultColumnAttribute<R, A>(rcm, ad);
        }

        @Override
        public String toString()
        {
            return column.getAlias() + " = " + column.getRClass().getName() + "["
                + attribute.getName() + "]";
        }
    }

}
