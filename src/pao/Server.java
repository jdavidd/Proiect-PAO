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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class Server {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    //Aceasta lista contine toti utilizatorii logati in server
    private static ArrayList <Player> listaUtilizatori;
    private static conexiuneBazaUtilizator conexiuneUtilizator;
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        int clientNumber=0;
        listaUtilizatori=new ArrayList<>();
        conexiuneUtilizator=new conexiuneBazaUtilizator();
        try(ServerSocket listener = new ServerSocket(9999)) {
            while(true){
            new Player(listener.accept(),clientNumber++).start();
            }
        }
    }
    
    public static class Player extends Thread {
        private final int clientNumber;
        private final Socket socket;
        private int idCompanie;
        private int idUser;
        private int idClient;
        private String numeUser;
        private  BufferedReader in;         
        private  PrintWriter out;            
        private final Boolean mutex;       
        
        private ObjectOutputStream outt;
        private ObjectInputStream inn;

        
        public Player(Socket socket,int clientNumber){
            idCompanie=0;
            numeUser=new String();
            this.clientNumber=clientNumber;
            this.socket=socket;
            
            try {
                in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out=new PrintWriter(socket.getOutputStream(),true);
                
                outt = new ObjectOutputStream(socket.getOutputStream());
                inn = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.out.println("There is a new connection with client number "+clientNumber);
            mutex=false;
        }

        @Override
        public void run(){
            try {
                //conexiuneUtilizator.selectAll();
             
                OUTER:
                while (true) {
                   
                    String request=in.readLine();
                    ArrayList<String> cerere=decode(request);
                    int requestType=Integer.parseInt(cerere.get(0));
                    System.out.println(request + " " + requestType);
                    
                    if(requestType<-1)//Daca s-a facut o cerere cu un cod de identificare nevalid
                    {
                        out.println("0;Cere nerecunoscuta de server, a fost transmis un cod mai mic decat -1");
                    }
                    else
                    switch (requestType) {
                    //S-a facut o cere de intrerupere a conexiunii
                        case -1:
                        {
                            
                            out.println("EXIT_CLIENT");
                            System.out.println("am inchis clientul");
                            break OUTER;
                        }
                    //S-a facut o cerere de inregistrare
                        case 1:
                        {System.out.println("MA INREGISTREZZZZ");
                            
                            int resultConexiune=conexiuneUtilizator.inregistrare(cerere.get(1),cerere.get(2),cerere.get(3));
                        switch (resultConexiune) {
                            case -1:
                            {
                                out.println("0;Inregistrare nereusita, au aparut probleme tehnice");
                                break;
                            }
                            case 0:
                            {
                                out.println("1;Inregistrare Reusita");
                                break;
                            }
                            case 1:
                            {
                                out.println("0;ID deja folosit");
                                break;
                            }
                            case 2:
                                out.println("0;Username deja folosit");
                                break;
                            case 3:
                                out.println("0;Inregistrare nereusita, incercati din nou");
                                break;
                            default:
                                break;
                        }
                            break;
                        }
                    //S-a facut o cere de login
                        case 2:
                        {
                            ArrayList<Integer> resultConexiune = new ArrayList<>();
                            resultConexiune = conexiuneUtilizator.login(cerere.get(1),cerere.get(2));
                            int result = resultConexiune.get(0);
                            if(result > 0)
                            {
                                idCompanie = resultConexiune.get(1);
                                idUser = resultConexiune.get(0);
                                //System.out.println(idCompanie+" "+idUser);
                                numeUser=cerere.get(1);
                                Boolean ok=true;
                                int lungime=listaUtilizatori.size();
                        
                                for(int i=0;i<lungime;++i)
                                    if(idUser ==(listaUtilizatori.get(i).getID()))
                                    {
                                        ok=false;
                                    }
                                if(ok==true)
                                {
                                    System.out.println("logatt");
                                    listaUtilizatori.add(this);
                                    out.println("1;Login reusit");
                         
                                }
                                else
                                out.println("0;Exista deja un utilizator logat in acest cont");
                                
                            }
                        switch (result) {
                            case -1:
                                out.println("0;Login nereusit, au aparut probleme tehnice");
                                break;
                            case -2:
                                out.println("0;Acest Username nu exista");
                                break;
                            case -3:
                                out.println("0;Parola gresita");
                                break;
                            default:
                                break;
                        }
                            break;
                        }
                        
                        case 3:
                        {
                            ArrayList <String> numeParolaList=conexiuneUtilizator.getUtilizatorParolaMail(cerere.get(3));
                            if(numeParolaList.isEmpty())
                            {
                                out.println("0;Nu exista niciun cont cu aceasta adresa de mail");
                            }
                            else
                            {
                                out.println("1;Un mail cu datele contului a fost trimis");
                                
                            }
                            break;
                        }
                        case 4:
                        {
                            
                            String  rezultat = conexiuneUtilizator.getCompanyInfo(idCompanie);
                            if(rezultat.equals(""))
                                out.println("0;Eroare nu s-au gasit date.");
                            else
                                out.println(rezultat);
                            break;
                        }
                        //update company
                        case 5:
                        {
                            String rezultat = conexiuneUtilizator.updateCompany(cerere, idCompanie);
                            if(rezultat.equals(""))
                                out.println("0;eroare la update");
                            else
                                out.println("1;Succes");
                            break;
                        }
                        //Insert Client
                        case 6:
                        {
                            String rezultat = conexiuneUtilizator.insertClient(cerere, idCompanie);
                            if(rezultat.equals(""))
                                out.println("0;Eroare la inserarea clientului");
                            else
                                out.println("1;Succes");
                            break;
                        }
                        //Delete client
                        case 7:
                        {
                            System.out.println("sterg");
                            String rezultat = conexiuneUtilizator.deleteClient(cerere);
                            if(rezultat.equals(""))
                                out.println("0;Eroare la stergerea clientului");
                            else
                                out.println("1;Succes");
                            break;
                        }
                        //Get info from client
                        case 8:
                        {
                            ArrayList<String> rezultat = new ArrayList();
                            rezultat = conexiuneUtilizator.getClient(cerere);
                           // System.out.println(rezultat.get(0)+" "+rezultat.get(1));
                             if(rezultat.get(0).equals(""))
                                out.println("0;Clientul nu exista");
                            else
                                {
                                    out.println(rezultat.get(0));
                                    idClient = Integer.parseInt(rezultat.get(1));
                                }
                            break;
                        }
                        //Update client
                        case 9:
                        {
                            String rezultat = conexiuneUtilizator.updateClient(cerere, idClient);
                            if(rezultat.equals(""))
                                out.println("0;eroare la update");
                            else
                                out.println("1;Succes");
                            break;
                        }
                        default:
                        {
                            //!!!!!!!!!!!!!!!!!!!!!AICICI AI ADAUGAT COD!!!!!!!!!!!!!!!!!!!!!!!
                            out.println("Cod nerecunoscut");
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally{
                try{
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!AICICICICI!!!!!!!!!!!!!!!!!!!!!
                    listaUtilizatori.remove(this);
                    socket.close();
                }
                catch(IOException e)
                {
                    System.out.println("Can't close the socket of client "+clientNumber+" Exceptio: "+e);
                }
            }
            
            
        }
    public int getID()
    {
        return idUser;
    }
    //AICICI INCEP MODIFICARI!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    
    public String getNume()
    {
        return numeUser;
    }
     
    private static ArrayList decode(String a)
    {
        ArrayList<String> localList=new ArrayList<>();
        int n=a.length();
        int poz=0;
        String subExit=a.substring(0,1);
        switch (subExit) {
            case "-1":
                localList.add(subExit);
                break;
            case "4":
                localList.add(subExit);
                break;
            case "5":
            {   
                 localList.add(subExit);
                poz=2;
                       for(int i=0;i<4;++i)
                        {
                        String word="";
                        while(poz<n)
                        {
                            String sub=a.substring(poz,poz+1);
                            if(sub.equals(";"))
                            break;
                        word = word.concat(sub);
                        ++poz;
                        
                    }
                    ++poz;
                    localList.add(word);
                    } 
                    String word="";
                    String sub=a.substring(poz,n);
                    word=word.concat(sub);
                    
                    localList.add(word);
            break;
            }
            case "6":
            {   
                localList.add(subExit);
                poz=2;
                       for(int i=0;i<4;++i)
                        {
                        String word="";
                        while(poz<n)
                        {
                            String sub=a.substring(poz,poz+1);
                            if(sub.equals(";"))
                            break;
                        word = word.concat(sub);
                        ++poz;
                        
                    }
                    ++poz;
                    localList.add(word);
                    } 
                    String word="";
                    String sub=a.substring(poz,n);
                    word=word.concat(sub);
                    
                    localList.add(word);
                break;
            }
            case "7":
            {
                localList.add(subExit);
                localList.add(a.substring(2,a.length()));
                break;
            }
            case "8":
            {
                localList.add(subExit);
                localList.add(a.substring(2,a.length()));
                break;
            }
            case "9":
            {
                localList.add(subExit);
                poz=2;
                       for(int i=0;i<4;++i)
                        {
                        String word="";
                        while(poz<n)
                        {
                            String sub=a.substring(poz,poz+1);
                            if(sub.equals(";"))
                            break;
                        word = word.concat(sub);
                        ++poz;
                        
                    }
                    ++poz;
                    localList.add(word);
                    } 
                    String word="";
                    String sub=a.substring(poz,n);
                    word=word.concat(sub);
                    
                    localList.add(word);
               
                break;
            }
            default:
                if(a.substring(0,2).equals("DA"))
                {
                    localList.add("99999");
                    break;
                }
                for(int i=0;i<3;++i)
                {
                    String word="";
                    while(poz<n)
                    {
                        String sub=a.substring(poz,poz+1);
                        if(sub.equals(";"))
                            break;
                        word = word.concat(sub);
                        ++poz;
                        
                    }
                    ++poz;
                    localList.add(word);
                }       String word="";
                String sub=a.substring(poz,n);
                word=word.concat(sub);
                localList.add(word);
                break;
        }
    return localList;
    }
    }
}   
