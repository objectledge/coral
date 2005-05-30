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

import junit.framework.TestCase;

import org.jcontainer.dna.Logger;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.database.persistence.PersistentFactory;
import org.objectledge.logging.LoggerFactory;
import org.objectledge.pico.customization.CustomizedComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PicoInstantiatorTest.java,v 1.4 2005-05-30 09:47:46 zwierzem Exp $
 */
public class PicoInstantiatorTest
    extends TestCase
{
    private MutablePicoContainer picoContainer;
    private PicoInstantiator picoInstantiator;
    
    public void setUp()
    {
        picoContainer = new DefaultPicoContainer();
        picoInstantiator = new PicoInstantiator(picoContainer);
    }
    
    public void testLoadClass()
        throws Exception
    {
        assertEquals(Object.class, picoInstantiator.loadClass("java.lang.Object"));
    }
    
    public void testSimpleInstantiation()
        throws Exception
    {
        assertEquals(Object.class, picoInstantiator.newInstance(Object.class).getClass());
    }
    
    public void testDependencyInstantiation()
        throws Exception
    {
        picoContainer.registerComponentImplementation(Red.class);
        assertEquals(Blue.class, picoInstantiator.newInstance(Blue.class).getClass());
    }
    
    public void testCustomizedDependencyInstantiation()
        throws Exception
    {
        LoggerFactory factory = new LoggerFactory(null);
        picoContainer.registerComponent(new CustomizedComponentAdapter(Logger.class, factory));
        assertEquals(Green.class, picoInstantiator.newInstance(Green.class).getClass());
    }
    
    public void testPersistentFactory()
        throws Exception
    {
        PersistentFactory persistentFactory = picoInstantiator.getPersistentFactory(Orange.class);
        assertEquals(Orange.class, persistentFactory.newInstance().getClass());
    }
    
    public static class Red
    {
        public Red()
        {
            // a default ctor
        }
    }
    
    public static class Blue
    {
        public Blue(Red red)
        {
            // a ctor that requires a component
        }
    }
    
    public static class Green
    {
        public Green(Logger logger)
        {
            // a ctor that requires a customized component
        }
    }
    
    public static class Orange implements Persistent
    {
        /** 
         * {@inheritDoc}
         */
        public void getData(OutputRecord record) throws PersistenceException
        {
        }

        /** 
         * {@inheritDoc}
         */
        public String[] getKeyColumns()
        {
            return null;
        }

        /** 
         * {@inheritDoc}
         */
        public boolean getSaved()
        {
            return false;
        }

        /** 
         * {@inheritDoc}
         */
        public String getTable()
        {
            return null;
        }

        /** 
         * {@inheritDoc}
         */
        public void setData(InputRecord record) throws PersistenceException
        {
        }

        /** 
         * {@inheritDoc}
         */
        public void setSaved(long id)
        {
        }
    }
}
