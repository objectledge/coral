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
package org.objectledge.coral.tools.generator;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.script.parser.ASTalterAttributeClassSetJavaClassStatement;
import org.objectledge.coral.script.parser.ASTalterAttributeClassSetNameStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAddAttributeStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAddSuperclassStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAlterAttributeSetDomainStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAlterAttributeSetFlagsStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassAlterAttributeSetNameStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassDeleteAttributeStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassDeleteSuperclassStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetDbTableStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetFlagsStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetJavaClassStatement;
import org.objectledge.coral.script.parser.ASTalterResourceClassSetNameStatement;
import org.objectledge.coral.script.parser.ASTattributeClass;
import org.objectledge.coral.script.parser.ASTattributeDefinition;
import org.objectledge.coral.script.parser.ASTattributeDefinitionList;
import org.objectledge.coral.script.parser.ASTattributeFlag;
import org.objectledge.coral.script.parser.ASTattributeFlagList;
import org.objectledge.coral.script.parser.ASTcreateAttributeClassStatement;
import org.objectledge.coral.script.parser.ASTcreateResourceClassStatement;
import org.objectledge.coral.script.parser.ASTdeleteAttributeClassStatement;
import org.objectledge.coral.script.parser.ASTdeleteResourceClassStatement;
import org.objectledge.coral.script.parser.ASTresourceClass;
import org.objectledge.coral.script.parser.ASTresourceClassFlag;
import org.objectledge.coral.script.parser.ASTresourceClassFlagList;
import org.objectledge.coral.script.parser.ASTresourceClassList;
import org.objectledge.coral.script.parser.ASTscript;
import org.objectledge.coral.script.parser.DefaultRMLVisitor;
import org.objectledge.coral.script.parser.RMLParser;
import org.objectledge.coral.script.parser.RMLParserFactory;
import org.objectledge.coral.tools.generator.model.Attribute;
import org.objectledge.coral.tools.generator.model.AttributeClass;
import org.objectledge.coral.tools.generator.model.ResourceClass;
import org.objectledge.coral.tools.generator.model.Schema;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: RMLModelLoader.java,v 1.5 2004-03-23 11:34:36 fil Exp $
 */
public class RMLModelLoader
{
    private RMLParserFactory parserFactory = new RMLParserFactory();
    private RMLVisistor visitor = new RMLVisistor();
    private List exceptions = new ArrayList();    
    private Schema schema;

    /**
     * Creates a model loader instance.
     * 
     * @param schema the schema to load data into.
     */
    public RMLModelLoader(Schema schema)
    {
        this.schema = schema;
    }
    
    /**
     * Processes a script.
     * 
     * @param in the Reader to read script from.
     * @throws Exception if there is a problem parsing or executing the script.
     */
    public synchronized void load(Reader in)
        throws Exception
    {
        RMLParser parser = parserFactory.getParser(in);
        try
        {
            ASTscript script = parser.script();
            visitor.visit(script, null);
        }
        finally
        {
            parserFactory.recycle(parser);
        }
        
        if(!exceptions.isEmpty())
        {
            Exception e = (Exception)exceptions.get(0);
            exceptions.clear();
            throw new Exception("there were exceptions running the script", e); 
        }
    }

    /**
     * Processes a script.
     * 
     * @param in the script in String form.
     * @throws Exception if there is a problem parsing or executing the script.
     */
    public void execute(String in)
        throws Exception
    {
        load(new StringReader(in));
    }

