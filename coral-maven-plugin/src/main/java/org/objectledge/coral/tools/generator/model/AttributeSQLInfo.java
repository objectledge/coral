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
package org.objectledge.coral.tools.generator.model;

/**
 * A holder of SQL attribute information. 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeSQLInfo.java,v 1.2 2004-12-21 08:45:30 rafal Exp $
 */
public class AttributeSQLInfo
{
    private String externalTable;
    private String externalTableKey;
    private String sqlType;
    
    /**
     * Creates new SQLAttribute instance.
     * 
     * @param sqlType the SQL type
     * @param externalTable the SQL table
     * @param externalTableKey the SQL table key
     */
    public AttributeSQLInfo(String sqlType, String externalTable, String externalTableKey)
    {
        this.sqlType = sqlType;
        this.externalTable = externalTable;
        this.externalTableKey = externalTableKey;
    }

    /**
     * Checks if the attribute is represented internally.
     * 
     * @return <code>true</code> if the attribute is represented internally.
     */
    public boolean isInternal()
    {
        return sqlType != null && sqlType.length() > 0;
    }
    
    /**
     * Returns the sqlType.
     *
     * @return the sqlType.
     */
    public String getInternalType()
    {
        return sqlType;
    }
    
    /**
     * Returns the externalTable.
     *
     * @return the externalTable.
     */
    public String getExternalTable()
    {
        return externalTable;
    }
    
    /**
     * Returns the entityKey.
     *
     * @return the entityKey.
     */
    public String getExternalTableKey()
    {
        return externalTableKey;
    }    
}
