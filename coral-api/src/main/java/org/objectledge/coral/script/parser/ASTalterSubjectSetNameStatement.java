/* Generated By:JJTree: Do not edit this line. ASTalterSubjectSetNameStatement.java */

package org.objectledge.coral.script.parser;

public class ASTalterSubjectSetNameStatement extends SimpleNode {
  public ASTalterSubjectSetNameStatement(int id) {
    super(id);
  }

  public ASTalterSubjectSetNameStatement(RML p, int id) {
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
  
  public String getNewName()
  {
      return newName;
  }
}
