import dao.conexion.GeneradorConexiones;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Aplicacion {

    public static void main(String[] args){

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try(InputStream in = (classLoader.getResourceAsStream("conexion-bd.json"))){
            GeneradorConexiones generadorConexiones = GeneradorConexiones.getInstance();
            generadorConexiones.cargarParametros(in);
            try(Connection connection = generadorConexiones.obtenerConexion()){

            }

            try(Connection connection = generadorConexiones.obtenerConexion()){

            }

            try(Connection connection = generadorConexiones.obtenerConexion()){
                try(PreparedStatement pst = connection.prepareStatement("select * from eps");
                    ResultSet rs = pst.executeQuery()){
                    while(rs.next()){
                        for(int i = 1 ; i <= rs.getMetaData().getColumnCount() ; i++)
                            System.out.print(rs.getString(i) + " ");

                        System.out.println();
                    }
                }
            }

            generadorConexiones.cerrarPoolDeConexiones();
        }
        catch (IOException | SQLException e){
            e.printStackTrace(System.err);
        }
    }

}
