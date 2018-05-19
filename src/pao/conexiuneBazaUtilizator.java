/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pao;
import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.PreparedStatement;
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author c-tin
 */


//Ai doua metoda: inregistrare, login
//Input inregistrare():3 stringuri: Unsername, Parola, ADresa de mail
//return value inregistrare():0->S-a inregistrat cu succes utilizatorul
//                     1->Adresa de mail este deja folosita
//                     2->Username deja folosit
//                     3->Nu s-a putut insera la querryUpdate, probleme mai naspa
//                     -1->a aparut o exceptiue cu printare in consola a excpetiei,E de rau, trebuie chemata echipa tehnica
//Input login():2 Stringuri: Username, Parola
//return value login(): 0->Este ok, se poate loga utilizatorul
//                      1->Username-ul nu exista
//                      2->Parola e gresita

public class conexiuneBazaUtilizator {
    
    private Connection conex;
    public conexiuneBazaUtilizator() {
        try{
            conex=DriverManager.getConnection("jdbc:mysql://sql11.freemysqlhosting.net:3306/sql11236630",
                    "sql11236630","s3Ta6t7lWV");
        }
        catch(SQLException e) {

            System.out.println(e);
        }
    }
    
    public void closeCon() throws SQLException {
        conex.close();
    }
    
    public synchronized int inregistrare(String nume, String parola, String id_companie) {
        try {
            Statement st=conex.createStatement();
            System.out.println(Integer.parseInt(id_companie));
            ResultSet rez=st.executeQuery("Select * from companie where id_companie='"+Integer.parseInt(id_companie)+"'");
            if((rez.next()==false)) {
                return 1;
            }   //1, inseamna ca NU EXISTA NICIO COMPANIE CU ACEST ID
            else
            {
                rez=st.executeQuery("Select * from users where nume='"+nume+"'");
                if(!(rez.next()==false))
                    return 2;   //2 inseamna ca numele e deja folosit
                else
                {
                    int inserted=st.executeUpdate("insert into users (nume,parola,id_companie" + ") values('"+nume+"','"+parola+"','"+id_companie+"')");
                    if (inserted<=0){
                        System.out.println("erorrrrrrrrrooooorr");
                        return 3; //NU s-a putut insera, eroare de inserare
                    }
                    return 0;
                }

            }
        }
    catch(SQLException e) {
        System.out.println("erorrrrrrrrrrr");
        System.out.println(e);
        return -1;  //Probleme grave, trebuie chemata echipa tehnica
    }

    }
    
    public synchronized int login(String nume, String parola) {
        
        try {
            Statement st=conex.createStatement();
            ResultSet rez=st.executeQuery("Select * from users where nume='"+nume+"'");
            if(rez.next()==false)
            {
                return -2;}   //-2, nu exista asemena utilizator
            else
            {
                if(!(rez.getString(3).equals(parola)))
                    return -3;   //-3 inseamna ca numele e deja folosit
                else
                {
                    
                    return rez.getInt(4);   //E ok >0, inseamna ca se poate loga, se returneaza id-ul
                }

            }
        }
    catch(SQLException e) {
        System.out.println(e);
        return -1;  //Probleme grave, trebuie chemata echipa tehnica
    }

    }
    public synchronized String updateCompany(ArrayList<String> array,int id_companie) {
        String result;
        try {
            PreparedStatement ps = conex.prepareStatement(
            "UPDATE companie SET  nume_companie = ?, nr_inregistrare = ?, adresa = ?, manager = ?, cont_bancar = ? WHERE id_companie = ?");
            // set the preparedstatement parameters
            ps.setString(1,array.get(1));
            ps.setInt(2,Integer.parseInt(array.get(5)));
            ps.setString(3,array.get(2));
            ps.setString(4,array.get(3));
            ps.setString(5,array.get(4));
            ps.setInt(6,id_companie);
            // call executeUpdate to execute our sql update statement
            ps.executeUpdate();
            result = "1;Reusit!";
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(conexiuneBazaUtilizator.class.getName()).log(Level.SEVERE, null, ex);
            result = "";
            return result;
        }
        return result;
    }
    public synchronized String getCompanyInfo(int idCompanie) {
        String info;
        info = "";
        StringBuilder i = new StringBuilder(50);
        
            
        try {
            
            Statement st = conex.createStatement();
            ResultSet rez = st.executeQuery("Select nume_companie,adresa,manager,cont_bancar,nr_inregistrare from companie where id_companie='"+idCompanie+"'");
            
            if(rez.next() == false) {
                return info;
            } else {
                i.append(1).append(";");
                i.append(rez.getString(1)).append(";");
                i.append(rez.getString(2)).append(";");
                i.append(rez.getString(3)).append(";");
                i.append(rez.getString(4)).append(";");
                i.append(rez.getInt(5));  
                info = i.toString();
            }
        } catch (SQLException ex) {
            Logger.getLogger(conexiuneBazaUtilizator.class.getName()).log(Level.SEVERE, null, ex);
            return info;
        }   
        return info;
    }
    
    public synchronized ArrayList getUtilizatorParolaMail(String mail) 
    {
        ArrayList <String> username;
        username = new ArrayList<>();
        try {
            Statement st=conex.createStatement();
            ResultSet rez=st.executeQuery("Select nume,parola from utilizator where adresa_mail='"+mail+"'");
            
            if(rez.next()==false) {
                    
                    return username;
                }   //1, nu exista asemena utilizator
            else {
                username.add(rez.getString(1));
                username.add(rez.getString(2));
            }
        }
        catch(SQLException e) {
            System.out.println("Probleme tehnice la Statement/executeQuery"+e);
            return username;
        }
        return username;
    }    
        
    
    public synchronized void selectAll()
    {
        try{
            Statement st=conex.createStatement();
            ResultSet rez=st.executeQuery("SELECT * FROM users");
            while(rez.next())
            {
                
                System.out.println(rez.getInt(1)+" "+rez.getString(2)+" "+rez.getString(3)+" "+rez.getString(4));
            }
            
        }
        catch(SQLException e)
        {
            System.out.println(e);
            
        }
    }
}
