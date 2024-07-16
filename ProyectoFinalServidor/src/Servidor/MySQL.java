/*
 *  Licencia Mario Raya
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servidor;

import Servidor.consultar.Consultar;
import Servidor.dml.Eliminar;
import Servidor.dml.Insertar;
import Servidor.dml.Modificar;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Esta clase contiene metodos para conectar a una base de datos, asi como para
 * realizar operaciones de consulta, actualizacion, eliminacion e insercion.
 * Tambien proporciona metodos para obtener metadatos de la base de datos.
 *
 * @author Mario Raya Leon
 */
public class MySQL {

    private static Connection cx;
    private String mError;

    public static String bd = "bdproyectofinal";
    public static String url = "jdbc:mysql://localhost:3306/";
    public static String user = "root"; //Por defecto
    public static String pass = "";
    public static String driver = "com.mysql.cj.jdbc.Driver";

    public MySQL(String mensaje) {
        this.mError = mensaje;
    }

    /**
     * Establece una conexion a la base de datos MySQL.
     *
     * @return La conexion a la base de datos.
     */
    public Connection conectar() {
        try {
            Class.forName(driver);//Nombre del driver
            cx = DriverManager.getConnection(url + bd, user, pass);

        } catch (ClassNotFoundException | SQLException ex) {
            JOptionPane.showMessageDialog(null, mError, "Fallo en conexion/desconexion", JOptionPane.ERROR_MESSAGE);
            System.out.println("NO SE HA PODIDO CONECTAR A LA Base de datos: " + bd);
        }
        return cx;
    }

    /**
     * Ejecuta una consulta SQL en la base de datos.
     *
     * @param consulta La consulta SQL a ejecutar.
     * @param cn La conexion a la base de datos.
     * @return El ResultSet que contiene el resultado de la consulta.
     */
    public ResultSet consultar(String consulta, Connection cn) {
        ResultSet rs;
        rs = Consultar.consulta(cn, consulta);

        return rs;
    }

    /**
     * Ejecuta una sentencia SQL de actualizacion en la base de datos.
     *
     * @param consulta La consulta SQL de actualizacion.
     * @param datosActualizar Los datos para la actualizacion.
     * @param cn La conexion a la base de datos.
     * @return un 0 si no ha podido hacer nada o -1 si hay algun error
     */
    public int actualizar(String consulta, String[] datosActualizar, Connection cn) {
        int resultado;
        resultado = Modificar.actualizar(cn, consulta, datosActualizar);

        return resultado;
    }

    /**
     * Ejecuta una sentencia SQL de eliminacion en la base de datos.
     *
     * @param consulta La consulta SQL de eliminacion.
     * @param ID El ID del registro a eliminar.
     * @param cn La conexion a la base de datos.
     * @return un 0 si no ha podido hacer nada o -1 si hay algun error
     */
    public int eliminar(String consulta, String ID, Connection cn) {
        int resultado;
        resultado = Eliminar.eliminar(cn, consulta, ID);

        return resultado;
    }

    /**
     * Ejecuta una sentencia SQL de insercion en la base de datos.
     *
     * @param consulta La consulta SQL de insercion.
     * @param datosInsertar Los datos a insertar.
     * @param cn La conexion a la base de datos.
     * @return un 0 si no ha podido hacer nada o -1 si hay algun error
     */
    public int insertar(String consulta, String[] datosInsertar, Connection cn) {
        int resultado;
        resultado = Insertar.insertar(cn, consulta, datosInsertar);

        return resultado;
    }

    /**
     * Obtiene el nombre de la base de datos actualmente conectada.
     *
     * @return El nombre de la base de datos.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public String getNombreBD() throws SQLException {
        return cx.getCatalog();
    }

    /**
     * Obtiene los metadatos de la base de datos actualmente conectada.
     *
     * @return Los metadatos de la base de datos.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public DatabaseMetaData metaDatos() throws SQLException {
        DatabaseMetaData metadata = cx.getMetaData();

        return metadata;
    }

    /**
     * Obtiene un listado de las tablas en la base de datos.
     *
     * @param metadata Los metadatos de la base de datos.
     * @return Un ResultSet que contiene el listado de tablas.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public ResultSet listadoTablas(DatabaseMetaData metadata) throws SQLException {
        ResultSet resultSet = metadata.getTables(bd, null, null, null);
        return resultSet;
    }

    /**
     * Muestra las columnas de una tabla especifica en la base de datos.
     *
     * @param metadata Los metadatos de la base de datos.
     * @param nombreTabla El nombre de la tabla.
     * @return Un ResultSet que contiene las columnas de la tabla.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public ResultSet mostrarColumnas(DatabaseMetaData metadata, String nombreTabla) throws SQLException {
        ResultSet resultSet = metadata.getColumns(null, null, nombreTabla, "%");
        return resultSet;
    }

}//end MySQL

