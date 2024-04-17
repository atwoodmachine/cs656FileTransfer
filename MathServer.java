import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MathServer {
    /* Expected path of server's file directory */
    public static final String directory = "./files";

    public static String getFiles(){
        File dir = new File(directory);
        File[] files = dir.listFiles();
        String filesToString = "";
        for(int i = 0; i < files.length; i++ ){
           filesToString += files[i].getName() + "\n";
        }

        return filesToString;
    }

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

    public static Boolean fileExists(String fileName){
        File dir = new File(directory);
        File[] files = dir.listFiles();
        for(int i = 0; i < files.length; i++ ){
            if(fileName.equals(files[i].getName())){
                return true;
            }
        }
        return false;
    }

    public static String getFileContent(String fileName){
        String location = directory + "/" + fileName;
        String content = "";
        int let;
        try {
            FileReader download = new FileReader(location);
            while((let = download.read()) != -1){
                content += (char)let;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    public static boolean uploadFile(String fileInfo){
        //format of fileInfo: Upload FileNameLength FileName Filecontent
        int fileNameLength = Integer.parseInt(fileInfo.substring(7,8));
        String fileName = fileInfo.substring(9, 9 + fileNameLength);
        String fileContent = fileInfo.substring(10 + fileNameLength);

        if(fileExists(fileName)){
            return false;
        }

        FileWriter upload = null;
        String location = directory + "/" + fileName;
        try {
            upload = new FileWriter(location);
            upload.write(fileContent);
            upload.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        /* any port from 1024-65535 can be used as the port number */
        int portNumber = 1025;
        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket clientSocket = serverSocket.accept();
                DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        ) {
            out.writeUTF("Welcome. You have connected to the server.\n" +
                    "Server files are located in the ./files directory\n" +
                    "Commands:\n" +
                    "\"List\" - list files stored on server.\n" +
                    "\"Upload <file_name>\" - upload file. Note that you must enter the entire file path if the file is not in the same directory as the client and server files.\n" +
                    "\"Download <file_name>\" - download file. \n" +
                    "\"Exit\" - quit connection.\n" +
                    "Commands are case-sensitive.");

            String inputLine;
            while ((inputLine = in.readUTF()) != null) {
                if(inputLine.startsWith("Download")){
                    if(!fileExists(parseFileName(inputLine))){
                        out.writeUTF("File not found");
                    }
                    else{
                        String download = getFileContent(parseFileName(inputLine));
                        out.writeUTF(download);
                    }
                }
                if(inputLine.startsWith("Upload")){
                    if(uploadFile(inputLine)){
                        out.writeUTF("Upload complete");
                    }
                    else{
                        out.writeUTF("File already exists");
                    }
                }
                if(inputLine.equals("List")){
                    out.writeUTF(getFiles());
                }
                if(inputLine.equals("Exit")){
                    out.writeUTF("Exit command");
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}