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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectledge.database.persistence.Persistent;
import org.objectledge.database.persistence.PersistentFactory;
import org.objectledge.pico.customization.CustomizingConstructorComponentAdapter;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.defaults.AssignabilityRegistrationException;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.NotConcreteRegistrationException;

/**
 * An implemention of the Instantiator interface using the PicoContainer.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PicoInstantiator.java,v 1.8 2005-02-07 00:35:42 rafal Exp $
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
        ClassLoader cl = null;
        try
        {
            cl = Thread.currentThread().getContextClassLoader();
            if(cl == null)
            {
                cl = getClass().getClassLoader();
            }
            return cl.loadClass(className);
        }
        catch(ClassNotFoundException e)
        {
            throw new ClassNotFoundException("could not find "+className+" in "+cl, e);
        }
    }        

    /** 
     * {@inheritDoc}
     */
    public Object newInstance(Class clazz) throws InstantiationException
    {
        ComponentAdapter adapter = getAdapter(clazz);
        return adapter.getComponentInstance(container); 
    }

    /** 
     * {@inheritDoc}
     */
    public Object newInstance(Class clazz, Map additional) throws InstantiationException
    {
        ComponentAdapter adapter = new CustomizingConstructorComponentAdapter(clazz, clazz, null);
        MutablePicoContainer tempContainer = new DefaultPicoContainer(container);
        for(Iterator i=additional.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry)i.next();
            tempContainer.registerComponentInstance(entry.getKey(), entry.getValue());
        }
        return adapter.getComponentInstance(tempContainer); 
    }
    
    /** 
     * {@inheritDoc}
     */
    public PersistentFactory getPersistentFactory(final Class clazz)
    {
        if(!Persistent.class.isAssignableFrom(clazz))
        {
            throw new IllegalArgumentException(clazz.getName()+
                " does not implmement Persistent interface");
        }
        return new PersistentFactory()
        {
            public Persistent newInstance()
                throws Exception
            {
                return (Persistent)PicoInstantiator.this.newInstance(clazz);
            }
        };
    }

    private final Map<Class<?>,ComponentAdapter> adapterMap = 
        new HashMap<Class<?>,ComponentAdapter>();
    
    private ComponentAdapter getAdapter(Class clazz)
    {
        ComponentAdapter adapter = adapterMap.get(clazz);
        if(adapter == null)
        {
            adapter = new ArgumentCachingComponentAdapter(clazz);
            adapterMap.put(clazz, adapter);
        }
        return adapter;
    }
    
    /**
     * Adapter that caches constructor arguments per implementation class.
     *
     * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
     * @version $Id: PicoInstantiator.java,v 1.8 2005-02-07 00:35:42 rafal Exp $
     */
    private class ArgumentCachingComponentAdapter
        extends CustomizingConstructorComponentAdapter
    {
        private Constructor ctor;
        private Object[] args;
        
        /**
         * Creates new ArgumentCachingComponentAdapter instance.
         * 
         * @param componentImplementation the component implementation class.
         * @throws AssignabilityRegistrationException if the class does not implement required 
         * interface.
         * @throws NotConcreteRegistrationException if the implementation class is not concrete.
         */
        public ArgumentCachingComponentAdapter(Class componentImplementation)
            throws AssignabilityRegistrationException, NotConcreteRegistrationException
        {
            super(componentImplementation, componentImplementation);
        }

        /**
         * {@inheritDoc}
         */
        protected Object[] getConstructorArguments(PicoContainer container, Constructor ctor)
        {
            if(args == null)
            {
                args = super.getConstructorArguments(container, ctor);
            }
            return args;
        }
        
        /**
         * {@inheritDoc}
         */
        protected Constructor getGreediestSatisfiableConstructor(PicoContainer container)
        {
           if(ctor == null)
           {
               ctor = super.getGreediestSatisfiableConstructor(container);
           }
           return ctor;
        }

        /**
         * {@inheritDoc}
         */
        public Object getComponentInstance(PicoContainer container)
        {
            if(ctor != null)
            {
                try
                {
                    return ctor.newInstance(args);
                }
                catch(Exception e)
                {
                    throw new PicoInitializationException("unexpected exception", e);
                }
            }
            else
            {
                return super.getComponentInstance(container);
            }
        }
    }
}
