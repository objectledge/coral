/* Generated By:JJTree: Do not edit this line. ASTalterAttributeClassSetNameStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterAttributeClassStatement extends SimpleNode {
  public ASTalterAttributeClassStatement(int id) {
    super(id);
  }

  public ASTalterAttributeClassStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////////
  
  public ASTattributeClass getAttributeClass()
  {
      return attributeClass;
  }
}
