package beans;

import com.azoth.eve.anotaciones.CampoTabla;
import com.azoth.eve.anotaciones.LlavePrimaria;
import com.azoth.eve.anotaciones.LlavePrimariaSimple;
import com.azoth.eve.anotaciones.NombreTabla;

import java.sql.Types;

@LlavePrimariaSimple
@NombreTabla(nombre = "public.gis_usuarios")
public class Usuario {

    @LlavePrimaria
    @CampoTabla(campo = "user_cod",tipoDato = Types.VARCHAR)
    private String codigoUsuario;
    @CampoTabla(campo = "user_name",tipoDato = Types.VARCHAR)
    private String nombreUsuario;
    @CampoTabla(campo = "user_pw",tipoDato = Types.VARCHAR)
    private String password;
    @CampoTabla(campo = "perfil_cod",tipoDato = Types.INTEGER)
    private Integer codigoPerfil;
    @CampoTabla(campo = "perfil_des",tipoDato = Types.VARCHAR)
    private String descripcionPerfil;

    public Usuario(){}

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public void setCodigoUsuario(String codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getCodigoPerfil() {
        return codigoPerfil;
    }

    public void setCodigoPerfil(Integer codigoPerfil) {
        this.codigoPerfil = codigoPerfil;
    }

    public String getDescripcionPerfil() {
        return descripcionPerfil;
    }

    public void setDescripcionPerfil(String descripcionPerfil) {
        this.descripcionPerfil = descripcionPerfil;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "codigoUsuario='" + codigoUsuario + '\'' +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", password='" + password + '\'' +
                ", codigoPerfil=" + codigoPerfil +
                ", descripcionPerfil='" + descripcionPerfil + '\'' +
                '}';
    }
}
