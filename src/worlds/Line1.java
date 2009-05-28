package worlds;

import utils.Utils;
import worlds.intf.WorldGridView;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * 2x2 creature, 2 arms added
 */
public class Line1 implements WorldGridView, Serializable {
  int lastCmdRes=0;
  int availableResults = 0;
  int width = 10;
  int height = 1;

  int pos;

  public Collection<String> targetSensors() {
    return Collections.singleton("$");
  }

  void init(){
    Random r = new Random();
    pos = r.nextInt(width);
  }

  String data(int i){
    if( i==pos ){
      return "0";
    }else{
      return "x";
    }
  }

  public int availableResults() {
    return availableResults;
  }

  public boolean commandWrong(String cmd) {
    return false;
  }


  public Line1() {
    init();
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  /**
   * For painting only, not used from creature algorithm
   */
  public Color getColorDisplay(int x, int y) {
    return Color.WHITE;
  }


  public String getChar(int x, int y) {
    return data(x);
  }

  public List<String> commands() {
    return Arrays.asList("L", "R");
  }

  public void command(String cmd) {
    int result = 0;
    if (cmd.equals("L") ) {
      if( pos==0 ){
        result=1;
        init();
      }else{
        pos--;
      }
    }
    if (cmd.equals("R") ) {
      if( pos==width-1 ){
        result=1;
        init();
      }else{
        pos++;
      }
    }

    lastCmdRes = result;
  }



  public Map<String, Point> sensorLocations(){
    return null;
  }


  public Map<String, Object> view() {
    Map<String, Object> m = new HashMap<String, Object>();
    for( int i=0; i<width; i++ ){
      m.put("z"+i, data(i));
    }
    m.put("$",lastCmdRes);
    return m;
  }


}