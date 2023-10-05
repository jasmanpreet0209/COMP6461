package org.example;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class httpLibrary {
    public void get(String []arguments)
    {
        boolean verboseFlag=false;
        boolean headerFlag=false;
        boolean outputFlag=false;
        String outputFileName="";
        String url = "";
        List<String> headerKey=new ArrayList<>();
        List<String> headerVal=new ArrayList<>();
        for(int i=0;i<arguments.length;i++)
        {
            if(arguments[i].equals("-h"))
            {
                headerFlag = true;
                headerKey.add(arguments[i+1].split(":")[0]);
                headerVal.add(arguments[i+1].split(":")[1]);
                i++;
            }
            if(arguments[i].equals("-v"))
            {
                verboseFlag=true;
            }
            if(arguments[i].contains("http://")||arguments[i].contains("https://")) {

                url = arguments[i].replace("\'","");
            }
            if(arguments[i].contains("-o"))
            {
                outputFlag=true;
                outputFileName= arguments[i+1];
                i++;
            }

        }
        if(url==null||url.length()==0)
        {
            System.out.println("Empty URL! Please check the url and try again!");
            System.exit(0);
        }
        URI uri = null;
        try
        {
            uri= new URI(url);
        } catch (Exception e) {
            System.out.println("Invalid URL! Please check the url and try again!");
            return;
        }
        String host =uri.getHost();
        Socket socket;
        try
        {
            socket=new Socket(host,80);
            String headerValue  = "";
            if(headerFlag)
            {
                for (int i=0;i<headerKey.size();i++)
                {
                    headerValue+=headerKey.get(i)+":"+headerVal.get(i)+"\n";
                }
            }

            StringBuilder request=new StringBuilder();
            try{
                PrintStream out = new PrintStream(socket.getOutputStream()); //for sending the data to the stream , we can easily write text with methods like println().
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //for reading from the socket stream in order to easily read text with methods like readLine()
                String r;
                if(uri.getPath()!=null &&uri.getQuery()!=null)
                    r="GET " + uri.getPath()+"?"+uri.getQuery() + " HTTP/1.0";
                else if (uri.getPath()==null)
                    r="GET /?"+uri.getQuery() + " HTTP/1.0";
                else
                    r="GET "+ uri.getPath()+ " HTTP/1.0";
                request.append(r);
                request.append("\n");

                request.append("Host: "+host+"\n");
                if(headerFlag) {
                    request.append(headerValue);
                    request.append("User-Agent: Concordia-HTTP/1.0\n");
                }
                out.println(request);
                out.println();

                String line=in.readLine();
                String verboseData="";
                if(line.contains("HTTP/1.1")) {
                    while (!line.contains("Access-Control-Allow-Credentials")) {

                        verboseData += line;
                        verboseData += "\n";
                        line = in.readLine();
                    }
                    verboseData+=line;
                    verboseData+="\n";
                }

                StringBuilder outFileString= new StringBuilder();
                if(verboseFlag==true)
                {
                    if(outputFlag)
                    {
                        outFileString.append(verboseData+"\n");
                    }
                    else
                        System.out.println(verboseData);
                }
                line=in.readLine();

                if(outputFlag)
                {
                    while (line!=null)
                    {
                        outFileString.append(line+"\n");
                        line = in.readLine();
                    }
                    try {
                        printInfile(outFileString, outputFileName);
                    }
                    catch (IOException e)
                    {
                        System.out.println("Unable to write to file! Please check and try again!!");
                        return;
                    }
                }
                else {
                    while (line != null) {
                        System.out.println(line);
                        line = in.readLine();
                    }
                }


            }
            catch (Exception e)
            {
                System.out.println("Error occurred, please try again!");
                return;
            }


        } catch (Exception e) {
            System.out.println("Sorry, Host is not found"+ host);
            return;
        }



    }

    private void printInfile(StringBuilder outFileString, String outputFileName) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFileName,true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(outFileString.toString());
        bufferedWriter.close();
        System.out.println("Output added to file: "+outputFileName);
    }

    public void post(String[] arguments)
    {
        boolean outputFlag=false;
        String outputFileName="";
        boolean verboseFlag=false;
        boolean headerFlag=false;
        boolean dataFlag = false;
        boolean fileFlag=false;
        String url = "";
        String filePath="";
        String fileVal="";
        int contentlen=0;
        List<String> headerKey=new ArrayList<>();
        List<String> headerVal=new ArrayList<>();
        String dataKeyValue="";
        for(int i=0;i<arguments.length;i++)
        {
            if(arguments[i].equals("-h"))
            {
                headerFlag = true;
                headerKey.add(arguments[i+1].split(":")[0]);
                headerVal.add(arguments[i+1].split(":")[1]);
                i++;
            }
            if(arguments[i].equals("-v"))
            {
                verboseFlag=true;
            }
            if(arguments[i].equals("-d") ||arguments[i].equals("--d") )
            {
                if(fileFlag==true)
                {
                    System.out.println("You have entered both -d and -f parameters. please change the command to include only one of them.\n\n\n");
                    return;

                }
                dataFlag=true;
                String temp="";
                while(!arguments[i+1].contains("}"))
                {
                    arguments[i+1]=arguments[i+1].replace("\'","");
                    System.out.println("arg "+arguments[i+1]);
                    temp+=arguments[i+1]+" ";
                    i++;
                }
                temp+=arguments[i+1].replace("\'","");
                i++;
                dataKeyValue+=temp;
                contentlen=dataKeyValue.length();
                dataKeyValue+="\n";
            }
            if(arguments[i].contains("-o"))
            {
                outputFlag=true;
                outputFileName= arguments[i+1];
                i++;
            }
            if(arguments[i].equals("-f"))
            {
                if(dataFlag==true)
                {
                    System.out.println("You have entered both -d and -f parameters. please change the command to include only one of them.\n\n\n");
                    return;
                }
                fileFlag = true;
                filePath="./"+arguments[i+1];
                try
                {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
                    String inp;
                    while((inp = bufferedReader.readLine()) != null)
                    {
                        fileVal+=inp;
                        fileVal+="\n";
                    }
                    bufferedReader.close();
                }
                catch (Exception e)
                {
                    System.out.println("Error occurred while reading file! Please check file exists and try again!");
                    return;
                }
                contentlen=fileVal.length();
                i++;
            }
            if(arguments[i].contains("http://")||arguments[i].contains("https://")) {

                url = arguments[i].replace("\'","");
            }

        }
        if(url==null||url.length()==0)
        {
            System.out.println("Empty URL! Please check the url and try again!");
            return;
        }
        URI uri = null;
        try
        {
            uri= new URI(url);
        } catch (URISyntaxException e) {
            System.out.println("Invalid URL! Please check the url and try again!");
            return;
        }
        String host =uri.getHost();
        Socket socket;
        try
        {
            socket=new Socket(host,80);
            String headerValue  = "";
            if(headerFlag)
            {
                for (int i=0;i<headerKey.size();i++)
                {
                    headerValue+=headerKey.get(i)+":"+headerVal.get(i)+"\n";
                }
            }
            StringBuilder request=new StringBuilder();
            try{
                PrintStream out = new PrintStream(socket.getOutputStream()); //for sending the data to the stream , we can easily write text with methods like println().
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //for reading from the socket stream in order to easily read text with methods like readLine()


                String r = "POST " + url + " HTTP/1.0";
                request.append(r);
                request.append("\n");
                request.append("Host: "+uri.getHost()+"\n");
                request.append("Content-Length: "+contentlen +"\n");
                if(headerFlag) {
                    request.append(headerValue);
                    request.append("User-Agent: Concordia-HTTP/1.0\n");
                }

                out.println(request);
                if(dataFlag)
                {
                    out.println(dataKeyValue);
                }
                else if (fileFlag)
                {
                    out.println(fileVal);
                }
                out.println();



//                String line=in.readLine();
//                if(verboseFlag==true)
//                {
//                    while (line!=null)
//                    {
//                        line=in.readLine();
//                        System.out.println(line);
//                    }
//                }
//                else
//                {
//                    while (line!=null)
//                    {
//                        if (line.startsWith("{") && line != null) {
//                            if (line != null)
//                                System.out.println(line);
//                            while (!line.startsWith("}") && line != null) {
//                                line = in.readLine();
//                                if (line != null)
//                                    System.out.println(line);
//                            }
//                        }
//                        line = in.readLine();
//
//                    }
//                }
                String line=in.readLine();
                String verboseData="";
                if(line.contains("HTTP/1.1")) {
                    while (!line.contains("Access-Control-Allow-Credentials")) {

                        verboseData += line;
                        verboseData += "\n";
                        line = in.readLine();
                    }
                    verboseData+=line;
                    verboseData+="\n";
                }
                StringBuilder outFileString= new StringBuilder();
                if(verboseFlag==true)
                {
                    if(outputFlag)
                    {
                        outFileString.append(verboseData+"\n");
                    }
                    else
                        System.out.println(verboseData);
                }
                line=in.readLine();
                if(outputFlag)
                {
                    while (line!=null)
                    {
                        outFileString.append(line+"\n");
                        line = in.readLine();
                    }
                    try {
                        printInfile(outFileString, outputFileName);
                    }
                    catch (IOException e)
                    {
                        System.out.println("Unable to write to file! Please check and try again!!");
                        return;
                    }
                }
                else {
                    while (line != null) {
                        System.out.println(line);
                        line = in.readLine();
                    }
                }

            }
            catch (Exception e)
            {
                System.out.println("Error occurred, please try again!");
                return;
            }


        } catch (Exception e) {
            System.out.println("Sorry, Host is not found"+ host);
            return;
        }
    }
    public void help(String type)
    {
        if(type.trim().equals(""))
        {
            System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n" +
                    "Usage:\n " +
                    "\thttpc command [arguments] \n " +
                    "The commands are: \n" +
                    "\tget\texecutes a HTTP GET request and prints the response. \n" +
                    "\tpost\texecutes a HTTP POST request and prints the response. \n " +
                    "\thelp\tprints this screen.\n\n" +
                    "Use \"httpc help [command]\" for more information about a command.");
        }
        else if (type.trim().toLowerCase().equals("get"))
        {
            System.out.println("usage: httpc get [-v] [-h key:value] URL \n\n" +
                    "Get executes a HTTP GET request for a given URL.\n\n " +
                    "\t-v\t\t\t\tPrints the detail of the response such as protocol, status,\n" +
                    "and headers.\n" +
                    "\t-h key:value\tAssociates headers to HTTP Request with the format\n" +
                    "'key:value'.");
        }
        else if (type.trim().toLowerCase().equals("post"))
        {
            System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f " +
                    "file]" +
                    " URL \nPost executes a HTTP POST request for a given URL with inline " +
                    "data " +
                    "or from file.\n\t-v Prints the detail of the response such as " +
                    "protocol, " +
                    "status, and headers.\n\t-h key:value Associates headers to HTTP " +
                    "Request " +
                    "with the format 'key:value'.\n\t-d string Associates an inline data to" +
                    " the" +
                    " body HTTP POST request.\n\t-f file Associates the content of a file " +
                    "to " +
                    "the body HTTP POST request.\n\t-o filename.txt Print result in the " +
                    "file\nEither [-d] or [-f] can be used but not " +
                    "both.");
        }
        else
        {
            System.out.println("Invalid Option or flag:- " + type+".\nPlease type \"http help\" to know more about the commands and options available.");
        }

    }
}
