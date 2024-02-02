package org.example;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.nio.channels.SelectionKey.OP_READ;

public class UDPClient {
    static String routerhost = "localhost";
    static int ackNum = 0;
    static int timeout=3000;
    static int seqNum = 0;
    static List<Long> receivedPackets = new ArrayList<>();
    static String url="";
    public static void main(String[] args) throws URISyntaxException {
        int routerPort=3000;

        Scanner sc=new Scanner(System.in);

        String input="";
        if(args.length==0)
        {
            do {
                System.out.println("Please enter command or enter 0 to exit the system:");
                input= sc.nextLine();
                if(input.equals("0")==true)
                {
                    System.out.println("You entered command to exit the System! GoodBye");
                    System.exit(0);
                }
                url=input.substring(input.indexOf("http://"), input.length());
                String serverHost = new URI(url).getHost();
                int serverPort = new URI(url).getPort();
                System.out.println("Debug +serverhost: "+serverHost+"serverport: "+serverPort);
                SocketAddress routerAddress = new InetSocketAddress(routerhost,routerPort);
                InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
                handshake(routerAddress, serverAddress);
                runClient(routerAddress, serverAddress, input);

            }while(input.equals("0")==false);
        }
        else
        {
            for (int i=0;i< args.length;i++)
            {
                if(args[i].startsWith("http://"))
                {
                    url=args[i];
                }

            }
            input = Arrays.toString(args);
            String serverHost = new URI(url).getHost();
            int serverPort = new URI(url).getPort();
            SocketAddress routerAddress = new InetSocketAddress(routerhost,routerPort);
            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
            handshake(routerAddress, serverAddress);
            runClient(routerAddress, serverAddress, input);
        }

    }

    private static void runClient(SocketAddress routerAddress, InetSocketAddress serverAddress, String input) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            seqNum++;
            Packet p = new Packet.Builder().setType(0).setSequenceNumber(seqNum)
                    .setPortNumber(serverAddress.getPort()).setPeerAddress(serverAddress.getAddress())
                    .setPayload(input.getBytes()).create();
            channel.send(p.toBuffer(), routerAddress);
            System.out.println("Sequence Number: "+seqNum+" Message: \"" + input + "\"  to router");
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            selector.select(timeout);
            Set<SelectionKey> keys = selector.selectedKeys();
            if (keys.isEmpty()) {
                System.out.println("\nNo response after timeout----- Sending again-----");
                System.out.println("RESENDING Sequence Number: "+seqNum+" Message: \"" + input + "\"  to router");
                resend(channel, p, routerAddress);
            }
            ByteBuffer buff = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buff);
            buff.flip();

            Packet response = Packet.fromBuffer(buff);
            String payload = new String(response.getPayload(), StandardCharsets.UTF_8);
            //selective repeat ARQ. whatever is received out of order, store it . increase window size
            if(!receivedPackets.contains(response.getSequenceNumber())){
                receivedPackets.add(response.getSequenceNumber());
                System.out.println("\nResponse from Server : \n" + payload);

//                String responseCode = payload.split("\n")[0].split(" ")[1];
//
//                if (responseCode.equals("201")) {
//                    System.out.println("Request succeeded and data  overwritten in file.");
//
//                }
//                if (responseCode.equals("202")) {
//                    System.out.println("Request fulfilled and new resource created.");
//
//
//                }
//                if (responseCode.equals("203")||responseCode.equals("200")) {
//                    System.out.println("Request succeeded.");
//
//                }
//                if (responseCode.equals("404")) {
//                    System.out.println("The server couldnt find anything matching the Request url.");
//                    System.out.println(payload);
//                    return;
//                }
            }
            seqNum++;
            Packet AckPacket = new Packet.Builder().setType(0).setSequenceNumber(seqNum).setPortNumber(serverAddress.getPort()).setPeerAddress(serverAddress.getAddress())
                    .setPayload(input.getBytes()).create();
            channel.send(AckPacket.toBuffer(), routerAddress);

            System.out.println("Sending Sequence Number: "+seqNum+ " and ACK for: "+input);

            channel.configureBlocking(false);
            selector = Selector.open();
            channel.register(selector, OP_READ);
            selector.select(timeout);

            keys = selector.selectedKeys();
            if (keys.isEmpty()) {
                System.out.println("\nNo response after timeout----- Sending again-----");
                System.out.println("RESENDING Sequence Number: "+seqNum+" Message: \"" + input + "\"  to router");
                resend(channel, p, routerAddress);
            }

            buff.flip();

            keys.clear();

            seqNum++;
            Packet closePacket = new Packet.Builder().setType(0).setSequenceNumber(seqNum)
                    .setPortNumber(serverAddress.getPort()).setPeerAddress(serverAddress.getAddress())
                    .setPayload("Ok".getBytes()).create();
            channel.send(closePacket.toBuffer(), routerAddress);
            System.out.println("Sequence Number "+seqNum+" sent OK");
            System.out.println("Connection closed!");
        } catch (IOException e) {
            System.out.println("Error sending message to Server in runClient");
            throw new RuntimeException(e);
        }
    }

    private static void handshake(SocketAddress routerAddress, InetSocketAddress serverAddress) {
        try (DatagramChannel channel = DatagramChannel.open()){
            String msg = "Hello Server from Client";
            seqNum++;
            //SYN packet
            Packet p = new Packet.Builder().setType(0).setSequenceNumber(seqNum).setPortNumber(serverAddress.getPort())
                    .setPeerAddress(serverAddress.getAddress())
                    .setPayload(msg.getBytes()).create();
            channel.send(p.toBuffer(), routerAddress);
            System.out.println("Sequence Number: "+seqNum+" Message: \"" + msg + "\"  to router");
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            selector.select(timeout);
            Set<SelectionKey> keys = selector.selectedKeys();
            if (keys.isEmpty()) {
                System.out.println("\nNo response after timeout----- Sending again-----");
                System.out.println("RESENDING Sequence Number: "+seqNum+" Message: \"" + msg + "\"  to router");
                resend(channel, p, routerAddress);
            }
            ByteBuffer buff = ByteBuffer.allocate(Packet.MAX_LEN);
            Packet response = Packet.fromBuffer(buff);
            String routerPayload = new String(response.getPayload());
            System.out.println("\n ---RECEIVED---!!");
            receivedPackets.add(response.getSequenceNumber());
            keys.clear();


        } catch (IOException e) {
            System.out.println("Error Encountered in handshaking!! Unable to establish connection");
            throw new RuntimeException(e);
        }
    }

    private static void resend(DatagramChannel channel, Packet p, SocketAddress routerAddress) {
        try {
            channel.send(p.toBuffer(), routerAddress);
            if (new String(p.getPayload()).equals("Received")) {
                ackNum++;
            }
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            selector.select(timeout);

            Set<SelectionKey> keys = selector.selectedKeys();
            if (keys.isEmpty() && ackNum<10) {
                System.out.println("\nNo response after timeout----- Sending again-----");
                System.out.println("RESENDING Sequence Number: "+seqNum+" to router");
                resend(channel, p, routerAddress);
            }
        } catch (IOException e) {
            System.out.println("Error encountered while resending");
            throw new RuntimeException(e);
        }

    }

}

