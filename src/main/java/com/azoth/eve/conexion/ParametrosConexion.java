package com.azoth.eve.conexion;

/***
 * Clase que contiene los parametros necesarios
 * para realizar la conexión a una Base de Datos
 */
public class ParametrosConexion {

    //Tipo de Conexion a realizar
    private String tipo;

    //En caso este desplegado en un servidor de aplicaciónes con pool de conexiones activo
    private String jndi;

    //Componentes básicos para la conexion
    private String url;
    private String puerto;
    private String usuario;
    private String password;
    private String baseDeDatos;

    //Cantidad de conexiones para el Pooling local
    private int numeroDeConexiones;

    //Proveedor de la Base de Datos
    private String proveedor;

    public ParametrosConexion(){}

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBaseDeDatos() {
        return baseDeDatos;
    }

    public void setBaseDeDatos(String baseDeDatos) {
        this.baseDeDatos = baseDeDatos;
    }

    public int getNumeroDeConexiones() {
        return numeroDeConexiones;
    }

    public void setNumeroDeConexiones(int numeroDeConexiones) {
        this.numeroDeConexiones = numeroDeConexiones;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
}
