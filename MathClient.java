import java.io.*;
import java.net.*;

public class MathClient {
    public static String parseFileName(String input){
        String fileName = "";
        //download_
        if(input.substring(0, 1).equals("D")){
            fileName = input.substring(9);
        }
        else{
            //upload_
            fileName = input.substring(7);
        }
        return fileName;
    }

    public static void writeToFile(String fileContent, String fileName){
        FileWriter download = null;
        try {
            download = new FileWriter(fileName);
            download.write(fileContent);
            download.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static String getFileContent(String fileName){
        String location = fileName;
        String content = "";
        int let;
        try {
            FileReader file = new FileReader(location);
            while((let = file.read()) != -1){
                content += (char)let;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    public static void main(String[] args) throws IOException {

        String hostName = "127.0.0.1"; //hard set as localhost
        int portNumber = 1025; //match port number with port server listens on

        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                DataInputStream in = new DataInputStream(new BufferedInputStream(echoSocket.getInputStream()));
                DataOutputStream out = new DataOutputStream(echoSocket.getOutputStream());
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        ) {
            //displays instructions
            System.out.println(in.readUTF());

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if(userInput.equals("Exit")){
                    in.close();
                    out.close();
                    stdIn.close();
                    echoSocket.close();
                    System.out.println("Disconnected from server");
                    System.exit(0); //
                }
                if(userInput.startsWith("Download")){
                    out.writeUTF(userInput);
                    String fileContent = in.readUTF();
                    String filename = parseFileName(userInput);
                    writeToFile(fileContent, filename);
                    System.out.println("Download complete");
                }
                if(userInput.equals("List")){
                    out.writeUTF(userInput);
                    System.out.println(in.readUTF());
                }
                if(userInput.startsWith("Upload")){
                    int fileLength = parseFileName(userInput).length();
                    String fileName = parseFileName(userInput);
                    //format of send: Upload FileNameLength FileName FileContent
                    String send = "Upload " + fileLength + " " + fileName + " " + getFileContent(fileName);
                    out.writeUTF(send);
                    System.out.println(in.readUTF());
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }
}