/* Generated By:JJTree: Do not edit this line. ASTalterResourceClassAddPermissionsStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterResourceClassAddPermissionsStatement extends SimpleNode {
  public ASTalterResourceClassAddPermissionsStatement(int id) {
    super(id);
  }

  public ASTalterResourceClassAddPermissionsStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  
  public ASTresourceClass getResourceClass()
  {
      return resourceClass;
  }
  
  public ASTpermissionList getPermissions()
  {
      return permissions;
  }
}
