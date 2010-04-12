package test;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: Mar 5, 2010
 * Time: 3:04:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class A {
    public static void main(String[] args) {
        int[] a = new int[3];
        int[] b = new int[3];
        System.out.println("eq "+(a.equals(b))+" "+a.hashCode()+" "+b.hashCode());
    }
}
