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
package org.objectledge.coral.script;

import java.io.OutputStreamWriter;
import java.io.StringReader;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.script.parser.ParseException;
import org.objectledge.coral.script.parser.RMLParser;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ParserAndExecutorTest.java,v 1.6 2004-04-30 13:00:15 fil Exp $
 */
public class ParserAndExecutorTest extends LedgeTestCase
{
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockCoralSecurity;
    private CoralSecurity coralSecurity;
    private Mock mockCoralStore;
    private CoralStore coralStore;
    private Mock mockCoralQuery;
    private CoralQuery coralQuery;
    private Mock mockCoralSession;
    private CoralSession coralSession;
    private Mock mockCoralSessionFactory;
    private CoralSessionFactory coralSessionFactory;
    
    private RMLExecutor executor;
    
    private Mock mockAttributeClass;
    private AttributeClass attributeClass;
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    private Mock mockParentResourceClass;
    private ResourceClass parentResourceClass;
    private Mock mockAttributeDefinition1;
    private AttributeDefinition attributeDefinition1;
    private Mock mockAttributeDefinition2;
    private AttributeDefinition attributeDefinition2;
    private Mock mockPermission;
    private Permission permission;
    
    public void setUp()
        throws Exception
    {
        mockCoralSchema = mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockCoralStore = mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralQuery = mock(CoralQuery.class);
        coralQuery = (CoralQuery)mockCoralQuery.proxy();
        mockCoralSession = mock(CoralSession.class);
        coralSession = (CoralSession)mockCoralSession.proxy();
        mockCoralSession.stub().method("getSchema").will(returnValue(coralSchema));
        mockCoralSession.stub().method("getSecurity").will(returnValue(coralSecurity));
        mockCoralSession.stub().method("getStore").will(returnValue(coralStore));
        mockCoralSession.stub().method("getQuery").will(returnValue(coralQuery));
        mockCoralSessionFactory = mock(CoralSessionFactory.class);
        
        executor = new RMLExecutor(coralSession, new OutputStreamWriter(System.out), 
            coralSessionFactory);
            
        mockAttributeClass = mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockResourceClass = mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        mockParentResourceClass = mock(ResourceClass.class, "mockParentResourceClass");
        parentResourceClass = (ResourceClass)mockParentResourceClass.proxy();
        mockAttributeDefinition1 = mock(AttributeDefinition.class, "mockAttributeDefinition1");
        attributeDefinition1 = (AttributeDefinition)mockAttributeDefinition1.proxy();
        mockAttributeDefinition2 = mock(AttributeDefinition.class, "mockAttributeDefinition2");
        attributeDefinition2 = (AttributeDefinition)mockAttributeDefinition2.proxy();
        mockPermission = mock(Permission.class);
        permission = (Permission)mockPermission.proxy();
    }
    
    private void execute(String script) 
        throws ParseException
    {
        RMLParser parser = new RMLParser(new StringReader(script));
        executor.visit(parser.script(), null);        
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    public void testCreateAttributeClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("createAttributeClass").with(eq("class"), 
            eq("java_class"), eq("handler_class"), eq("db_table")).will(returnValue(null));
        execute("CREATE ATTRIBUTE CLASS class JAVA CLASS java_class HANDLER CLASS handler_class "+
            "DB TABLE db_table;");

        mockCoralSchema.expect(once()).method("createAttributeClass").with(eq("class"), 
            eq("java_class"), eq("handler_class"), NULL).will(returnValue(null));
        execute("CREATE ATTRIBUTE CLASS class JAVA CLASS java_class HANDLER CLASS handler_class;");            
    }
    
