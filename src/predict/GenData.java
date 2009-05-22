package predict;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 21.05.2009
 * Time: 22:46:40
 * To change this template use File | Settings | File Templates.
 */
public class GenData {
    public static void main(String[] args){
//        YELLOW{f=YELLOW, !=N, fl=WHITE, r=BLUE, ff=BLACK, $=0, ffr=BLACK, frr=WHITE, rr=BLACK, fr=YELLOW, l=YELLOW}
//        WHITE{f=YELLOW, !=L, fl=WHITE, r=BLUE, ff=BLACK, $=0, ffr=BLACK, frr=WHITE, rr=BLACK, fr=YELLOW, l=YELLOW}
        for( int i=0; i<250; i++ ){
            char x = (char)('A'+(int)(Math.random()*5));
            char y = (char)('A'+(int)(Math.random()*5));
            String res="0";
            if( y=='D' || y=='E' ){
                res="1";
            }else if( x=='C'){
                res="2";
            }
            Map view = new HashMap();
            view.put("x",""+x);
            view.put("y",""+y);
            view.put("z",""+(char)('A'+(int)(Math.random()*5)));
            view.put("u",""+(char)('A'+(int)(Math.random()*5)));
            view.put("v",""+(char)('A'+(int)(Math.random()*5)));
            System.out.println(res+view);
        }
    }
}
