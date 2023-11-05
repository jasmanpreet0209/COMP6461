package org.example;
import java.util.Scanner;
public class httpc {
    public static void main(String[] args) {
        httpLibrary httplib=new httpLibrary();
        String input="";
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
                else if (inpArr[1].equals("get"))
                {
                    String [] tempArr=new String[inpArr.length-2];
                    int j=0;
                    for(int i = 2;i< inpArr.length;i++)
                    {
                        tempArr[j]=inpArr[i];
                        System.out.println(tempArr[j]);
                        j++;
                    }

                    httplib.get(tempArr);
                }
                else if (inpArr[1].equals("post"))
                {
                    String [] tempArr=new String[inpArr.length-2];
                    int j=0;
                    for(int i = 2;i< inpArr.length;i++)
                    {
                        tempArr[j]=inpArr[i];
                        System.out.println(tempArr[j]);
                        j++;
                    }

                    httplib.post(tempArr);

                }
            }

        }while(input!="0");
    }
}