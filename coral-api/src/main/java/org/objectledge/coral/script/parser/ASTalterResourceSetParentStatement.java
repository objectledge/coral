/* Generated By:JJTree: Do not edit this line. ASTalterResourceSetParentStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterResourceSetParentStatement extends ASTalterResourceStatement {
  public ASTalterResourceSetParentStatement(int id) {
    super(id);
  }

  public ASTalterResourceSetParentStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  
  public ASTresource getParent()
  {
      return parentResource;
  }
}
