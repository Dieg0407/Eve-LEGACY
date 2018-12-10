package beans;

import com.azoth.eve.anotaciones.CampoTabla;
import com.azoth.eve.anotaciones.LlavePrimaria;
import com.azoth.eve.anotaciones.LlavePrimariaCompuesta;
import com.azoth.eve.anotaciones.NombreTabla;

import java.sql.Types;

@NombreTabla(nombre = "public.localidad")
@LlavePrimariaCompuesta
public class Localidad {

    @LlavePrimaria
    @CampoTabla(campo = "codlocacion",tipoDato = Types.INTEGER)
    private int codigoLocalidad;

    @CampoTabla(campo = "denlocacion",tipoDato = Types.VARCHAR)
    private String descripcionLocalidad;

    @CampoTabla(campo = "fecultact",tipoDato = Types.VARCHAR)
    private String fechaActualizacion;

    @CampoTabla(campo = "horultact",tipoDato = Types.VARCHAR)
    private String horaAcualizacion;

    @CampoTabla(campo = "usrultact",tipoDato = Types.VARCHAR)
    private String usuarioActualizador;

    @LlavePrimaria
    @CampoTabla(campo = "codeps",tipoDato = Types.INTEGER)
    private int codigoEps;

    public Localidad(){

    }

    public int getCodigoLocalidad() {
        return codigoLocalidad;
    }

    public void setCodigoLocalidad(int codigoLocalidad) {
        this.codigoLocalidad = codigoLocalidad;
    }

    public int getCodigoEps() {
        return codigoEps;
    }

    public void setCodigoEps(int codigoEps) {
        this.codigoEps = codigoEps;
    }

    public String getDescripcionLocalidad() {
        return descripcionLocalidad;
    }

    public void setDescripcionLocalidad(String descripcionLocalidad) {
        this.descripcionLocalidad = descripcionLocalidad;
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

    @Override
    public String toString() {
        return "Localidad{" +
                "codigoLocalidad=" + codigoLocalidad +
                ", codigoEps=" + codigoEps +
                ", descripcionLocalidad=" + descripcionLocalidad +
                ", fechaActualizacion='" + fechaActualizacion + '\'' +
                ", horaAcualizacion='" + horaAcualizacion + '\'' +
                ", usuarioActualizador='" + usuarioActualizador + '\'' +
                '}';
    }
}
