package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Scanner;

public class httpfs {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String req = "";
            System.out.println("Enter httpfs command: ");
            req = sc.nextLine();
            if (req.length() == 0) {
                System.out.println("Please enter a valid command");
                continue;
            }

            if (req.contains("post") && !req.contains("-d")) {
                System.out.println("Please check the command. No data found to post");
                continue;
            }

            String url = req.split(" ")[3];
            URI uri = null;
            try {
                uri = new URI(url);
                System.out.println(uri.getHost()+ " "+uri.getPort());
                int port = uri.getPort();
                if (port == -1) {
                    port = 8080; // Provide a default port if not specified
                }

                Socket socket = new Socket(uri.getHost(), port);
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                System.out.println("Sending request to server");
                out.print(req + "\n");
                out.flush();
                socket.setSoTimeout(1000);
                String response = "";
                String line=in.readLine();
                try
                {
                    while (line != null) {
                        response += line;
                        response+="\n";
                        line=in.readLine();
                    }

                    out.close();
                    in.close();
                }
                catch (SocketTimeoutException e)
                {
                    socket.close();
                }


                System.out.println("Response from server is:");
                System.out.println(response);
            } catch (Exception e) {
                System.out.println("Error communicating with server! Please check and try again!");
                continue;
            }
        }
    }
}
