/* Generated By:JJTree: Do not edit this line. ASTalterRelationRemovePairsStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterRelationRemovePairsStatement extends SimpleNode {
  public ASTalterRelationRemovePairsStatement(int id) {
    super(id);
  }

  public ASTalterRelationRemovePairsStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
