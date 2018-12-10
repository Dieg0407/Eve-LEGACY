package dao.condicionales;

public enum Operacion {
    IGUAL(" = "),DIFERENTE(" != "),IN(" IN "),NOT_IN(" NOT IN ");

    String simbolo;
    Operacion(String simbolo){this.simbolo = simbolo;}

    public String getSimbolo() {
        return simbolo;
    }
}
