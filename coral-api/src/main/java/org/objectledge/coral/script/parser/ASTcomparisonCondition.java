/* Generated By:JJTree: Do not edit this line. ASTcomparisonCondition.java */

package org.objectledge.coral.script.parser;

public class ASTcomparisonCondition extends SimpleNode {
  public ASTcomparisonCondition(int id) {
    super(id);
  }

  public ASTcomparisonCondition(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
