/* Generated By:JJTree: Do not edit this line. ASTandExpression.java */

package org.objectledge.coral.script.parser;

public class ASTandExpression extends SimpleNode {
  public ASTandExpression(int id) {
    super(id);
  }

  public ASTandExpression(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
