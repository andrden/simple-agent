package audio.cords;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Dec 25, 2009
 * Time: 5:33:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinearRegression {
  // y = a+bx
  double sx, sy, sxy, sx2;
  int n=0;
  void add(double x, double y){
    n++;
    sx += x;
    sy += y;
    sxy += x*y;
    sx2 += x*x;
  }
  void addByIdx(double[] data){
    for( int i=0; i<data.length; i++ ){
      add(i,data[i]);
    }
  }
  double getB(){
    return (sx*sy-n*sxy)/(sx*sx-n*sx2);
  }
  public static void main(String[] args){
    LinearRegression lr = new LinearRegression();
    // 1+2x
    lr.add(3,7);
    lr.add(9,19);
    lr.add(4,9);

//    lr.add(3,7.1);
//    lr.add(9,18.8);
//    lr.add(4,9.3);
    System.out.println(lr.getB());
  }
}
