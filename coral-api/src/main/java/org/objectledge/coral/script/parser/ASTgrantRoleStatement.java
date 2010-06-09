/* Generated By:JJTree: Do not edit this line. ASTgrantRoleStatement.java */

package org.objectledge.coral.script.parser;

public class ASTgrantRoleStatement extends SimpleNode {
  public ASTgrantRoleStatement(int id) {
    super(id);
  }

  public ASTgrantRoleStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////////
  
  public ASTrole getRole()
  {
      return role;
  }
  
  public ASTsubject getSubject()
  {
      return subject;
  }
  
  public boolean canGrant()
  {
      return canGrant;
  }
}
