/* Generated By:JJTree: Do not edit this line. ASTfindRolesStatement.java */

package org.objectledge.coral.script.parser;

public class ASTfindRolesStatement extends SimpleNode {
  public ASTfindRolesStatement(int id) {
    super(id);
  }

  public ASTfindRolesStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////////
  
  public ASTsubject getSubject()
  {
      return subject;
  }
  
  public boolean getDirect()
  {
      return direct;
  }
}
