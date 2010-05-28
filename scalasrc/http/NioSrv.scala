package http

import java.nio.ByteBuffer
import java.net.InetSocketAddress
import java.nio.channels.{SocketChannel, ServerSocketChannel}

class NioSrv{
  val ssockCh = ServerSocketChannel.open
  //ssockCh.setOption(SocketOption.)
  ssockCh.socket.bind(new InetSocketAddress("localhost",8080), 100)
  println("Listen "+ssockCh)
  //val ssock = new ServerSocket(8080, 100)
  //val buf = new Array[Byte](Const.BUF_SIZE)
  //bufNio.rewind
  val meter = new Meter("Srv")
  val proc = new NioSrvProcessor
  new Thread(){
    override def run = {
      while(true){
        val sockCh = ssockCh.accept
        proc.run(sockCh)
      }
    }
  }.start();
}

class NioSrvProcessor{
  val bufResp = ByteBuffer.allocate(Const.BUF_SIZE)
  val arrayResp = bufResp.array

//Content-Type: text/html;charset=UTF-8
//Content-Length: 1451
//Date: Fri, 28 May 2010 10:40:43 GMT
//Connection: close
  val contLen = 10000
  val respContent = Array.fill(contLen)('+').mkString
  val respBytes = ("HTTP/1.1 200 OK\r\n" +
          "Server: ScalaMicroNIO\r\n" +
          "Content-Type: text/html;charset=UTF-8\r\n" +
          "Content-Length: "+contLen+"\r\n" +
          "Connection: close\r\n" +
          "\r\n"+respContent).getBytes
  bufResp.put(respBytes)
  bufResp.limit(respBytes.length)

  val bufReq = ByteBuffer.allocate(Const.BUF_SIZE)
  val arrayReq = bufReq.array
  def run(sockCh: SocketChannel){
    bufReq.rewind()
    val len = sockCh.read(bufReq)
    val from = 0
    val emptyLinePos = findReqEmptyLine(from, len)
    //println("accept "+sockCh+ " pos="+emptyLinePos)
    bufResp.rewind
    val writeLen = sockCh.write(bufResp)
    //println("writeLen "+writeLen)
    sockCh.close()
    //val ch = sock.getChannel
    //val out = sock.getOutputStream
//    while(true){
//      //out write buf
//      bufNio.rewind()
//      meter add sockCh.write(bufNio)
//    }

  }
  def findReqEmptyLine(from:Int, until:Int):Int = {
    var i=from;
    while(i+3 < until){ // 4 bytes \r\n\r\n
      if( arrayReq(i)=='\r' && arrayReq(i+1)=='\n' && arrayReq(i+2)=='\r' && arrayReq(i+3)=='\n' ){
        return i
      }
      i += 1
    }
    -1
  }
}
