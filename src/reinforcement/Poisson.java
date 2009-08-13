package reinforcement;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Aug 11, 2009
 * Time: 5:36:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Poisson {
  public static void main(String[] args){
    new Poisson().r();
  }
  void r(){
    System.out.println(poisson(10,10));
    System.out.println(poisson(10,0));
    System.out.println(poisson(10,0,20));

    System.out.println(poisson(2000, 1950,2050));
  }

  double poisson(double lambda, int k1, int k2){
    double sum=0;
    for( int i=k1; i<=k2; i++ ){
      sum += poisson(lambda, i);
    }
    return sum;
  }

  double poisson(double lambda, int k){
    return Math.pow(lambda, k)*Math.exp(-lambda)/fact(k);
  }

  double fact(int i){
    double f=1;
    for( int j=1; j<=i; j++ ){
      f*=j;
    }
    return f;
  }

}
