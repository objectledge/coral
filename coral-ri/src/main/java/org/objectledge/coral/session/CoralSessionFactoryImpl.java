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
package org.objectledge.coral.session;

import java.security.Principal;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.security.Subject;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSessionFactoryImpl.java,v 1.5 2004-06-28 07:58:35 fil Exp $
 */
public class CoralSessionFactoryImpl implements CoralSessionFactory
{
    private KeyedObjectPool pool;
    
    private CoralCore coral;

    /**
     * Constructs a session factory instance.
     * 
     * @param coral the Coral component hub.
     */
    public CoralSessionFactoryImpl(CoralCore coral)
    {
        this.coral = coral;
        GenericKeyedObjectPool.Config poolConfig = new GenericKeyedObjectPool.Config();
        poolConfig.whenExhaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
        pool = new GenericKeyedObjectPool(new Factory(), poolConfig);
    }
    
    /** 
     * {@inheritDoc}
     */
    public CoralSession getSession(Principal user)
        throws EntityDoesNotExistException
    {
        try
        {
            return (CoralSession)pool.borrowObject(user);
        }
        catch(EntityDoesNotExistException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new BackendException("failed to open session", e);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSession getRootSession()
    {
        try
        {
            Subject subject = coral.getSecurity().getSubject(Subject.ROOT);
            return getSession(subject.getPrincipal());
        }
        catch(Exception e)
        {
            throw new BackendException("failed to open superuser session", e);
        }
    }

    /** 
     * {@inheritDoc}
     */
    public CoralSession getAnonymousSession()
    {
        try
        {
            Subject subject = coral.getSecurity().getSubject(Subject.ANONYMOUS);
            return getSession(subject.getPrincipal());
        }
        catch(Exception e)
        {
            throw new BackendException("failed to open superuser session", e);
        }
    }

    /**
     * Commons pool integration point.
     */
    private class Factory extends BaseKeyedPoolableObjectFactory
    {    
        /** 
         * {@inheritDoc}
         */
        public void activateObject(Object principalObject, Object sessionObject) throws Exception
        {
            CoralSessionImpl session = (CoralSessionImpl)sessionObject;
            Principal principal = (Principal)principalObject;
            Subject subject = coral.getSecurity().getSubject(principal.getName());
            session.open(principal, subject);
        }

        /** 
         * {@inheritDoc}
         */
        public Object makeObject(Object arg0) throws Exception
        {
            return new CoralSessionImpl(coral, pool);
        }
    }    
}
