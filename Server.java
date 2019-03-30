import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private static List<Person> persons = Collections.synchronizedList(new ArrayList<>());
    private static Date date = new Date();

    public static void main(String[] args) {
        try {
            int port = 8888;

            if (args.length < 1) {
                System.out.println("TCP server running on port: " + port);
            } else {
                port = Integer.valueOf(args[0]).intValue();
            }

            ServerSocket serverSocket = new ServerSocket(port);

            while(true) {

                Socket socket = serverSocket.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());

                Thread reader = new Thread(() -> {
                    while(true) {
                        try {
                            String msg_in = in.readUTF();
                            caseInput(msg_in, socket);
                        } catch (IOException var2) {

                        }
                    }
                });

                Thread kicker = new Thread(()-> {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                alive();
                            } catch (Exception e1) {
                            }
                        }
                    }, 60000);
                });

                reader.start();
                kicker.start();
            }


        } catch (Exception c) {
        }
    }

    public static void caseInput(String msg, Socket socket){
        try {
            switch (msg.substring(0, 4).toUpperCase()) {
                case "JOIN":

                    String[] array = msg.split(" ");
                    String nickName = array[1].substring(0, array[1].length()-1);

                    int numOfChar =nickName.length();
                    if(numOfChar>=12){
                        sendMessage("J_ERR: Name has too many characters, please try again", socket);
                        System.out.println(msg);
                        return;
                    }


                    for (Person p : persons) {
                        if (p.getNickName().equals(nickName)) {
                            sendMessage("J_ERR: Name already exist", socket);
                            System.out.println(msg);
                            return;
                        }
                    }

                    String[] server = array[2].split(":");
                    String address = server[0];
                    int port = Integer.parseInt(server[1]);

                    persons.add(new Person(nickName, address, port, socket, date.getTime()));
                    System.out.println(msg);

                    for (Person p : persons) {
                        if (p.getSocket() != socket) {
                            sendMessage(getname(socket) + ": join the chat group", p.getSocket());
                        } else {
                            sendMessage("J_OK Welcome •"+p.getNickName() +"• in chat system\n"
                                    +"Manual :)\n"
                                    +"1. To stay online - type every minute ,,ALVE,,\n" +
                                    "2. For list of active users - type ,,LIST,,\n" +
                                    "3. To leave chat group - type ,,QUITE,,\n" +
                                    "----------------------------------------------------\n"+
                                    "Rules :)\n" +
                                    "1. No hate towards anyone in the chat\n"+
                                    "2. No racism\n"+
                                    "3. No sexism\n"+
                                    ""+ "", socket);
                        }
                    }

                    break;
                case "DATA":
                    for (Person p : persons) {
                        sendMessage(msg.substring(5), p.getSocket());
                        System.out.println(msg.substring(5));
                    }
                    break;
                case "ALVE":
                    for (Person p : persons) {
                        if (p.getSocket().equals(socket)) {
                            Date currentDate = new Date();
                            p.setAlive(currentDate.getTime());
                            System.out.println(getname(socket)+" "+currentDate.toString());
                            try {
                                alive();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case "LIST":
                    for (Person p : persons) {
                        sendMessage(p.getNickName() + ": is online "+p.getPortNumber()+p.getIP(), socket);
                    }
                    break;
                case "QUIT":
                    for (Person p : persons) {
                        sendMessage(getname(socket) + ": Closing down and leaving the group", p.getSocket());
                        socket.close();
                    }
                    break;
                default:
                    for (Person p : persons) {
                        sendMessage(getname(socket) + ": " + msg, p.getSocket());
                    }
                    System.out.println(getname(socket) + ": " + msg);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static void sendMessage(String s, Socket user) {
        try{
            DataOutputStream outToClient = new DataOutputStream(user.getOutputStream());
            outToClient.writeUTF(s);
            outToClient.flush();
        }catch (Exception e){
        }
    }

    private static String getname(Socket socket){
        for (Person p : persons) {
            if(p.getSocket().equals(socket)) {
                return p.getNickName();
            }
        }
        return null;
    }

    private static void alive() throws Exception{
        long date = (new Date()).getTime();

        Iterator<Person> i = persons.iterator();

        while (i.hasNext()) {
            Person p = i.next();

            long alive = p.getAlive();
            System.out.println(getname(p.getSocket())+" last alive time: "+alive);
            if(date - alive > 60000){
                try {
                    i.remove();
                    p.getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static class Person {
        private String nickName;
        private String IP;
        private int portNumber;
        private Socket socket;
        private long alive;

        public Person(String nickName, String IP, int portNumber, Socket socket, long alive) {
            this.nickName = nickName;
            this.IP = IP;
            this.portNumber = portNumber;
            this.socket = socket;
            this.alive = alive;
        }

        public String getNickName() {
            return nickName;
        }

        public String getIP() {
            return IP;
        }

        public int getPortNumber() {
            return portNumber;
        }

        public Socket getSocket() {
            return socket;
        }

        public long getAlive() {
            return alive;
        }

        public void setAlive(long alive){
            this.alive = alive;
        }
        public String toString(){
            return("Name: "+ this.getNickName()+"IP: "+IP+"portNumber: "+portNumber+"Socket: "+this.getSocket()+"Alive signal time: "+this.getAlive());
        }
    }


}


