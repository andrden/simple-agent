package mem;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: 31/1/2008
* Time: 18:53:56
* To change this template use File | Settings | File Templates.
*/
class StateDepthElem {
  static StateDepthElem CUR_VIEW = new StateDepthElem("V0");
  static StateDepthElem CUR_CMD = new StateDepthElem("C0");

  boolean viewOrCmd;
  int depth;

  StateDepthElem(boolean viewOrCmd, int depth) {
    this.viewOrCmd = viewOrCmd;
    this.depth = depth;
  }

  public StateDepthElem(String strRepresentation) {
    if( strRepresentation.charAt(0)=='V' ){
      viewOrCmd=true;
    }else if( strRepresentation.charAt(0)=='C' ){
      viewOrCmd=false;
    }else{
      throw new IllegalArgumentException(strRepresentation);
    }
    depth = Integer.parseInt(strRepresentation.substring(1));
  }

  public String toString(){
    return (viewOrCmd?"V":"C")+depth;
  }


  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    StateDepthElem that = (StateDepthElem) o;

    if (depth != that.depth) return false;
    if (viewOrCmd != that.viewOrCmd) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (viewOrCmd ? 1 : 0);
    result = 31 * result + depth;
    return result;
  }
}
