/* Generated By:JJTree: Do not edit this line. ASTfindPermissionStatement.java */

package org.objectledge.coral.script.parser;

public class ASTfindPermissionStatement extends ASTfindEntityStatement {
  public ASTfindPermissionStatement(int id) {
    super(id);
  }

  public ASTfindPermissionStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
