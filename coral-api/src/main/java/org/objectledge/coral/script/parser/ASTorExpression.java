/* Generated By:JJTree: Do not edit this line. ASTorExpression.java */

package org.objectledge.coral.script.parser;

public class ASTorExpression extends SimpleNode {
  public ASTorExpression(int id) {
    super(id);
  }

  public ASTorExpression(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
