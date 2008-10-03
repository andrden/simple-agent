package worlds.simple;

import utils.Utils;
import worlds.intf.WorldGridView;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class State1 implements WorldGridView {
  int availableResults = 0;

  String[] FIELD_INIT = {
          "bb       ",
  };

  int width = FIELD_INIT[0].length();
  int height = FIELD_INIT.length;
  Color[][] cdata = new Color[width][height];
  int x0 = 2;
  int y0 = 2;
  int dirIdx = 0;

  Random rnd = new Random();

  public int availableResults() {
    return availableResults;
  }


  public State1() {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        Color c;
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
    return Arrays.asList("cy", "cn", "cr");
  }

  public boolean commandWrong(String cmd) {
    return false;
  }


  public int command(String cmd) {
    int result = 0;
    Color[] variants = {Color.BLACK, Color.YELLOW, Color.RED};

    Color cnow = cdata[0][0];
    if (cnow == Color.YELLOW && cmd.equals("cy")) {
      cdata[1][0] = Color.GREEN;
    }
    if (cnow == Color.RED && cmd.equals("cr") && cdata[1][0] == Color.GREEN) {
      result = 1;
      cdata[1][0] = Color.BLACK;
    }

    int sw = (int) (Math.random() * variants.length);
    cdata[0][0] = variants[sw];
    return result;
  }


  public Map<String, Object> view() {
    Map<String, Object> m = new HashMap<String, Object>();
    m.put("f", Utils.color2name(getColorDisplay(0, 0)));
    m.put("s", Utils.color2name(getColorDisplay(1, 0)));
    m.put("r", "" + (int) (Math.random() * 100));
    m.put("c", "2008");
    return m;
  }


}
