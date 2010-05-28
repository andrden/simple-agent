package http

import java.nio.ByteBuffer
import java.net.InetSocketAddress
import java.nio.channels.{SocketChannel, ServerSocketChannel}
import java.util.concurrent.LinkedBlockingQueue

class NioSrv{
  val SSOCK_BACKLOG = 5000
  val ssockCh = ServerSocketChannel.open()
  //ssockCh.setOption(SocketOption.)
  ssockCh.socket.bind(new InetSocketAddress("localhost",8080), SSOCK_BACKLOG)
  println("Listen "+ssockCh)
  //val ssock = new ServerSocket(8080, 100)
  //val buf = new Array[Byte](Const.BUF_SIZE)
  //bufNio.rewind
  val meter = new Meter("Srv")

  val taskQueue = new LinkedBlockingQueue[SocketChannel]
  var procCounter = 0
  val procPool = List.fill(1500){ procCounter += 1; new NioSrvProcessor(procCounter, ssockCh) }

  //val proc = new NioSrvProcessor
  new Thread(){
    override def run = {
      while(true){
        //val sockCh = ssockCh.accept()
        //proc.run(sockCh)
        //taskQueue.put(sockCh)
        Thread sleep 10000
      }
    }
  }.start();
}

class NioSrvProcessor(procCounter: Int, ssockCh: ServerSocketChannel){
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

  new Thread(null,null,"NioSrvProcessor"+procCounter, 128*1000){
    override def run = threadRun()
  }.start()

  def threadRun() {
    while(true){
      //run( taskQueue.take )
      run( ssockCh.accept() )
    }
  }

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
