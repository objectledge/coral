/* Generated By:JJTree: Do not edit this line. ASTfindResourceClassStatement.java */

package org.objectledge.coral.script.parser;

public class ASTfindEntityStatement extends SimpleNode {
  public ASTfindEntityStatement(int id) {
    super(id);
  }

  public ASTfindEntityStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////////
  
  public long getId()
  {
      return entityId;
  }
  
  public String getName()
  {
      return entityName;
  }
}
