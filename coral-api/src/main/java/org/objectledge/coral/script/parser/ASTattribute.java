/* Generated By:JJTree: Do not edit this line. ASTattribute.java */

package org.objectledge.coral.script.parser;

public class ASTattribute extends SimpleNode {
  public ASTattribute(int id) {
    super(id);
  }

  public ASTattribute(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  
  public String getName()
  {
      return attributeName;
  }
  
  public String getValue()
  {
      return value;
  }
}
