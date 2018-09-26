import java.io.*;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    static Integer n_port;
    static Integer req;
    static InetAddress host;
    public static void main(String[] args) throws IOException{
        //用来接收参数的~~~~
        host = InetAddress.getByName(args[0]);
        n_port = Integer.parseInt(args[1]);
        req = Integer.parseInt(args[2]);
        UDPClient UdpClient = new UDPClient();
        Integer r_port = UdpClient.init(n_port,req);
        UdpClient.shutdown();
        TCPClient TcpClient = new TCPClient();
        TcpClient.sendString(r_port);
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
        ds = new DatagramSocket(req);
        dp_receive = new DatagramPacket(buf, 1024);
        String str_send = "13";
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
        //第一次收到portnum
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

    public void shutdown(){
        ds.close();
    }
}

class TCPClient{
    InetAddress loc;
    Socket socket;
    public void sendString(Integer r_port) throws IOException {
        loc = InetAddress.getLocalHost();
        socket = new Socket(loc, r_port);
        DataInputStream dis = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
        DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(socket.getOutputStream()));
        Scanner sc = new Scanner(System.in);

        boolean flag = false;

        while (!flag) {

            System.out.println("Please type in the string:");
            String str = sc.nextLine();
            dos.writeUTF(str);
            dos.flush();
            String rev = dis.readUTF();
            System.out.println("CLIENT_RCV_MSG="+rev);
            while (true) {
                System.out.println("Again?(Y/N)");

                String judge = sc.nextLine();
                if (judge.equalsIgnoreCase("N")) {
                    dos.writeInt(0);
                    dos.flush();
                    flag = true;
                    break;
                } else if (judge.equalsIgnoreCase("Y")) {
                    dos.writeInt(1);
                    dos.flush();
                    break;
                }
            }
        }
        socket.close();
    }
}

