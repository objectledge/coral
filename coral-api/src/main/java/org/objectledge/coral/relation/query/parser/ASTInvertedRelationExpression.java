/* Generated By:JJTree: Do not edit this line. ASTInvertedRelationExpression.java */

package org.objectledge.coral.relation.query.parser;

public class ASTInvertedRelationExpression extends SimpleNode {
  public ASTInvertedRelationExpression(int id) {
    super(id);
  }

  public ASTInvertedRelationExpression(RelationQueryParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RelationQueryParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
