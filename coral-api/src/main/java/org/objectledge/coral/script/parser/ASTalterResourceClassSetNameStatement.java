/* Generated By:JJTree: Do not edit this line. ASTalterResourceClassSetNameStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterResourceClassSetNameStatement extends ASTalterResourceClassStatement {
  public ASTalterResourceClassSetNameStatement(int id) {
    super(id);
  }

  public ASTalterResourceClassSetNameStatement(RML p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  
  public String getNewName()
  {
      return newName;
  }
}
