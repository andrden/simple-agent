package worlds;

import worlds.intf.WorldGridView;

import java.awt.*;
import java.util.*;

/**
 * Rules to learn from this world:
 *
 * (b - black, w - white, y - yellow - food
 * Main rules:
 * 1) b F -> noop
 * 2) y F  -> +
 * 3) N -> noop
 *
 * Optional rules:
 * 4) L then R -> noop
 */
public class SeqReact3 implements WorldGridView {
  int availableResults = 0;

  String[] FIELD_INIT = {
          "b        ",
  };

  java.util.List<Color> seq = new ArrayList<Color>();
  String correctCmd;

  int width=FIELD_INIT[0].length();
  int height=FIELD_INIT.length;
  Color[][] cdata = new Color[width][height];
  int x0 =2;
  int y0 =2;
  int dirIdx=0;

  Random rnd = new Random();
  void nextSeq(){
    availableResults++;
    int sw = (int) (Math.random() * 4);
    switch(sw){
      case 0:
        seq.add(Color.YELLOW);
        seq.add(Color.BLACK);
        //seq.add(Color.GREEN);
        correctCmd="A";
        break;
      case 1:
        seq.add(Color.YELLOW);
        seq.add(Color.YELLOW);
        //seq.add(Color.GREEN);
        correctCmd="B";
        break;
      case 2:
        seq.add(Color.BLACK);
        seq.add(Color.YELLOW);
        //seq.add(Color.GREEN);
        correctCmd="B";
        break;
      case 3:
        seq.add(Color.BLACK);
        seq.add(Color.BLACK);
        //seq.add(Color.GREEN);
        correctCmd="C";
        break;
    }

  }


  public int availableResults() {
    return availableResults;
  }


  public SeqReact3(){
    for( int i=0; i<width; i++ ){
      for( int j=0; j<height; j++ ){
        Color c;
//        if( i==0 || i==width-1 || j==0 || j==height-1 ){
//          c=Color.BLACK;
//        }else{
//          c=Color.WHITE;
//        }
        char ch = FIELD_INIT[j].charAt(i);
        switch(ch){
          case 'b':
            c = Color.BLACK;
            break;
          case ' ':
            c = Color.WHITE;
            break;
          case 'y':
            c = Color.YELLOW;
            break;
          default:
            throw new RuntimeException("char="+ch);
        }
        cdata[i][j]=c;
      }
    }
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public Color getColorDisplay(int x, int y){
    if( x==x0 && y==y0){
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
    return Arrays.asList("A","B","C");
  }

  public boolean commandWrong(String cmd) {
    if( seq.isEmpty() ){
      if( !cmd.equals(correctCmd) ){
            return true;
      }
    }
    return false;
  }


  public int command(String cmd) {
    int result=0;

    if( seq.isEmpty() ){
      if( cmd.equals(correctCmd) ){
            result=1;
      }
      nextSeq();
    }
    cdata[0][0] = seq.remove(0);
    if( seq.isEmpty() ){
      cdata[1][0]=Color.GREEN;
    }else{
      cdata[1][0]=Color.WHITE;
    }
    return result;
  }


  public Map<String, Object> view() {
    Map<String,Object> m = new HashMap<String,Object>();
    m.put("f", getColorDisplay(0,0));
    m.put("s", getColorDisplay(1,0));
    m.put("r", ""+ (int)(Math.random()*100) );
    return m;
  }


}
