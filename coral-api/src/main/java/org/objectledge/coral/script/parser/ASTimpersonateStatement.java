/* Generated By:JJTree: Do not edit this line. ASTimpersonateStatement.java */

package org.objectledge.coral.script.parser;

public class ASTimpersonateStatement extends SimpleNode {
  public ASTimpersonateStatement(int id) {
    super(id);
  }

  public ASTimpersonateStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
