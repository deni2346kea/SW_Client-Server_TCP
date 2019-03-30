import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) {
        try {

            String address = "localhost";
            int port = 8888;

            Socket socket = new Socket(InetAddress.getByName(address), port);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            CountDownLatch startAcceptingMessages = new CountDownLatch(1);

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            Thread writer = new Thread(()-> {
                try {
                    while (true) {
                        System.out.println("Write your name:");
                        String name = br.readLine();

                        out.writeUTF("JOIN " + name + ", " + address + ":" + port);
                        out.flush();

                        String msg = in.readUTF();
                        System.out.println(msg);

                        if (msg.startsWith("J_OK")) {
                            break;
                        }
                    }

                    startAcceptingMessages.countDown();

                    while (true) {
                        try {
                            out.writeUTF(br.readLine());
                            out.flush();
                        } catch (IOException e1) {
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread aliveKeeper = new Thread(()-> {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                            out.writeUTF("ALVE");
                            out.flush();
                        } catch (IOException e1) {
                        }
                    }
                }, 60000);
            });

            Thread reader = new Thread(()-> {
                while(true){
                    try {
                        String msg_in = in.readUTF();
                        System.out.println(msg_in);
                    } catch (IOException e1) {

                    }
                }
            });

            writer.start();
            aliveKeeper.start();

            startAcceptingMessages.await();

            reader.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
