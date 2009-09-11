package neuro;

import com.pmstation.common.utils.CountingMap;
import com.pmstation.common.utils.MinMaxFinder;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 10, 2009
 * Time: 6:06:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class FreqGroup {
  public static void main(String[] args){
    new FreqGroup().doit();
  }

  private void doit() {
    String txt="n a new interview, Apple co-founder Steve Jobs discusses a number of issues, including the new lineup of iPods, the Amazon Kindle, and his own health and weight.\n" +
        "\n" +
        "Jobs spoke with David Pogue of The New York Times, and explained that the iPod touch, unlike the iPod nano, was not given a camera because the company wanted to focus on portraying the device as a gaming machine. Jobs said that the company's main goal was to lower the price of the iPod touch as much as possible, and he believes it achieved that with a 8GB model for $199.\n" +
        "\n" +
        "\"What customers told us was, they started to see it as a game machine,\" Jobs said. \"We started to market it that way, and it just took off. And now what we really see is it’s the lowest-cost way to the App Store, and that’s the big draw. So what we were focused on is just reducing the price to $199. We don’t need to add new stuff. We need to get the price down where everyone can afford it.\"\n" +
        "\n" +
        "Third-party cases with camera holes and even photos and video of an alleged prototype of a third-generation iPod touch with camera existed well before Wednesday's event, leading many to assume such a product would be announced. While Jobs said the reason was intentional, AppleInsider received word earlier this week that the camera was removed due to bad hardware.\n" +
        "\n" +
        "The chief executive went on to explain that the iPod nano can record video, but cannot take still pictures, because the sensors required for pictures are too thick to fit into the .02-inch thick device.\n" +
        "\n" +
        "He also vaguely hinted at products coming in the future. He said his absence from the company for several months did not prevent them from working on new products, and that Apple has \"some really good stuff coming up.\" Among those products is expected to be Apple's long-rumored 10-inch touchscreen tablet device, expected to debut in early 2010.\n" +
        "\n" +
        "Jobs also discussed his health, as Wednesday's appearance was his first official in public since receiving a liver transplant earlier this year. The executive told Pogue that he's eating \"a lot of ice cream\" to put on weight, and he probably needs to gain about 30 pounds.\n" +
        "\n" +
        "The Apple co-founder also took a jab at Amazon's Kindle e-book reader. He said \"dedicated devices\" like the Kindle will always remain niche products, while multi-purpose devices \"will win the day.\" Jobs noted that Amazon doesn't announce how many Kindles it has sold, suggesting the market for e-books is very small.\n" +
        "\n" +
        "\"Usually, if they sell a lot of something, you want to tell everybody,\" he said.\n" +
        "\n" +
        "Jobs' return to the stage at Wednesday's keynote was a welcome surprise for Apple fans. He had returned to work at Apple full-time in June, following his liver transplant. In his introduction at the iPod event, he thanked members of the Apple community for their concern, and also expressed gratitude toward co-workers who filled in, in his absence.\n" +
        "\n" +
        "Apple's media event with Jobs' keynote is available for stream from Apple via QuickTime.";

    CountingMap<String> s = new CountingMap<String>();
    for( int i=1; i<8; i++ ){
      Set<String> newPatt = new HashSet<String>();
      for( int j=0; j<txt.length()-i; j++ ){
        String p = txt.substring(j, j + i).toLowerCase();
        s.increment(p);
        newPatt.add(p);
      }
      System.out.println();
      if( i>1 ){
        for( String p : newPatt ){
          long c = s.getValOr0(p);
          MinMaxFinder mm = new MinMaxFinder();
          Map<String,Long> subC = new HashMap<String,Long>();
          for( int k=0; k<p.length(); k++ ){
            String sub = p.substring(0,k)+p.substring(k+1,p.length());
            mm.add(s.getValOr0(sub), sub);
            subC.put(sub, s.getValOr0(sub));
          }
          if( c>mm.getMaxVal()-c ){
            //mm.getMaxVal();
            System.out.println("|"+p+"| "+c+"   "+subC);
          }
        }
      }

    }
  }
}
