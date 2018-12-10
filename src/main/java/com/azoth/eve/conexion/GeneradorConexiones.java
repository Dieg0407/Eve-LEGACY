package com.azoth.eve.conexion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GeneradorConexiones {

    private ParametrosConexion parametrosConexion;

    private GenericObjectPool poolDeConexiones = null;
    private ConnectionFactory connectionFactory = null;
    private PoolableConnectionFactory poolableConnectionFactory = null;

    private static GeneradorConexiones instancia;

    private GeneradorConexiones(){}

    public static GeneradorConexiones getInstance(){
        if(instancia == null)
            instancia = new GeneradorConexiones();

        return instancia;
    }

    public void cargarParametros(InputStream archivo){
        ObjectMapper mapper = new ObjectMapper();
        ParametrosConexion parametrosConexion = new ParametrosConexion();

        try{
            JsonNode root = mapper.readTree(archivo);
            parametrosConexion.setTipo(root.get("tipo").textValue());

            JsonNode local = root.get("local"),servidor = root.get("servidor");

            parametrosConexion.setUsuario(local.get("usuario").textValue());
            parametrosConexion.setPassword(local.get("password").textValue());
            parametrosConexion.setUrl(local.get("url").textValue());
            parametrosConexion.setPuerto(local.get("puerto").textValue());
            parametrosConexion.setBaseDeDatos(local.get("bd").textValue());
            parametrosConexion.setProveedor(local.get("proveedor").textValue());
            parametrosConexion.setNumeroDeConexiones(local.get("conexiones").asInt());

            parametrosConexion.setJndi(servidor.get("jndi").textValue());

            this.parametrosConexion = parametrosConexion;
        }
        catch (IOException | NullPointerException e){
            e.printStackTrace(System.err);
        }
    }

    public ParametrosConexion getParametros(){
        return this.parametrosConexion;
    }

    public Connection obtenerConexion() throws SQLException{
        Connection conexion = null;
        if(parametrosConexion.getTipo().equals("local"))
            conexion = this.generarConexionLocal();

        else if(parametrosConexion.getTipo().equals("servidor"))
            conexion = this.generarConexionServidor();

        return conexion;
    }

    private Connection generarConexionLocal() throws SQLException {
        if(this.poolDeConexiones == null || this.connectionFactory == null || this.poolableConnectionFactory == null){

            String jdbcUrl = "";
            switch (this.parametrosConexion.getProveedor()){
                case "mysql":
                    DriverManager.registerDriver(new com.mysql.jdbc.Driver());
                    jdbcUrl = String.format("jdbc:mysql://%s:%s/%s",
                            this.parametrosConexion.getUrl(),
                            this.parametrosConexion.getPuerto(),
                            this.parametrosConexion.getBaseDeDatos());
                    break;
                case "postgres":
                    DriverManager.registerDriver(new org.postgresql.Driver());
                    jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s",
                            this.parametrosConexion.getUrl(),
                            this.parametrosConexion.getPuerto(),
                            this.parametrosConexion.getBaseDeDatos());
                    break;
            }

            this.poolDeConexiones = new GenericObjectPool();
            this.poolDeConexiones.setMaxActive(this.parametrosConexion.getNumeroDeConexiones());

            this.connectionFactory = new DriverManagerConnectionFactory(
                    jdbcUrl,
                    this.parametrosConexion.getUsuario(),
                    this.parametrosConexion.getPassword());

            this.poolableConnectionFactory = new PoolableConnectionFactory(
                    this.connectionFactory,
                    this.poolDeConexiones,
                    null,
                    null,
                    false,
                    true);
        }

        return new PoolingDataSource(this.poolDeConexiones).getConnection();
    }
    private Connection generarConexionServidor() throws SQLException{
        Connection connection;
        try {

            InitialContext initialContext = new InitialContext();
            DataSource ds = (DataSource)initialContext.lookup(this.parametrosConexion.getJndi());
            connection = ds.getConnection();

        } catch (NamingException e) {
            e.printStackTrace();
            connection = null;
        }
        return connection;
    }

    public void cerrarPoolDeConexiones(){
        if(this.poolDeConexiones != null){
            try{
                this.poolDeConexiones.close();
                this.poolDeConexiones = null;
                this.connectionFactory = null;
                this.poolableConnectionFactory = null;

                System.gc();
            }
            catch (Exception e){
                e.printStackTrace(System.err);
            }
        }
    }
}
