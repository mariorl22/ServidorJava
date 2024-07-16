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
 * Esta clase contiene un metodo estatico que ejecuta una sentencia SQL de insercion utilizando los datos proporcionados.
 * 
 * @author Mario Raya Leon
 */
public class Insertar {

    
    /**
     * Ejecuta una sentencia SQL de insercion en la conexion proporcionada utilizando los datos dados.
     * 
     * @param c La conexion a la base de datos.
     * @param sql La sentencia SQL de insercion a ejecutar.
     * @param datosInsertar Un array de cadenas que contiene los datos a insertar.
     * @return un 0 si no ha podido hacer nada o -1 si hay algun error
     */ 
    public static int insertar(Connection c, String sql, String[] datosInsertar) {
        PreparedStatement ps;
        
        try {
            //Preparamos sentencia
            ps = c.prepareStatement(sql);

            String[] datos = datosInsertar;
            int col = 1;
            while (col <= datos.length) {
                //Establecer parametro SQL correspondiente:               
                ps.setString(col, datos[col - 1]);            
                //siguiente columna
                col++;
            }

            return ps.executeUpdate();
        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, "Error en operacion", "Error", JOptionPane.ERROR_MESSAGE);

            return -1;//error
        }//end catch        
    }

}//end Insertar

