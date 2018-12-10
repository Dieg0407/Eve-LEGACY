package dao.condicionales;

public enum  Condicional {

    AND(1),OR(2);

    int id;
    Condicional(int id){this.id = id;}

    public int getId() {
        return id;
    }
}
