//
// Copyright (c) 2003, 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.
// nor the names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
// OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
//
package org.objectledge.coral.relation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.PreloadingParticipant;
import org.objectledge.coral.entity.AmbigousEntityNameException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityRegistry;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistentFactory;

/**
 * Implmentation of the Coral relation manager component.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralRelationManagerImpl.java,v 1.10 2005-01-26 08:05:26 rafal Exp $
 */
public class CoralRelationManagerImpl
    implements CoralRelationManager, PreloadingParticipant
{
    /** The {@link PersistenceService}. */
    private Persistence persistence;

    /** The logger. */
    private Logger log;

    /** The event hub. */
    private CoralEventHub coralEventHub;

    /** The component hub. */
    private CoralCore coral;

    private Map relationCache;
    private PersistentFactory relationFactory;
    private EntityRegistry relationRegistry;

    /**
     * Relation manager manages relation lifecycle.
     * 
     * @param persistence used to modify relation metadata
     * @param cacheFactory used to create caches for relation objects
     * @param coralEventHub TODO may be used to clean relations on resource deletions
     * @param coral used to get coral facilities
     * @param instantiator used to instantiate relation objects
     * @param log for loggin errors and alike
     * 
     * @throws ConfigurationException on problems with configuration values
     */
    public CoralRelationManagerImpl(Persistence persistence, CacheFactory cacheFactory, 
        CoralEventHub coralEventHub, CoralCore coral, Instantiator instantiator, Logger log)
        throws ConfigurationException
    {
        this.persistence = persistence;
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        this.log = log;

        this.relationCache = new WeakHashMap();
        this.relationFactory = instantiator.getPersistentFactory(RelationImpl.class);
        this.relationRegistry = new EntityRegistry(persistence, cacheFactory, instantiator, log, 
			"relation", RelationImpl.class);
    }

    /**
     * {@inheritDoc}
     */
    public Relation[] getRelation()
    {
        Set all = relationRegistry.get();
        Relation[] result = new Relation[all.size()];
        all.toArray(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Relation getRelation(long id) throws EntityDoesNotExistException
    {
        return (Relation)relationRegistry.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public Relation getRelation(String name)
        throws EntityDoesNotExistException
    {
        try
        {
            return (Relation)relationRegistry.getUnique(name);
        }
        catch(AmbigousEntityNameException e)
        {
            throw new BackendException("integrity constraints corrupted", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Relation createRelation(String name) throws EntityExistsException
    {
        RelationImpl relation =
            new RelationImpl(persistence, coral.getStore(), this, name);
        relationRegistry.addUnique(relation);
        return relation;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(Relation item, String name)
        throws EntityExistsException
    {
        relationRegistry.renameUnique(item, name);
    }

    /**
     * {@inheritDoc}
     */
    public void updateRelation(Relation relation, RelationModification modification)
    {
        synchronized (relation)
        {
            MinimalRelationModification minimalMod =
                new MinimalRelationModification(modification, relation);
            if (relation.isInverted())
            {
                relation = relation.getInverted();
            }
            RelationImpl relationImpl = (RelationImpl) relation;

            boolean shouldCommit = false;
			Connection conn = null;
            try
            {
                shouldCommit = persistence.getDatabase().beginTransaction();

				conn = persistence.getDatabase().getConnection();

                // update db and in memory relation representation
                
                if (minimalMod.getClear())
                {
                    // db update
                    Statement stmt = conn.createStatement();
                    stmt.execute(" DELETE FROM " + relationImpl.getDataTable()
                            + " WHERE relation_id = " + relationImpl.getIdString());

                    // memory update
                    relationImpl.clear();
                }

                PreparedStatement pstmt = conn.prepareStatement(
							" DELETE FROM " + relationImpl.getDataTable()
                            + " WHERE relation_id = " + relationImpl.getIdString()
                            + " AND resource1 = ? AND resource2 = ?");
                long[][] data = minimalMod.getRemoved();
                if (data.length > 0)
                {
                    for (int i = 0; i < data.length; i++)
                    {
						// db update
                        pstmt.setLong(1, data[i][0]);
                        pstmt.setLong(2, data[i][1]);
                        pstmt.addBatch();

						// memory update
                        relationImpl.remove(data[i][0], data[i][1]);
                    }
					// db update
                    pstmt.executeBatch();
                }

                pstmt = conn.prepareStatement(
                        "INSERT INTO " + relationImpl.getDataTable() 
                        + "(relation_id, resource1, resource2) VALUES (" + 
                        relationImpl.getIdString() + ", ?, ?)");
                data = minimalMod.getAdded();
                if (data.length > 0)
                {
                    for (int i = 0; i < data.length; i++)
                    {
                        // db update
                        pstmt.setLong(1, data[i][0]);
                        pstmt.setLong(2, data[i][1]);
                        pstmt.addBatch();

                        // memory update
                        relationImpl.add(data[i][0], data[i][1]);
                    }
					// db update
                    pstmt.executeBatch();
                }

                persistence.getDatabase().commitTransaction(shouldCommit);
            }
            catch (BackendException ex)
            {
                try
                {
                    persistence.getDatabase().rollbackTransaction(shouldCommit);
                }
                catch (SQLException ee)
                {
                    log.error("rollback failed", ee);
                }
                throw ex;
            }
            catch (Exception e)
            {
                try
                {
                    persistence.getDatabase().rollbackTransaction(shouldCommit);
                }
                catch (SQLException ee)
                {
                    log.error("rollback failed", ee);
                }
                throw new BackendException("failed to update relation", e);
            }
            finally
            {
            	DatabaseUtils.close(conn);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteRelation(Relation relation) throws IllegalArgumentException
    {
		if (relation.isInverted())
		{
			relation = relation.getInverted();
		}
		RelationImpl relationImpl = (RelationImpl) relation;

		Connection conn = null;
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();

			conn = persistence.getDatabase().getConnection();

            // delete relation contents
			Statement stmt = conn.createStatement();
			stmt.execute(
				"DELETE FROM "+relationImpl.getDataTable()
				+" WHERE relation_id = "+relationImpl.getIdString()
			);

            relationRegistry.delete(relation);

            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(BackendException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to delete relation", e);
        }
		finally
		{
			DatabaseUtils.close(conn);
		}
    }
    
    // definitions ///////////////////////////////////////////////////////////////////////////////
    
    private Map<Long,long[]> preloadedData = new HashMap<Long,long[]>();
    
    private void preloadDefinitions()
        throws SQLException
    {
        Connection conn = persistence.getDatabase().getConnection();
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(
            "SELECT relation_id, resource1, resource2 FROM coral_relation_data " +
            "ORDER BY relation_id"
        );
        if(result.next())
        {
            List<Long> temp = new ArrayList<Long>();
            long lastId;
            do
            {
                do
                {
                    lastId = result.getLong(1);
                    temp.add(result.getLong(2));
                    temp.add(result.getLong(3));
                }
                while(result.next() && result.getLong(1) == lastId);
                long[] longs = new long[temp.size()];
                int i = 0;
                for(long item : temp)
                {
                    longs[i++] = item;
                }
                preloadedData.put(lastId, longs);
                temp.clear();
            }
            while(!result.isAfterLast());
        }
    }
    
    private long[] loadDefinition(long relationId)
        throws SQLException
    {
        Connection conn = persistence.getDatabase().getConnection();
        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery(
            "SELECT resource1, resource2 FROM coral_relation_data WHERE relation_id = " + 
            Long.toString(relationId)
        );
        List<Long> temp = new ArrayList<Long>();
        while(result.next())
        {
            temp.add(result.getLong(1));
            temp.add(result.getLong(1));
        }
        long[] longs = new long[temp.size()];
        int i = 0;
        for(long item : temp)
        {
            longs[i++] = item;
        }
        return longs;
    }

    /**
     * {@inheritDoc}
     */
    public long[] getRelationDefinition(Relation relation)
    {
        if(preloadedData.containsKey(relation.getIdObject()))
        {
             return preloadedData.get(relation.getIdObject());
        }
        else
        {
            try
            {
                return loadDefinition(relation.getId());
            }
            catch(SQLException e)
            {
                throw new BackendException("failed to load relation data", e);
            }
        }
    }

    // preloading ////////////////////////////////////////////////////////////////////////////////

    private static final int[] PRELOADING_PHASES = { 5 };
    
    /**
     * {@inheritDoc}
     */
    public int[] getPhases()
    {
        return PRELOADING_PHASES;
    }

    /**
     * {@inheritDoc}
     */
    public void preloadData(int phase)
        throws Exception
    {
        log.info("preloading relation definitions");
        long time = System.currentTimeMillis();
        preloadDefinitions();
        log.info("preloading relation definitions: done in "+
            (System.currentTimeMillis()-time)+"ms");

        log.info("building relation objects");
        time = System.currentTimeMillis();
        relationRegistry.get();
        log.info("building relation objects: done in "+
            (System.currentTimeMillis()-time)+"ms");
    }
}