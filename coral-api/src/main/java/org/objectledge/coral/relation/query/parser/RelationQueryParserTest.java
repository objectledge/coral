// 
// Copyright (c) 2003, 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
//   
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//   
// * Redistributions of source code must retain the above copyright notice,  
// this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
// this list of conditions and the following disclaimer in the documentation  
// and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
// nor the names of its contributors may be used to endorse or promote products  
// derived from this software without specific prior written permission. 
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
package org.objectledge.coral.relation.query.parser;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationQueryParserTest.java,v 1.1 2004-03-01 13:52:26 zwierzem Exp $
 */
public class RelationQueryParserTest extends TestCase
{

    /**
     * Constructor for RelationQueryParserTest.
     * @param arg0
     */
    public RelationQueryParserTest(String arg0)
    {
        super(arg0);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RelationQueryParserTest.class);
    }

    public void testExecuteParse()
    {
    	String query = "MAP(!'Name of Relation 1'){ RES(12345) } + " +			"MAPTRANS('Name of Relation 2'){ " +			"RES('/r/es/our-ce_pa.t,h') + RES('/res2') + RES('/res3') } * RES(98765) * RES(1);";
		
		SimpleNode tree = null;
		try
		{
			tree = RelationQueryParser.executeParse(query);
		}
		catch(Exception e)
		{
			fail("query is malformed");
		}
		
		Visitor visitor = new Visitor();
		Object result = tree.jjtAccept(visitor, null);
		assertNull(result);
    }
    
    private class Visitor implements RelationQueryParserVisitor
    {
    	private int nodeNum = 0;
    	
        /**
         * {@inheritDoc}
         */
        public Object visit(SimpleNode node, Object data)
        {
			fail("SimpleNode should not be called");
			return null;
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTStart node, Object data)
        {
			nodeNum = 0;
			assertTrue(node.jjtGetNumChildren() == 1);
			return visitChildren(node, data);
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTSumExpression node, Object data)
        {
			nodeNum++;
			int numChildren = node.jjtGetNumChildren();
			assertFalse(numChildren < 2);
			if(nodeNum == 1) assertEquals(numChildren, 2);
			if(nodeNum == 9) assertEquals(numChildren, 3);
			return visitChildren(node, data);
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTIntersectionExpression node, Object data)
        {
			nodeNum++;
			int numChildren = node.jjtGetNumChildren();
			assertFalse(numChildren < 2);
			if(nodeNum == 6) assertEquals(numChildren, 3);
			return visitChildren(node, data);
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTRelationMapExpression node, Object data)
        {
			nodeNum++;
			assertEquals(nodeNum, 2);
			assertEquals(node.jjtGetNumChildren(), 2);
			return visitChildren(node, data);
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTTransitiveRelationMapExpression node, Object data)
        {
			nodeNum++;
			assertEquals(nodeNum, 7);
			assertEquals(node.jjtGetNumChildren(), 2);
			return visitChildren(node, data);
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTInvertedRelationExpression node, Object data)
        {
			nodeNum++;
			assertEquals(nodeNum, 3);
			assertEquals(node.jjtGetNumChildren(), 1);
			return visitChildren(node, data);
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTRelationName node, Object data)
        {
			nodeNum++;
			assertEquals(node.jjtGetNumChildren(), 0);
			if(nodeNum == 4) assertEquals(node.getRelationName(), "Name of Relation 1");
			if(nodeNum == 8) assertEquals(node.getRelationName(), "Name of Relation 2");
			return null;
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTResourceIdentifierId node, Object data)
        {
			nodeNum++;
			assertEquals(node.jjtGetNumChildren(), 0);
			if(nodeNum == 5) assertEquals(node.getIdentifier(), "12345");
			if(nodeNum == 13) assertEquals(node.getIdentifier(), "98765");
			if(nodeNum == 14) assertEquals(node.getIdentifier(), "1");
			return null;
        }

        /**
         * {@inheritDoc}
         */
        public Object visit(ASTResourceIdentifierPath node, Object data)
        {
			nodeNum++;
			assertEquals(node.jjtGetNumChildren(), 0);
			if(nodeNum == 10) assertEquals(node.getIdentifier(), "/r/es/our-ce_pa.t,h");
			if(nodeNum == 11) assertEquals(node.getIdentifier(), "/res2");
			if(nodeNum == 12) assertEquals(node.getIdentifier(), "/res3");
            return null;
        }
        
        // implementation -------------------------------------------------------------------------
        private Object visitChildren(SimpleNode node, Object data)
        {
			int numChildren = node.jjtGetNumChildren();
			for(int i=0; i<numChildren; i++)
			{
				node.jjtGetChild(i).jjtAccept(this, data);
			}
        	return null;
        }
    }
}
