/* Generated By:JJTree: Do not edit this line. ASTalterRelationDeletePairs.java */

package org.objectledge.coral.script.parser;

public class ASTalterRelationDeletePairs extends SimpleNode {
  public ASTalterRelationDeletePairs(int id) {
    super(id);
  }

  public ASTalterRelationDeletePairs(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
