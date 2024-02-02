package org.example;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;
public class httpc {
    public static void main(String[] args) throws IOException {
        httpLibrary httplib=new httpLibrary();
        String input="";
        Socket socket=null;
        PrintWriter pw=null;
        BufferedReader br=null;
        System.out.println("--------Welcome--------");
        do {
            System.out.println("Enter command or enter 0 to exit. Type \"help\" to know more about available options");
            Scanner sc=new Scanner(System.in);
            input=sc.nextLine();
            if(input.equals("0"))
            {
                System.out.println("Exitting the application as you entered 0!");
                System.exit(0);
            }
            if(input.length()>1)
            {
                String [] inpArr=input.trim().toLowerCase().split(" ");
                if(inpArr[1].equals("help"))
                {
                    if(inpArr.length>2)
                    {
                        httplib.help(inpArr[2]);
                    }
                    else
                    {
                        httplib.help("");
                    }
                }
                else if (inpArr[1].equals("get")||inpArr[1].equals("post"))
                {
                    String url = input.substring(input.indexOf("http://"), input.length() - 1);
                    if(url.contains(" "))
                    {
                        url = url.split(" ")[0];
                    }



                    try
                    {
                        URI uri = new URI(url);
                        String hostName = uri.getHost();
//                        System.out.println(hostName+" "+uri.getPort());
                        socket = new Socket(hostName, uri.getPort());

                    } catch (URISyntaxException e) {
                        System.out.println("Error in url");
                        System.exit(1);
                    } catch (UnknownHostException e) {
                        System.out.println("Error occurred while establishing connection to the server");
                        System.exit(1);
                    } catch (IOException e) {
                        System.out.println("Error occurred while establishing connection to the server");
                        System.exit(1);
                    }

                    try{
                        pw = new PrintWriter(socket.getOutputStream());

                    } catch (IOException e) {
                        System.out.println("Error sending request to server");
                        System.exit(1);
                    }


                    System.out.println("Sending request to Server");
                    pw.write(input + "\n");
                    pw.flush();
                    try
                    {
                        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (IOException e) {
                        System.out.println("Error receiving response from server");
                        System.exit(0);
                    }


                    socket.setSoTimeout(1000);
                    String response="";
                    try
                    {
                        String line=br.readLine();
                        while ((line ) != null) {
                            response+=line + "\n";
                            line=br.readLine();
                        }
                    }
                    catch (SocketTimeoutException s)
                    {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Errror reading response!");
                    }

                    pw.close();
                    br.close();
                    System.out.println("\nResponse from Server : \n " + response);


                }

            }

        }while(input!="0");
    }
}