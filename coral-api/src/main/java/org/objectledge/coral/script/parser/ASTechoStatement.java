/* Generated By:JJTree: Do not edit this line. ASTechoStatement.java */

package org.objectledge.coral.script.parser;

public class ASTechoStatement extends SimpleNode {
  public ASTechoStatement(int id) {
    super(id);
  }

  public ASTechoStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////////
  
  public String getMessage()
  {
      return message;
  }
}
