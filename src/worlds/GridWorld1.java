package worlds;

import worlds.intf.WorldGridView;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.Serializable;

import utils.Utils;

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
public class GridWorld1 implements WorldGridView, Serializable {
  int availableResults=5;
  String[] FIELD_INIT = {
          "bbbbbbbb",
          "b yy  bb",
          "by by bb",
          "b  b   b",
          "b     bb",
          "bbbbbbbb",
  };
//  String[] FIELD_INIT = {
//          "bbbbbbbbbbb",
//          "b yy  bbbyb",
//          "by by bbb b",
//          "b  b      b",
//          "b     bbbbb",
//          "bbbbbbbbbbb",
//  };


  public int availableResults() {
    return availableResults;
  }

  public boolean commandWrong(String cmd) {
    return false;
  }

  enum Dir{
    left(-1,0,"<"),
    up(0,-1,"^"),
    right(1,0,">"),
    down(0,1,"V");

    int dx;
    int dy;
    String ch;
    Dir(int dx, int dy, String ch){
      this.dx=dx;
      this.dy=dy;
      this.ch=ch;
    }
  }

  int width=FIELD_INIT[0].length();
  int height=FIELD_INIT.length;
  Color[][] cdata = new Color[width][height];
  int x0 =2;
  int y0 =2;
  int dirIdx=0;
  int result=0;
  String prevCommand=null;

  Dir getDir(){
    return Dir.values()[dirIdx];
  }

  Point getFwd(int dxFwd, int dyLeft){
    Dir dirLeft = Dir.values()[dirIdxLeft()];
    return new Point(
            x0 + getDir().dx*dxFwd + dirLeft.dx*dyLeft,
            y0 + getDir().dy*dxFwd + dirLeft.dy*dyLeft);
  }

  Point getFwd(){
    return getFwd(1,0);
  }

  public GridWorld1(){
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

  /**
   * For painting only, not used from creature algorithm
   */
  public Color getColorDisplay(int x, int y){
    Color c = getColor(x, y);
    return c;
  }

  public Color getColor(int x, int y){
    if( x==x0 && y==y0){
      return Color.BLUE;
    }
    if( x<0 || y<0 || x>=getWidth() || y>=getHeight() ){
      return Color.BLACK;
    }
    Color c = cdata[x][y];
    return c;
  }

  public Color getColor(Point p){
    return getColor(p.x, p.y);
  }

  public String getColorName(Point p){
    return Utils.color2name(getColor(p));
  }

  public String getChar(int x, int y) {
    if( x==x0 && y==y0 ){
      return getDir().ch;
    }

    if( crVisible().contains(new Point(x,y)) ){
      return "*";
    }

    return null;
  }

  public List<String> commands() {
    return Arrays.asList("L","R","N","Fa","Fb","E","Ep");
  }

  int dirIdxLeft(){
    int i = dirIdx-1;
    if(i<0){
      i=Dir.values().length-1;
    }
    return i;
  }

  public void command(String cmd) {
    if( cmd.equals("L") ){
      dirIdx = dirIdxLeft();
    }
    if( cmd.equals("R") ){
      dirIdx++;
      if(dirIdx==Dir.values().length){
        dirIdx=0;
      }
    }

    // Fa + Fb composition:
    if( cmd.equals("Fb") /*&& "Fa".equals(prevCommand)*/ ){
      moveFwd();
    }
    if( cmd.equals("E") ){
      Point p = getFwd();
      Color color = getColor(p);
      if( color.equals(Color.ORANGE )){
        cdata[p.x][p.y]=Color.WHITE;
        result=1;
      }
    }
    if( cmd.equals("Ep") ){
      Point p = getFwd();
      Color color = getColor(p);
      if( color.equals(Color.YELLOW )){
        cdata[p.x][p.y]=Color.ORANGE;
      }
    }

    prevCommand = cmd;
  }

  private void moveFwd() {
    Point p = getFwd();
    Color color = getColor(p);
    if( color.equals(Color.WHITE) /*|| color==Color.YELLOW*/ ){
      x0 = p.x; // do move
      y0 = p.y;
//      if( color==Color.YELLOW ){
//        cdata[x0][y0]=Color.WHITE;
//        result=1;
//      }
    }else if( color.equals(Color.BLACK) ){
      result = -1;
    }

    addNewObject();
  }

  int count(Color c){
    int count=0;
    for( int x=0; x<width; x++ ){
      for( int y=0; y<height; y++ ){
        if( getColor(x,y).equals(c) ){
          count++;
        }
      }
    }
    return count;
  }

  void addNewObject(){
    if( count(Color.YELLOW)> width*height*0.1 ){
      return;
    }
    long t0=System.currentTimeMillis();
    while(true){
      int x = (int)(Math.random()*width);
      int y = (int)(Math.random()*height);
      Point pNew = new Point(x, y);
      if( getColor(pNew).equals(Color.WHITE) && distToCr(pNew)>=3 ){
        cdata[x][y]=Color.YELLOW;
        return;
      }
      if( System.currentTimeMillis()-t0>30 ){
        return;
      }
    }
  }

  int distToCr(Point p){
    return Math.max( Math.abs(p.x-x0), Math.abs(p.y-y0) );
  }

  Map<String,Point> visibleCrRelatives(){
    Map<String,Point> m = new HashMap<String,Point>();
    m.put("f", new Point(1,0));
    m.put("ff", new Point(2,0));
    //m.put("fl", (Object)getColor(getFwd(1,1)));
    //m.put("fr", (Object)getColor(getFwd(1,-1)));
    m.put("l", new Point(0,1));
    m.put("r", new Point(0,-1));
    return m;
  }

  Set<Point> crVisible(){
    Set<Point> s = new HashSet<Point>();
    Map<String,Point> rel = visibleCrRelatives();
    for( String k : rel.keySet() ){
      Point abs = getFwd(rel.get(k).x, rel.get(k).y);
      s.add(abs);
    }
    return s;
  }

  public Map<String, Object> view() {
    Map<String, Object> m = new HashMap<String, Object>();
    Map<String,Point> rel = visibleCrRelatives();
    for( String k : rel.keySet() ){
      Point abs = getFwd(rel.get(k).x, rel.get(k).y);
      m.put(k, (Object)getColorName(abs) );
    }
    return m;
  }


  public int result(boolean reset) {
    int r=result;
    if( reset ){
      result=0;
    }
    return r;
  }
}

