package com.azoth.eve.condicionales;

public enum  Condicional {

    AND(" AND "),OR(" OR ");

    String id;
    Condicional(String id){this.id = id;}

    public String getId() {
        return id;
    }
}
