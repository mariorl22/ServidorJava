/*
 *  Licencia Mario Raya
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Servidor.consultar;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Esta clase contiene un metodo estatico que ejecuta una consulta SQL y devuelve el resultado como un ResultSet.
 * 
 * @author Mario Raya Leon
 */
public class Consultar {

    
    /**
     * Ejecuta una consulta SQL en la conexion proporcionada y devuelve el resultado.
     * 
     * @param c La conexion a la base de datos.
     * @param sql La sentencia SQL a ejecutar.
     * @return El ResultSet que contiene el resultado de la consulta, o null si ocurre un error.
     */
    public static ResultSet consulta(Connection c, String sql){        
        PreparedStatement ps; 
        try{
           ps = c.prepareStatement(sql);
           return ps.executeQuery();
        }catch(SQLException e){
           JOptionPane.showMessageDialog(null, "Error en consulta", "Error", JOptionPane.ERROR_MESSAGE); 
           return null;
        }
        
    }
    
}//end Consultar

