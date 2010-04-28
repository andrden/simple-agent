package sound

import java.io.{FileInputStream, DataInputStream}
import collection.mutable.ListBuffer

object LocalSimilar{
  val rate = 11025

  def soundFileStream =
    new DataInputStream(new FileInputStream("/opt/project/simple-agent/sounds/shshss.voice"))

  def soundFileData : List[Short] = {
    val b = new ListBuffer[Short];
    val str = soundFileStream
    while(str.available>0){
      b += str.readShort
    }
    b.toList
  }

  def main(args: Array[String]) = {
    printf("sound samples %d,  seconds %f %n", soundFileData.size, soundFileData.size.toDouble / rate)
  }
}

