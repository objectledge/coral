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

import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClassTest.java,v 1.1 2004-03-24 16:02:04 fil Exp $
 */
public class ResourceClassTest extends LedgeTestCase
{    
    private ResourceClass resourceClass;

    public void setUp()
    {
        resourceClass = new ResourceClass("RC1", "org.objectledge.datatypes.Node", "dt1", 0);
    }
    
    public void testGetPackageName()
    {
        assertEquals("org.objectledge.datatypes", resourceClass.getPackageName());
    }

    public void testGetImplClassName()
    {
        assertEquals("NodeImpl", resourceClass.getImplClassName());
    }

    public void testGetFQImplClassName()
    {
        assertEquals("org.objectledge.datatypes.NodeImpl", resourceClass.getFQImplClassName());
    }

    public void testGetInterfaceClassName()
    {
        assertEquals("Node", resourceClass.getInterfaceClassName());
    }

    public void testGetFQInterfaceClassName()
    {
        assertEquals("org.objectledge.datatypes.Node", resourceClass.getFQInterfaceClassName());
    }
    
    public void testGetDbTable()
    {
        assertEquals("dt1", resourceClass.getDbTable());
    }
    
    public void testSetDbTable()
    {
        resourceClass.setDbTable(null);
        assertNull(resourceClass.getDbTable());
        resourceClass.setDbTable("dt2");
        assertEquals("dt2", resourceClass.getDbTable());
    }
    
    public void testIsAbstract()
    {
        assertFalse(resourceClass.isAbstract());
        resourceClass.setFlags(ResourceClassFlags.ABSTRACT);
        assertTrue(resourceClass.isAbstract());
    }
}
