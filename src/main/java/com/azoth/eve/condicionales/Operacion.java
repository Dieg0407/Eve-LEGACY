package com.azoth.eve.condicionales;

public enum Operacion {
    IGUAL(" = "),DIFERENTE(" != "),IN(" IN "),NOT_IN(" NOT IN "),
    MENOR (" < "), MAYOR(" > "),MENOR_IGUAL(" <= "), MAYOR_IGUAL(" >= ");

    String simbolo;
    Operacion(String simbolo){this.simbolo = simbolo;}

    public String getSimbolo() {
        return simbolo;
    }
}
