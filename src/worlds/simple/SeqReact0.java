package worlds.simple;

import worlds.intf.WorldGridView;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Rules to learn from this world:
 * <p/>
 * (b - black, w - white, y - yellow - food
 * Main rules:
 * 1) b F -> noop
 * 2) y F  -> +
 * 3) N -> noop
 * <p/>
 * Optional rules:
 * 4) L then R -> noop
 */
public class SeqReact0 implements WorldGridView {
  int availableResults = 0;
  String[] FIELD_INIT = {
          "b        ",
  };

  List<Color> seq = new ArrayList<Color>();
  String correctCmd;

  int width = FIELD_INIT[0].length();
  int height = FIELD_INIT.length;
  Color[][] cdata = new Color[width][height];
  int x0 = 2;
  int y0 = 2;
  int dirIdx = 0;

  Random rnd = new Random();

  void nextSeq() {
    if (rnd.nextBoolean()) {
      seq.add(Color.YELLOW);
      correctCmd = "B";
    } else {
      seq.add(Color.BLACK);
      correctCmd = "C";
    }
    availableResults++;
  }


  public int availableResults() {
    return availableResults;
  }

  public boolean commandWrong(String cmd) {
    return false;
  }

  public SeqReact0() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        Color c;
//        if( i==0 || i==width-1 || j==0 || j==height-1 ){
//          c=Color.BLACK;
//        }else{
//          c=Color.WHITE;
//        }
        char ch = FIELD_INIT[j].charAt(i);
        switch (ch) {
          case'b':
            c = Color.BLACK;
            break;
          case' ':
            c = Color.WHITE;
            break;
          case'y':
            c = Color.YELLOW;
            break;
          default:
            throw new RuntimeException("char=" + ch);
        }
        cdata[i][j] = c;
      }
    }
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public Color getColorDisplay(int x, int y) {
    if (x == x0 && y == y0) {
      return Color.BLUE;
    }
    return cdata[x][y];
  }


  public String getChar(int x, int y) {
//    if( x==x0 && y==y0 ){
//      return getDir().ch;
//    }
    return null;
  }


  public java.util.List<String> commands() {
    return Arrays.asList("A", "B", "C");
  }


  public int command(String cmd) {
    int result = 0;

    if (seq.isEmpty()) {
      if (!cmd.equals(correctCmd)) {
        result = -1;
      }
      nextSeq();
    }
    cdata[0][0] = seq.remove(0);
    return result;
  }


  public Map<String, Object> view() {
    return Collections.singletonMap("f", (Object) getColorDisplay(0, 0));
  }

  public Map<String, Point> sensorLocations(){
    return null;
  }

}
