/* Generated By:JJTree: Do not edit this line. ASTIntersectionExpression.java */

package org.objectledge.coral.relation.query.parser;

public class ASTIntersectionExpression extends SimpleNode {
  public ASTIntersectionExpression(int id) {
    super(id);
  }

  public ASTIntersectionExpression(RelationQueryParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RelationQueryParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
