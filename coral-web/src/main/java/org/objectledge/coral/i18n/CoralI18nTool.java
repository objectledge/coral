//
//Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
//All rights reserved. 
// 
//Redistribution and use in source and binary forms, with or without modification,  
//are permitted provided that the following conditions are met: 
// 
//* Redistributions of source code must retain the above copyright notice,  
//this list of conditions and the following disclaimer. 
//* Redistributions in binary form must reproduce the above copyright notice,  
//this list of conditions and the following disclaimer in the documentation  
//and/or other materials provided with the distribution. 
//* Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//nor the names of its contributors may be used to endorse or promote products  
//derived from this software without specific prior written permission. 
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
//WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
//IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
//INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
//BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
//OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
//WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
//POSSIBILITY OF SUCH DAMAGE. 
//

package org.objectledge.coral.i18n;


import java.util.Locale;

import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;
import org.objectledge.i18n.I18n;
import org.objectledge.i18n.I18nTool;

/**
 * The I18n contex tool.
 * 
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralI18nTool.java,v 1.13 2005-02-21 14:04:20 rafal Exp $
 */
public class CoralI18nTool extends I18nTool
{
    /** the coral i18n helper */
    protected CoralI18nHelper coralI18nHelper;
    
    /**
     * @param i18n the i18n component.
     * @param coralI18nHelper the i18n coral helper.
     * @param locale the locale.
     * @param prefix the prefix.
     */
    public CoralI18nTool(I18n i18n, CoralI18nHelper coralI18nHelper,
        Locale locale, String prefix)
    {
        super(i18n, locale, prefix);
        this.coralI18nHelper = coralI18nHelper;
    }

    /**
     * {@inheritDoc}
     */
    protected I18nTool createInstance(I18nTool source)
    {
        CoralI18nTool s = (CoralI18nTool)source;
        return new CoralI18nTool( s.i18n, s.coralI18nHelper, s.locale, s.prefixBuf.toString());
    }

    /**
     * Get the localized name of the resource class.
     * 
     * @param resourceClass the resource class.
     * @return the localized name of the resource class.
     */
    public String getName(ResourceClass resourceClass)
    {
        return coralI18nHelper.getName(locale, resourceClass);
    }

    /**
     * Get the localized name of the resource.
     * 
     * @param resource the resource.
     * @return the name of the resource.
     */
    public String getName(Resource resource)
    {
        return coralI18nHelper.getName(locale, resource);
    }
   
}
