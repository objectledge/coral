/* Generated By:JJTree: Do not edit this line. ASTcreateSubjectStatement.java */

package org.objectledge.coral.script.parser;

public class ASTcreateSubjectStatement extends SimpleNode {
  public ASTcreateSubjectStatement(int id) {
    super(id);
  }

  public ASTcreateSubjectStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
