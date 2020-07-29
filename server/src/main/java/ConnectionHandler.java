import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;


public class ConnectionHandler implements Runnable {

    private DataInputStream is;
    private DataOutputStream os;

    public ConnectionHandler(Socket socket) throws IOException, InterruptedException {
        System.out.println("Connection accepted");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        Thread.sleep(2000);
    }


    @Override
    public void run() {
        String fileName;
        File file;
        byte [] buffer = new byte[1024];
        while (true) {
            try {
                String command = is.readUTF();
                if (command.equals("./upload")) {
                    fileName = is.readUTF();
                    System.out.println("fileName: " + fileName);
                    long fileLength = is.readLong();
                    System.out.println("fileLength: " + fileLength);
                    file = new File(Server.serverPath + "/" + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        for (long i = 0; i < (fileLength / 1024 == 0 ? 1 : fileLength / 1024); i++) {
                            int bytesRead = is.read(buffer);
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    os.writeUTF("OK");
                }
                if (command.equals("./download")){
                    fileName = is.readUTF();
                    System.out.println("fileName: " + fileName);
                    file = new File(Server.serverPath + "/" + fileName);
                    if(file.exists()){
                        os.writeUTF("+");
                        os.writeLong(file.length());
                        FileInputStream fis = new FileInputStream(file);
                        buffer = new byte[1024];
                        while (fis.available() > 0) {
                            int bytesRead = fis.read(buffer);
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                    } else {
                        os.writeUTF("-");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
