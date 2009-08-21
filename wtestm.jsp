<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="org.w3c.dom.Document" %>
<%@ page import="org.w3c.dom.NodeList" %>
<%@ page import="org.w3c.dom.Element" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="org.xml.sax.SAXException" %>
<%@ page import="java.io.IOException" %>
<%@ page import="javax.xml.parsers.ParserConfigurationException" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    class T{
        long t=System.currentTimeMillis();
        double rate;
        double amountStop;
        double rate2;
        double amountStop2;
        T() throws Exception{
            findRate();

            findRate2();
        }

        private void findRate2() throws SAXException, IOException, ParserConfigurationException {
            Document d2 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    "http://wm.exchanger.ru/asp/XMLWMList.asp?exchtype=2");
            NodeList queries2 = d2.getElementsByTagName("query");
            for( int i=0; i<queries2.getLength(); i++ ){
                double amount =
                   Double.parseDouble( ((Element)queries2.item(i)).getAttribute("amountout").replace(',','.') );
                double inoutrate =
                   Double.parseDouble( ((Element)queries2.item(i)).getAttribute("inoutrate").replace(',','.') );
                rate2 = inoutrate;
                if( amount>5000 ){
                    amountStop2=amount;
                    break;
                }
            }
        }

        private void findRate() throws SAXException, IOException, ParserConfigurationException {
            Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    "http://wm.exchanger.ru/asp/XMLWMList.asp?exchtype=1");
            NodeList queries = d.getElementsByTagName("query");
            for( int i=0; i<queries.getLength(); i++ ){
                double amount =
                   Double.parseDouble( ((Element)queries.item(i)).getAttribute("amountin").replace(',','.') );
                double outinrate =
                   Double.parseDouble( ((Element)queries.item(i)).getAttribute("outinrate").replace(',','.') );
                rate = outinrate;
                if( amount>5000 ){
                    amountStop=amount;
                    break;
                }
            }
        }
    }

    static List<T> ts = new ArrayList<T>();

    class TRun extends Thread{
        TRun(){
            super("TRun");
        }

        @Override
        public void run() {
            for(;;){
                try{
                  Thread.sleep(60*1000);
                  ts.add(new T());
                }catch(Exception e){}
            }
        }
    }
%>

<%
    if( request.getParameter("xml")!=null ){
        ts.add(new T());
    }
    if( request.getParameter("thread")!=null ){
        if( System.getProperty("wtestm")==null ){
            System.setProperty("wtestm","1");
            new TRun().start();
            out.println("thread started");
        }else{
            out.println("can't start second thread");
        }
    }
%>

<pre>
    data for "amount over 5000"
    t delta r1 r2 amountStop1 amountStop2
<%
    for( T t : ts ){
      out.println(new Timestamp(t.t+7*3600*1000).toString()
              +"   "+String.format("%.2f",(t.rate-t.rate2))
              +"   "+String.format("%.4f",t.rate)
              +" "+String.format("%.4f",t.rate2)

              +"   "+String.format("%.2f",t.amountStop)
              +" "+String.format("%.2f",t.amountStop2)
      );
    }
%>
</pre>
