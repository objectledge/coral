/* Generated By:JJTree: Do not edit this line. ASTselectSpecifier.java */

package org.objectledge.coral.script.parser;

public class ASTselectSpecifier extends SimpleNode {
  public ASTselectSpecifier(int id) {
    super(id);
  }

  public ASTselectSpecifier(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  public String getAttribute()
  {
  	return attributeName;
  }
}
