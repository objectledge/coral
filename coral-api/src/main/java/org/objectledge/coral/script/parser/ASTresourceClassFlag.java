/* Generated By:JJTree: Do not edit this line. ASTresourceClassFlag.java */

package org.objectledge.coral.script.parser;

public class ASTresourceClassFlag extends SimpleNode {
  public ASTresourceClassFlag(int id) {
    super(id);
  }

  public ASTresourceClassFlag(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  
  public String getValue()
  {
      return value;
  }
}
