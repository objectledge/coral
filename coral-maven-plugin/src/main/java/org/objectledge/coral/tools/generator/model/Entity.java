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
 * Represents a Coral Entity.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: Entity.java,v 1.2 2004-03-24 14:40:07 fil Exp $
 */
public class Entity
    implements Comparable
{
    // variables ////////////////////////////////////////////////////////////////////////////////
    
    /** the name of the entity. */
    protected String name;
    
    /** the flags of the entity. */
    protected int flags;

    // constructors /////////////////////////////////////////////////////////////////////////////

    /**
     * Protected no-arg constructor, to allow mocking.
     */
    protected Entity()
    {
    }

    /**
     * Creates entity instance.
     * 
     * @param name entity name.
     * @param flags entity flags.
     */
    protected Entity(String name, int flags)
    {
        this.name = name;
        this.flags = flags;        
    }
    
    // name /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns entity name.
     * 
     * @return entity name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets entity name.
     * 
     * @param name entity name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    // flags ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns entity flags.
     * 
     * @return entity flags.
     */
    public int getFlags()
    {
        return flags;
    }

    /**
     * Checks if the entitity has all the requested flags set.
     * 
     * @param requested bit sum of requested flags.
     * @return <code>true</code> if all the flags are set for the entity.
     */
    public boolean hasFlags(int requested)
    {
        return (flags & requested) == requested;
    }

    /**
     * Sets entity flags.
     * 
     * @param flags entity flags.
     */
    public void setFlags(int flags)
    {
        this.flags = flags;
    }

    // comparing, hashing & equality ////////////////////////////////////////////////////////////
    
    /**
     * Compares an entity with another objetct.
     * 
     * <p>Entities of the same concrete class may be compared - passing any other object will raise 
     * ClassCastException. Entities are compared with respect to their names.</p>
     * 
     * @param o object to compare to.
     * @return result of name comparison. 
     */
    public int compareTo(Object o)
    {
        if(!getClass().equals(o.getClass()))
        {
            throw new ClassCastException("expected "+getClass());
        }
        Entity e = (Entity)o;
        return name.compareTo(e.getName());   
    }
    
    /**
     * Compares an entity with another objetct for equality.
     * 
     * <p>Entities of the same concrete class may be compared - passing any other object will raise 
     * ClassCastException. Entities are compared with respect to their names.</p>
     * 
     * @param o object to compare to.
     * @return <code>true</code> if the two enties are equal to one antother. 
     */
    public boolean equals(Object o)
    {
        if(!getClass().equals(o.getClass()))
        {
            throw new ClassCastException("expected "+getClass());
        }
        Entity e = (Entity)o;
        return name.equals(e.getName());           
    }
    
    /**
     * Computes entitie's hash code.
     * 
     * <p>The code is composed of concrete classe's hash code and name's hash code, to make sure
     * it's consitent with the {#equals(Object)} method.</p>
     * 
     * @return entitie's has code. 
     */
    public int hashCode()
    {
        return getClass().hashCode() ^ name.hashCode();
    }
    
    // string representation /////////////////////////////////////////////////////////////////////
    
    /**
     * Returns a string representation of the entity.
     * 
     * @return a string representation of the entity.
     */
    public String toString()
    {
        return getClass().getName()+" "+name;
    }
}
