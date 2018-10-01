import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static int tcp_port;
    static int req_code;
    static int n_port;
    static UDPServer UDPserver;
    static TCPServer TcpServer;

    public static void main(String[] args) throws IOException {
        req_code = Integer.parseInt(args[0]);

        UDPserver = new UDPServer();
        n_port = UDPserver.getAvailablePort();
        System.out.println("SERVER_PORT="+ n_port);

        while (true){
            UDPserver.init(n_port);
            while(!UDPserver.conneted){
                UDPserver.receive();
            }
            UDPserver.conneted = false;
            TcpServer = new TCPServer();
            TcpServer.receiveString(tcp_port);
        }
    }
}

class UDPServer{
    Integer r_port;
    DatagramSocket ds;
    DatagramPacket dp_receive;
    DatagramPacket dp_send;
    Boolean conneted = false;
    public void init(Integer n_port) throws IOException{
        byte[] buf = new byte[1024];
        ds = new DatagramSocket(n_port);
        dp_receive = new DatagramPacket(buf, 1024);
    }

    public void receive() throws IOException{
        ds.receive(dp_receive);
        String str_receive = new String(dp_receive.getData(),0,dp_receive.getLength());
        Integer order = Integer.parseInt(str_receive);
        if(order.equals(Server.req_code)){
            Integer port = getAvailablePort();
            System.out.println("SERVER_TCP_PORT="+ port.toString());
            sentPort(port,dp_receive);
        }
        else{
            acknowledge(order, dp_receive);
        }
    }

    public int getAvailablePort() throws IOException{
        ServerSocket serverSocket =  new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        r_port = port;
        return port;
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
            Server.tcp_port = r_port;
            conneted = true;
            ds.close();
        }else{
            String acknowledgement = "NO";
            dp_send = new DatagramPacket(acknowledgement.getBytes(), acknowledgement.length(), dp_receive.getAddress(), dp_receive.getPort());
            ds.send(dp_send);
            ds.close();
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

