package http

import java.nio.ByteBuffer
import java.net.InetSocketAddress
import java.nio.channels.{SocketChannel, ServerSocketChannel}
import java.util.concurrent.LinkedBlockingQueue
import java.nio.charset.Charset

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

