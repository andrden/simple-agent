package audio.sssh;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: Feb 23, 2010
 * Time: 1:46:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExampleSnd {
    static class Elem{
     int beg;
     int end;
     String ch;

        Elem(int beg, int end, String ch) {
            this.beg = beg;
            this.end = end;
            this.ch = ch;
        }
    }

    List<Elem> elems = new ArrayList();

    static ExampleSnd shsh1 = new ExampleSnd();
    static ExampleSnd shsh2 = new ExampleSnd();

    static{
        shsh1.elems.add(new Elem(203,303,"sh"));
        shsh1.elems.add(new Elem(380,483,"ss"));
        shsh1.elems.add(new Elem(565,659,"sh"));
        shsh1.elems.add(new Elem(745,837,"ss"));
        shsh1.elems.add(new Elem(929,1032,"sh"));
        shsh1.elems.add(new Elem(1143,1260,"ss"));
        shsh1.elems.add(new Elem(1401,1417,"chch"));
        shsh1.elems.add(new Elem(1519,1589,"chch"));
        shsh1.elems.add(new Elem(1693,1788,"chch"));
        shsh1.elems.add(new Elem(1840,1969,"sh"));
        shsh1.elems.add(new Elem(2042,2160,"ss"));
        shsh1.elems.add(new Elem(2258,2364,"chch"));
        shsh1.elems.add(new Elem(2464,2558,"ss"));

        shsh2.elems.add(new Elem(xxx203,303,"sh"));
        shsh2.elems.add(new Elem(380,483,"ss"));
        shsh2.elems.add(new Elem(565,659,"sh"));
        shsh2.elems.add(new Elem(745,837,"ss"));
        shsh2.elems.add(new Elem(929,1032,"sh"));
        shsh2.elems.add(new Elem(1143,1260,"ss"));
        shsh2.elems.add(new Elem(1401,1417,"chch"));
        shsh2.elems.add(new Elem(1519,1589,"chch"));
        shsh2.elems.add(new Elem(1693,1788,"chch"));
        shsh2.elems.add(new Elem(1840,1969,"sh"));
        shsh2.elems.add(new Elem(2042,2160,"ss"));
        shsh2.elems.add(new Elem(2258,2364,"chch"));
        shsh2.elems.add(new Elem(2464,2558,"ss"));
    }


}
