/* Generated By:JJTree: Do not edit this line. ASTalterResourceClassSetNameStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterResourceClassStatement extends SimpleNode {
  public ASTalterResourceClassStatement(int id) {
    super(id);
  }

  public ASTalterResourceClassStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  
  public ASTresourceClass getResourceClass()
  {
      return resourceClass;
  }
}