    public void testFindAttributeClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getAttributeClass").will(returnValue(null));
        execute("FIND ATTRIBUTE CLASS;");

        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).
            will(returnValue(null));
        execute("FIND ATTRIBUTE CLASS 1;");        

        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq("class")).
            will(returnValue(null));
        execute("FIND ATTRIBUTE CLASS class;");        
    }
    
    public void testDeleteAttributeClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("deleteAttributeClass").with(same(attributeClass)).
            isVoid();
        execute("DELETE ATTRIBUTE CLASS 1;");

        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq("class")).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("deleteAttributeClass").with(same(attributeClass)).
            isVoid();
        execute("DELETE ATTRIBUTE CLASS class;");
    }
    
    public void testAlterAttributeClassSetName()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("setName").with(same(attributeClass), eq("new_name")).
            isVoid();
        execute("ALTER ATTRIBUTE CLASS 1 SET NAME new_name;");
    }
    
    public void testAlterAttributeClassSetJavaClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("setJavaClass").with(same(attributeClass), 
            eq("new_class")).isVoid();
        execute("ALTER ATTRIBUTE CLASS 1 SET JAVA CLASS new_class;");
    }

    public void testAlterAttributeClassSetHandlerClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("setHandlerClass").with(same(attributeClass), 
            eq("new_class")).isVoid();
        execute("ALTER ATTRIBUTE CLASS 1 SET HANDLER CLASS new_class;");
    }

    public void testAlterAttributeClassSetDbTableClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq(1L)).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("setDbTable").with(same(attributeClass), 
            eq("new_table")).isVoid();
        execute("ALTER ATTRIBUTE CLASS 1 SET DB TABLE new_table;");
    }
    
    public void testCreateResourceClass()
        throws Exception
    {
        // basic
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), NULL, eq(0) }).isVoid();
        execute("CREATE RESOURCE CLASS rclass JAVA CLASS jclass HANDLER CLASS hclass;");
        // flags
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), NULL, eq(ResourceClassFlags.ABSTRACT +
            ResourceClassFlags.BUILTIN) }).isVoid();
        execute("CREATE RESOURCE CLASS ABSTRACT BUILTIN rclass JAVA CLASS jclass "+
            "HANDLER CLASS hclass;");
        // db table NONE
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), NULL, eq(0) }).isVoid();
        execute("CREATE RESOURCE CLASS rclass JAVA CLASS jclass HANDLER CLASS hclass "+
            "DB TABLE NONE;");
        // db table
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), eq("db_table"), eq(0) }).isVoid();
        execute("CREATE RESOURCE CLASS rclass JAVA CLASS jclass HANDLER CLASS hclass "+
            "DB TABLE db_table;");
        // superclass
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), NULL, eq(0) }).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq("class")).
            will(returnValue(parentResourceClass));
        mockCoralSchema.expect(once()).method("addParentClass").with(same(resourceClass), 
            same(parentResourceClass), ANYTHING).isVoid();
        execute("CREATE RESOURCE CLASS rclass JAVA CLASS jclass HANDLER CLASS hclass "+
            "SUPERCLASSES ( class );");
        // attributes
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), NULL, eq(0) }).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(atLeastOnce()).method("getAttributeClass").with(eq("string")).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("createAttribute").with(eq("attribute1"), 
            same(attributeClass), NULL, eq(0)).will(returnValue(attributeDefinition1));
        mockCoralSchema.expect(once()).method("createAttribute").with(eq("attribute2"), 
            same(attributeClass), NULL, eq(0)).will(returnValue(attributeDefinition2));
        mockCoralSchema.expect(once()).method("addAttribute").with(same(resourceClass), 
            same(attributeDefinition1), NULL).isVoid();           
        mockCoralSchema.expect(once()).method("addAttribute").with(same(resourceClass), 
            same(attributeDefinition2), NULL).isVoid();           
        execute("CREATE RESOURCE CLASS rclass JAVA CLASS jclass HANDLER CLASS hclass "+
            "ATTRIBUTES ( string attribute1, string attribute2 );");  
        // attribute with flags      
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), NULL, eq(0) }).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq("string")).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("createAttribute").with(eq("attribute1"), 
            same(attributeClass), NULL, eq(AttributeFlags.REQUIRED + AttributeFlags.DESCRIPTIVE)).
            will(returnValue(attributeDefinition1));
        mockCoralSchema.expect(once()).method("addAttribute").with(same(resourceClass), 
            same(attributeDefinition1), NULL).isVoid();           
        execute("CREATE RESOURCE CLASS rclass JAVA CLASS jclass HANDLER CLASS hclass "+
            "ATTRIBUTES ( REQUIRED DESCRIPTIVE string attribute1 );");  
        // attribute with domain
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), NULL, eq(0) }).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("getAttributeClass").with(eq("string")).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("createAttribute").with(eq("attribute1"), 
            same(attributeClass), eq("domain"), eq(0)).
            will(returnValue(attributeDefinition1));
        mockCoralSchema.expect(once()).method("addAttribute").with(same(resourceClass), 
            same(attributeDefinition1), NULL).isVoid();           
        execute("CREATE RESOURCE CLASS rclass JAVA CLASS jclass HANDLER CLASS hclass "+
            "ATTRIBUTES ( string(domain) attribute1 );");  
        // permission
        mockCoralSchema.expect(once()).method("createResourceClass").with(new Constraint[] { 
            eq("rclass"), eq("jclass"), eq("hclass"), NULL, eq(0) }).
            will(returnValue(resourceClass));
        mockCoralSecurity.expect(once()).method("getPermission").with(eq("permission")).
            will(returnValue(new Permission[] { permission }));            
        mockCoralSecurity.expect(once()).method("addPermission").with(same(resourceClass), 
            same(permission)).isVoid();
        execute("CREATE RESOURCE CLASS rclass JAVA CLASS jclass HANDLER CLASS hclass "+
            "PERMISSIONS ( permission );");
    }
    
    public void testFindResourceClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").will(returnValue(null));
        execute("FIND RESOURCE CLASS;");

        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(null));
        execute("FIND RESOURCE CLASS 1;");        

        mockCoralSchema.expect(once()).method("getResourceClass").with(eq("class")).
            will(returnValue(null));
        execute("FIND RESOURCE CLASS class;");        
    }

    public void testDeleteResourceClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("deleteResourceClass").with(same(resourceClass)).
            isVoid();
        execute("DELETE RESOURCE CLASS 1;");

        mockCoralSchema.expect(once()).method("getResourceClass").with(eq("class")).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("deleteResourceClass").with(same(resourceClass)).
            isVoid();
        execute("DELETE RESOURCE CLASS class;");
    }
    
    public void testAlterResourceClassSetName()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("setName").with(same(resourceClass), eq("new_name")).
            isVoid();
        execute("ALTER RESOURCE CLASS 1 SET NAME new_name;");
    }
    
    public void testAlterResourceClassSetJavaClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("setJavaClass").with(same(resourceClass), 
            eq("new_class")).isVoid();
        execute("ALTER RESOURCE CLASS 1 SET JAVA CLASS new_class;");
    }

    public void testAlterResourceClassSetHandlerClass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("setHandlerClass").with(same(resourceClass), 
            eq("new_class")).isVoid();
        execute("ALTER RESOURCE CLASS 1 SET HANDLER CLASS new_class;");
    }

    public void testAlterResourceClassSetDbTable()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("setDbTable").with(same(resourceClass), 
            eq("new_table")).isVoid();
        execute("ALTER RESOURCE CLASS 1 SET DB TABLE new_table;");
    }

    public void testAlterResourceClassSetFlags()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("setFlags").with(same(resourceClass), 
            eq(ResourceClassFlags.FINAL)).isVoid();
        execute("ALTER RESOURCE CLASS 1 SET FLAGS FINAL;");
    }
    
    public void testAlterResourceClassAlterAttributeSetName()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockResourceClass.expect(once()).method("getAttribute").with(eq("attribute1")).
            will(returnValue(attributeDefinition1));    
        mockCoralSchema.expect(once()).method("setName").with(same(attributeDefinition1), 
            eq("new_name")).isVoid();
        execute("ALTER RESOURCE CLASS 1 ALTER ATTRIBUTE attribute1 SET NAME new_name;");
    }

    public void testAlterResourceClassAlterAttributeSetDomain()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockResourceClass.expect(once()).method("getAttribute").with(eq("attribute1")).
            will(returnValue(attributeDefinition1));    
        mockCoralSchema.expect(once()).method("setDomain").with(same(attributeDefinition1), 
            eq("new")).isVoid();
        execute("ALTER RESOURCE CLASS 1 ALTER ATTRIBUTE attribute1 SET DOMAIN new;");
    }

    public void testAlterResourceClassAlterAttributeSetFlags()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockResourceClass.expect(once()).method("getAttribute").with(eq("attribute1")).
            will(returnValue(attributeDefinition1));    
        mockCoralSchema.expect(once()).method("setFlags").with(same(attributeDefinition1), 
            eq(AttributeFlags.DESCRIPTIVE)).isVoid();
        execute("ALTER RESOURCE CLASS 1 ALTER ATTRIBUTE attribute1 SET FLAGS DESCRIPTIVE;");
    }
    
    public void testAlterResourceClassAddAttribute()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(atLeastOnce()).method("getAttributeClass").with(eq("string")).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("createAttribute").with(eq("capitalized"), 
            same(attributeClass), eq("[A-Z]*"), eq(AttributeFlags.REQUIRED)).
            will(returnValue(attributeDefinition1));
        mockCoralSchema.expect(once()).method("addAttribute").with(same(resourceClass), 
            same(attributeDefinition1), NULL).isVoid();
        execute("ALTER RESOURCE CLASS 1 ADD ATTRIBUTE REQUIRED string('[A-Z]*') capitalized;");
        
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(atLeastOnce()).method("getAttributeClass").with(eq("string")).
            will(returnValue(attributeClass));
        mockCoralSchema.expect(once()).method("createAttribute").with(eq("capitalized"), 
            same(attributeClass), eq("[A-Z]*"), eq(AttributeFlags.REQUIRED)).
            will(returnValue(attributeDefinition1));
        mockCoralSchema.expect(once()).method("addAttribute").with(same(resourceClass), 
            same(attributeDefinition1), eq("FOO")).isVoid();
        execute("ALTER RESOURCE CLASS 1 ADD ATTRIBUTE REQUIRED string('[A-Z]*') capitalized " +            "VALUE FOO;");
    }
    
    public void testAlterResourceClassDeleteAttribute()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockResourceClass.expect(once()).method("getAttribute").with(eq("attribute1")).
            will(returnValue(attributeDefinition1));    
        mockCoralSchema.expect(once()).method("deleteAttribute").with(same(resourceClass), 
            same(attributeDefinition1)).isVoid();
        execute("ALTER RESOURCE CLASS 1 DELETE ATTRIBUTE attribute1;");
    }
    
    public void testAlterResourceClassAddSuperclass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq("parent")).
            will(returnValue(parentResourceClass));
        mockCoralSchema.expect(once()).method("addParentClass").with(same(resourceClass), 
            same(parentResourceClass), ANYTHING).isVoid();
        execute("ALTER RESOURCE CLASS 1 ADD SUPERCLASS parent;");

        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq("parent")).
            will(returnValue(parentResourceClass));
        mockParentResourceClass.expect(once()).method("getAttribute").with(eq("a1")).
            will(returnValue(attributeDefinition1));
        mockParentResourceClass.expect(once()).method("getAttribute").with(eq("a2")).
            will(returnValue(attributeDefinition2));
        mockCoralSchema.expect(once()).method("addParentClass").with(same(resourceClass), 
            same(parentResourceClass), and(mapElement(attributeDefinition1, eq("v1")), 
            mapElement(attributeDefinition2, eq("v2")))).isVoid();
        execute("ALTER RESOURCE CLASS 1 ADD SUPERCLASS parent VALUES ( a1 = v1, a2 = v2 );");
    }
    
    public void testResourceClassDeleteSuperclass()
        throws Exception
    {
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).
            will(returnValue(resourceClass));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq("parent")).
            will(returnValue(parentResourceClass));
        mockCoralSchema.expect(once()).method("deleteParentClass").with(same(resourceClass), 
            same(parentResourceClass)).isVoid();
        execute("ALTER RESOURCE CLASS 1 DELETE SUPERCLASS parent;");
    }
}
