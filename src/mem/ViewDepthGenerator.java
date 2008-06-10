package mem;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 2/5/2008
 * Time: 17:47:56
 */
public class ViewDepthGenerator implements ViewDepthIterator{
  List<ViewDepthElem> toTrack = new ArrayList<ViewDepthElem>();

  final int MAX_GROUP_SIZE=6;
  int groupSize=0;
  List<Integer> group=null;
  int totalCreated=0;

  void initGroup(){
    group = new ArrayList<Integer>(groupSize);
    for( int j=0; j<groupSize; j++ ){
      group.add(j);
    }
  }

  ViewDepth makeViewdDepth(){
    ViewDepthElem[] e = new ViewDepthElem[groupSize];
    for( int i=0; i<groupSize; i++){
      e[i] = toTrack.get(group.get(i));
    }
    totalCreated++;
    return new ViewDepth(e);
  }

  boolean nextGroup(){
    /*
    1234
    1235
    1245
    1345
    2345

    - group 4 of 5 toTrack
     */

    groupSize=groupSize;
    // find from end to beginning to increment
    for( int i=groupSize-1; i>=0; i-- ){
      int max;
      if( i==groupSize-1 ){
        max = toTrack.size()-1;
      }else{
        max = group.get(i+1)-1;
      }
      if( group.get(i)<max ){
        int newVal = group.get(i)+1;
        group.set(i, newVal);
        for( int j=i+1; j<groupSize; j++ ){
          newVal++;
          group.set(j, newVal);
        }
        return true;
      }
    }
    return false;

//    for( int i=0; i<groupSize; i++ ){
//      if( i==groupSize-1 ){
//        if( group.get(i)==toTrack.size()-1 ){
//          return false;
//        }
//        group.set(i, group.get(i)+1);
//        return true;
//      }else{
//        if( group.get(i) + 1 < group.get(i+1) ){
//          group.set(i, group.get(i)+1);
//          return true;
//        }
//        // else continue to next element
//      }
//    }

//    throw new RuntimeException("we never get here");
  }

  public static ViewDepthIterator createWithViewElelms(Set<ViewDepthElem> viewEls){
    ViewDepthGenerator v = new ViewDepthGenerator();
    v.toTrack.addAll(viewEls);
    return v;
  }

  public ViewDepthGenerator(Set<String> viewKeys){
    //must randomize view keys order to avoid biasing
    List<String> keysList = new ArrayList<String>(viewKeys);
    Collections.shuffle(keysList);

    for( int i=0; i<=2; i++ ){
      toTrack.add(new ViewDepthElem(i, Hist.CMD_KEY));
      for( String v : keysList ){
        toTrack.add(new ViewDepthElem(i, v));
      }
    }
    toTrack.add(new ViewDepthElem(3, Hist.CMD_KEY));
  }

  private ViewDepthGenerator(){
  }

  public ViewDepth next(){
    if( group==null ){
      groupSize=1;
      initGroup();
      return makeViewdDepth();
    }
    if( nextGroup() ){
      return makeViewdDepth();
    }
    groupSize++;
    if( groupSize>MAX_GROUP_SIZE || groupSize>toTrack.size() ){
      return null;
    }
    initGroup();
    return makeViewdDepth();
  }

  public static void main(String[] args){
    List<String> s = new ArrayList<String>();
//    s.add("A");
//    s.add("B");
//    s.add("C");
//    s.add("D");
//    s.add("E");
//    s.add("F");
//    s.add("G");

    for( int i=0; i<10; i++ ){
      s.add("a"+i);
    }

    ViewDepthGenerator vdi = new ViewDepthGenerator();
    for( String si : s ){
      vdi.toTrack.add(new ViewDepthElem(0,si));
    }
    for(;;){
      ViewDepth vd = vdi.next();
      if( vd==null ){
        break;
      }
      System.out.println(vd);
    }
    System.out.println(vdi.totalCreated);
  }
}
