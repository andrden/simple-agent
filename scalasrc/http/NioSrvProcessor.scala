package http

import java.nio.ByteBuffer
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import java.nio.charset.Charset


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
    iterateLines(from, len){ (f,t) =>
      println(f+" "+t+" "+asString(f,t))
      true
    }
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

  type SegFunc = (Int,Int)=>Boolean
  val iso8859_1 = Charset.forName("iso-8859-1")

  def iterateLines(from:Int, until:Int)(f:SegFunc){
    var i=from
    var lineBeg=from
    while(i+1 < until){ // 4 bytes \r\n\r\n
      if( arrayReq(i)=='\r' && arrayReq(i+1)=='\n' ){
        if( !f(lineBeg, i) ){
          return
        }
        i += 2
        lineBeg=i
      }else{
        i += 1
      }
    }
  }

  def asString(from:Int, until:Int)={
    new String(arrayReq, from, until-from, iso8859_1)
  }

  abstract class StartBytesChecker(start:String){
    var starts : Boolean
    def check(from:Int, until:Int){
      var i=from
      var j=0
      while(j<start.length && i<until){
        //if( arrayReq(i) )
        i+=1
        j+=1
      }
    }
  }

  def iterateHeader(from:Int, until:Int){
    var i=from
    while(i+3 < until){

    }
  }

  def findReqEmptyLine(from:Int, until:Int):Int = {
    var i=from
    while(i+3 < until){ // 4 bytes \r\n\r\n
      if( arrayReq(i)=='\r' && arrayReq(i+1)=='\n' && arrayReq(i+2)=='\r' && arrayReq(i+3)=='\n' ){
        return i
      }
      i += 1
    }
    -1
  }
}