    /**
     * 
     * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
     * @version $Id: RMLModelLoader.java,v 1.5 2004-03-23 11:34:36 fil Exp $
     */
    private class RMLVisistor extends DefaultRMLVisitor
    {
        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTcreateAttributeClassStatement node, Object data)
        {
            AttributeClass ac = new AttributeClass(node.getName(), node.getJavaClass());
            schema.addAttributeClass(ac); 
            return data;
        }
    
        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTdeleteAttributeClassStatement node, Object data)
        {
            try
            {
                AttributeClass ac = resolve(node.getAttributeClass());
                schema.deleteAttributeClass(ac);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }
    
        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterAttributeClassSetNameStatement node, Object data)
        {
            try
            {
                AttributeClass ac = resolve(node.getAttributeClass());
                schema.deleteAttributeClass(ac);
                ac.setName(node.getNewName());
                schema.addAttributeClass(ac);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterAttributeClassSetJavaClassStatement node, Object data)
        {
            try
            {
                AttributeClass ac = resolve(node.getAttributeClass());
                ac.setJavaClassName(node.getJavaClass());
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }
    
        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTcreateResourceClassStatement node, Object data)
        {
            try
            {
                ResourceClass rc = new ResourceClass(node.getName(), node.getJavaClass(), 
                    node.getDbTable(), parseFlags(node.getFlags()));
                ResourceClass[] parents = resolve(node.getParents());
                for(int i=0; i<parents.length; i++)
                {
                    rc.addParentClass(parents[i]);
                }
                ASTattributeDefinition[] attrs = items(node.getAttributes());
                for(int i=0; i<attrs.length; i++)
                {
                    AttributeClass ac = resolve(attrs[i].getAttributeClass());
                    int flags = parseFlags(attrs[i].getFlags());
                    Attribute attr = new Attribute(attrs[i].getName(), ac, attrs[i].getDomain(), 
                        flags);
                    rc.addAttribute(attr);
                }
                schema.addResourceClass(rc);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTdeleteResourceClassStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                schema.deleteResourceClass(rc);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassAddAttributeStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                ASTattributeDefinition attrDef = node.getAttributeDefinition();
                AttributeClass ac = resolve(attrDef.getAttributeClass());
                int flags = parseFlags(attrDef.getFlags());
                Attribute attr = new Attribute(attrDef.getName(), ac, attrDef.getDomain(), flags);
                rc.addAttribute(attr);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassDeleteAttributeStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                Attribute attr = rc.getAttribute(node.getAttributeName());
                rc.deleteAttribute(attr);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassAddSuperclassStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                ResourceClass parent = resolve(node.getParentClass());
                rc.addParentClass(parent);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassDeleteSuperclassStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                ResourceClass parent = resolve(node.getParentClass());
                rc.deleteParentClass(parent);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassSetNameStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                schema.deleteResourceClass(rc);
                rc.setName(node.getNewName());
                schema.addResourceClass(rc);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassSetJavaClassStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                rc.setJavaClassName(node.getJavaClass());
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassSetFlagsStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                rc.setFlags(parseFlags(node.getFlags()));
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassSetDbTableStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                rc.setDbTable(node.getDbTable());
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassAlterAttributeSetNameStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                Attribute attr = rc.getAttribute(node.getAttributeName());
                rc.deleteAttribute(attr);
                attr.setName(node.getNewName());
                rc.addAttribute(attr);
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassAlterAttributeSetFlagsStatement node, Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                Attribute attr = rc.getAttribute(node.getAttributeName());
                attr.setFlags(parseFlags(node.getFlags()));
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }

        /**
         * {@inheritDoc}
         */    
        public Object visit(ASTalterResourceClassAlterAttributeSetDomainStatement node, 
            Object data)
        {
            try
            {
                ResourceClass rc = resolve(node.getResourceClass());
                Attribute attr = rc.getAttribute(node.getAttributeName());
                attr.setDomain(node.getDomain());
            }
            catch(Exception e)
            {
                wrap(e);
            }
            return data;
        }
    
        // implementation ///////////////////////////////////////////////////////////////////////

        private int parseFlags(ASTresourceClassFlagList flags)
        {
            StringBuffer buff = new StringBuffer();
            int count = flags.jjtGetNumChildren();
            for(int i=0; i<count; i++)
            {
                ASTresourceClassFlag flag = (ASTresourceClassFlag)flags.jjtGetChild(i);
                buff.append(flag.getValue());
                buff.append(' ');
            }
            if(count > 0)
            {
                buff.setLength(buff.length()-1);
            }
            return ResourceClassFlags.parseFlags(buff.toString());       
        }

        private int parseFlags(ASTattributeFlagList flags)
        {
            StringBuffer buff = new StringBuffer();
            int count = flags.jjtGetNumChildren();
            for(int i=0; i<count; i++)
            {
                ASTattributeFlag flag = (ASTattributeFlag)flags.jjtGetChild(i);
                buff.append(flag.getValue());
                buff.append(' ');
            }
            if(count > 0)
            {
                buff.setLength(buff.length()-1);
            }
            return AttributeFlags.parseFlags(buff.toString());       
        }

        private ResourceClass resolve(ASTresourceClass node)
            throws EntityDoesNotExistException
        {
            if(node.getId() != -1)
            {
                throw new UnsupportedOperationException("numeric ids are not supported");
            }
            else
            {
                return schema.getResourceClass(node.getName());
            }
        }

        private ResourceClass[] resolve(ASTresourceClassList node)
            throws EntityDoesNotExistException
        {
            if(node == null)
            {
                return new ResourceClass[0];
            }
            ResourceClass[] result = new ResourceClass[node.jjtGetNumChildren()];
            for(int i=0; i<result.length; i++)
            {
                result[i] = resolve((ASTresourceClass)node.jjtGetChild(i));
            }
            return result;
        }

        private AttributeClass resolve(ASTattributeClass node)
            throws EntityDoesNotExistException
        {
            if(node.getId() != -1)
            {
                throw new UnsupportedOperationException("numeric ids are not supported");
            }
            else
            {
                return schema.getAttributeClass(node.getName());
            }
        }
    
        private ASTattributeDefinition[] items(ASTattributeDefinitionList list)
        {
            if(list != null)
            {
                ASTattributeDefinition[] result = new ASTattributeDefinition[list.
                    jjtGetNumChildren()];
                for(int i=0; i<list.jjtGetNumChildren(); i++)
                {
                    result[i] = (ASTattributeDefinition)list.jjtGetChild(i);
                }
                return result;
            }
            return new ASTattributeDefinition[0];
        }
    
        private void wrap(Exception e)
        {
            exceptions.add(e);
        }
    }
}
