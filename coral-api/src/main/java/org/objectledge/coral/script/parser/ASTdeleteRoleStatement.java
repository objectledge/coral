/* Generated By:JJTree: Do not edit this line. ASTdeleteRoleStatement.java */

package org.objectledge.coral.script.parser;

public class ASTdeleteRoleStatement extends SimpleNode {
  public ASTdeleteRoleStatement(int id) {
    super(id);
  }

  public ASTdeleteRoleStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  
  public ASTrole getRole()
  {
      return role;
  }
}
