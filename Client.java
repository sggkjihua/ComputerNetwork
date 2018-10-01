import java.io.*;
import java.net.*;


public class Client {
    static Integer n_port;
    static Integer req;
    static InetAddress host;
    static String msg;
    public static void main(String[] args) throws IOException{
        // four inputs
        host = InetAddress.getByName(args[0]);
        n_port = Integer.parseInt(args[1]);
        req = Integer.parseInt(args[2]);
        msg = args[3];
        UDPClient UdpClient = new UDPClient();

        // get the available port returned by the server
        Integer r_port = UdpClient.init(n_port,req);
        // close the connection with n_port
        UdpClient.close();

        // open up an TCP connection with r_port and send string to server
        TCPClient TcpClient = new TCPClient();
        TcpClient.sendString(r_port, msg);
    }
}

class UDPClient{
    DatagramSocket ds;
    DatagramPacket dp_receive;
    DatagramPacket dp_send;
    Integer r_port;
    Boolean receiveResponse = false;
    Boolean confirmation = false;
    public int init(Integer n_port, Integer req) throws IOException {
        byte[] buf = new byte[1024];
        ds = new DatagramSocket(getAvailablePort());

        dp_receive = new DatagramPacket(buf, 1024);
        String str_send = req.toString();
        DatagramPacket dp_send= new DatagramPacket(str_send.getBytes(), str_send.length(), Client.host, n_port);
        while(!receiveResponse){
            ds.send(dp_send);
            receive();
        }
        while(!confirmation){
            returnPort(r_port,dp_receive);
            receive();
        }
        return r_port;
    }

    public void receive() throws IOException{
        ds.receive(dp_receive);
        System.out.println("UDPClient received message from UDPServer:");
        String str_receive = new String(dp_receive.getData(),0,dp_receive.getLength());
        String str = str_receive +
                " from " + dp_receive.getAddress().getHostAddress() + ":" + dp_receive.getPort();
        System.out.println(str);
        if (str_receive.equals("OK")){
            System.out.println("Port number has already been confirmed");
            confirmation = true;
        }
        else{
            receiveResponse = true;
            r_port = Integer.parseInt(str_receive);
            returnPort(r_port,dp_receive);
        }
    }

    public void returnPort(Integer portnum,DatagramPacket dp_receive)throws IOException{
        String r_port = portnum.toString();
        dp_send = new DatagramPacket(r_port.getBytes(), r_port.length(), dp_receive.getAddress(), dp_receive.getPort());
        ds.send(dp_send);
    }

    public int getAvailablePort() throws IOException{
        ServerSocket serverSocket =  new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        r_port = port;
        serverSocket.close();
        return r_port;
    }

    public void close(){
        ds.close();
    }
}

class TCPClient {
    InetAddress loc;
    Socket socket;

    public void sendString(Integer r_port, String str ) throws IOException {
        loc = Client.host;
        socket = new Socket(loc, r_port);
        DataInputStream dis = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
        DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(socket.getOutputStream()));
        dos.writeUTF(str);
        dos.flush();
        String rev = dis.readUTF();
        System.out.println("CLIENT_RCV_MSG=" + rev);
        socket.close();
    }
}

