/* Generated By:JJTree: Do not edit this line. ASTcopyResourceStatement.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package org.objectledge.coral.script.parser;

public class ASTcopyResourceStatement extends SimpleNode {
  public ASTcopyResourceStatement(int id) {
    super(id);
  }

  public ASTcopyResourceStatement(RML p, int id) {
    super(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(RMLVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
  
  public ASTresource getResource()
  {
      return resource;
  }
  
  public ASTresource getTargetResource()
  {
      return targetResource;
  }
  
  public boolean getRecursive()
  {
      return recursive;
  }
}
/* JavaCC - OriginalChecksum=4b70443bccf82a27024c904c1e8a4a93 (do not edit this line) */
