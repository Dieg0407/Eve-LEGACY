package com.azoth.eve;

import com.azoth.eve.anotaciones.CampoTabla;
import com.azoth.eve.condicionales.Condicional;
import com.azoth.eve.condicionales.Operacion;

import java.lang.reflect.Field;
import java.util.List;

public class Parametro {

    private CampoTabla campoTabla;
    private Condicional condicional;
    private Operacion operacion;

    private Object valorUnico;
    private List<Object> valorCompuesto;

    private boolean trim = false;

    public Parametro(CampoTabla campo, Condicional condicional, Operacion operacion, Object valorUnico){
        this.campoTabla = campo;
        this.condicional = condicional;
        this.operacion = operacion;
        this.valorUnico = valorUnico;
    }
    public Parametro(CampoTabla campo, Condicional condicional, Operacion operacion, List<Object> valorCompuesto){
        this.campoTabla = campo;
        this.condicional = condicional;
        this.operacion = operacion;
        this.valorCompuesto = valorCompuesto;
    }

    public Parametro(Field campo, Condicional condicional, Operacion operacion, Object valorUnico){
        this(campo.getAnnotation(CampoTabla.class),condicional,operacion,valorUnico);
    }

    public Parametro(Field campo, Condicional condicional, Operacion operacion, List<Object> valorCompuesto){
        this(campo.getAnnotation(CampoTabla.class),condicional,operacion,valorCompuesto);
    }

    public Parametro(CampoTabla campo, Condicional condicional, Operacion operacion, Object valorUnico, boolean trim){
        this(campo,condicional,operacion,valorUnico);
        this.trim = trim;
    }
    public Parametro(CampoTabla campo, Condicional condicional, Operacion operacion, List<Object> valorCompuesto, boolean trim){
        this(campo,condicional,operacion,valorCompuesto);
        this.trim = trim;
    }
    public Parametro(Field campo, Condicional condicional, Operacion operacion, Object valorUnico, boolean trim){
        this(campo.getAnnotation(CampoTabla.class),condicional,operacion,valorUnico,trim);
    }
    public Parametro(Field campo, Condicional condicional, Operacion operacion, List<Object> valorCompuesto, boolean trim){
        this(campo.getAnnotation(CampoTabla.class),condicional,operacion,valorCompuesto,trim);
    }

    public CampoTabla getCampoTabla() {
        return campoTabla;
    }

    public Condicional getCondicional() {
        return condicional;
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public Object getValorUnico() {
        return valorUnico;
    }

    public List<Object> getValorCompuesto() {
        return valorCompuesto;
    }

    public boolean isTrim() {
        return trim;
    }
}
