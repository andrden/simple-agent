package sound

import java.io.{FileInputStream, DataInputStream}
import collection.mutable.{ArrayBuffer, ListBuffer}

object LocalSimilar{
  val rate = 11025

  def soundFileStream =
    new DataInputStream(new FileInputStream("/opt/project/simple-agent/sounds/shshss.voice"))

  def soundFileStream2 =
    new DataInputStream(new FileInputStream("/opt/project/simple-agent/sounds/shshss2.voice"))

  def soundFileData(stream : DataInputStream) : Array[Short] = {
    val b = new ArrayBuffer[Short];
    val str = stream
    while(str.available>0){
      b += str.readShort
    }
    b.toArray
  }

  def volume1(snd: Array[Short], from: Int, to: Int)={
    var i=from;
    var v=0;
    var sum=0
    while(i<to){
      var vi = snd(i)
      sum += (v-vi).abs
      v=vi
      i+=1
    }
    sum
  }

  def printVolumeStats(snd: Array[Short]){
    Iterator.range(0, snd.length-1000, 1000) foreach ((i:Int) => {println (i + "  "+ volume1(snd,i,i+1000))} )
  }

  def corr(a:Array[Short],b:Array[Short])={
    var suma,sumb,suma2,sumb2,sumab=0.0
    var i=0;
    val n = a.length
    while(i<n){
      val ai = a(i)
      val bi = b(i)
      suma += ai
      sumb += bi
      suma2 += ai*ai
      sumb2 += bi*bi
      sumab += ai*bi
      i+=1
    }

    (n*sumab - suma*sumb) / Math.sqrt(n*suma2 - suma*suma) / Math.sqrt(n*sumb2 - sumb*sumb)
  }

  def printCorr(){
    val len = 100
    //val all = soundFileData(soundFileStream2)
    val all = soundFileData(soundFileStream)

    //val test = all.slice(28000, 28000+len)
    //val test = all.slice(198000, 198000+len)
    //val test = all.slice(320000, 320000+len) // ssss
    val test = soundFileData(soundFileStream).slice(320000, 320000+len) // ssss
    //val test = all.slice(44000, 44000+len) // noise

    println("printCorr()====")
    val sumGrp = new Array[Int](350)
    for( i <- 0 to 329000 ){
      //val beg = 28000+i
      //val beg = 125000+i
      val beg = 0+i
      val c = 100*corr(test, all.slice(beg, beg+len))
      if(c>30){
        sumGrp(  beg/1000 ) += 1
        //println(beg+" "+c )
      }
    }
    println("printCorr()====grp")
    for( i <- 0 until sumGrp.length ){
      //if( sumGrp(i)>0 ){
        val beg = i*1000
        println(beg+"   "+volume1(all,beg,beg+1000)+"  "+sumGrp(i))
      //}
    }
  }

  def printLocalCorr(){
    val all = soundFileData(soundFileStream)

    //val point = 320200
    val point = 28000
    for( i <- 2 to 1000 ){
      val before = all.slice(point-i, point)
      val after = all.slice(point, point+i)
      val c = 100*corr(before, after)
      if(c>20){
        printf("%d  %f  %n", i, c)
      }
    }

  }

  def main(args: Array[String]) = {
    val data = soundFileData(soundFileStream)
    printf("sound samples %d,  seconds %f %n", data.size, data.size.toDouble / rate)
    //printVolumeStats(soundFileData)
    //printCorr()
    printLocalCorr()
  }
}

