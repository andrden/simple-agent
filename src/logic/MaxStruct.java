package logic;

import mem.DeepState;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: 13/3/2008
* Time: 17:54:39
*/
class MaxStruct {
  int allmax=Integer.MIN_VALUE;
  DeepState ds;
  int baseSize;
  boolean valid(){
    return allmax!=Integer.MIN_VALUE;
  }
  void recMax(int m, DeepState ds, int baseSize){
    if( m>allmax ){
      allmax=m;
      this.ds=ds;
      this.baseSize=baseSize;
    }
  }

  public String toString() {
    return allmax + " at "+ds;
  }
}
