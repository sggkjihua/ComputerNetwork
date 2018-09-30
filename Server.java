import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static Boolean conneted = false;
    static int re_port;
    static int req_code;
    public static void main(String[] args) throws IOException {
        req_code = Integer.parseInt(args[0]);
        UDPServer UDPs = new UDPServer();
        int n_port = UDPs.getAvailablePort();
        UDPs.init(n_port);
        System.out.println("SERVER_PORT="+ n_port);
        while(!conneted){
            UDPs.receive();
        }
        TCPServer TcpServer = new TCPServer();
        TcpServer.receiveString(re_port);
    }
}

class UDPServer{
    Integer r_port;
    DatagramSocket ds;
    DatagramPacket dp_receive;
    DatagramPacket dp_send;
    Boolean confirmation = false;
    public void init(Integer n_port) throws IOException{
        byte[] buf = new byte[1024];
        ds = new DatagramSocket(n_port);
        dp_receive = new DatagramPacket(buf, 1024);
    }

    public void receive() throws IOException{
        ds.receive(dp_receive);
        System.out.println("I came here");
        String str_receive = new String(dp_receive.getData(),0,dp_receive.getLength());
        Integer order = Integer.parseInt(str_receive);
        if(order.equals(Server.req_code)){
            Integer port = getAvailablePort();
            System.out.println("SERVER_TCP_PORT="+ port.toString());
            System.out.println();
            sentPort(port,dp_receive);
        }
        else{
            if (!confirmation) {
                acknowledge(order, dp_receive);
            }
        }
    }

    public int getAvailablePort() throws IOException{
        ServerSocket serverSocket =  new ServerSocket(0); //读取空闲的可用端口
        int port = serverSocket.getLocalPort();
        r_port = port;
        serverSocket.close();
        return r_port;
    }

    public void sentPort(Integer port, DatagramPacket dp_receive) throws IOException{
        String r_port = port.toString();
        dp_send = new DatagramPacket(r_port.getBytes(), r_port.length(), dp_receive.getAddress(), dp_receive.getPort());
        ds.send(dp_send);
    }


    public void acknowledge(Integer confirmPort, DatagramPacket dp_receive) throws IOException{
        if (confirmPort.equals(r_port)){
            String acknowledgement = "OK";
            dp_send = new DatagramPacket(acknowledgement.getBytes(), acknowledgement.length(), dp_receive.getAddress(), dp_receive.getPort());
            ds.send(dp_send);
            ds.close();
            Server.re_port = r_port;
            Server.conneted = true;
        }else{
            String acknowledgement = "NO";
            dp_send = new DatagramPacket(acknowledgement.getBytes(), acknowledgement.length(), dp_receive.getAddress(), dp_receive.getPort());
            ds.send(dp_send);
        }
    }
}

class TCPServer {
    public void receiveString(Integer r_port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(r_port);
        Socket socket = serverSocket.accept();
        DataInputStream dis = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
        DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(socket.getOutputStream()));
        String str = dis.readUTF();
        String mes = "SERVER_RCV_MSG="+str;
        System.out.println(mes);
        String reverse = reverseString(str);
        dos.writeUTF(reverse);
        dos.flush();
        socket.close();
        serverSocket.close();
    }
    public static String reverseString(String str){
        String reverse = "";
        for(int i = str.length() - 1; i >= 0; i--)
        {
            reverse = reverse + str.charAt(i);
        }
        return reverse;
    }
}

