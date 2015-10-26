/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tdldemoserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dataObj.UserProfile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import messaging.UIReqMessage;
import messaging.UIResMessage;

/**
 *
 * @author wpawgasa
 */
public class TDLDemoServer {

    private static final int PORT = 9890;

    private static HashSet<String> names = new HashSet<String>();
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        System.out.println("The demo server is running");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {

        private String message;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        private Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                ObjectMapper mapper = new ObjectMapper();
                UIResMessage resmsg = new UIResMessage();
                resmsg.msg_name = "client connected";
                out.println(mapper.writeValueAsString(resmsg));

                while (true) {

                    message = in.readLine();
                    System.out.println("Received: " + message);

                    UIReqMessage reqmsg = mapper.readValue(message, UIReqMessage.class);

                    System.out.println(reqmsg.msg_name);
                    if ((reqmsg.msg_name).equalsIgnoreCase("register profile")) {
                        UserProfile profile = mapper.readValue(reqmsg.msg_params, UserProfile.class);
                        System.out.println(profile.getProfileName());
                        if (profile.getProfileName() != null) {

                            synchronized (profile.getProfileName()) {
                                if (!names.contains(profile.getProfileName())) {
                                    names.add(profile.getProfileName());
                                    writers.add(out);
                                    //break;
                                }
                            }
                        }

                    } else {
                        for (PrintWriter writer : writers) {
                            writer.println(message);

                        }
                    }

                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

}
