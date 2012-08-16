package org.objectledge.coral.tools.transform;

import java.sql.Connection;
import java.sql.SQLException;

import org.jcontainer.dna.Logger;
import org.objectledge.filesystem.FileSystem;

public interface TransformationComponent
{
    void run(Connection sourceConn, Connection targetConn, FileSystem fileSystem, Logger log)
        throws SQLException;
}
