/* Generated By:JJTree: Do not edit this line. ASTalterAttributeClassSetNameStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterAttributeClassSetNameStatement extends ASTalterAttributeClassStatement {
  public ASTalterAttributeClassSetNameStatement(int id) {
    super(id);
  }

  public ASTalterAttributeClassSetNameStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  
  public String getNewName()
  {
      return newName;
  }
}
