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
package org.objectledge.coral;

import org.objectledge.pico.customization.CustomizingConstructorComponentAdapter;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PicoInstantiator.java,v 1.1 2004-02-26 10:33:23 fil Exp $
 */
public class PicoInstantiator 
    implements Instantiator
{
    private PicoContainer container;
    
    /**
     * Crates an instantiator instance.
     * 
     * @param container the pico container to resolve dependencies from.
     */
    public PicoInstantiator(PicoContainer container)
    {
        this.container = container;
    }

    /** 
     * {@inheritDoc}
     */
    public Class loadClass(String className) 
        throws ClassNotFoundException
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl == null)
        {
            cl = getClass().getClassLoader();
        }
        return cl.loadClass(className);
    }

    /** 
     * {@inheritDoc}
     */
    public Object newInstance(Class clazz) throws CoralInstantiationException
    {
        ComponentAdapter adapter = new CustomizingConstructorComponentAdapter(clazz, clazz, null);
        adapter.setContainer(container);
        return adapter.getComponentInstance(); 
    }
}
