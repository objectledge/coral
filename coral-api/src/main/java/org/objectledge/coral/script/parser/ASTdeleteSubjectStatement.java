/* Generated By:JJTree: Do not edit this line. ASTdeleteSubjectStatement.java */

package org.objectledge.coral.script.parser;

public class ASTdeleteSubjectStatement extends SimpleNode {
  public ASTdeleteSubjectStatement(int id) {
    super(id);
  }

  public ASTdeleteSubjectStatement(RML p, int id) {
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
}
