/* Generated By:JJTree: Do not edit this line. ASTpermission.java */

package org.objectledge.coral.script.parser;

public class ASTentity extends SimpleNode {
  public ASTentity(int id) {
    super(id);
  }

  public ASTentity(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////////
  
  public long getId()
  {
      return entityId;
  }
  
  public String getName()
  {
      return entityName;
  }
}
