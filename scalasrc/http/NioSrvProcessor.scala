package http

import java.nio.ByteBuffer
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import java.nio.charset.Charset
import java.io.IOException

/**
 * 20K req/sec with Keep-Alive
 */
class NioSrvProcessor(procCounter: Int, ssockCh: ServerSocketChannel){
  val bufResp = ByteBuffer.allocate(Const.BUF_SIZE)
  val arrayResp = bufResp.array

//Content-Type: text/html;charset=UTF-8
//Content-Length: 1451
//Date: Fri, 28 May 2010 10:40:43 GMT
//Connection: close
  val contLen = 10000
  val respContent = new String(Array.fill(contLen)('+'))
  val respBytes = ("HTTP/1.1 200 OK\r\n" +
          "Server: ScalaMicroNIO\r\n" +
          "Content-Type: text/html;charset=UTF-8\r\n" +
          "Content-Length: "+contLen+"\r\n" +
          "Connection: close\r\n" +
          "\r\n"+respContent).getBytes
  val respBytesKeepAlive = ("HTTP/1.1 200 OK\r\n" +
          "Server: ScalaMicroNIO\r\n" +
          "Content-Type: text/html;charset=UTF-8\r\n" +
          "Content-Length: "+contLen+"\r\n" +
          "Connection: keep-alive\r\n" +
          "\r\n"+respContent).getBytes

  val bufReq = ByteBuffer.allocate(Const.BUF_SIZE)
  val arrayReq = bufReq.array

  new Thread(null,null,"NioSrvProcessor"+procCounter, 16*1000){
    override def run = threadRun()
  }.start()

  def threadRun() {
    while(true){
      //run( taskQueue.take )
      val sockCh = ssockCh.accept()
      try{
        runChannel( sockCh )
      }finally{
        sockCh.close
      }
    }
  }

  val checkReqConnKeepAlive = new StartBytesChecker("connection: keep-alive")
  val checkReqGET = new StartBytesChecker("GET /")

  def runRequest(sockCh: SocketChannel): Boolean = {
    bufReq.rewind()
    val len =
      try{
        sockCh.read(bufReq)
      }catch{
        case e:IOException if e.getMessage.equals("Connection reset by peer") => return false
      }
    if(len<1){
      return false
    }
    var from = 0
    var emptyLinePos = 0
    do {
      if (from > 0) {
        println("from=" + from)
      }
      checkReqConnKeepAlive.starts = false
      if (!checkReqGET.check(from, len)) {
        error("not GET request from="+from+" len="+len)
      }
      iterateLines(from, len) {
        (f, t) =>
          //println(f + " " + t + " " + asString(f, t))
          checkReqConnKeepAlive.check(f, t)
          true
      }
      //println("keep-alive from client=" + checkReqConnKeepAlive.starts)
      emptyLinePos = findReqEmptyLine(from, len)
      //println("accept "+sockCh+ " pos="+emptyLinePos)
      bufResp.rewind
      if(checkReqConnKeepAlive.starts){
        bufResp.limit(respBytesKeepAlive.length)
        bufResp.put(respBytesKeepAlive)
      }else{
        bufResp.limit(respBytes.length)
        bufResp.put(respBytes)
      }
      bufResp.rewind
      //println("bufResp.rem="+bufResp.remaining)
      val writeLen = sockCh.write(bufResp)
      //println("writeLen="+writeLen)
      from = emptyLinePos + 4
      //println("writeLen "+writeLen)
    } while (checkReqConnKeepAlive.starts && from < len)
    checkReqConnKeepAlive.starts
  }

  def runChannel(sockCh: SocketChannel){
    //println(sockCh)
    while(runRequest(sockCh)){}
  }

  type SegFunc = (Int,Int)=>Boolean
  val iso8859_1 = Charset.forName("iso-8859-1")

  /**
   * Note: f is called with limits to exclude newline symbols
   */
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

  class StartBytesChecker(start:String){
    var starts : Boolean=_
    def check(from:Int, until:Int):Boolean={
      var i=from
      var j=0
      while(j<start.length && i<until){
        if( Character.toLowerCase(start.charAt(j))!=Character.toLowerCase(arrayReq(i)) ){
          return false
        }
        i+=1
        j+=1
      }
      if(j==start.length){
        starts=true
        return true
      }
      return false
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
