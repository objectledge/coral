/* Generated By:JJTree: Do not edit this line. ASTalterResourceClassAlterAttributeSetFlagsStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterResourceClassAlterAttributeSetFlagsStatement extends ASTalterResourceClassAlterAttributeStatement {
  public ASTalterResourceClassAlterAttributeSetFlagsStatement(int id) {
    super(id);
  }

  public ASTalterResourceClassAlterAttributeSetFlagsStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////////
  
  public ASTattributeFlagList getFlags()
  {
      return attributeFlags;
  }
}
