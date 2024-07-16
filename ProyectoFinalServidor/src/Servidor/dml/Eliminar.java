/*
 *  Licencia Mario Raya
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Servidor.dml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Esta clase contiene un metodo estatico que ejecuta una sentencia SQL de eliminacion usando un ID proporcionado.
 * 
 * @author Mario Raya Leon
 */
public class Eliminar {

    /**
     * Ejecuta una sentencia SQL de eliminacion en la conexion proporcionada utilizando el ID dado.
     * 
     * @param c La conexion a la base de datos.
     * @param sql La sentencia SQL de eliminacion a ejecutar.
     * @param ID El ID que se utilizara como parametro en la sentencia SQL.
     * @return un 0 si no ha podido hacer nada o -1 si hay algun error
     */ 
    public static int eliminar(Connection c, String sql, String ID){        
        PreparedStatement ps; 
        
        try{
           ps = c.prepareStatement(sql);          
           ps.setString(1, ID);//Establezco parametro SQL             
                      
           return ps.executeUpdate();
        }catch(SQLException e){
           JOptionPane.showMessageDialog(null, "Error en operacion", "Error", JOptionPane.ERROR_MESSAGE); 
           return -1;//error
        }//end catch        
    }
    
}//end Eliminar

