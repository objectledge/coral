/* Generated By:JJTree: Do not edit this line. ASTroleList.java */

package org.objectledge.coral.script.parser;

public class ASTroleList extends SimpleNode {
  public ASTroleList(int id) {
    super(id);
  }

  public ASTroleList(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
