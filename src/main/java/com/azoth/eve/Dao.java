package com.azoth.eve;

import com.azoth.eve.anotaciones.*;
import com.azoth.eve.excepciones.BadDefinitionException;
import com.azoth.eve.condicionales.Condicional;
import com.azoth.eve.condicionales.Operacion;
import com.azoth.eve.excepciones.WrongOperationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


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
        boolean existeRegistro = false;
        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            pst.setObject(1,dato,llavesPrimarias.get(0).tipoDato());
            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()) {
                    this.resultSetAObjeto(bean, rs, campos);
                    existeRegistro = true;
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
        }
        if(!existeRegistro)
            bean = null;
        return bean;
    }
    public Object obtenerRegistro(List<Object> datos)throws WrongOperationException,BadDefinitionException{
        if(claseBean.getAnnotation(LlavePrimariaSimple.class) != null)
            throw new WrongOperationException("La llave primaria esta definida como simple");

        List<Field> campos = new ArrayList<>();
        List<CampoTabla> llavesPrimarias = new ArrayList<>();
        StringBuilder campoSeleccionados =  new StringBuilder();

        this.construirSelectPrimario(campos,llavesPrimarias,campoSeleccionados);


        List<Parametro> parametros = new ArrayList<>();
        int c = 0;
        for(CampoTabla llave : llavesPrimarias){
            parametros.add(new Parametro(llave, Condicional.AND, Operacion.IGUAL,datos.get(c++)));
        }

        String sql = String.format("SELECT %s FROM %s %s ",
                campoSeleccionados.toString(),
                this.nombreTabla.nombre(),
                this.clausulaWhere(parametros));

        Object bean = this.obtenerNuevaInstancia();
        boolean existeRegistro = false;
        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 0;
            for(Object dato : datos)
                pst.setObject(i+1,dato,llavesPrimarias.get(i++).tipoDato());

            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()) {
                    this.resultSetAObjeto(bean, rs, campos);
                    existeRegistro = true;
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
        }
        if(!existeRegistro)
            bean = null;
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

    public List<Object> listarTodos() throws BadDefinitionException{
        List<Field> campos = new ArrayList<>();
        StringBuilder campoSeleccionados =  new StringBuilder();
        this.construirSelect(campos,campoSeleccionados);

        String sql = String.format("SELECT %s FROM %s ",
                campoSeleccionados.toString(),
                this.nombreTabla.nombre());

        List<Object> beans = new ArrayList<>();
        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 0;
            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    beans.add(this.resultSetAObjeto(this.obtenerNuevaInstancia(),rs,campos));
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
        }
        return beans;
    }

    public List<Object> listarRegistros(List<Parametro> parametros) throws BadDefinitionException{
        List<Field> campos = new ArrayList<>();
        StringBuilder campoSeleccionados =  new StringBuilder();
        this.construirSelect(campos,campoSeleccionados);

        String sql = String.format("SELECT %s FROM %s %s",
                campoSeleccionados.toString(),
                this.nombreTabla.nombre(),
                this.clausulaWhere(parametros));

        List<Object> beans = new ArrayList<>();
        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 0;

            for(Parametro parametro:  parametros){
                if(parametro.getValorUnico() != null)
                    pst.setObject(++i,parametro.getValorUnico(),parametro.getCampoTabla().tipoDato());
                else
                    for(Object valorIn : parametro.getValorCompuesto() )
                        pst.setObject(++i,valorIn,parametro.getCampoTabla().tipoDato());
            }

            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    beans.add(this.resultSetAObjeto(this.obtenerNuevaInstancia(),rs,campos));
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
        }
        return beans;
    }

    public List<Object> listarRegistros(List<Parametro> parametros,Map<Field,Boolean> orderBy) throws BadDefinitionException{
        List<Field> campos = new ArrayList<>();
        StringBuilder campoSeleccionados =  new StringBuilder();
        this.construirSelect(campos,campoSeleccionados);

        Map<CampoTabla,Boolean> clausulaOrderBy = new LinkedHashMap<>();
        if(orderBy != null)
            for(Field f : orderBy.keySet())
                if(f.getAnnotation(CampoTabla.class) != null)
                    clausulaOrderBy.put(f.getAnnotation(CampoTabla.class),orderBy.get(f));
                else
                    throw new BadDefinitionException(String.format("El campo %s no posee la anotación CampoTabla",f.getName()));


        String sql = String.format("SELECT %s FROM %s %s %s",
                campoSeleccionados.toString(),
                this.nombreTabla.nombre(),
                this.clausulaWhere(parametros),
                this.clausulaOrderBy(clausulaOrderBy));

        List<Object> beans = new ArrayList<>();
        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 0;

            for(Parametro parametro:  parametros){
                if(parametro.getValorUnico() != null)
                    pst.setObject(++i,parametro.getValorUnico(),parametro.getCampoTabla().tipoDato());
                else
                    for(Object valorIn : parametro.getValorCompuesto() )
                        pst.setObject(++i,valorIn,parametro.getCampoTabla().tipoDato());
            }

            try(ResultSet rs = pst.executeQuery()){
                while(rs.next()){
                    beans.add(this.resultSetAObjeto(this.obtenerNuevaInstancia(),rs,campos));
                }
            }
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
        }
        return beans;
    }

    private void construirSelect(List<Field> campos,StringBuilder campoSeleccionados){

        for(Field campo : claseBean.getDeclaredFields()){
            CampoTabla campoTabla = campo.getAnnotation(CampoTabla.class);
            if(campo.getAnnotation(CampoTabla.class) != null){
                campoSeleccionados.append(campoTabla.campo());
                campoSeleccionados.append(",");
                campos.add(campo);
            }
        }
        campoSeleccionados.deleteCharAt(campoSeleccionados.length()-1);
    }

    public boolean insertarRegistro(Object registro) throws IllegalArgumentException, BadDefinitionException{
        if(!this.claseBean.isInstance(registro))
            throw new IllegalArgumentException("El objeto no es de la clase mapeada en el DAO");

        boolean exito;

        List<Field> campos = new ArrayList<>();
        StringBuilder sentencia =  new StringBuilder();

        this.construirInsert(campos,sentencia);

        String sql = String.format("INSERT INTO %s %s ",
                this.nombreTabla.nombre(),
                sentencia.toString());

        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 1;

            for(Field f : campos){
                CampoTabla campoTabla = f.getAnnotation(CampoTabla.class);
                pst.setObject(i++,this.obtenerDato(registro,f),campoTabla.tipoDato());
            }

            pst.executeUpdate();
            exito = true;
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
            exito = false;
        }
        return exito;
    }

    private void construirInsert(List<Field> campos,StringBuilder sentencia){
        StringBuilder parte1 = new StringBuilder();
        StringBuilder parte2 = new StringBuilder();

        for(Field campo : claseBean.getDeclaredFields()){
            CampoTabla campoTabla = campo.getAnnotation(CampoTabla.class);
            if(campo.getAnnotation(CampoTabla.class) != null){
                parte1.append(campoTabla.campo());
                parte1.append(",");

                parte2.append("?");
                parte2.append(",");

                campos.add(campo);
            }
        }
        parte1.deleteCharAt(parte1.length()-1);
        parte2.deleteCharAt(parte2.length()-1);

        sentencia.append("(");
        sentencia.append(parte1.toString());
        sentencia.append(")");
        sentencia.append(" VALUES (");
        sentencia.append(parte2.toString());
        sentencia.append(");");
    }

    public int borrarRegistros(List<Parametro> parametros) {
        int afectados;

        String sql = String.format("DELETE FROM %s %s",
                this.nombreTabla.nombre(),
                this.clausulaWhere(parametros));

        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 0;

            for(Parametro parametro:  parametros){
                if(parametro.getValorUnico() != null)
                    pst.setObject(++i,parametro.getValorUnico(),parametro.getCampoTabla().tipoDato());
                else
                    for(Object valorIn : parametro.getValorCompuesto() )
                        pst.setObject(++i,valorIn,parametro.getCampoTabla().tipoDato());
            }

            afectados = pst.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
            afectados = -1;
        }
        return afectados;
    }

    public int borrarTodos() {
        int afectados;

        String sql = String.format("DELETE FROM %s ",
                this.nombreTabla.nombre());

        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            afectados = pst.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
            afectados = -1;
        }
        return afectados;
    }

    public int actualizarRegistro(Object registro) throws IllegalArgumentException,BadDefinitionException{
        if(!this.claseBean.isInstance(registro))
            throw new IllegalArgumentException("El objeto no es de la clase mapeada en el DAO");

        int registros;

        List<Field> campos = new ArrayList<>();
        List<Field> llavesPrimarias = new ArrayList<>();
        StringBuilder sentencia = new StringBuilder();

        this.construirUpdate(campos,llavesPrimarias,sentencia);

        List<Parametro> parametros = new ArrayList<>();
        for(Field llave : llavesPrimarias){
            parametros.add(new Parametro(llave.getAnnotation(CampoTabla.class), Condicional.AND, Operacion.IGUAL,new Object()));
        }

        String sql = String.format("UPDATE %s SET %s %s",
                this.nombreTabla.nombre(),
                sentencia.toString(),
                this.clausulaWhere(parametros));

        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 1;
            for(Field f : campos)
                pst.setObject(i++,this.obtenerDato(registro,f),f.getAnnotation(CampoTabla.class).tipoDato());
            for(Field f : llavesPrimarias)
                pst.setObject(i++,this.obtenerDato(registro,f),f.getAnnotation(CampoTabla.class).tipoDato());

            registros = pst.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
            registros = -1;
        }
        return registros;
    }

    private void construirUpdate(List<Field> campos,List<Field> llavePrimaria,StringBuilder sentencia)  {
        for(Field campo : this.claseBean.getDeclaredFields()){
            if(campo.getAnnotation(LlavePrimaria.class) != null)
                llavePrimaria.add(campo);

            CampoTabla campoTabla = campo.getAnnotation(CampoTabla.class);
            if(campoTabla != null){
                sentencia.append(campoTabla.campo());
                sentencia.append(" = ? ,");

                campos.add(campo);
            }
        }
        sentencia.deleteCharAt(sentencia.length()-1);
    }

    public int actualizarRegistro(Map<Field, Object> campos, List<Parametro> condiciones){
        int registros;

        String sql = String.format("UPDATE %s SET %s %s",
                this.nombreTabla.nombre(),
                this.contruirUpdate(campos.keySet()),
                this.clausulaWhere(condiciones));

        Map<CampoTabla,Object> campos2 = new LinkedHashMap<>();
        if(campos != null)
            for(Field f : campos.keySet())
                if(f.getAnnotation(CampoTabla.class) != null)
                    campos2.put(f.getAnnotation(CampoTabla.class),campos.get(f));

        try(PreparedStatement pst = this.conexion.prepareStatement(sql)){
            int i = 1;
            for(CampoTabla f : campos2.keySet())
                pst.setObject(i++,campos2.get(f),f.tipoDato());

            for(Parametro parametro : condiciones)
                if(parametro.getValorUnico() != null)
                    pst.setObject(i++,parametro.getValorUnico(),parametro.getCampoTabla().tipoDato());
                else
                    for(Object valorIn : parametro.getValorCompuesto() )
                        pst.setObject(i++,valorIn,parametro.getCampoTabla().tipoDato());


            registros = pst.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace(System.err);
            registros = -1;
        }
        return registros;

    }

    private String contruirUpdate(Set<Field> fieldSet) throws IllegalArgumentException{
        StringBuilder retorno = new StringBuilder();
        try {
            for(Field f : fieldSet){
                this.claseBean.getDeclaredField(f.getName());
                CampoTabla campoTabla = f.getAnnotation(CampoTabla.class);
                retorno.append(campoTabla.campo());
                retorno.append(" = ? ,");
            }
        }
        catch (NoSuchFieldException e){
            e.printStackTrace(System.err);
            throw new IllegalArgumentException("Uno de los campos no pertenece a la Clase");
        }
        retorno.deleteCharAt(retorno.length()-1);

        return retorno.toString();
    }

    //METODOS PRIVADOS DE CORTE GENERAL
    private String clausulaWhere(List<Parametro> parametros){
        String retorno;
        if(parametros.size() == 0)
            retorno =  "";
        else{
            StringBuilder builder = new StringBuilder(" WHERE ");

            Parametro parametro = parametros.get(0);
            if(parametro.isTrim() && parametro.getCampoTabla().tipoDato() == Types.VARCHAR){
                builder.append("TRIM(");
                builder.append(parametro .getCampoTabla().campo());
                builder.append(")");
            }
            else
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

            for(int i = 1 ; i < parametros.size() ; i++){
                Parametro p = parametros.get(i);
                builder.append(p.getCondicional().getId());

                if(p.isTrim() && p.getCampoTabla().tipoDato() == Types.VARCHAR){
                    builder.append("TRIM(");
                    builder.append(p .getCampoTabla().campo());
                    builder.append(")");
                }
                else
                    builder.append(p .getCampoTabla().campo());

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
            }

            retorno = builder.toString();
        }
        return retorno;
    }
    //Retorna un String con la clausula Order By, si el valor es false entonces es desc
    private String clausulaOrderBy(Map<CampoTabla,Boolean> campoTablaHashMap){
        String retorno;
        if(campoTablaHashMap.keySet().size() == 0)
            retorno =  "";
        else {
            StringBuilder builder = new StringBuilder(" ORDER BY ");
            for(CampoTabla campoTabla : campoTablaHashMap.keySet()){
                builder.append(campoTabla.campo());
                if(!campoTablaHashMap.get(campoTabla))//Si es falso, entonces es desc
                    builder.append(" desc");
                builder.append(",");
            }
            builder.deleteCharAt(builder.length()-1);
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

                Object object = resultSet.getObject(i++);
                if(object != null)
                    if(!object.getClass().equals(campo.getType())) {
                        throw new BadDefinitionException(String.format(
                                "El campo %s del Bean no es compatible con la BD, BD : %s - Bean : %s",
                                nombre, object.getClass(), campo.getType()));
                    }

                setter.invoke(bean,object);

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

    private Object obtenerDato(Object bean, Field campo) throws BadDefinitionException{
        try{
            String nombre = campo.getName();
            nombre = nombre.substring(0,1).toUpperCase() + nombre.substring(1);
            Method getter =  claseBean.getDeclaredMethod("get"+nombre);
            return getter.invoke(bean);
        }
        catch (NoSuchMethodException e){
            throw new BadDefinitionException("Falta un Getter para el Bean");
        }
        catch (InvocationTargetException e){
            throw new BadDefinitionException("No se pudo llamar al getter del Bean");
        }
        catch (IllegalAccessException e){
            throw new BadDefinitionException("No se tiene acceso al getter del Bean, debe de estar en público");
        }

    }

    private void verificarEstructura()  throws BadDefinitionException{
        if(claseBean.getAnnotation(NombreTabla.class) == null ||
                ( claseBean.getAnnotation(LlavePrimariaSimple.class) == null &&
                        claseBean.getAnnotation(LlavePrimariaCompuesta.class) == null ))
            throw new BadDefinitionException("Error en la definición del Bean");

        if(Arrays.asList(claseBean.getDeclaredFields())
                .stream()
                .filter(d -> d.getAnnotation(CampoTabla.class) != null)
                .collect(Collectors.toList())
            .size() == 0)
            throw new BadDefinitionException("El bean debe de tener por lo menos un campo " +
                    "con la anotación 'CampoTabla'");


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
