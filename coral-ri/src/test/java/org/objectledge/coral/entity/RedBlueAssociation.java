// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral.entity;

import java.sql.SQLException;

import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;


class RedBlueAssociation
    extends AbstractAssociation
{
    private static final String TABLE = "red_blue_association";
    
    private static final String[] KEY_COLUMNS = { "red_id", "blue_id" };

    private Persistence persistence;
    
    private RedBlueEntityFactory redBlueEntityFactory;
    
    private RedEntity red;
    
    private BlueEntity blue;

    public RedBlueAssociation(Persistence persistence, RedBlueEntityFactory redBlueEntityFactory)
    {
        this.persistence = persistence;
        this.redBlueEntityFactory = redBlueEntityFactory;
    }
    
    public RedBlueAssociation(Persistence persistence, RedBlueEntityFactory redBlueEntityFactory, 
        RedEntity red, BlueEntity blue)
    {
        this.persistence = persistence;
        this.redBlueEntityFactory = redBlueEntityFactory;
        this.red = red;
        this.blue = blue;
    }

    /** 
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return getClass().hashCode() ^ hashCode(red.getId()) ^ hashCode(blue.getId());
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {
        if(obj == null || !getClass().isAssignableFrom(obj.getClass()))
        {
            return false;
        }
        RedBlueAssociation other = (RedBlueAssociation)obj;
        return other.red.equals(red) && other.blue.equals(blue);
    }

    /** 
     * {@inheritDoc}
     */
    public String getTable()
    {
        return TABLE;
    }

    /** 
     * {@inheritDoc}
     */
    public String[] getKeyColumns()
    {
        return KEY_COLUMNS;
    }

    /** 
     * {@inheritDoc}
     */
    public void getData(OutputRecord record) throws SQLException
    {
        record.setLong("red_id", red.getId());
        record.setLong("blue_id", blue.getId());
    }

    /** 
     * {@inheritDoc}
     */
    public void setData(InputRecord record) throws SQLException
    {
        setRed(redBlueEntityFactory.getRed(record.getLong("red_id")));
        setBlue(redBlueEntityFactory.getBlue(record.getLong("blue_id")));
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    public RedEntity getRed()
    {
        return red;
    }
    
    public BlueEntity getBlue()
    {
        return blue;
    }
    
    public void setRed(RedEntity red)
    {
        this.red = red;
    }
    
    public void setBlue(BlueEntity blue)
    {
        this.blue = blue;
    }
}