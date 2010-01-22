// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
// 
// * Redistributions of source code must retain the above copyright notice,  
//       this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//       this list of conditions and the following disclaimer in the documentation  
//       and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//       nor the names of its contributors may be used to endorse or promote products  
//       derived from this software without specific prior written permission. 
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
 
package org.objectledge.coral.test;

import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Defines the accessor methods of <code>coral.test.PersistentDerived</code> Coral resource class.
 *
 * @author Coral Maven plugin
 */
public interface PersistentDerived
    extends Resource, Persistent
{
    // constants /////////////////////////////////////////////////////////////

    /** The name of the Coral resource class. */    
    public static final String CLASS_NAME = "coral.test.PersistentDerived";

    // public interface //////////////////////////////////////////////////////
	
    /**
     * Returns the value of the <code>i1</code> attribute.
     *
     * @return the value of the the <code>i1</code> attribute.
     */
    public int getI1();

    /**
     * Sets the value of the <code>i1</code> attribute.
     *
     * @param value the value of the <code>i1</code> attribute.
     */
    public void setI1(int value);
    
    /**
     * Returns the value of the <code>s1</code> attribute.
     *
     * @return the value of the the <code>s1</code> attribute.
     */
    public String getS1();
 
    /**
     * Sets the value of the <code>s1</code> attribute.
     *
     * @param value the value of the <code>s1</code> attribute.
     * @throws ValueRequiredException if you attempt to set a <code>null</code> 
     *         value.
     */
    public void setS1(String value)
        throws ValueRequiredException;
     
    // @custom methods ///////////////////////////////////////////////////////
}
