/* Generated By:JJTree: Do not edit this line. ASTdeleteResourceClassStatement.java */

package org.objectledge.coral.script.parser;

public class ASTdeleteResourceClassStatement extends SimpleNode {
  public ASTdeleteResourceClassStatement(int id) {
    super(id);
  }

  public ASTdeleteResourceClassStatement(RML p, int id) {
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
