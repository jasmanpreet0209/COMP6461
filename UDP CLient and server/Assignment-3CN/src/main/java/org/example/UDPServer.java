package org.example;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class UDPServer {
    static String Server = "Server: httpfs/1.0.0";
    static String Date = "Date: ";
    static String AccessControlAllowOrigin = "Access" +
            "-Control-Allow-Origin: *";
    static String AccessControlAllowCredentials =
            "Access-Control-Allow" + "-Credentials: true";
    static String OkStatusCode = "HTTP/1.1 200 OK";
    static String FileNotFoundStatusCode = "HTTP/1.1 404 " +
            "FILE NOT FOUND";
    static String URLNotFound = "HTTP/1.1 404 " +
            "URL NOT FOUND";
    static String FileOverwrittenStatusCode = "HTTP/1.1 " +
            "201 FILE OVER-WRITTEN";

    static String NewFileCreatedStatusCode = "HTTP/1.1 " +
            "202 NEW FILE CREATED";
    static String UnauthorizedAccessStatusCode = "UNAUTHORIZED ACCESS TO THE REQUESTED DIRECTORY.";
    static String NoFilesInDir = "HTTP/1.1 " +
            "203 NO FILES FOUND IN DIRECTORY";
    static String ConnectionAlive = "Connection: keep-alive";
    static int defaultPort=8080;
    static String dirPath="/Users/jasman/Desktop/Concordia/CN/LabAssignments/COMP6461/A3/Assignment-3CN";
    static Boolean debugFlag=false,portFlag=false,dirFlag=false;
    public static void main(String[] args) {

        System.out.println("Enter the command to run the server");
        Scanner sc=new Scanner(System.in);
        String command = sc.nextLine();

        if (command.length()==0)
        {
            System.out.println("Please enter a valid command to run the server");
        }
        String []commandArr = command.split(" ");
        for (int i =0;i<command.split(" ").length;i++)
        {
            if(commandArr[i].equals("-v")==true)
                debugFlag=true;
            else if(commandArr[i].equals("-p"))
            {
                portFlag=true;
                defaultPort = Integer.parseInt(commandArr[i+1]);
                i++;
            }
            else if (commandArr[i].equals("-d"))
            {
                //security
                dirFlag=true;
                if(commandArr[i+1].contains(dirPath))
                    dirPath=commandArr[i+1];
                else
                    dirPath+="/"+commandArr[i+1];
                i++;
            }
        }
        UDPServer server =new UDPServer();
        Runnable r = () ->{
            try{
//                System.out.println("Debug runnable");
                server.runServer();
            }
            catch (Exception e)
            {
                System.out.println("Error starting server");
                e.printStackTrace();
                System.exit(0);
            }
        };
        Thread thread=new Thread(r);
        thread.start();

    }
    private void runServer()
    {
        System.out.println("Debug run server");

        try(DatagramChannel channel = DatagramChannel.open())
        {
            channel.bind(new InetSocketAddress(defaultPort));
            Packet response;
            ByteBuffer buff=ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
            if (debugFlag)
            {
                System.out.println("ByteBuffer = "+buff);
            }
            while (true)
            {
                buff.clear();
                SocketAddress router = channel.receive(buff);
                if(router!=null)
                {
                    buff.flip();
                    Packet routerPacket = Packet.fromBuffer(buff);
                    buff.flip();
                    System.out.println(routerPacket.toString());
                    String reqPayload= new String(routerPacket.getPayload(), StandardCharsets.UTF_8);

                    if(reqPayload.equals("Hello Server from Client"))
                    {
                        if (debugFlag)
                        {
                            System.out.println("Server Received:" +reqPayload);
                        }
                        response=routerPacket.toBuilder().setPayload("Hello Client from Server".getBytes()).create();
                        channel.send(response.toBuffer(),router);
                        if (debugFlag)
                        {
                            System.out.println("Server sent message: \" Hello Client from Server\"");
                        }
                    }
                    else if (reqPayload.contains("httpfs")||reqPayload.contains("httpc"))
                    {
                        if (debugFlag) {
                            System.out.println("Received: " + reqPayload + " from client");
                        }
                            String responseToRouter=processQuery(reqPayload);
                            response = routerPacket.toBuilder().setPayload(responseToRouter.getBytes()).create();
                            channel.send(response.toBuffer(), router);
                        if (debugFlag)
                        {
                            System.out.println("Server sent message: \""+responseToRouter+"\"");
                        }
                    }
                    else if(reqPayload.equals("Received"))
                    {
                        if (debugFlag) {
                            System.out.println("Received: " + reqPayload + " from client");
                        }
                        response= routerPacket.toBuilder().setPayload("Close".getBytes()).create();
                        channel.send(response.toBuffer(),router);
                        if (debugFlag)
                        {
                            System.out.println("Server sent message: \" Close\"");
                        }

                    }
                    else if (reqPayload.equals("Ok"))
                    {

                        System.out.println("Received: " + reqPayload + " from client");
                    }
                }

            }

        } catch (IOException e) {
            System.out.println("RuntimeException while running server");
            System.exit(0);
        }
    }
    private String processQuery(String payload) throws IOException {
        String reqMethod="";
        String statusCode="";
        String url="";
        String headerKeyValue="";
        String postInlineData="";
        String [] payloadArr=payload.split(" ");
        for (int i=0;i<payloadArr.length;i++)
        {
            if (payloadArr[i].toLowerCase().equals("get"))
                reqMethod="get";
            else if (payloadArr[i].toLowerCase().equals("post"))
                reqMethod="post";
            if(payloadArr[i].startsWith("http://"))
                url=payloadArr[i];
            if(payloadArr[i].equals("-h"))
            {
                headerKeyValue+=payloadArr[i+1]+"\n";
                i++;
            }
            if(payloadArr[i].equals("-d"))
            {
                while(!payloadArr[i+1].contains("}"))
                {
                    payloadArr[i+1]=payloadArr[i+1].replace("\'","");
                    postInlineData+=payloadArr[i+1]+" ";
                    i++;
                }
                postInlineData+=payloadArr[i+1].replace("\'","");
                i++;
            }
            if(payloadArr[i].equals("-f"))
            {
                try {
                    File dataFile = new File(payloadArr[i+1]);
                    BufferedReader fileReader = new BufferedReader(new FileReader(dataFile));
                    String tempFileData;
                    while ((tempFileData = fileReader.readLine()) != null) {
                        tempFileData = tempFileData.replaceAll(" ", "~");
                        postInlineData+=tempFileData+"\n";
                    }
                    postInlineData=postInlineData.replaceAll("\"", "");
                    fileReader.close();
                } catch (Exception e) {
                    System.out.println("Enter Correct File");
                    System.exit(1);
                }
                i++;
            }

        }
        URI uri=null;
        boolean error=false;
        try{
            uri=new URI(url);
        } catch (Exception e) {
            error=true;
            statusCode=URLNotFound;
        }
        if(error==false)
        {
            String body = "{\n\t\"args\":{},\n";
            body += "\t\"headers\": {\n\t\t\"Connection\": \"close\",\n";
            body +="\t\t\"Host\": \"" + uri.getHost() + "\"\n\t},\n";
            String code="";
            List<String> filesInDir=getFilesFromDir();
            if(reqMethod.equals("get"))
            {
                System.out.println(uri.getPath());
                if(uri.getPath().equals(""))
                {
                    System.out.println("Debug processQuery in get");
                    body +="\t\"files\": { ";

                    if(filesInDir.size()==0)
                    {
                        body +="},\n";
                        code = NoFilesInDir;
                    }
                    else {
                        for (int i = 0; i < filesInDir.size(); i++) {
                            body += filesInDir.get(i);
                            if (i != filesInDir.size() - 1) {
                                body += " , ";
                            }
                        }
                        body += "},\n";
                        code = OkStatusCode;

                    }
                }
                else {
                    String filename=uri.getPath().substring(1);
                    File requestedFile=null;
                    if(dirPath.endsWith("/"))
                        requestedFile = new File(dirPath+ filename);
                    else
                        requestedFile = new File(dirPath+"/"+filename);
                    String absolutePath = requestedFile.getCanonicalPath().substring(0,
                            requestedFile.getCanonicalPath().lastIndexOf("/"));
                    String tempDirectory = dirPath.substring(0,
                            dirPath.length() - 1);
                    System.out.println("abs: "+ absolutePath+" dir: "+dirPath + absolutePath.toLowerCase().contains(tempDirectory.toLowerCase()));
                    if (absolutePath.equals(tempDirectory) || absolutePath.contains(tempDirectory))
                    {
                        List<String> filesInDir2 =getFilesFromDir(absolutePath);
                        System.out.println(filesInDir2+" "+filename);
                        if (filesInDir2.contains(filename.split("/")[filename.split("/").length-1])) {
                            File f = new File(dirPath + "/" + filename);
                            String fileContent = getContentFromFile(f);
                            body += "\t\"data\": \"" + fileContent + "\",\n";
                            code = OkStatusCode;
                        }
                        else
                            code=FileNotFoundStatusCode;
                    }
                    else
                        code=UnauthorizedAccessStatusCode;

                }
            }
            else if(reqMethod.equals("post")) {
                String temp = "";
                if (debugFlag) {
                    temp += OkStatusCode + "\n";
                    temp += Date + Calendar.getInstance().getTime() + "\n";
                    temp += "Content-Type: application/json\n";
                    temp += "Content-Length: " + body.length() + "\n";
                    temp += "Connection: close\n";
                    temp += "Server: Localhost\n";
                    temp += AccessControlAllowOrigin + "\n";
                    temp += AccessControlAllowCredentials + "\n";

                    body = temp + body;
                }

                String fileContent = "";

                String filename = uri.getPath().substring(1);

                filename=uri.getPath().substring(1);
                File requestedFile=null;
                if(dirPath.endsWith("/"))
                    requestedFile = new File(dirPath+ filename);
                else
                    requestedFile = new File(dirPath+"/"+filename);
                String absolutePath = requestedFile.getCanonicalPath().substring(0,
                        requestedFile.getCanonicalPath().lastIndexOf("/"));
                String tempDirectory = dirPath.substring(0,
                        dirPath.length() - 1);
                System.out.println("abs: "+ absolutePath+" dir: "+dirPath + absolutePath.toLowerCase().contains(tempDirectory.toLowerCase()));
                if (absolutePath.equals(tempDirectory) || absolutePath.contains(tempDirectory)) {
                    List<String> filesInDir2 = getFilesFromDir(absolutePath);
                    System.out.println(filesInDir2 + " " + filename);
                    if (filesInDir2.contains(filename.split("/")[filename.split("/").length - 1])) {
                        if (filesInDir.contains(filename)) {
                            code = FileOverwrittenStatusCode;
                        } else {

                            code = NewFileCreatedStatusCode;
                        }
                    }
                }
                else
                    code = UnauthorizedAccessStatusCode;

                File f = new File(dirPath + "/" + filename);
                body += "\t\"data\": \"" + postInlineData + "\",\n";

                printOutputInFile(postInlineData,filename);
            }
            body+="\t\"status\": \"" + code + "\"\n";
            body +="\t\"origin\": \"" + InetAddress.getLocalHost().getHostAddress() + "\",\n";
            body +="\t\"url\": \"" + url + "\"\n";
            body +="}\n";
            if(debugFlag)
                System.out.println(body);
            return body;


    }
        else
        {
            return "URL Not FOUND. Please check the URL and try again!";
        }



}
    private List<String> getFilesFromDir(String path) {
        List<String> filelist = new ArrayList<>();
        File currentDir=new File(path);
        for (File file : currentDir.listFiles()) {
            if (!file.isDirectory()) {
                filelist.add(file.getName());
            }
        }
        return filelist;
    }

    private List<String> getFilesFromDir() {
        List<String> filelist = new ArrayList<>();
        File currentDir=new File(dirPath);
        for (File file : currentDir.listFiles()) {
            if (!file.isDirectory()) {
                filelist.add(file.getName());
            }
        }
        return filelist;
    }
    private static void printOutputInFile(String outFileString, String outputFileName) throws IOException {
        FileWriter fileWriter = new FileWriter(dirPath+"/"+outputFileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(outFileString);
        bufferedWriter.close();
        if(debugFlag) {
            System.out.println("Response added to file: " + outputFileName);
        }
    }

    private static String getContentFromFile(File f) throws IOException {
        String content="";
        BufferedReader br=new BufferedReader(new FileReader(f));
        String line=br.readLine();
        while(line!=null)
        {
            content+=line+"\n";
            line=br.readLine();
        }
        br.close();
        if (debugFlag)
        {
            System.out.println("File read successfully");
        }

        return content;

    }
}


