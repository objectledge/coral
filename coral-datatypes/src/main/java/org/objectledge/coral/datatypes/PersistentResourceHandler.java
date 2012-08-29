package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.InstantiationException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;

/**
 * An implementation of {@ResourceHandler} interface using the
 * <code>PersistenceService</code>.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResourceHandler.java,v 1.19 2008-01-01 22:36:16 rafal Exp $
 */
public class PersistentResourceHandler<T extends PersistentResource>
    extends AbstractResourceHandler<T>
{
    // instance variables ////////////////////////////////////////////////////

    /** The persistence. */
    private Persistence persistence;

    /**
     * Constructor.
     * 
     * @param coralSchema the coral schema.
     * @param resourceClass the resource class.
     * @param database the database.
     * @param persistence the persistence.
     * @param instantiator the instantiator.
     * @param cacheFactory the cache factory.
     * @param logger the logger.
     * @throws Exception if there is a problem instantiating an resource object.
     */
    public PersistentResourceHandler(CoralSchema coralSchema, CoralSecurity coralSecurity,
        ResourceClass<T> resourceClass, Database database, Persistence persistence,
        Instantiator instantiator, CacheFactory cacheFactory, Logger logger)
        throws Exception
    {
        super(coralSchema, instantiator, resourceClass, database, cacheFactory, logger);
        this.persistence = persistence;
    }

    // schema operations (not supported) /////////////////////////////////////

    /**
     * Called when an attribute is added to a structured resource class.
     * <p>
     * Concrete resource classes will probably deny this operation by throwing
     * <code>UnsupportedOperationException</code>.
     * </p>
     * 
     * @param attribute the new attribute.
     * @param value the initial value to be set for existing instances of this class.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *        part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod should consider rolling
     *         back the whole transaction.
     * @throws ValueRequiredException if <code>null</code> value was provided for a REQUIRED
     *         attribute.
     */
    public <A> void addAttribute(AttributeDefinition<A> attribute, A value, Connection conn)
        throws ValueRequiredException, SQLException
    {
        boolean tableShouldExist = false;
        for(AttributeDefinition<?> attr : attribute.getDeclaringClass().getDeclaredAttributes())
        {
            if(!attr.equals(attribute) && (attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                tableShouldExist = true;
            }
        }
        Statement stmt = conn.createStatement();
        try
        {
            CoralAttributeMapping repr;
            try
            {
                repr = persistence.load(CoralAttributeMapping.FACTORY, attribute
                    .getAttributeClass().getId());
            }
            catch(PersistenceException e)
            {
                throw new SQLException("failed to retrieve attribute mapping data", e);
            }
            StringBuilder buff = new StringBuilder();
            if(!tableShouldExist)
            {
                buff.append("CREATE TABLE ");
                buff.append(attribute.getDeclaringClass().getDbTable());
                buff.append(" (\n resource_id BIGINT,\n ");
                buff.append(attribute.getName()).append(" ");
                buff.append(repr.getSqlType());
                if((attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
                {
                    buff.append(" NOT NULL");
                }
                buff.append(",\n PRIMARY KEY (resource_id)");
                if(repr.isFk())
                {
                    buff.append(",\n FOREIGN KEY (").append(attribute.getName()).append(") ");
                    buff.append("\n REFERENCES ").append(repr.getFkTable());
                    buff.append("(").append(repr.getFkKeyColumn()).append(")");
                }
                buff.append("\n)");
                stmt.execute(buff.toString());
            }
            else
            {
                buff.append("ALTER TABLE ");
                buff.append(attribute.getDeclaringClass().getDbTable());
                buff.append("\n ADD COLUMN ");
                buff.append(attribute.getName()).append(" ");
                buff.append(repr.getSqlType());
                if((attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
                {
                    buff.append(" NOT NULL");
                }
                stmt.execute(buff.toString());
                if(repr.isFk())
                {
                    buff.setLength(0);
                    buff.append("ALTER TABLE ");
                    buff.append(attribute.getDeclaringClass().getDbTable());
                    buff.append("\n ADD CONSTRAINT fk_")
                        .append(attribute.getDeclaringClass().getDbTable()).append("_")
                        .append(attribute.getName());
                    buff.append(" FOREIGN KEY (").append(attribute.getName()).append(") ");
                    buff.append("\n REFERENCES ").append(repr.getFkTable());
                    buff.append("(").append(repr.getFkKeyColumn()).append(")");
                    stmt.execute(buff.toString());
                }
            }
        }
        finally
        {
            stmt.close();
        }
    }

    /**
     * Called when an attribute is removed from a structured resource class.
     * <p>
     * Concrete resource classes will probably deny this operation by throwing
     * <code>UnsupportedOperationException</code>.
     * </p>
     * 
     * @param attribute the removed attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *        part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod should consider rolling
     *         back the whole transaction.
     */
    public void deleteAttribute(AttributeDefinition<?> attribute, Connection conn)
        throws SQLException
    {
        boolean shouldDropTable = true;
        for(AttributeDefinition<?> attr : attribute.getDeclaringClass().getDeclaredAttributes())
        {
            if(!attr.equals(attribute) && (attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                shouldDropTable = false;
            }
        }
        Statement stmt = conn.createStatement();
        try
        {
            CoralAttributeMapping repr;
            try
            {
                repr = persistence.load(CoralAttributeMapping.FACTORY, attribute
                    .getAttributeClass().getId());
            }
            catch(PersistenceException e)
            {
                throw new SQLException("failed to retrieve attribute mapping data", e);
            }
            StringBuilder buff = new StringBuilder();
            if(shouldDropTable)
            {
                buff.append("DROP TABLE ").append(attribute.getDeclaringClass().getDbTable());
            }
            else
            {
                if(repr.isFk())
                {
                    buff.append("ALTER TABLE ").append(attribute.getDeclaringClass().getDbTable());
                    buff.append("\n DROP CONSTRAINT fk_")
                        .append(attribute.getDeclaringClass().getDbTable()).append("_")
                        .append(attribute.getName());
                    stmt.execute(buff.toString());
                    buff.setLength(0);

                }
                buff.append("ALTER TABLE ").append(attribute.getDeclaringClass().getDbTable())
                    .append("\n DROP COLUMN ").append(attribute.getName());
            }
            stmt.execute(buff.toString());
        }
        finally
        {
            stmt.close();
        }
    }

    /**
     * Called when a parent class is added to a sturctured resource class.
     * <p>
     * Concrete resource classes will probably deny this operation by throwing
     * <code>UnsupportedOperationException</code>.
     * </p>
     * 
     * @param parent the new parent class.
     * @param attributes the initial values of the attributes. Values are keyed with
     *        {@link AttributeDefinition} objects.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *        part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod should consider rolling
     *         back the whole transaction.
     * @throws ValueRequiredException if values for any of parent class REQUIRED attributes are
     *         missing from <code>attributes</code> map.
     */
    public void addParentClass(ResourceClass<?> parent, Map<AttributeDefinition<?>, ?> attributes,
        Connection conn)
        throws ValueRequiredException, SQLException
    {
        // throw new UnsupportedOperationException("schema modifications are not supported"+
        // " for this resource class");
    }

    /**
     * Called when a parent class is removed from a structured resource class.
     * <p>
     * Concrete resource classes will probably deny this operation by throwing
     * <code>UnsupportedOperationException</code>.
     * </p>
     * 
     * @param parent the new parent class.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *        part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod should consider rolling
     *         back the whole transaction.
     */
    public void deleteParentClass(ResourceClass<?> parent, Connection conn)
        throws SQLException
    {
        throw new UnsupportedOperationException("schema modifications are not supported"
            + " for this resource class");
    }

    // implementation ////////////////////////////////////////////////////////

    /**
     * Checks if the passed resource is really a {@link Persistent}. implemenation
     * 
     * @param resource the resource to check.
     */
    protected void checkResource(Resource resource)
    {
        if(!(resource instanceof PersistentResource))
        {
            throw new ClassCastException("PersistenceResourceHanler won't operate on "
                + resource.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Object getData(Resource delegate, Connection conn, Object prev)
        throws SQLException
    {
        try
        {
            Map<Long, Map<ResourceClass<?>, InputRecord>> data;
            if(prev == null)
            {
                data = new HashMap<Long, Map<ResourceClass<?>, InputRecord>>();
            }
            else
            {
                data = (Map<Long, Map<ResourceClass<?>, InputRecord>>)prev;
            }
            if(delegate.getResourceClass().getDbTable() != null)
            {
                data.put(delegate.getIdObject(), getInputRecords(delegate));
            }
            return data;
        }
        catch(PersistenceException e)
        {
            throw new SQLException("failed to retrieve data", e);
        }
        catch(org.objectledge.coral.InstantiationException e)
        {
            throw (SQLException)new SQLException("failed to instantiate helper instance")
                .initCause(e);
        }
    }

    private Map<ResourceClass<?>, InputRecord> getInputRecords(Resource delegate)
        throws SQLException, PersistenceException, InstantiationException
    {
        Map<ResourceClass<?>, InputRecord> map = new HashMap<ResourceClass<?>, InputRecord>();
        map.put(delegate.getResourceClass(), getInputRecord(delegate, delegate.getResourceClass()));
        for(ResourceClass<?> parentClass : delegate.getResourceClass().getParentClasses())
        {
            if(parentClass.getDbTable() != null)
            {
                map.put(parentClass, getInputRecord(delegate, parentClass));
            }
        }
        return map;
    }

    private InputRecord getInputRecord(Resource delegate, final ResourceClass<?> rClass)
        throws SQLException, PersistenceException, InstantiationException
    {
        List<InputRecord> irs = persistence.loadInputRecords(retrievePersistentView(rClass), "resource_id = ?",
            delegate.getId());
        if(irs.isEmpty())
        {
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & AttributeFlags.REQUIRED) != 0)
                {
                    throw new SQLException("missing data for "
                        + delegate.getResourceClass().getName() + " WHERE resource_id = "
                        + delegate.getIdString());
                }
            }
            return null;
        }
        return irs.get(0);
    }

    private Persistent retrievePersistentView(final ResourceClass<?> rClass)
    {
        return new Persistent()
            {
                @Override
                public String getTable()
                {
                    return rClass.getDbTable();
                }

                @Override
                public String[] getKeyColumns()
                {
                    return PersistentResource.KEY_COLUMNS;
                }

                @Override
                public void getData(OutputRecord record)
                    throws PersistenceException
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setData(InputRecord record)
                    throws PersistenceException
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean getSaved()
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setSaved(long id)
                {
                    throw new UnsupportedOperationException();
                }
            };
    }

    /**
     * {@inheritDoc}
     */
    protected Object getData(ResourceClass<?> rc, Connection conn)
        throws SQLException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Object getData(Connection conn)
        throws SQLException
    {
        return null;
    }

    /**
     * @return Returns the persistence.
     */
    Persistence getPersistence()
    {
        return persistence;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getFallbackResourceImplClass()
    {
        return PersistentResource.class;
    }
}
