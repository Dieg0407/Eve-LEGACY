package beans;

import dao.anotaciones.CampoTabla;
import dao.anotaciones.LlaveForanea;
import dao.anotaciones.LlavePrimaria;

import java.sql.Types;

public class Eps {

    @LlavePrimaria
    @CampoTabla(campo = "codeps",tipoDato = Types.INTEGER)
    private int codigo;

    @CampoTabla(campo = "deneps",tipoDato = Types.VARCHAR)
    private String denominacion;

    @CampoTabla(campo = "fecultact",tipoDato = Types.VARCHAR)
    private String fechaActualizacion;

    @CampoTabla(campo = "horultact",tipoDato = Types.VARCHAR)
    private String horaAcualizacion;

    @CampoTabla(campo = "usrultact",tipoDato = Types.VARCHAR)
    private String usuarioActualizador;

    @LlaveForanea
    @CampoTabla(campo = "codgrupo",tipoDato = Types.INTEGER)
    private int codigoGrupo;

    @LlaveForanea
    @CampoTabla(campo = "codregion",tipoDato = Types.INTEGER)
    private int codigoRegion;

    public Eps(){

    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getHoraAcualizacion() {
        return horaAcualizacion;
    }

    public void setHoraAcualizacion(String horaAcualizacion) {
        this.horaAcualizacion = horaAcualizacion;
    }

    public String getUsuarioActualizador() {
        return usuarioActualizador;
    }

    public void setUsuarioActualizador(String usuarioActualizador) {
        this.usuarioActualizador = usuarioActualizador;
    }

    public int getCodigoGrupo() {
        return codigoGrupo;
    }

    public void setCodigoGrupo(int codigoGrupo) {
        this.codigoGrupo = codigoGrupo;
    }

    public int getCodigoRegion() {
        return codigoRegion;
    }

    public void setCodigoRegion(int codigoRegion) {
        this.codigoRegion = codigoRegion;
    }
}
