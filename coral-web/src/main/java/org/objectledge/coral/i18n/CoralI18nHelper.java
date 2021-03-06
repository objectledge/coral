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

/**
 * The I18n coral entities name resolver.
 * 
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralI18nHelper.java,v 1.1 2005-02-14 18:14:00 pablo Exp $
 */
public class CoralI18nHelper
{
    /** i18n compoennt */    
    protected I18n i18n;
    
    /**
     * @param i18n the i18n component.
     */
    public CoralI18nHelper(I18n i18n)
    {
        this.i18n = i18n;
    }

    /**
     * Get the localized name of the resource class.
     *
     * @param locale the locale.
     * @param resourceClass the resource class.
     * @return the localized name of the resource class.
     */
    public String getName(Locale locale, ResourceClass resourceClass)
    {
        return i18n.get(locale, getNameKey(resourceClass), resourceClass.getName());
    }

    /**
     * Get the localized name of the resource.
     * 
     * @param locale the locale.
     * @param resource the resource.
     * @return the name of the resource.
     */
    public String getName(Locale locale, Resource resource)
    {
        return i18n.get(locale, getNameKey(resource), resource.getName());
    }
    
    /**
     * Get the localization key for the name of the resource class.
     * 
     * @param resourceClass the resource class.
     * @return the key for the name of the resource class.
     */
    protected String getNameKey(ResourceClass resourceClass)
    {
        StringBuilder nameBuf = new StringBuilder();
        return nameBuf.append("resource.").append(resourceClass.getName()).toString();
    }

    /**
     * Get the localization key for the name of the resource.
     * 
     * @param resource the resource.
     * @return the key for the name of the resource.
     */
    protected String getNameKey(Resource resource)
    {
        StringBuilder nameBuf = new StringBuilder();
        return nameBuf.append("resource.").append(resource.getResourceClass().getName())
            .append(".resource-name.").append(resource.getName()).toString();
    }
}
