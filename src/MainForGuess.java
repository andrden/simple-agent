import intf.AlgIntf;
import intf.World;
import logic.Alg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MainForGuess implements World{
  AlgIntf alg = new Alg((World) this);
  String lastInput="0";
  int guess=0;
  int miss=0;

  public List<String> commands() {
    return Arrays.asList("0","1");
  }

  public Map<String, Object> view() {
    return Collections.singletonMap("in", (Object)lastInput);
  }

  public int command(String cmd) {
    throw new UnsupportedOperationException();
  }

  public void run() throws IOException {
    alg.setByCausesOnly(true); // no experimenting

    System.out.println("Enter values (0 or 1):");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while(true){
      String inp = br.readLine().trim();
      if( commands().contains(inp) ){
        String cmd = alg.nextCmd(null);

        // now telling Alg our next value and corresponding result
        lastInput = inp;
        if( cmd.equals(inp ) ){
          alg.cmdCompleted(1);
          guess++;
          System.out.println("- guessed; "+total());
        }else{
          alg.cmdCompleted(0);
          miss++;
          System.out.println("- missed; "+total());
        }
      }
    }
  }

  String total(){
    return "total "+guess+"/"+miss;
  }

  public static void main(String[] args) throws IOException {
    new MainForGuess().run();
  }
}
