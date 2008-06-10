package logic;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 13/3/2008
 * Time: 17:51:49
 */
public class CmdSet implements Serializable {
  static final CmdSet EMPTY = new CmdSet((String)null);

  CmdSet parent;
  boolean groupCmd;
  String command;
  List<CmdSet> parts;
  int execPos=0;
  int targetResult=0;
  String foundFrom;

  public CmdSet(String command) {
    this.command = command;
  }

  public CmdSet(String ... cs) {
    makeParts(cs);
  }

  private void makeParts(String... cs) {
    parts = new ArrayList<CmdSet>();
    for( String c : cs ){
      CmdSet cmdSet = new CmdSet(c);
      cmdSet.parent=this;
      parts.add(cmdSet);
    }
  }

  boolean inGroup(){
    if( parent==null ){
      return false;
    }
    if( parent.groupCmd ){
      return true;
    }
    return parent.inGroup();
  }

  boolean finished(){
    return findNext()==null;
  }

  String nextCmdForHistory(){
    CmdSet n = findNext();
    assert(n.parts==null);
    if( n.inGroup() ){
      return null;
    }
    return n.command;
  }

  String goNext(Map<String,List<String>> cmdGroups){
    CmdSet n = findNext();
    n.expand(cmdGroups);
    n = n.findNext();
    n.shiftPos();
    assert(n.parts==null);
    return n.command;
  }

  void shiftPos(){
    execPos++;
    if( execPos>=len() && parent!=null ){
      parent.shiftPos();
    }
  }

  void expand(Map<String,List<String>> cmdGroups){
    if( command!=null && cmdGroups.containsKey(command) ){
      groupCmd=true;
      List<String> l = cmdGroups.get(command);
      makeParts(l.toArray(new String[l.size()]));
    }
  }

  int len(){
    return parts!=null ? parts.size() : (command==null ? 0 : 1);
  }

  CmdSet findNext(){
    if( parts==null ){
      if( execPos==0 && command!=null ){
        return this;
      }
      return null;
    }
    if( execPos>=parts.size() ){
      return null;
    }
    return parts.get(execPos).findNext();
  }


  public String toString() {
    if( command==null ){
      return ""+parts;
    }
    return ""+command;
  }


  public int getTargetResult() {
    return targetResult;
  }

  public void setTargetResult(int targetResult) {
    this.targetResult = targetResult;
  }

  public void setFoundFrom(String foundFrom) {
    this.foundFrom = foundFrom;
  }

  public String getFoundFrom() {
    return foundFrom;
  }
}
