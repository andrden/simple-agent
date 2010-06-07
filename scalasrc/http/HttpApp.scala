package http

import java.nio.ByteBuffer
import java.net.{InetSocketAddress, Socket, ServerSocket}
import java.nio.channels.{SocketChannel, ServerSocketChannel}

/**
 * Created by IntelliJ IDEA.
 * User: дом
 * Date: 27 трав 2010
 * Time: 22:00:03
 * To change this template use File | Settings | File Templates.
 */

object Const{
  val BUF_SIZE=1024*16

// val BUF_SIZE=1024*64, single thread srv-cli, BIO, Notebook Core2Duo Vista JDK7 - 200 Mbytes/sec
// val BUF_SIZE=1024*64, single thread srv-cli, NIO direct buffer, Notebook Core2Duo Vista JDK7 - 240 Mbytes/sec

// val BUF_SIZE=1024*64, single thread srv-cli, BIO, PC Core2Duo 3.1GHz Linux Ubuntu10 JDK6 - 940 Mbytes/sec
// val BUF_SIZE=1024*64, single thread srv-cli, NIO direct buffer, PC Core2Duo 3.1GHz Linux Ubuntu10 JDK6 - 1080 Mbytes/sec
//  val BUF_SIZE=1024*64, single thread srv-cli, NIO heap buffer, PC Core2Duo 3.1GHz Linux Ubuntu10 JDK6 - 1200 Mbytes/sec
}

class Meter(name:String){
  var count : Double=0
  var t=System.currentTimeMillis
  def add(c:Double) = {
    count += c
    val tnow = System.currentTimeMillis
    val dt = tnow - t
    if( dt>1000 ){
      println( name+" speed "+ count*1000/dt )
      t = tnow
      count = 0
    }
  }
}

class Srv{
  val ssock = new ServerSocket(8080, 100)
  ssock.setPerformancePreferences(0,0,1)
  val buf = new Array[Byte](Const.BUF_SIZE)
  val bufNio = ByteBuffer.allocate(Const.BUF_SIZE)
  val meter = new Meter("Srv")
  new Thread(){
    override def run = {
      while(true){
        val sock = ssock.accept
        sock.setSendBufferSize(Const.BUF_SIZE*16)
        println("accept "+sock)
        //val ch = sock.getChannel
        val out = sock.getOutputStream
        while(true){
          out write buf
          //ch write bufNio
          meter add buf.size
        }
      }
    }
  }.start();
}


class Client{
  val meter = new Meter("Client")
  val s = new Socket("localhost", 8080)
  s.setReceiveBufferSize(Const.BUF_SIZE*16)
  println("connected "+s)
  val buf = new Array[Byte](Const.BUF_SIZE)
  val in = s.getInputStream
  while(true){
    meter add in.read(buf)/1024./1024.
  }
}

class ClientNio{
  val meter = new Meter("Client")
  //val s = new Socket("localhost", 8080)
  val sch = SocketChannel.open(new InetSocketAddress("localhost",8080))
  println("connected "+sch)
  val bufNio = ByteBuffer.allocate(Const.BUF_SIZE)
  while(true){
    bufNio.rewind()
    meter add sch.read(bufNio)/1024./1024.
  }
}

object TimeTrack{
  def log[B](msg: String, minTime: Long, f: ()=>B) = {
    val t0 = System.currentTimeMillis
    try{
      f()
    }finally{
      val t1 = System.currentTimeMillis
      val d = t1-t0
      if( d>minTime ){
        println("TimeTrack "+msg+" "+d+" ms")
      }
    }
  }
}


object HttpApp{
  def main(args:Array[String]){
    //val Srv = new Srv
    val Srv = new NioSrv
    //val Client = new Client
    //val Client = new ClientNio

    //testArraySpeed
    //testArraySpeedNIO
  }

  def testArraySpeed {
    val buf = new Array[Byte](Const.BUF_SIZE)
    "GET /sdfsd".getBytes.copyToArray(buf)

    def isGet(bufi:Array[Byte], len:Long) = len>=3 && bufi(0)=='G' && bufi(1)=='E' && bufi(2)=='T'

    1 to 7 foreach ((x) => TimeTrack.log("Array[Byte]",0, ()=>{
      var i:Long=3*1000*1000*1000;
      println(i)
      while(i>0){
        if( !isGet(buf,i+5) ){
          error("aaaaa i="+i)
        }
        i -= 1
      }
    } ))
  }

  def testArraySpeedNIO {
    val buf = ByteBuffer.allocate(Const.BUF_SIZE)
    println( buf.hasArray )

//    "GET /sdfsd".getBytes.copyToArray(buf)
//
//    def isGet(bufi:Array[Byte], len:Long) = len>=3 && bufi(0)=='G' && bufi(1)=='E' && bufi(2)=='T'
//
//    1 to 7 foreach ((x) => TimeTrack.log("Array[Byte]",0, ()=>{
//      var i:Long=3*1000*1000*1000;
//      println(i)
//      while(i>0){
//        if( !isGet(buf,i+5) ){
//          error("aaaaa i="+i)
//        }
//        i -= 1
//      }
//    } ))
  }
}