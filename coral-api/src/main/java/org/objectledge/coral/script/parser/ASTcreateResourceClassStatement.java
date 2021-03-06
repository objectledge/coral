/* Generated By:JJTree: Do not edit this line. ASTcreateResourceClassStatement.java */

package org.objectledge.coral.script.parser;

public class ASTcreateResourceClassStatement extends ASTcreateClassStatement {
  public ASTcreateResourceClassStatement(int id) {
    super(id);
  }

  public ASTcreateResourceClassStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////

  public ASTresourceClassFlagList getFlags()
  {
      return resourceClassFlags;
  }
  
  public ASTresourceClassList getParents()
  {
      return superClasses;
  }
  
  public ASTattributeDefinitionList getAttributes()
  {
      return attributeDefinitions;
  }
  
  public ASTpermissionList getPermissions()
  {
      return permissions;
  }
}
