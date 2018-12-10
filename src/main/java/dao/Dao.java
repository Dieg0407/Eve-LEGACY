package dao;

import dao.anotaciones.*;
import dao.excepciones.BadDefinitionException;
import dao.excepciones.WrongOperationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Dao {
    private Connection conexion;
    private Class claseBean;
    private NombreTabla nombreTabla;

    public Dao(Connection conexion, Class claseBean) throws BadDefinitionException {
        this.conexion = conexion;
        this.claseBean = claseBean;
        this.nombreTabla = (NombreTabla) claseBean.getAnnotation(NombreTabla.class);

        this.verificarEstructura();
    }

    public Object obtenerRegistro(Object dato) throws WrongOperationException,BadDefinitionException{
        if(claseBean.getAnnotation(LlavePrimariaCompuesta.class) != null)
            throw new WrongOperationException("La llave primaria esta definida como compuesta");

        List<Field> campos = new ArrayList<>();
        List<CampoTabla> llavesPrimarias = new ArrayList<>();
        StringBuilder campoSeleccionados =  new StringBuilder();

        this.construirSelectPrimario(campos,llavesPrimarias,campoSeleccionados);

        String sql = String.format("SELECT %s FROM %s WHERE %s = ? ",
                campoSeleccionados.toString(),
                this.nombreTabla.nombre(),
                llavesPrimarias.get(0).campo());

        Object bean = this.obtenerNuevaInstancia();

        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            pst.setObject(1,dato,llavesPrimarias.get(0).tipoDato());
            try(ResultSet rs = pst.executeQuery()){
                while(rs.next())
                    this.resultSetAObjeto(bean,rs,campos);

            }
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
        }
        return bean;
    }
    public Object obtenerRegistro(List<Object> datos)throws WrongOperationException,BadDefinitionException{
        if(claseBean.getAnnotation(LlavePrimariaSimple.class) != null)
            throw new WrongOperationException("La llave primaria esta definida como simple");

        List<Field> campos = new ArrayList<>();
        List<CampoTabla> llavesPrimarias = new ArrayList<>();
        StringBuilder campoSeleccionados =  new StringBuilder();

        this.construirSelectPrimario(campos,llavesPrimarias,campoSeleccionados);

        String sql = String.format("SELECT %s FROM %s WHERE %s = ? ",
                campoSeleccionados.toString(),
                this.nombreTabla.nombre(),
                llavesPrimarias.get(0).campo());

        Object bean = this.obtenerNuevaInstancia();

        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 0;
            for(Object dato : datos)
                pst.setObject(i+1,dato,llavesPrimarias.get(i++).tipoDato());

            try(ResultSet rs = pst.executeQuery()){
                while(rs.next())
                    this.resultSetAObjeto(bean,rs,campos);

            }
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
        }
        return bean;
    }

    private void construirSelectPrimario(List<Field> campos,List<CampoTabla> llavePrimaria,StringBuilder campoSeleccionados){

        for(Field campo : claseBean.getDeclaredFields()){
            if(campo.getAnnotation(LlavePrimaria.class) != null)
                llavePrimaria.add(campo.getAnnotation(CampoTabla.class));

            CampoTabla campoTabla = campo.getAnnotation(CampoTabla.class);
            if(campo.getAnnotation(CampoTabla.class) != null){
                campoSeleccionados.append(campoTabla.campo());
                campoSeleccionados.append(",");
                campos.add(campo);
            }
        }
        campoSeleccionados.deleteCharAt(campoSeleccionados.length()-1);
    }

    //METODOS PRIVADOS DE CORTE GENERAL

    private String clausulaWhere(List<Parametro> parametros){
        String retorno = null;
        if(parametros.size() == 0)
            retorno =  "";
        else{
            StringBuilder builder = new StringBuilder();

            Parametro parametro = parametros.get(0);
            builder.append(parametro .getCampoTabla().campo());
            builder.append(parametro .getOperacion().getSimbolo());
            if(parametro .getValorUnico() != null)
                builder.append("?");
            else {
                builder.append("(");
                parametro .getValorCompuesto().forEach(o -> builder.append("?,"));
                builder.deleteCharAt(builder.length()-1);
                builder.append(")");
            }

            parametros.remove(0);
            parametros.forEach(p -> {
                builder.append(p.getCondicional().getId());
                builder.append(" ");
                builder.append(p.getCampoTabla().campo());
                builder.append(p.getOperacion().getSimbolo());
                if(p.getValorUnico() != null)
                    builder.append("?");
                else {
                    builder.append("(");
                    p.getValorCompuesto().forEach(o -> builder.append("?,"));
                    builder.deleteCharAt(builder.length()-1);
                    builder.append(")");
                }
                builder.append(" ");
            });
            retorno = builder.toString();
        }
        return retorno;
    }

    private Object obtenerNuevaInstancia() throws BadDefinitionException{
        Object bean;
        try{
            Constructor constructor = this.claseBean.getDeclaredConstructor();
            bean = constructor.newInstance();
        }
        catch (NoSuchMethodException e){
            throw new BadDefinitionException("Debe de existir un Constructor vacío");
        } catch (IllegalAccessException e) {
            throw new BadDefinitionException("No se tiene acceso al constructor del Bean, debe de estar en público");
        } catch (InvocationTargetException | InstantiationException e) {
            throw new BadDefinitionException("No se pudo llamar al constructor del Bean");
        }

        return bean;
    }

    private Object resultSetAObjeto(Object bean, ResultSet resultSet, List<Field> campos)
            throws BadDefinitionException, SQLException{
        try{
            int i = 1;
            for(Field campo : campos){

                String nombre = campo.getName();
                nombre = nombre.substring(0,1).toUpperCase() + nombre.substring(1);
                Method setter =  claseBean.getDeclaredMethod("set"+nombre,campo.getType());
                setter.invoke(bean,resultSet.getObject(i++));

            }
        }
        catch (NoSuchMethodException e){
            throw new BadDefinitionException("Falta un Setter para el Bean");
        }
        catch (IllegalArgumentException e){
            throw new BadDefinitionException("El campo del Bean no es compatible con la Base de Datos");
        }
        catch (InvocationTargetException e){
            throw new BadDefinitionException("No se pudo llamar al setter del Bean");
        }
        catch (IllegalAccessException e){
            throw new BadDefinitionException("No se tiene acceso al setter del Bean, debe de estar en público");
        }
        return bean;
    }

    private void verificarEstructura()  throws BadDefinitionException{
        if(claseBean.getAnnotation(NombreTabla.class) == null ||
                ( claseBean.getAnnotation(LlavePrimariaSimple.class) == null &&
                        claseBean.getAnnotation(LlavePrimariaCompuesta.class) == null ))
            throw new BadDefinitionException("Error en la definición del Bean");

        if(claseBean.getAnnotation(LlavePrimariaSimple.class) != null){
            Field primaria = null;
            for(Field campo : this.claseBean.getDeclaredFields()){
                if(campo.getAnnotation(LlavePrimaria.class) != null && primaria == null)
                    primaria = campo;
                else if(campo.getAnnotation(LlavePrimaria.class) != null && primaria != null)
                    throw new BadDefinitionException("Existen dos llaves primarias");
            }
            if(primaria == null){
                throw new BadDefinitionException("No existe llave primaria");
            }
        }
        else if(claseBean.getAnnotation(LlavePrimariaCompuesta.class) != null){
            Field primaria = null;
            for(Field campo : this.claseBean.getDeclaredFields()){
                if(campo.getAnnotation(LlavePrimaria.class) != null && primaria == null)
                    primaria = campo;
            }
            if(primaria == null){
                throw new BadDefinitionException("No existe llave primaria");
            }
        }

    }
}
