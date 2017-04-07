import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client{
  public static void main(String[] args) throws Exception{
    if(args.length != 1){
      System.out.println("Please specify port number.");
      System.exit(0);
    }
    int port = Integer.parseInt(args[0]);
    Socket socket = null;
    try{
      File file = new File("data.txt");
      Scanner inputFromFile = new Scanner(file);

      String inp = inputFromFile.nextLine();
      String exp = inp;
      while(inputFromFile.hasNextLine()){
        // Connect to server
          socket = new Socket("localhost", port);
          Scanner in = new Scanner(socket.getInputStream());
          PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
          exp += "\n" + inputFromFile.nextLine();
          // Send to server.
          out.println(exp);
          System.out.print(exp + " = ");
          // Receive from server.
          System.out.println(in.nextLine());
          exp = inp;
      }
    }catch(FileNotFoundException e){
      e.printStackTrace();
    }finally{
      if(socket != null) socket.close();
    }
  }
}
