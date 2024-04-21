import java.net.*;
import java.io.*;

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
        //format of fileInfo: Upload FileNameLength@ FileName Filecontent
        int endFileLength = fileInfo.indexOf("@");
        int fileNameLength = Integer.parseInt(fileInfo.substring(7, endFileLength));
        String fileName = fileInfo.substring(endFileLength + 1, endFileLength + 2 + fileNameLength);
        String fileContent = fileInfo.substring(endFileLength + 2 + fileNameLength);

        if(fileExists(fileName)){
            return false;
        }

        FileWriter upload = null;
        String location = directory + "/" + fileName;
        try {
            upload = new FileWriter(location);
            upload.write(fileContent);
            upload.close();
            System.out.println("Uploaded file: " + fileName + "\n--File Contents--\n" + fileContent + "\n--End File Contents--\nUpload complete");
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        int portNumber = 1025;
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server is listening on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());

                // Handle client request in a separate thread
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + portNumber);
            System.out.println(e.getMessage());
        }
    }

}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        ) {
            out.writeUTF("Welcome. You have connected to the server.\n" +
                    "Server files are located in the ./files directory\n" +
                    "Commands:\n" +
                    "\"List\" - list files stored on server.\n" +
                    "\"Upload <file_name>\" - upload file. Note that files must be in the same directory as the client and server files src folder for this to function properly.\n" +
                    "\"Download <file_name>\" - download file. \n" +
                    "\"Exit\" - quit connection.\n" +
                    "Commands are case-sensitive.");

            String inputLine;
            while ((inputLine = in.readUTF()) != null) {
                if (inputLine.startsWith("Download")) {
                    if (!MathServer.fileExists(MathServer.parseFileName(inputLine))) {
                        out.writeUTF("File not found");
                    } else {
                        String download = MathServer.getFileContent(MathServer.parseFileName(inputLine));
                        out.writeUTF(download);
                    }
                } else if (inputLine.startsWith("Upload")) {
                    if (MathServer.uploadFile(inputLine)) {
                        out.writeUTF("Upload complete");
                    } else {
                        out.writeUTF("File already exists");
                    }
                } else if (inputLine.equals("List")) {
                    out.writeUTF(MathServer.getFiles());
                }
            }
        } catch (IOException e) {
            System.out.println("Client #" + clientSocket.getInetAddress().getHostAddress() + " disconnected");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}