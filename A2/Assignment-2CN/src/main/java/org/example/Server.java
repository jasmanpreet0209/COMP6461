package org.example;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    static String Server = "Server: httpfs/1.0.0";
    static String Date = "Date: ";
    static String AccessControlAllowOrigin = "Access" +
            "-Control-Allow-Origin: *";
    static String AccessControlAllowCredentials =
            "Access-Control-Allow" + "-Credentials: true";
    static String OkStatusCode = "HTTP/1.1 200 OK";
    static String FileNotFoundStatusCode = "HTTP/1.1 404 " +
            "FILE NOT FOUND";
    static String FileOverwrittenStatusCode = "HTTP/1.1 " +
            "201 FILE OVER-WRITTEN";

    static String NewFileCreatedStatusCode = "HTTP/1.1 " +
            "202 NEW FILE CREATED";
    static String NoFilesInDir = "HTTP/1.1 " +
            "203 NO FILES FOUND IN DIRECTORY";
    static String ConnectionAlive = "Connection: keep-alive";
    static boolean debugFlag=false,portFlag=false,dirFlag = false;
    private static ServerSocket serversocket=null;
    static String dirPath="/Users/jasman/Desktop/Concordia/CN/LabAssignments/COMP6461/A2/Assignment-2CN";
    public static void runServer(ServerSocket serversocket) throws IOException, URISyntaxException {
//        System.out.println("debug - Run server called");
        PrintWriter out=null;
        BufferedReader in=null;
        String request="";
        while(true)
        {
            Socket socket = serversocket.accept();
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            request = in.readLine();

            if(request.contains("httpc"))
            {
//                System.out.println("debug - Entered httpc ");
                if (debugFlag)
                    System.out.println("Performing HTTPC operations\n");

                String body="";
                boolean getVerbose=false,inlineflag=false,jsonflag=false;
                String url = request.substring(request.indexOf("http://"), request.length() - 1);

                URI uri = new URI(url);
                if(debugFlag ==true) {
                    System.out.println("Server is processing httpc request: " + request.replace("httpc ", ""));
                }
                if(request.contains("-v"))
                {
                    getVerbose=true;
                }
                List<String> headerData=new ArrayList<>();
                List<String> inlineData=new ArrayList<>();
                    String []reqd=request.split(" ");
                    for (int i=0;i<reqd.length;i++)
                    {
                        if(reqd[i].equals("-h"))
                        {
                            if(reqd[i].contains("json"))
                            {
                                jsonflag=true;
                            }
                            getVerbose=true;
                            headerData.add(reqd[i+1]);
                            i++;
                        }
                        if(reqd[i].equals("-d"))
                        {
                            inlineflag=true;
                            String temp="";
                            while(!reqd[i+1].contains("}"))
                            {
                                reqd[i+1]=reqd[i+1].replace("\'","");
                                System.out.println("arg "+reqd[i+1]);
                                temp+=reqd[i+1]+" ";
                                i++;
                            }
                            temp+=reqd[i+1].replace("\'","");
                            i++;
                            inlineData.add(temp);
                        }
                    }
                if(request.contains("get"))
                {

                    String query=uri.getQuery();
                    String [] queryArr = query.split("&");
                    body += "\t\"args\": {\n";
                    for ( int i=0;i<queryArr.length;i++)
                    {
                        body+="\t\t\"";
                        body+=queryArr[i].split("=")[0];
                        body+="\": \"";
                        body+=queryArr[i].split("=")[1];
                        body+="\",\n";
                    }
                    body+= "\t}, \n";
                    body+="\t\"headers\": {\n";
                    for (int i=0;i<headerData.size();i++)
                    {
                        body+= "\t\t\"" ;
                        body+= headerData.get(i).split(":")[0];
                        body+= "\": \"" ;
                        body+= headerData.get(i).split(":")[1];
                        body+= "\",\n";

                    }
                    body += "\t\t\"Connection\": \"close\",\n";
                    body += "\t\t\"Host\": \"" + uri.getHost() + "\"\n";
                    body += "\t},\n";
                }
                else if (request.contains("post"))
                {
                    int contentlen=0;
                    body += "\t\"args\": {},\n\t\"data\": {";
                    if (inlineflag==true)
                    {
                        for (int i=0;i<inlineData.size();i++)
                        {
                            contentlen+=inlineData.get(i).length();
                            body+=inlineData.get(i);
                            if(inlineData.size()>1 && i<inlineData.size()-1)
                            {
                                body+=",\n";
                            }
                        }
                        body+="}, \n";
                    }
                    body += "\t\"files\": {},\n";
                    body += "\t\"form\": {},\n";
                    body += "\t\"headers\": {\n";
                    for (int i=0;i<headerData.size();i++)
                    {
                        body+= "\t\t\"" ;
                        body+= headerData.get(i).split(":")[0];
                        body+= "\": \"" ;
                        body+= headerData.get(i).split(":")[1];
                        body+= "\",\n";

                    }
                    body += "\t\t\"Connection\": \"close\",\n";
                    body += "\t\t\"Host\": \"" + uri.getHost() + "\"\n";
                    body += "\t\t\"Content-Length\": \"" + contentlen + "\"\n";
                    body += "\t},\n";
                    if(jsonflag==true)
                    {
                        body +="\t\"json\": {\n";

                        for (int i=0;i<inlineData.size();i++)
                        {
                            body +="\t\t" + inlineData.get(i).replace("{","").replace("}","");
                            if(inlineData.size()>1 && i<inlineData.size()-1)
                            {
                                body+=",\n";
                            }
                        }
                        body +="\t},\n";
                    }
                }
                body+="\t\"status\": \"" + OkStatusCode + "\"\n";
                body +="\t\"origin\": \"" + InetAddress.getLocalHost().getHostAddress() + "\",\n";
                body +="\t\"url\": \"" + url + "\"\n";
                body +="}\n";

                String response = body;

                String verboseBody = "";

                if(getVerbose)
                {
                    verboseBody = verboseBody + OkStatusCode+"\n";
                    verboseBody = verboseBody + Date + java.util.Calendar.getInstance().getTime() + "\n";
                    verboseBody = verboseBody + "Content-Type: application/json\n";
                    verboseBody = verboseBody + "Content-Length: "+ body.length() +"\n";
                    verboseBody = verboseBody + "Connection: close\n";
                    verboseBody = verboseBody + "Server: Localhost\n";
                    verboseBody = verboseBody + AccessControlAllowOrigin+"\n";
                    verboseBody = verboseBody + AccessControlAllowCredentials+"\n";

                    response = verboseBody;
                    response = response + body;
                }
                if(debugFlag)
                    System.out.println(response);
                out.write(response);
                out.flush();

                socket.close();


            }
            else if (request.contains("httpfs"))
            {
//                System.out.println("debug - entered httpfs");
                if (debugFlag)
                    System.out.println("Performing HTTPFS operations\n");
                String url = request.split(" ")[3];

                URI uri = new URI(url);
                if(debugFlag ==true) {
                    System.out.println("Server is processing httpfs request: " + request.replace("httpfs ", ""));
                }
                String body = "{\n\t\"args\":{},\n";
                body += "\t\"headers\": {\n\t\t\"Connection\": \"close\",\n";
                body +="\t\t\"Host\": \"" + uri.getHost() + "\"\n\t},\n";
                String []reqData=request.split(" ");
                String code="";
                List<String> filesInDir=getFilesFromDir();
                if(request.contains("get"))
                {
                    if(reqData[2].equals("/"))
                    {
                        body +="\t\"files\": { ";

                        if(filesInDir.size()==0)
                        {
                            body +="},\n";
                            code = NoFilesInDir;
                        }
                        else
                        {
                            for (int i=0;i<filesInDir.size();i++)
                            {
                                body+=filesInDir.get(i);
                                if(i!=filesInDir.size()-1)
                                {
                                    body+=" , ";
                                }
                            }
                            body +="},\n";
                            code=OkStatusCode;

                        }
                    }
                    else
                    {
                        String filename=reqData[2].replace("/","");
                        if (filesInDir.contains(filename))
                        {
                            File f=new File(dirPath+"/"+filename);
                            String fileContent=getContentFromFile(f);
                            body+="\t\"data\": \"" + fileContent + "\",\n";
                            code=OkStatusCode;
                        }
                        else
                            code=FileNotFoundStatusCode;
                    }

                }
                else if (request.contains("post"))
                {
                    String fileContent="";
                    if(filesInDir.contains(reqData[2].replace("/","")))
                        code=FileOverwrittenStatusCode;
                    else
                        code=NewFileCreatedStatusCode;
                    for (int i=4;i<reqData.length;i++)
                    {
                        if(reqData[i].equals("-d"))
                        {
                            while(!reqData[i+1].contains("}"))
                            {
                                reqData[i+1]=reqData[i+1].replace("\'","");
                                fileContent+=reqData[i+1]+" ";
                                i++;
                            }
                            fileContent+=reqData[i+1].replace("\'","");
                            i++;

                        }
                    }
                    printOutputInFile(fileContent,reqData[2].replace("/",""));
                }
                body+="\t\"status\": \"" + code + "\"\n";
                body +="\t\"origin\": \"" + InetAddress.getLocalHost().getHostAddress() + "\",\n";
                body +="\t\"url\": \"" + url + "\"\n";
                body +="}\n";

                if(debugFlag)
                    System.out.println(body);
                out.println(body);
                out.flush();
            }

        }

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
            content+=line;
            line=br.readLine();
        }
        br.close();
        if (debugFlag)
        {
            System.out.println("File read successfully");
        }

        return content;

    }

    private static List<String> getFilesFromDir() {
        List<String> filelist = new ArrayList<>();
        File currentDir=new File(dirPath);
        for (File file : currentDir.listFiles()) {
            if (!file.isDirectory()) {
                filelist.add(file.getName());
            }
        }
        return filelist;

    }

    public static void main(String[] args) {
        int defaultPort=8080;
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

        try{
            serversocket=new ServerSocket(defaultPort);
            if (debugFlag) {
                System.out.println("Server Listening on Port number : " + defaultPort);
            }
        }
        catch (Exception e)
        {
            System.out.println("Problem starting server. Please try again and check your arguments!");
        }
        try
        {
            runServer(serversocket);
            System.out.println("Server Run successfully!");
        }
        catch (IOException e)
        {
            System.out.println("Error starting the server. IOException");
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Error starting the server.");
        }


    }
}
