// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//  
// * Redistributions of source code must retain the above copyright notice,  
//	 this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//	 this list of conditions and the following disclaimer in the documentation  
//	 and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//	 nor the names of its contributors may be used to endorse or promote products  
//	 derived from this software without specific prior written permission. 
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
package org.objectledge.coral.tools;

import javax.sql.DataSource;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.tags.sql.DataSourceWrapper;
import org.apache.maven.jelly.MavenJellyContext;
import org.jcontainer.dna.Configuration;
import org.objectledge.configuration.ConfigurationFactory;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.xml.XMLGrammarCache;
import org.objectledge.xml.XMLValidator;

/**
 * Initializes a DataSource based on ObjectLedge configuration.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: DataSourceTag.java,v 1.1 2005-02-16 16:20:50 rafal Exp $
 */
public class DataSourceTag
    extends CoralPluginTag
{
    private String variable;
    
    /**
     * {@inheritDoc}
     */
    public void doTag(XMLOutput out) 
        throws MissingAttributeException, JellyTagException
    {
        checkAttribute(variable, "variable");
        DataSource dataSource;
        MavenJellyContext pluginContext = getPluginContext();
        if(pluginContext.getVariable("coral.db.url") != null)
        {
            dataSource = getPropertiesDataSource();
        }
        else
        {
            dataSource = getConfigDataSource();
        }
        getContext().setVariable(variable, dataSource);
    }

    /**
     * Sets the var.
     *
     * @param var The var to set.
     */
    public void setVariable(String var)
    {
        this.variable = var;
    }

    private DataSource getConfigDataSource()
        throws JellyTagException
    {
        MavenJellyContext pluginContext = getPluginContext();
        String ledgeBasedir = (String)pluginContext.getVariable("ledge.basedir");
        String dbKey = (String)pluginContext.getVariable("coral.db.key");
        dbKey = dbKey == null ? "org.objectledge.database.XaPoolDataSource" : dbKey;
        String dbImpl = (String)pluginContext.getVariable("coral.db.impl");
        dbImpl = dbImpl == null ? "org.objectledge.database.XaPoolDataSource" : dbImpl;
        try
        {
            FileSystem fileSystem = FileSystem.getStandardFileSystem(ledgeBasedir);
            XMLGrammarCache xmlGrammarCache = new XMLGrammarCache();
            XMLValidator xmlValidator = new XMLValidator(xmlGrammarCache);
            ConfigurationFactory configurationFactory = new ConfigurationFactory(fileSystem,
                xmlValidator, "/config");
            Class dbImplClass = Class.forName(dbImpl);
            Configuration config = configurationFactory.getConfig(dbKey, dbImplClass);
            
            Configuration connection = config.getChild("connection");
            String dbUrl = connection.getChild("url").getValue();
            String dbUser = connection.getChild("user").getValue();
            String dbPass = connection.getChild("password").getValue();
            return newDataSource(dbUrl, dbUser, dbPass);
        }
        catch(Exception e)
        {
            throw new JellyTagException("failed to initialize dataSource", e);
        }
    }
    
    private DataSource getPropertiesDataSource()
        throws MissingAttributeException, JellyTagException
    {
        String coralDbUrl = (String)getPluginContext().getVariable("coral.db.url");
        String coralDbUser = (String)getPluginContext().getVariable("coral.db.user");
        String coralDbPassword = (String)getPluginContext().getVariable("coral.db.password");
        return newDataSource(coralDbUrl, coralDbUser, coralDbPassword);
    }

    /**
     * @param coralDbUrl
     * @param coralDbUser
     * @param coralDbPassword
     * @return a DataSource
     * @throws JellyTagException
     */
    private DataSource newDataSource(String coralDbUrl, String coralDbUser, String coralDbPassword)
        throws JellyTagException
    {
        String coralDbDriver = getDriver(coralDbUrl);
        DataSourceWrapper dataSource = new DataSourceWrapper();
        try
        {
            dataSource.setDriverClassName(coralDbDriver);
            dataSource.setJdbcURL(coralDbUrl);
            dataSource.setUserName(coralDbUser);
            dataSource.setPassword(coralDbPassword);
            return dataSource;
        }
        catch(Exception e)
        {
            throw new JellyTagException("failed to initialize database driver", e); 
        }
    }

    private String getDriver(String url)
        throws JellyTagException
    {
        String dbType = url.substring(5, url.indexOf(':', 5));
        String dbDriver = (String)getPluginContext().getVariable("coral.driver."+ dbType);
        if(dbDriver == null)
        {
            throw new JellyTagException("Unknown database type "+dbType);
        }
        return dbDriver;
    }
}
