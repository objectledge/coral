package org.objectledge.coral.tools.refcheck;

import static java.lang.String.format;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.tools.transform.GenericToTabular;
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

    private String nullAttrValues;

    private String attrCheckList;

    private String attrCheckMatch;

    private String attrCheckMatchMulti;

    private String attrCheckMatch2;

    private String attrCheckMatchMulti2;

    private String orphanedGenericResource;

    private String resourceDomains;

    private String resourceDomainsGenericCheck;

    private String resourceDomainsTabularCheck;

    private String tabularAttributeLocations;

    private String tabularReqAttrClasses;

    private String tabularReqAttrResources;

    // fixes

    private StringWriter fixesStringWriter;

    private PrintWriter fixes;

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
        fixesStringWriter = new StringWriter();
        fixes = new PrintWriter(fixesStringWriter);
    }

    public void run()
        throws SQLException
    {
        checkGenericRequiredAttributes();
        checkGenericNullAttributeValues();
        checkGenericAttributes();
        checkSharedGenericAttributes();
        checkOrphanedGenericResourceRecords();
        checkResourceReferenceDomain();
        checkTabularRequiredAttributes();
        fixes.flush();
        System.out.println(fixesStringWriter.toString());
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
                            String query = format(genericRequiredData, attrId, attrTable, fkColumn,
                                descClassId, attrId, fkColumn);
                            log.info("  checking attribute " + attrName + " in class " + className);
                            ResultSet rset2 = stmt2.executeQuery(query);
                            try
                            {
                                while(rset2.next())
                                {
                                    final long resId = rset2.getLong(1);
                                    final long attrDefId = rset2.getLong(2);
                                    final long dataKey = rset2.getLong(3);
                                    final String msg = format(
                                        "missing required attribute resource_id=%d attribute_defnition_id=%d (%s) data_key=%d",
                                        resId, attrDefId, attrName, dataKey);
                                    log.error(msg);
                                    fixes.println("-- " + msg);
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

    private void checkGenericNullAttributeValues()
        throws SQLException
    {
        log.info("checking null attribute values");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(nullAttrValues);
                try
                {
                    while(rset1.next())
                    {
                        String table = rset1.getString(1);
                        String column = rset1.getString(2);

                        String query = format("SELECT data_key FROM %s WHERE %s IS NULL", table,
                            column);
                        log.info("  checking table " + table);
                        ResultSet rset2 = stmt2.executeQuery(query);
                        try
                        {
                            while(rset2.next())
                            {
                                final long dataKey = rset2.getLong(1);
                                log.error(format("null value in %s data_key = %d", table, dataKey));
                                fixes.println(format("DELETE FROM %s WHERE data_key = %d;", table,
                                    dataKey));
                            }
                        }
                        finally
                        {
                            rset2.close();
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

    private String getTabularAttributeValueQuery(long attrClassId, boolean includeResourceId)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        try
        {
            ResultSet rset = stmt.executeQuery(format(tabularAttributeLocations, attrClassId));
            try
            {
                StringBuilder buff = new StringBuilder();
                while(rset.next())
                {
                    String column = rset.getString(1);
                    String table = rset.getString(2);
                    long attrDefId = rset.getLong(3);
                    buff.append("   union all select\n");
                    if(includeResourceId)
                    {
                        buff.append("     resource_id,\n");
                    }
                    buff.append("     ").append(GenericToTabular.transformColumnName(column))
                        .append(",\n");
                    buff.append("     ").append(attrDefId).append(" attribute_definition_id\n");
                    buff.append("   from ").append(GenericToTabular.transformTableName(table))
                        .append("\n");
                    buff.append("   where ").append(GenericToTabular.transformColumnName(column))
                        .append(" is not null\n");
                }
                return buff.toString();
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
    }

    private void checkGenericAttributes()
        throws SQLException
    {
        log.info("checking attribute value to generic attribute matching");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(attrCheckList);
                // System.out.println(attrCheckList);
                try
                {
                    while(rset1.next())
                    {
                        long attrClass = rset1.getLong(1);
                        String table = rset1.getString(2);
                        String column = rset1.getString(3);
                        boolean isMulti = rset1.getBoolean(4);

                        String query;
                        final boolean resourceList = table.contains("list");
                        query = format(resourceList ? attrCheckMatchMulti : attrCheckMatch,
                            attrClass, isMulti ? getTabularAttributeValueQuery(attrClass, false)
                                : "", column, table);
                        log.info("  checking table " + table);
                        // System.out.println(query);
                        ResultSet rset2 = stmt2.executeQuery(query);
                        try
                        {
                            while(rset2.next())
                            {
                                if(!resourceList || (rset2.getLong(1) == 0 && rset2.wasNull()))
                                {
                                    log.error(format("coral_generic_resource.data_key=%d %s.%s=%d",
                                        rset2.getLong(1), table, column, rset2.getLong(2)));
                                    if(rset2.getLong(1) == 0 && rset2.wasNull())
                                    {
                                        fixes.println(format("DELETE FROM %s WHERE %s = %d;",
                                            table, column, rset2.getLong(2)));
                                    }
                                    else
                                    {
                                        fixes
                                            .println(format(
                                                "DELETE FROM coral_generic_resource WHERE data_key = %d AND attribute_definition_id = %d;",
                                                rset2.getLong(1), rset2.getLong(3)));
                                    }
                                }
                            }
                        }
                        finally
                        {
                            rset2.close();
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

    private void checkSharedGenericAttributes()
        throws SQLException
    {
        log.info("checking null attribute values");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(attrCheckList);
                // System.out.println(attrCheckList);
                try
                {
                    while(rset1.next())
                    {
                        long attrClass = rset1.getLong(1);
                        String table = rset1.getString(2);
                        String column = rset1.getString(3);
                        boolean isMulti = rset1.getBoolean(4);

                        String query;
                        final boolean resourceList = table.contains("list")
                            || table.contains("parameters");
                        query = format(resourceList ? attrCheckMatchMulti2 : attrCheckMatch2,
                            attrClass, isMulti ? getTabularAttributeValueQuery(attrClass, true)
                                : "", column, table);
                        log.info("  checking table " + table);
                        // System.out.println(query);
                        ResultSet rset2 = stmt2.executeQuery(query);
                        try
                        {
                            while(rset2.next())
                            {

                                final String msg = format(
                                    "%s.%s=%d is joined with %d coral_generic_resource_records",
                                    table, column, rset2.getLong(1), rset2.getInt(2));
                                log.error(msg);
                                fixes.println("-- " + msg);
                            }
                        }
                        finally
                        {
                            rset2.close();
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

    private void checkOrphanedGenericResourceRecords()
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        try
        {
            ResultSet rset = stmt.executeQuery(orphanedGenericResource);
            try
            {
                while(rset.next())
                {
                    final long dataKey = rset.getLong(1);
                    log.error(format(
                        "coral_generic_resource.resource_id=%d does not have corresponding coral_resource",
                        dataKey));
                    fixes.println(format(
                        "DELETE FROM coral_generic_resource WHREE resource_id = %d;", dataKey));
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
    }

    private void checkResourceReferenceDomain()
        throws SQLException
    {
        log.info("checking resource reference attributes domain constraints");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(resourceDomains);
                try
                {
                    while(rset1.next())
                    {
                        long classId = rset1.getLong(1);
                        String attrDefClass = rset1.getString(2);
                        long attributeId = rset1.getLong(3);
                        String attrName = rset1.getString(4);
                        String domain = rset1.getString(5);
                        long domainClassId = rset1.getLong(6);
                        if(domainClassId == 0 && rset1.wasNull())
                        {
                            final String msg = format(
                                "%s.%s attribute has invalid domain %s thad does not correspond to an exiting resource class",
                                attrDefClass, attrName, domain);
                            log.error(msg);
                            fixes.println("-- " + msg);
                        }
                        else
                        {
                            log.info(format("  checking %s.%s resource(%s)", attrDefClass,
                                attrName, domain));
                            String attrClasses = mkString(hierarchy.get(classId), "(", ", ", ")");
                            String domClasses = mkString(hierarchy.get(domainClassId), "(", ", ",
                                ")");
                            String query = format(resourceDomainsGenericCheck, attrClasses,
                                attributeId, domClasses);

                            ResultSet rset2 = stmt2.executeQuery(query);
                            try
                            {
                                while(rset2.next())
                                {
                                    long resId = rset2.getLong(1);
                                    long resId2 = rset2.getLong(4);
                                    String resClass2 = classNames.get(rset2.getLong(5));
                                    log.error(format(
                                        "resource_id = %d attribute %s is %d of type %s not compatible with %s",
                                        resId, attrName, resId2, resClass2, domain));
                                }
                            }
                            finally
                            {
                                rset2.close();
                            }

                            if(rset1.getString(7) != null)
                            {
                                query = format(resourceDomainsTabularCheck, attributeId,
                                    GenericToTabular.transformTableName(classNames.get(classId)),
                                    GenericToTabular.transformColumnName(attrName), domClasses);
                                // System.out.println(query);

                                rset2 = stmt2.executeQuery(query);
                                try
                                {
                                    while(rset2.next())
                                    {
                                        long resId = rset2.getLong(1);
                                        long resId2 = rset2.getLong(4);
                                        String resClass2 = classNames.get(rset2.getLong(5));
                                        log.error(format(
                                            "resource_id = %d attribute %s is %d of type %s not compatible with %s",
                                            resId, attrName, resId2, resClass2, domain));
                                    }
                                }
                                finally
                                {
                                    rset2.close();
                                }
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

    private void checkTabularRequiredAttributes()
        throws SQLException
    {
        log.info("checking tabular resources consistency");
        Statement stmt1 = conn.createStatement();
        try
        {
            Statement stmt2 = conn.createStatement();
            try
            {
                ResultSet rset1 = stmt1.executeQuery(tabularReqAttrClasses);
                try
                {
                    while(rset1.next())
                    {
                        long classId = rset1.getLong(1);
                        String tableName = rset1.getString(2);
                        for(long subClassId : hierarchy.get(classId))
                        {
                            final String subclassName = classNames.get(subClassId);
                            log.info("  checking class "
                                + subclassName
                                + (subClassId != classId ? (" for superclass " + classNames
                                    .get(classId)) : ""));

                            String query = format(tabularReqAttrResources, tableName, subClassId);
                            // System.out.println(query);
                            ResultSet rset2 = stmt2.executeQuery(query);
                            try
                            {
                                while(rset2.next())
                                {
                                    long resId = rset2.getLong(1);
                                    final String msg = format(
                                        "row is missing from %s for resource_id=%d of type %s",
                                        tableName, resId, subclassName);
                                    log.error(msg);
                                    fixes.println("-- " + msg);
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

    private <T> String mkString(Collection<T> coll, String pre, String sep, String post)
    {
        StringBuilder buff = new StringBuilder();
        Iterator<T> i = coll.iterator();
        buff.append(pre);
        while(i.hasNext())
        {
            buff.append(i.next());
            if(i.hasNext())
            {
                buff.append(sep);
            }
        }
        buff.append(post);
        return buff.toString();
    }
}
