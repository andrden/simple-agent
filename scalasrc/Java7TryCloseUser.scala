import java.io.{Reader, FileReader, BufferedReader}
import Java7TryClose._

class BufReader(r:Reader) extends BufferedReader(r) with Closable

// using new  "tryClose" contructs
object Java7TryCloseUser extends Application{

  tryClose ( new BufferedReader(new FileReader("/etc/resolv.conf")) with Closable ) { br =>
     for(i<- 1 to 10) println(br.readLine())
  }

  tryClose (  new BufferedReader(new FileReader("/etc/resolv.conf")) with Closable,
             new BufferedReader(new FileReader("/etc/hosts")) with Closable
    ) { (br1, br2) =>
     for(i<- 1 to 10) println(br1.readLine())
     for(i<- 1 to 10) println(br2.readLine())
  }

  tryClose (  new BufReader(new FileReader("/etc/resolv.conf")),
             new BufReader(new FileReader("/etc/hosts")),
            new BufReader(new FileReader("/etc/fstab"))
    ) { (br1, br2, br3) =>
     for(i<- 1 to 10) println(br1.readLine())
     for(i<- 1 to 10) println(br2.readLine())
     for(i<- 1 to 10) println(br3.readLine())
  }

}