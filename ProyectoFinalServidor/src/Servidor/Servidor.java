/*
 *  Licencia Mario Raya
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Servidor;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 * El servidor escucha en un puerto especificado y acepta conexiones de
 * clientes, asignando un nuevo puerto para cada cliente conectado y metodos de insertar, modificar, agregar y eliminar tanto piezas
 * como cliente y vendedores.
 *
 * @author Mario Raya Leon
 */
public class Servidor {

    public static MySQL mysql = new MySQL("Error en la conexion");
    public static ServerSocket serverSocket;
    public static Socket clienteSocket;
    public static Connection cx;
    public static int PUERTO;
    public static boolean admin;
    private static List<Socket> listaSockets = new ArrayList<>();
    private static List<String> listaClientes = new ArrayList<>();
    private static List<String> listaVendedores = new ArrayList<>();
    public static String nombreUsuario;
    public static String nombrePieza;
    public static String imagen;
    public static String ruta;

    /**
     * Metodo principal que inicia el servidor y maneja las conexiones de los
     * clientes.
     *
     * @param args Los argumentos de la linea de comandos.
     */
    public static void main(String[] args) {
        System.out.println("Servidor iniciado");

        PUERTO = 12345; // Puerto en el que escucha el servidor
        cx = mysql.conectar();
        try {
            // Crea un ServerSocket que escucha en el puerto especificado
            serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado. Esperando conexiones...");

            while (true) {
                // Espera a que llegue una conexion de un cliente
                clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clienteSocket.getInetAddress());

                int nuevoPuerto = generarPuerto();
                cambiarPuerto(clienteSocket, nuevoPuerto);
                nuevaConexion(nuevoPuerto);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Genera un puerto aleatorio basado en el puerto base.
     *
     * @return Un numero de puerto aleatorio.
     */
    private static int generarPuerto() {
        return PUERTO + new Random().nextInt(100);
    }

    /**
     * Envia un nuevo puerto al cliente para que se conecte.
     *
     * @param clientSocket El socket del cliente.
     * @param nuevoPuerto El nuevo puerto asignado al cliente.
     */
    private static void cambiarPuerto(Socket clientSocket, int nuevoPuerto) {
        try (PrintWriter salidaCliente = new PrintWriter(clientSocket.getOutputStream(), true)) {
            salidaCliente.println("CAMBIAR_PUERTO:" + nuevoPuerto); // Enviar el nuevo puerto al cliente
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establece una conexion en un nuevo puerto y maneja los datos del cliente.
     *
     * @param nuevoPuerto El nuevo puerto en el que se establecera la conexion.
     */
    private static void nuevaConexion(int nuevoPuerto) {
        try (ServerSocket nuevoServerSocket = new ServerSocket(nuevoPuerto)) {
            Socket nuevoClientSocket = nuevoServerSocket.accept(); // Aceptar la conexion del cliente en el nuevo puerto
            listaSockets.add(nuevoClientSocket);
            System.out.println("Nuevo cliente conectado en el puerto " + nuevoClientSocket.getPort());
            Thread thread = new Thread(() -> manejarDatos(nuevoClientSocket));
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Maneja los datos recibidos de un cliente.
     *
     * @param clientSocket El socket del cliente.
     */
    private static void manejarDatos(Socket clientSocket) {

        try {
            // Crea un BufferedReader para leer los mensajes del cliente
            BufferedReader entradaDesdeCliente = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Lee los mensajes del cliente y los muestra en la consola
            String mensajeCliente;
            while ((mensajeCliente = entradaDesdeCliente.readLine()) != null) {
                String[] partes = mensajeCliente.split("\\|");
                String comando = partes[0];

                switch (comando) {
                    case "INICIO_SESION" ->
                        iniciarSesion(clientSocket, mensajeCliente);

                    case "REGISTRO" ->
                        registro(clientSocket, mensajeCliente);

                    case "VENDEDORES" ->
                        recogerVendedores(clientSocket);

                    case "INSERTAR_PIEZA" ->
                        pieza(mensajeCliente);

                    case "PIEZAS" ->
                        recogerPiezas(clientSocket);

                    case "TPIEZAS" ->
                        totalPiezas(clientSocket);

                    case "TVENDEDORES" ->
                        totalVendedores(clientSocket);

                    case "TCLIENTES" ->
                        totalClientes(clientSocket);

                    case "ANIADIRCLIENTEPIEZA" ->
                        recogerIDsPiezasCliente(mensajeCliente);

                    case "ELIMINARPIEZA" ->
                        eliminarPieza(mensajeCliente);

                    case "ELIMINARFAVORITO" ->
                        eliminarFavorito(mensajeCliente);

                    case "ELIMINARVENDEDOR" ->
                        eliminarVendedor(mensajeCliente);

                    case "ELIMINARCLIENTE" ->
                        eliminarCliente(mensajeCliente);

                    case "LONGITUD" ->
                        longitud(mensajeCliente);

                    case "MODIFICARLONGITUD" ->
                        modificarImagen(mensajeCliente);

                    case "MODIFICARDATOS" ->
                        modificarDatos(mensajeCliente);

                    case "MODIFICARVENDEDORES" ->
                        modificarVendedor(clientSocket, mensajeCliente);

                    case "MODIFICARCLIENTES" ->
                        modificarCliente(clientSocket, mensajeCliente);

                    case "DATOSVENDEDORES" ->
                        datosVendedores(clientSocket, mensajeCliente);

                    case "DATOSPIEZAS" ->
                        datosPiezas(clientSocket, mensajeCliente);

                    case "CLIENTEPIEZA" ->
                        recogerPiezasCliente(clientSocket, mensajeCliente);

                    case "PIEZAVENDEDOR" ->
                        recogerPiezasVendedor(clientSocket, mensajeCliente);

                    case "ADMININICIADO" ->
                        inicioAdmin(clientSocket);

                    case "ADMINCERRADO" ->
                        admin = false;

                    case "CERRADO" ->
                        cerrarUsuario(clientSocket, mensajeCliente);
                    default ->
                        System.out.println("");
                }
            }
        } catch (IOException ex) {
            Iterator<Socket> iterator = listaSockets.iterator();
            while (iterator.hasNext()) {
                Socket socket = iterator.next();
                if (socket.equals(clientSocket)) {
                    iterator.remove();
                    System.out.println("Cliente eliminado de la lista");
                }
            }
        } catch (SQLException ex) {
        }
    }

    /**
     * Obtiene los nombres de las tablas en la base de datos.
     *
     * @return Un array de strings con los nombres de las tablas.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static String[] obtenerNombresTablas() throws SQLException {
        DatabaseMetaData metadata;
        ResultSet resultSet = null;

        metadata = mysql.metaDatos();
        // Obtener las tablas de la base de datos
        resultSet = mysql.listadoTablas(metadata);
        List<String> nombresTablas = new ArrayList<>();
        if (resultSet != null) {
            while (resultSet.next()) {
                // Obtener el nombre de la tabla y agregarlo a la lista
                String nombreTabla = resultSet.getString("TABLE_NAME");
                nombresTablas.add(nombreTabla);
            }
        }

        // Convertir la lista a un array de strings
        return nombresTablas.toArray(new String[0]);
    }

    /**
     * Obtiene los nombres de las columnas de una tabla especifica.
     *
     * @param tabla El nombre de la tabla.
     * @return Un array de strings con los nombres de las columnas.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static String[] obtenerColumnas(String tabla) throws SQLException {
        DatabaseMetaData metadata = mysql.metaDatos();

        ResultSet resultSet = mysql.mostrarColumnas(metadata, tabla);
        List<String> nombresColumnas = new ArrayList<>();

        while (resultSet.next()) {
            String nombreColumna = resultSet.getString("COLUMN_NAME");
            nombresColumnas.add(nombreColumna);
        }
        return nombresColumnas.toArray(new String[0]);
    }

    /**
     * Elimina una pieza de la base de datos y borra el archivo asociado a ella.
     *
     * @param mensaje El mensaje que contiene los datos necesarios para la
     * eliminacion.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void eliminarPieza(String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        System.out.println("Datos: " + campos[1]);
        String ID = campos[1];
        String[] tablas = obtenerNombresTablas();
        String[] columnas = obtenerColumnas(tablas[1]);

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[1], cx);
        String[] columnas2 = obtenerColumnas(tablas[1]);
        String rutaArchivo = "";
        try {
            while (rs.next()) {
                int IDPieza = rs.getInt(columnas2[0]);
                String tipo = rs.getString(columnas2[1]);
                String nombre = rs.getString(columnas2[2]);
                String descripcion = rs.getString(columnas2[3]);
                double precio = rs.getDouble(columnas2[4]);
                String imagen = rs.getString(columnas2[5]);

                if (IDPieza == Integer.parseInt(ID)) {
                    rutaArchivo = imagen;
                    System.out.println("Ruta- " + rutaArchivo);
                }

            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        File archivo = new File(rutaArchivo);
        archivo.delete();

        int resultado = mysql.eliminar("DELETE FROM " + tablas[1] + " WHERE " + columnas[0] + "=?", ID, cx);

        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
            case 0 ->
                JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch
    }

    /**
     * Elimina un favorito de la base de datos.
     *
     * @param mensaje El mensaje que contiene los datos necesarios para la
     * eliminacion.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void eliminarFavorito(String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        System.out.println("Datos: " + campos[1]);
        String ID = campos[1];
        String[] tablas = obtenerNombresTablas();
        String[] columnas = obtenerColumnas(tablas[2]);
        int resultado = mysql.eliminar("DELETE FROM " + tablas[2] + " WHERE " + columnas[2] + "=?", ID, cx);

        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
            case 0 ->
                JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch
    }

    /**
     * Elimina un vendedor de la base de datos y notifica a los clientes
     * conectados.
     *
     * @param mensaje El mensaje que contiene los datos necesarios para la
     * eliminacion.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void eliminarVendedor(String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        System.out.println("Datos Eliminar: " + campos[1]);
        String nombre = campos[1];
        String[] tablas = obtenerNombresTablas();
        String[] columnas = obtenerColumnas(tablas[4]);
        recogerIDsPiezasVendedor("PIEZAVENDEDOR|" + nombre);
        int resultado = mysql.eliminar("DELETE FROM " + tablas[4] + " WHERE " + columnas[1] + "=?", nombre, cx);
        for (Socket socket : listaSockets) {
            enviarMensaje(socket, "VENDEDORELIMINADO|" + nombre);
        }
        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
            case 0 ->
                JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch
    }

    /**
     * Elimina un cliente de la base de datos.
     *
     * @param mensaje El mensaje que contiene los datos necesarios para la
     * eliminacion.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void eliminarCliente(String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        System.out.println("Datos Eliminar: " + campos[1]);
        String nombre = campos[1];
        String[] tablas = obtenerNombresTablas();
        String[] columnas = obtenerColumnas(tablas[0]);
        int resultado = mysql.eliminar("DELETE FROM " + tablas[0] + " WHERE " + columnas[1] + "=?", nombre, cx);
//        for (Socket socket : listaSockets) {
//            enviarMensaje(socket, "CLIENTEELIMINADO|" + nombre);
//        }
        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
            case 0 ->
                JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch
    }

    /**
     * Inserta un cliente en la base de datos.
     *
     * @param datosClienteInsertar Los datos del cliente a insertar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void insertarCliente(String[] datosClienteInsertar) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        String[] datosInsertar = datosClienteInsertar;
        String[] columnas = obtenerColumnas(tablas[0]);

        int resultado = mysql.insertar("INSERT INTO " + tablas[0] + "(" + columnas[1] + "," + columnas[2] + ")" + " VALUES (?,?)", datosInsertar, cx);
        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
            case 0 ->
                JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch

    }

    /**
     * Inserta un vendedor en la base de datos.
     *
     * @param datosVendedorInsertar Los datos del vendedor a insertar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void insertarVendedor(String[] datosVendedorInsertar) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        String[] datosInsertar = datosVendedorInsertar;
        String[] columnas = obtenerColumnas(tablas[4]);

        int resultado = mysql.insertar("INSERT INTO " + tablas[4] + "(" + columnas[1] + "," + columnas[2] + "," + columnas[3] + ")" + " VALUES (?,?,?)", datosInsertar, cx);
        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
            case 0 ->
                JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch
    }

    /**
     * Inserta una pieza en la base de datos y asigna la pieza a vendedores.
     *
     * @param datosPiezaInsertar Los datos de la pieza a insertar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void insertarPieza(String[] datosPiezaInsertar) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        String[] datosInsertar = datosPiezaInsertar;
        String[] columnas = obtenerColumnas(tablas[1]);

        int resultado = mysql.insertar("INSERT INTO " + tablas[1] + "(" + columnas[1] + "," + columnas[2] + "," + columnas[3] + "," + columnas[4] + "," + columnas[5] + ")" + " VALUES (?,?,?,?,?)", datosInsertar, cx);

        String[] IDs = recogerIDsPiezasVendedores();
        insertarPiezaVendedor(IDs);
        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
            case 0 ->
                JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch

    }

    /**
     * Inserta una relacion entre pieza y vendedor en la base de datos.
     *
     * @param datosPiezaVendedorInsertar Los datos de la relacion pieza-vendedor
     * a insertar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void insertarPiezaVendedor(String[] datosPiezaVendedorInsertar) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        String[] datosInsertar = datosPiezaVendedorInsertar;
        String[] columnas = obtenerColumnas(tablas[3]);

        int resultado = mysql.insertar("INSERT INTO " + tablas[3] + "(" + columnas[1] + "," + columnas[2] + ")" + " VALUES (?,?)", datosInsertar, cx);
        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
            case 0 ->
                JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch
    }

    /**
     * Inserta una relacion entre pieza y cliente en la base de datos,
     * verificando que la pieza no este duplicada.
     *
     * @param datosPiezaClienteInsertar Los datos de la relacion pieza-cliente a
     * insertar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void insertarPiezaCliente(String[] datosPiezaClienteInsertar) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        String[] datosInsertar = datosPiezaClienteInsertar;
        String[] columnas = obtenerColumnas(tablas[2]);

        boolean piezaYaExiste = false;
        int idPiezaNueva = Integer.parseInt(datosInsertar[1]); // Suponiendo que el ID de la pieza esta en la primera posicion de datosInsertar
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[2], cx); // Consulta todas las piezas favoritas

        try {
            while (rs.next()) {

                int idPiezaExistente = rs.getInt(columnas[2]); // Suponiendo que el ID de la pieza esta en la primera columna de la tabla
                if (idPiezaExistente == idPiezaNueva) {
                    piezaYaExiste = true; // La pieza ya esta en la lista
                    break;
                }
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al verificar la existencia de la pieza en la lista de favoritos", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (!piezaYaExiste) {
            int resultado = mysql.insertar("INSERT INTO " + tablas[2] + "(" + columnas[1] + "," + columnas[2] + ")" + " VALUES (?,?)", datosInsertar, cx);
            //Informo sobre resultado
            switch (resultado) {
                case -1 ->
                    JOptionPane.showMessageDialog(null, "Excepcion SQL", "Error", JOptionPane.ERROR_MESSAGE);
                case 0 ->
                    JOptionPane.showMessageDialog(null, "No se ha podido realizar nada", "Error", JOptionPane.WARNING_MESSAGE);
                default ->
                    System.out.println("Operacion realizada con EXITO");

            }//end switch
        }
    }

    /**
     * Actualiza los datos de un vendedor en la base de datos.
     *
     * @param vendedor Los datos del vendedor a actualizar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void actualizarVendedor(String[] vendedor) throws SQLException {
        String[] datosActualizar = pedirDatosActualizarVendedor(vendedor);
        String[] tablas = obtenerNombresTablas();
        String[] columnas = obtenerColumnas(tablas[4]);

        int resultado = mysql.actualizar("UPDATE " + tablas[4] + " SET " + columnas[1] + "=?" + "," + columnas[2] + "=?" + "," + columnas[3] + "=? WHERE " + columnas[0] + "=?", datosActualizar, cx);

        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                System.out.println("Excepcion SQL");
            case 0 ->
                System.out.println("No se ha podido realizar nada");
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch

    }

    /**
     * Actualiza los datos de un cliente en la base de datos.
     *
     * @param cliente Los datos del cliente a actualizar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void actualizarCliente(String[] cliente) throws SQLException {
        String[] datosActualizar = pedirDatosActualizarCliente(cliente);
        String[] tablas = obtenerNombresTablas();
        String[] columnas = obtenerColumnas(tablas[0]);

        int resultado = mysql.actualizar("UPDATE " + tablas[0] + " SET " + columnas[1] + "=?" + "," + columnas[2] + "=? WHERE " + columnas[0] + "=?", datosActualizar, cx);

        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                System.out.println("Excepcion SQL");
            case 0 ->
                System.out.println("No se ha podido realizar nada");
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch

    }

    /**
     * Actualiza los datos de una pieza en la base de datos.
     *
     * @param piezas Los datos de la pieza a actualizar.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void actualizarPiezas(String[] piezas) throws SQLException {
        String[] datosActualizar = pedirDatosActualizar(piezas);
        String[] tablas = obtenerNombresTablas();
        String[] columnas = obtenerColumnas(tablas[1]);
        int resultado = mysql.actualizar("UPDATE " + tablas[1] + " SET " + columnas[1] + "=?" + "," + columnas[2] + "=?" + "," + columnas[3] + "=?" + "," + columnas[4] + "=?" + "," + columnas[5] + "=? WHERE " + columnas[0] + "=?", datosActualizar, cx);

        //Informo sobre resultado
        switch (resultado) {
            case -1 ->
                System.out.println("Excepcion SQL");
            case 0 ->
                System.out.println("No se ha podido realizar nada");
            default ->
                System.out.println("Operacion realizada con EXITO");
        }//end switch

    }

    /**
     * Prepara los datos para actualizar un vendedor.
     *
     * @param datosVendedor Los datos actuales del vendedor.
     * @return Los datos preparados para la actualizacion.
     */
    public static String[] pedirDatosActualizarVendedor(String[] datosVendedor) {
        List<String> datos = new ArrayList();

        String idActualizar = datosVendedor[0];
        String nombre = datosVendedor[1];
        String contrasenia = datosVendedor[2];
        String ubicacion = datosVendedor[3];

        datos.add(nombre);
        datos.add(contrasenia);
        datos.add(ubicacion);
        datos.add(idActualizar);
        //Devuelvo los datos obtenidos
        return datos.toArray(new String[0]);
    }

    /**
     * Prepara los datos para actualizar un cliente.
     *
     * @param datosCliente Los datos actuales del cliente.
     * @return Los datos preparados para la actualizacion.
     */
    public static String[] pedirDatosActualizarCliente(String[] datosCliente) {
        List<String> datos = new ArrayList();

        String idActualizar = datosCliente[0];
        String nombre = datosCliente[1];
        String contrasenia = datosCliente[2];

        datos.add(nombre);
        datos.add(contrasenia);
        datos.add(idActualizar);
        //Devuelvo los datos obtenidos
        return datos.toArray(new String[0]);
    }

    /**
     * Prepara los datos para actualizar una pieza.
     *
     * @param datosPieza Los datos actuales de la pieza.
     * @return Los datos preparados para la actualizacion.
     */
    public static String[] pedirDatosActualizar(String[] datosPieza) {
        List<String> datos = new ArrayList();

        String idActualizar = datosPieza[0];
        String tipo = datosPieza[1];
        String nombre = datosPieza[2];
        String descripcion = datosPieza[3];
        String precio = datosPieza[4];
        String imagen = datosPieza[5];

        datos.add(tipo);
        datos.add(nombre);
        datos.add(descripcion);
        datos.add(precio);
        datos.add(imagen);
        datos.add(idActualizar);
        //Devuelvo los datos obtenidos
        return datos.toArray(new String[0]);
    }

    /**
     * Comprueba si un cliente existe en la base de datos.
     *
     * @param cliente El nombre del cliente a comprobar.
     * @return true si el cliente existe, false en caso contrario.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static boolean comprobarUsuarioCliente(String cliente) throws SQLException {
        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[0], cx);
        String[] columnas = obtenerColumnas(tablas[0]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);

                if (nombre.equals(cliente)) {
                    return true;
                }

            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Comprueba si un vendedor existe en la base de datos.
     *
     * @param vendedor El nombre del vendedor a comprobar.
     * @return true si el vendedor existe, false en caso contrario.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static boolean comprobarUsuarioVendedor(String vendedor) throws SQLException {
        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);

                if (nombre.equals(vendedor)) {
                    return true;
                }

            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Inicia sesion como administrador.
     *
     * @param clienteSocket El socket del cliente que solicita el inicio de
     * sesion.
     */
    public static void inicioAdmin(Socket clienteSocket) {
        if (admin) {
            enviarMensaje(clienteSocket, "INICIADO");
        } else {
            admin = true;
        }
    }

    /**
     * Cierra la sesion de un usuario.
     *
     * @param clienteSocket El socket del cliente que solicita el cierre de
     * sesion.
     * @param mensaje El mensaje que contiene la informacion del usuario a
     * cerrar sesion.
     */
    public static void cerrarUsuario(Socket clienteSocket, String mensaje) {
        String[] campos = mensaje.split("\\|");
        if (campos[1].equals("Cliente")) {
            listaClientes.remove(campos[2]);
        } else {
            listaVendedores.remove(campos[2]);
        }
    }

    /**
     * Envia los datos del vendedor correspondiente al ID especificado al
     * cliente.
     *
     * @param clienteSocket El socket del cliente al que se enviaran los datos
     * del vendedor.
     * @param mensaje El mensaje que contiene el ID del vendedor.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void datosVendedores(Socket clienteSocket, String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        System.out.println("ID Vendedor " + campos[1]);
        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);
                String ubicacion = rs.getString(columnas[3]);

                if (ID == Integer.parseInt(campos[1])) {

                    enviarMensaje(clienteSocket, "DATOSVENDEDORES|" + nombre + "|" + contrasenia + "|" + ubicacion);

                }
            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Envia los datos de la pieza correspondiente al ID especificado al
     * cliente.
     *
     * @param clienteSocket El socket del cliente al que se enviaran los datos
     * de la pieza.
     * @param mensaje El mensaje que contiene el ID de la pieza.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void datosPiezas(Socket clienteSocket, String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        System.out.println("ID pieza " + campos[1]);
        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[1], cx);
        String[] columnas = obtenerColumnas(tablas[1]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String tipo = rs.getString(columnas[1]);
                String nombre = rs.getString(columnas[2]);
                String descripcion = rs.getString(columnas[3]);
                double precio = rs.getDouble(columnas[4]);
                String ruta = rs.getString(columnas[5]);

                if (ID == Integer.parseInt(campos[1])) {
                    imagen = ruta;
                    try {
                        String extension = "";
                        String[] separador = ruta.split("\\.");
                        extension = separador[2];

                        BufferedImage image = ImageIO.read(new File(ruta));
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(image, extension, byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();

                        String contenido = extension + ",";
                        for (int i = 0; i < bytes.length; i++) {
                            contenido += bytes[i];
                            if (i + 1 != bytes.length) {
                                contenido += ",";
                            }
                        }

                        String datos = ID + "|" + tipo + "|" + nombre + "|" + descripcion + "|" + precio + "|" + imagen;
                        enviarMensaje(clienteSocket, "DATOSPIEZAS|" + datos);

                        enviarMensaje(clienteSocket, "LONGITUD|" + bytes.length + "," + contenido);

                    } catch (IOException ex) {

                    }
                }

            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    /**
     * Recoge los IDs para insertar en la base de datos.
     *
     * @param mensaje El mensaje que contiene el ID del cliente y la pieza.
     * @return Los IDs de las piezas.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static String[] recogerIDsPiezasCliente(String mensaje) throws SQLException {
        String[] IDs = new String[2];
        String[] tablas = obtenerNombresTablas();

        String[] campos = mensaje.split("\\|");

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[0], cx);
        String[] columnas = obtenerColumnas(tablas[0]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);
                if (nombre.equals(nombreUsuario)) {

                    IDs[0] = String.valueOf(ID);
                    IDs[1] = String.valueOf(campos[1]);
                    insertarPiezaCliente(IDs);
                }

            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return IDs;
    }

    /**
     * Recoge los IDs de las piezas de los vendedores.
     *
     * @return Los IDs de las piezas de los vendedores.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static String[] recogerIDsPiezasVendedores() throws SQLException {
        String[] IDs = new String[2];
        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);

        ResultSet rs2 = mysql.consultar("SELECT * FROM " + tablas[1], cx);
        String[] columnas2 = obtenerColumnas(tablas[1]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);
                if (nombre.equals(nombreUsuario)) {

                    IDs[0] = String.valueOf(ID);
                }

            }
            while (rs2.next()) {
                int ID = rs2.getInt(columnas2[0]);
                String tipo = rs2.getString(columnas2[1]);
                String nombre = rs2.getString(columnas2[2]);
                if (nombre.equals(nombrePieza)) {

                    IDs[1] = String.valueOf(ID);
                }

            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return IDs;
    }

    /**
     * Verificar el nombre de cliente y contrasenia
     * especificados.
     *
     * @param nombreCliente El nombre del cliente.
     * @param contraseniaCliente La contrasenia del cliente.
     * @return true si la sesion se inicia correctamente, false de lo contrario.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static boolean iniciarSesionCliente(String nombreCliente, String contraseniaCliente) throws SQLException {
        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[0], cx);
        String[] columnas = obtenerColumnas(tablas[0]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);

                if (nombre.equals(nombreCliente) && contrasenia.equals(contraseniaCliente)) {
                    nombreUsuario = nombreCliente;
                    return true;
                }

            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifica el nombre de vendedor y contrasenia
     * especificados.
     *
     * @param nombreVendedor El nombre del vendedor.
     * @param contraseniaVendedor La contrasenia del vendedor.
     * @return true si la sesion se inicia correctamente, false de lo contrario.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static boolean iniciarSesionVendedor(String nombreVendedor, String contraseniaVendedor) throws SQLException {
        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);

                if (nombre.equals(nombreVendedor) && contrasenia.equals(contraseniaVendedor)) {
                    return true;
                }

            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Inicia sesion para un cliente o vendedor y envia un mensaje de inicio de
     * sesion correcto o error al cliente.
     *
     * @param clientSocket El socket del cliente.
     * @param mensaje El mensaje de inicio de sesion.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void iniciarSesion(Socket clientSocket, String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        String[] datos = null;
        System.out.println("MENSAJE: " + mensaje.substring(14));
        if (mensaje.substring(14).startsWith("Cliente")) {
            System.out.println("CLIENTE");
            datos = new String[2];
            datos[0] = campos[2];
            datos[1] = campos[3];
            if (iniciarSesionCliente(datos[0], datos[1])) {
                if (listaClientes.contains(datos[0])) {
                    enviarMensaje(clientSocket, "INICIADO");
                } else {
                    listaClientes.add(datos[0]);
                    enviarMensaje(clientSocket, "INICIOSESION_CORRECTO" + "|" + datos[0]);
                }
            } else {
                System.out.println("Error datos incorrectos");
                enviarMensaje(clientSocket, "ERROR_INICIOSESION");
            }
        } else if (mensaje.substring(14).startsWith("Vendedor")) {
            System.out.println("VENDEDOR");
            datos = new String[3];
            datos[0] = campos[2];
            datos[1] = campos[3];
            if (iniciarSesionVendedor(datos[0], datos[1])) {
                if (listaVendedores.contains(datos[0])) {
                    enviarMensaje(clientSocket, "INICIADO");
                } else {
                    nombreUsuario = datos[0];
                    listaVendedores.add(datos[0]);
                    enviarMensaje(clientSocket, "INICIOSESION_CORRECTO" + "|" + datos[0]);
                }
            } else {
                System.out.println("Error datos incorrectos");
                enviarMensaje(clientSocket, "ERROR_INICIOSESION");
            }
        }
    }

    /**
     * Registra un nuevo cliente o vendedor y envia un mensaje de registro
     * correcto o error al cliente.
     *
     * @param clientSocket El socket del cliente.
     * @param mensaje El mensaje de registro.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void registro(Socket clientSocket, String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        String[] datos = null;
        if (mensaje.substring(9).startsWith("Cliente")) {
            datos = new String[2];
            datos[0] = campos[2];
            datos[1] = campos[3];
            if (!comprobarUsuarioCliente(datos[0])) {
                insertarCliente(datos);
                enviarMensaje(clientSocket, "REGISTRO_CORRECTO");
            } else {
                System.out.println("Error en el registro, el usuario que inserta ya existe");
                enviarMensaje(clientSocket, "ERROR_REGISTRO");
            }
        } else if (mensaje.substring(9).startsWith("Vendedor")) {
            datos = new String[3];
            datos[0] = campos[2];
            datos[1] = campos[3];
            datos[2] = campos[4];
            if (!comprobarUsuarioVendedor(datos[0])) {
                insertarVendedor(datos);
                enviarMensaje(clientSocket, "REGISTRO_CORRECTO");

                for (Socket socket : listaSockets) {
                    enviarMensaje(socket, "NUEVOVENDEDOR|" + datos[0]);
                }

            } else {
                System.out.println("Error en el registro, el usuario que inserta ya existe");
                enviarMensaje(clientSocket, "ERROR_REGISTRO");
            }
        }

    }

    /**
     * Lee los datos recibidos y los convierte en una imagen guardada en el
     * servidor.
     *
     * @param mensaje El mensaje que contiene los bytes de la imagen.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void longitud(String mensaje) throws SQLException {

        String[] datos = mensaje.split("\\,");
        byte[] array = new byte[Integer.parseInt(datos[0].substring(9))];

        for (int i = 2; i < datos.length; i++) {
            //System.out.println("Datos " + datos[i]);
            array[i - 2] = Byte.parseByte(datos[i]);

        }

        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT MAX(Imagen) AS max_ruta FROM " + tablas[1], cx);
        String num = "";
        if (rs.next()) {
            // Obtener el maximo valor de la columna "ruta"
            String max_ruta = rs.getString("max_ruta");
            if (max_ruta == null || max_ruta.isBlank()) {
                num = "Imagen1";
            } else {
                num = max_ruta.substring(29);
                System.out.println("Numero " + num);
                String nombre = num.substring(6);
                String[] separador = nombre.split("\\.");
                System.out.println("Separador: " + separador[0]);
                int numero = Integer.parseInt(separador[0]);

                num = "Imagen" + (numero + 1);
                System.out.println("Resultado " + num);
            }
            // Utilizar el valor obtenido
            System.out.println("Maximo valor de ruta: " + num);
        }
        ruta = "./Servidor/Recursos/Imagenes/" + num + ".jpg";
        File archivo = new File(ruta);
        try {
            archivo.createNewFile();
            ByteArrayInputStream byteArray = new ByteArrayInputStream(array);
            BufferedImage img = ImageIO.read(byteArray);
            ImageIO.write(img, "jpg", archivo);

        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Modifica la imagen existente en el servidor con los bytes recibidos.
     *
     * @param mensaje El mensaje que contiene los datos de la imagen.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void modificarImagen(String mensaje) throws SQLException {

        String[] datos = mensaje.split("\\,");
        byte[] array = new byte[Integer.parseInt(datos[0].substring(18))];

        for (int i = 2; i < datos.length; i++) {
            //System.out.println("Datos " + datos[i]);
            array[i - 2] = Byte.parseByte(datos[i]);

        }

        String nombre = imagen.substring(29);
        String[] nombreSeparador = nombre.split("\\.");
        String nombreImagen = nombreSeparador[0];

        ruta = "./Servidor/Recursos/Imagenes/" + nombreImagen + ".jpg";
        File archivo = new File(ruta);
        File archivoNuevo = new File(ruta);
        if (archivo.exists()) {
            if (archivo.delete()) {
                System.out.println("El archivo ha sido eliminado exitosamente.");
                try {
                    archivoNuevo.createNewFile();
                    System.out.println("RUTA - " + ruta);
                    ByteArrayInputStream byteArray = new ByteArrayInputStream(array);
                    BufferedImage img = ImageIO.read(byteArray);
                    ImageIO.write(img, "jpg", archivoNuevo);

                } catch (IOException ex) {
                    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Recoger los datos de una pieza para modificar los datos.
     *
     * @param mensaje El mensaje que contiene los nuevos datos de la pieza.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void modificarDatos(String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        String[] datos = new String[7];
        datos[0] = campos[1];
        datos[1] = campos[2];
        datos[2] = campos[3];
        datos[3] = campos[4];
        datos[4] = campos[5];
        datos[5] = campos[6];

        actualizarPiezas(datos);
    }

    /**
     * Recoger los datos de un vendedor para modificar los datos si ese nombre no esta ya en la base de datos.
     *
     * @param clienteSocket El socket del cliente.
     * @param mensaje El mensaje que contiene los nuevos datos del vendedor.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void modificarVendedor(Socket clienteSocket, String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        String[] datos = new String[4];
        datos[0] = campos[1];
        datos[1] = campos[2];
        datos[2] = campos[3];
        datos[3] = campos[4];

        String[] tablas = obtenerNombresTablas();
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);
        boolean encontrado = false;

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombreVendedor = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);

                if (nombreVendedor.equals(datos[1])) {
                    encontrado = true;
                    enviarMensaje(clienteSocket, "NOMBREENCONTRADO");
                }
            }
            if (!encontrado) {
                actualizarVendedor(datos);
            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    /**
     * Recoger los datos de un cliente para modificar los datos si ese nombre no esta ya en la base de datos.
     * @param clienteSocket El socket del cliente.
     * @param mensaje El mensaje que contiene los nuevos datos del cliente.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void modificarCliente(Socket clienteSocket, String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        String[] datos = new String[3];
        datos[0] = campos[1];
        datos[1] = campos[2];
        datos[2] = campos[3];

        String[] tablas = obtenerNombresTablas();
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[0], cx);
        String[] columnas = obtenerColumnas(tablas[0]);
        boolean encontrado = false;

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombrePieza = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);

                if (nombrePieza.equals(datos[1])) {
                    encontrado = true;
                    enviarMensaje(clienteSocket, "NOMBREENCONTRADO");
                }
            }
            if (!encontrado) {
                actualizarCliente(datos);
            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    /**
     * Recoge los datos para insertar una pieza.
     *
     * @param mensaje El mensaje que contiene los datos de la pieza.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void pieza(String mensaje) throws SQLException {
        String[] campos = mensaje.split("\\|");
        String[] datos = null;
        datos = new String[5];
        datos[0] = campos[1];
        datos[1] = campos[2];
        nombrePieza = datos[1];
        datos[2] = campos[3];
        datos[3] = campos[4];
        datos[4] = ruta;

        insertarPieza(datos);
    }

    /**
     * Envia la lista de vendedores al cliente a traves del socket.
     *
     * @param clientSocket El socket del cliente.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void recogerVendedores(Socket clientSocket) throws SQLException {
        String[] tablas = obtenerNombresTablas();

        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);
        enviarMensaje(clientSocket, "VENDEDORES");
        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);
                enviarMensaje(clientSocket, nombre);

            }
            enviarMensaje(clientSocket, "FIN");
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Envia todas las piezas a traves del socket.
     *
     * @param clientSocket El socket del cliente.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void totalPiezas(Socket clientSocket) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[1], cx);
        String[] columnas = obtenerColumnas(tablas[1]);
        try {
            while (rs.next()) {

                int IDPieza = rs.getInt(columnas[0]);
                String tipoPieza = rs.getString(columnas[1]);
                String nombrePieza = rs.getString(columnas[2]);
                String descripcionPieza = rs.getString(columnas[3]);
                String precioPieza = rs.getString(columnas[4]);
                String rutaPieza = rs.getString(columnas[5]);

                String datosPieza = IDPieza + "|" + tipoPieza + "|" + nombrePieza + "|" + descripcionPieza + "|" + precioPieza + "|" + rutaPieza;
                enviarMensaje(clientSocket, "TPIEZAS|" + datosPieza);
            }
            enviarMensaje(clientSocket, "FIN");
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Envia todos los clientes a traves del socket.
     *
     * @param clientSocket El socket del cliente.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void totalClientes(Socket clientSocket) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[0], cx);
        String[] columnas = obtenerColumnas(tablas[0]);
        try {
            while (rs.next()) {

                int ID = rs.getInt(columnas[0]);
                String nombrePieza = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);

                String datosPieza = ID + "|" + nombrePieza + "|" + contrasenia;
                enviarMensaje(clientSocket, "TCLIENTES|" + datosPieza);
            }

        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Envia todos los vendedores a traves del socket.
     *
     * @param clientSocket El socket del cliente.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void totalVendedores(Socket clientSocket) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);
        try {
            while (rs.next()) {

                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);
                String ubicacion = rs.getString(columnas[3]);

                String datosPieza = ID + "|" + nombre + "|" + contrasenia + "|" + ubicacion;
                enviarMensaje(clientSocket, "TVENDEDORES|" + datosPieza);
            }

        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Recoge las piezas asociadas al vendedor actual y las envia al cliente a
     * traves del socket.
     *
     * @param clientSocket El socket del cliente.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void recogerPiezas(Socket clientSocket) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        int totalPiezasEnviadas = 0;
        // Tabla Vendedores
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);

        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);
                String ubicacion = rs.getString(columnas[3]);

                if (nombre.equals(nombreUsuario)) {
                    // Obtener el ID del vendedor actual
                    int vendedorID = rs.getInt(columnas[0]);

                    // Consultar todas las filas de la tabla PiezasVendedor para este vendedor
                    ResultSet rs2 = mysql.consultar("SELECT * FROM " + tablas[3] + " WHERE VendedorID = " + vendedorID, cx);
                    String[] columnas2 = obtenerColumnas(tablas[3]);

                    while (rs2.next()) {
                        int PiezaID = rs2.getInt(columnas2[2]);

                        // Consultar los detalles de cada pieza y enviarlos al cliente
                        ResultSet rs3 = mysql.consultar("SELECT * FROM " + tablas[1] + " WHERE ID = " + PiezaID, cx);
                        String[] columnas3 = obtenerColumnas(tablas[1]);

                        while (rs3.next()) {
                            int IDPieza = rs3.getInt(columnas3[0]);
                            String tipoPieza = rs3.getString(columnas3[1]);
                            String nombrePieza = rs3.getString(columnas3[2]);
                            String descripcionPieza = rs3.getString(columnas3[3]);
                            String precioPieza = rs3.getString(columnas3[4]);
                            String rutaPieza = rs3.getString(columnas3[5]);

                            String datosPieza = IDPieza + "|" + tipoPieza + "|" + nombrePieza + "|" + descripcionPieza + "|" + precioPieza + "|" + rutaPieza;
                            enviarMensaje(clientSocket, "PIEZAS|" + datosPieza);
                            totalPiezasEnviadas++;
                        }

                        rs3.close();
                    }

                    rs2.close(); // Cerrar el ResultSet
                }
            }
            enviarMensaje(clientSocket, "FIN");
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Recoge las piezas asociadas al cliente especifico y las envia al cliente
     * a traves del socket.
     *
     * @param clientSocket El socket del cliente.
     * @param mensaje El mensaje que contiene el nombre del cliente.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void recogerPiezasCliente(Socket clientSocket, String mensaje) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        int totalPiezasEnviadas = 0;
        //Tabla Cliente
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[0], cx);
        String[] columnas = obtenerColumnas(tablas[0]);

        String[] partes = mensaje.split("\\|");
        String cliente = partes[1];
        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);

                if (nombre.equals(cliente)) {
                    // Obtener el ID del vendedor actual
                    int ClienteID = rs.getInt(columnas[0]);

                    // Consultar todas las filas de la tabla PiezasVendedor para este vendedor
                    ResultSet rs2 = mysql.consultar("SELECT * FROM " + tablas[2] + " WHERE ClienteID = " + ClienteID, cx);
                    String[] columnas2 = obtenerColumnas(tablas[2]);

                    while (rs2.next()) {
                        int PiezaID = rs2.getInt(columnas2[2]);

                        // Consultar los detalles de cada pieza y enviarlos al cliente
                        ResultSet rs3 = mysql.consultar("SELECT * FROM " + tablas[1] + " WHERE ID = " + PiezaID, cx);
                        String[] columnas3 = obtenerColumnas(tablas[1]);

                        while (rs3.next()) {
                            int IDPieza = rs3.getInt(columnas3[0]);
                            String tipoPieza = rs3.getString(columnas3[1]);
                            String nombrePieza = rs3.getString(columnas3[2]);
                            String descripcionPieza = rs3.getString(columnas3[3]);
                            String precioPieza = rs3.getString(columnas3[4]);
                            String rutaPieza = rs3.getString(columnas3[5]);

                            String datosPieza = IDPieza + "|" + tipoPieza + "|" + nombrePieza + "|" + descripcionPieza + "|" + precioPieza + "|" + rutaPieza;
                            System.out.println("DATOS -> " + datosPieza);
                            enviarMensaje(clientSocket, "CLIENTEPIEZA|" + datosPieza);
                            totalPiezasEnviadas++;
                        }

                        rs3.close();
                    }

                    rs2.close(); // Cerrar el ResultSet

                }
            }
            enviarMensaje(clientSocket, "FIN");
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Recoge el DI de la pieza asociada al vendedor y llama a eliminar pieza.
     *
     * @param mensaje El mensaje que contiene el nombre del vendedor.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void recogerIDsPiezasVendedor(String mensaje) throws SQLException {
        String[] tablas = obtenerNombresTablas();

        //Tabla Vendedores
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);

        String[] partes = mensaje.split("\\|");
        String vendedor = partes[1];
        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);
                String ubicacion = rs.getString(columnas[3]);

                if (nombre.equals(vendedor)) {
                    // Obtener el ID del vendedor actual
                    int vendedorID = rs.getInt(columnas[0]);

                    // Consultar todas las filas de la tabla PiezasVendedor para este vendedor
                    ResultSet rs2 = mysql.consultar("SELECT * FROM " + tablas[3] + " WHERE VendedorID = " + vendedorID, cx);
                    String[] columnas2 = obtenerColumnas(tablas[3]);

                    while (rs2.next()) {
                        int PiezaID = rs2.getInt(columnas2[2]);
                        eliminarPieza("ELIMINARPIEZA|" + PiezaID);
                    }

                    rs2.close(); // Cerrar el ResultSet
                }
            }
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Recoge las piezas asociadas al vendedor especifico y las envia al cliente
     * a traves del socket.
     *
     * @param clientSocket El socket del cliente.
     * @param mensaje El mensaje que contiene el nombre del vendedor.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    public static void recogerPiezasVendedor(Socket clientSocket, String mensaje) throws SQLException {
        String[] tablas = obtenerNombresTablas();
        int totalPiezasEnviadas = 0;
        //Tabla Vendedores
        ResultSet rs = mysql.consultar("SELECT * FROM " + tablas[4], cx);
        String[] columnas = obtenerColumnas(tablas[4]);

        String[] partes = mensaje.split("\\|");
        String vendedor = partes[1];
        try {
            while (rs.next()) {
                int ID = rs.getInt(columnas[0]);
                String nombre = rs.getString(columnas[1]);
                String contrasenia = rs.getString(columnas[2]);
                String ubicacion = rs.getString(columnas[3]);

                if (nombre.equals(vendedor)) {
                    // Obtener el ID del vendedor actual
                    int vendedorID = rs.getInt(columnas[0]);

                    // Consultar todas las filas de la tabla PiezasVendedor para este vendedor
                    ResultSet rs2 = mysql.consultar("SELECT * FROM " + tablas[3] + " WHERE VendedorID = " + vendedorID, cx);
                    String[] columnas2 = obtenerColumnas(tablas[3]);

                    while (rs2.next()) {
                        int PiezaID = rs2.getInt(columnas2[2]);

                        // Consultar los detalles de cada pieza y enviarlos al cliente
                        ResultSet rs3 = mysql.consultar("SELECT * FROM " + tablas[1] + " WHERE ID = " + PiezaID, cx);
                        String[] columnas3 = obtenerColumnas(tablas[1]);

                        while (rs3.next()) {
                            int IDPieza = rs3.getInt(columnas3[0]);
                            String tipoPieza = rs3.getString(columnas3[1]);
                            String nombrePieza = rs3.getString(columnas3[2]);
                            String descripcionPieza = rs3.getString(columnas3[3]);
                            String precioPieza = rs3.getString(columnas3[4]);
                            String rutaPieza = rs3.getString(columnas3[5]);

                            String datosPieza = IDPieza + "|" + tipoPieza + "|" + nombrePieza + "|" + descripcionPieza + "|" + precioPieza + "|" + rutaPieza;
                            System.out.println("DATOS -> " + datosPieza);
                            enviarMensaje(clientSocket, "PIEZAVENDEDOR|" + datosPieza);
                            totalPiezasEnviadas++;
                        }

                        rs3.close();
                    }

                    rs2.close(); // Cerrar el ResultSet
                }
            }
            enviarMensaje(clientSocket, "FIN");
        } catch (SQLException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Error leyendo consulta", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Envia un mensaje a traves del socket del cliente.
     *
     * @param clientSocket El socket del cliente.
     * @param mensaje El mensaje a enviar.
     */
    public static void enviarMensaje(Socket clientSocket, String mensaje) {
        try {
            PrintWriter salidaACliente = new PrintWriter(clientSocket.getOutputStream(), true);
            salidaACliente.println(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}//end MainServidor

