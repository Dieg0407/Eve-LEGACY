import beans.Eps;
import dao.anotaciones.CampoTabla;
import dao.anotaciones.LlavePrimaria;
import dao.conexion.GeneradorConexiones;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/*
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

*/
public abstract class Aplicacion {

    public static void main(String[] args) throws ClassNotFoundException {
        /*
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
        */

        Class clase = Class.forName("beans.Eps");
        for(Field f : clase.getDeclaredFields()){
            System.out.println("***" + f.getName()+ "***");
            CampoTabla campo = f.getAnnotation(CampoTabla.class);
            LlavePrimaria llavePrimaria = f.getAnnotation(LlavePrimaria.class);
            System.out.print(String.format("%s:%d:%s",campo.campo(),campo.tipoDato(),String.valueOf(llavePrimaria != null)));

            System.out.println();
            System.out.println();


        }
    }

}
