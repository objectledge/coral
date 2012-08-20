package org.objectledge.coral.tools.refcheck;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Deque;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jcontainer.dna.Logger;
import org.objectledge.database.DatabaseType;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.pico.inject.FileSystemResourceInjector;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Checks integrity constraints that are not enforced on the database level.
 */
public class IntegrityChecker
{
    private final Connection conn;

    private final DatabaseType dbType;

    private final Logger log;

    private SetMultimap<Long, Long> hierarchy;
    
    private SortedMap<Long, String> classNames;

    // injected queries

    private String genericRequiredAttr;

    private String genericRequiredData;

    private String classHierarchy;

    public IntegrityChecker(Connection conn, FileSystem fileSystem, Logger log)
        throws SQLException
    {
        this.conn = conn;
        this.log = log;
        this.dbType = DatabaseType.detect(conn);
        FileSystemResourceInjector.inject(fileSystem, this,
            String.format(".%s.sql", dbType.getSuffix()), ".sql");
        hierarchy = getResourceClassesHierarchy();
        classNames = getResourceClassNames();
    }

    public void run()
        throws SQLException
    {
        checkGenericRequiredAttributes();
    }

    private void checkGenericRequiredAttributes()
        throws SQLException
    {
        log.info("checking required attributes in Generic resources");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(genericRequiredAttr);
                try
                {
                    while(rset1.next())
                    {
                        long classId = rset1.getLong(1);
                        long attrId = rset1.getLong(2);
                        String attrName = rset1.getString(3);
                        String attrTable = rset1.getString(4);
                        String fkColumn = rset1.getBoolean(6) ? rset1.getString(5) : "data_key";
                        for(Long descClassId : hierarchy.get(classId))
                        {
                            String className = classNames.get(descClassId);
                            String query = String.format(genericRequiredData, attrId, attrTable,
                                fkColumn, descClassId, attrId, fkColumn);
                            log.info("  checking attribute " + attrName + " in class " + className);
                            ResultSet rset2 = stmt2.executeQuery(query);
                            try
                            {
                                while(rset2.next())
                                {
                                    log.error("missing required attribute resource_id="
                                        + rset2.getLong(1) + " attribute_defnition_id="
                                        + rset2.getLong(2) + " data_key=" + rset2.getLong(3));
                                }
                            }
                            finally
                            {
                                rset2.close();
                            }
                        }
                    }
                }
                finally
                {
                    rset1.close();
                }
            }
            finally
            {
                stmt2.close();
            }
        }
        finally
        {
            stmt1.close();
        }
    }

    private SetMultimap<Long, Long> getResourceClassesHierarchy()
        throws SQLException
    {
        SetMultimap<Long, Long> direct = HashMultimap.create();
        Statement stmt = conn.createStatement();
        try
        {
            ResultSet rset = stmt.executeQuery(classHierarchy);
            try
            {
                while(rset.next())
                {
                    direct.put(rset.getLong(1), rset.getLong(2));
                }
            }
            finally
            {
                rset.close();
            }
        }
        finally
        {
            stmt.close();
        }
        SetMultimap<Long, Long> indirect = HashMultimap.create();
        Deque<Long> stack = new LinkedList<Long>();
        for(Long parent : direct.keySet())
        {
            stack.addLast(parent);
            while(!stack.isEmpty())
            {
                Long current = stack.removeLast();
                for(Long descendant : direct.get(current))
                {
                    indirect.put(current, descendant);
                    if(!descendant.equals(current))
                    {
                        stack.addLast(descendant);
                    }
                }
            }
        }
        return indirect;
    }

    private SortedMap<Long, String> getResourceClassNames()
        throws SQLException
    {
        SortedMap<Long, String> map = new TreeMap<Long, String>();
        Statement stmt = conn.createStatement();
        try
        {
            ResultSet rset = stmt
                .executeQuery("SELECT resource_class_id, name FROM coral_resource_class");
            try
            {
                while(rset.next())
                {
                    map.put(rset.getLong(1), rset.getString(2));
                }
            }
            finally
            {
                rset.close();
            }
        }
        finally
        {
            stmt.close();
        }
        return map;
    }

    private void checkGenericScalarAttributes()
    {

    }

    private void checkMultivaluedAttributes()
    {

    }
}
