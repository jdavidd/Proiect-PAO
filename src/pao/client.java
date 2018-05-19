/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * A simple Swing-based client for the capitalization server.
 * It has a main frame window with a text field for entering
 * strings and a textarea to see the results of capitalizing
 * them.
 */
public class client {

    private static BufferedReader in;
    private static PrintWriter out;
 
    
    private static ObjectOutputStream outt;
    private static ObjectInputStream inn;

    /**
     * Constructs the client by laying out the GUI and registering a
     * listener with the textfield so that pressing Enter in the
     * listener sends the textfield contents to the server.
     */
    public client() {
   }
    
    
    public static String read() {
        String mesajServer = new String();
        try {
            mesajServer = in.readLine();
            
        } catch (IOException ex) {
            Logger.getLogger(client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mesajServer;
    }
    public static void send(String codedMessage) {
        out.println(codedMessage);
    }

    public static ArrayList<String> readplayer() {
        ArrayList<String> players = new ArrayList<>();
        try {
            players = (ArrayList<String>) inn.readObject();
         } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return players;
    }

    /**
     * Implements the connection logic by prompting the end user for
     * the server's IP address, connecting, setting up streams, and
     * consuming the welcome messages from the server.  The Capitalizer
     * protocol says that the server sends three lines of text to the
     * client immediately after establishing a connection.
     * @throws java.io.IOException
     */
    public void connectToServer() throws IOException {

        // Get the server address from a dialog box.
      
        // Make connection and initialize streams
        Socket socket = new Socket("localhost", 9999);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Consume the initial welcoming messages from the server
        outt = new ObjectOutputStream(socket.getOutputStream());
        inn = new ObjectInputStream(socket.getInputStream());
    }
    static void visible() {
       
    }
    /**
     * Runs the client application.
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        client client = new client();
        client.connectToServer();
        singup.mainn();
    }
    
    public static class ListenServer extends Thread {
        
        @Override
        public void run() {
            System.out.println("Threadul de listen server a inceput!!!!");
            while(true) {
                try {
                    String messageServer = in.readLine();
                    ArrayList<String> cerere = decode(messageServer);
                    int requestType;
                    requestType = Integer.parseInt(cerere.get(0));
                } catch (IOException ex) {
                    Logger.getLogger(client.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            
        }
        private static ArrayList decode(String message) {
            
            ArrayList<String> localList=new ArrayList<>();
            int length = message.length();
            int poz=0;
            String subExit = message.substring(0,1);
            if(subExit.equals("-")) {
               subExit =  subExit.concat(message.substring(1,2));
               localList.add(subExit);
               subExit = message.substring(3);
               localList.add(subExit);
            } else {
                localList.add(subExit);
                subExit = message.substring(2);
                localList.add(subExit);
            }

            return localList;
        }
    }
}
