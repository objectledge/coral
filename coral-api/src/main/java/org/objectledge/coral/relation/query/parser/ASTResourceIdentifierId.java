/* Generated By:JJTree: Do not edit this line. ASTResourceIdentifierId.java */

package org.objectledge.coral.relation.query.parser;

public class ASTResourceIdentifierId extends SimpleNode {

  String identifier;

  public ASTResourceIdentifierId(int id) {
    super(id);
  }

  public ASTResourceIdentifierId(RelationQueryParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RelationQueryParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  public String getIdentifier() {
	return identifier;
  }
}
