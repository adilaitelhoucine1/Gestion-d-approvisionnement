package com.tricol.gestionstock.exception;


public class CommandeNotReceptionnableException  extends RuntimeException{

    public  CommandeNotReceptionnableException(String mess){
        super("La commande ne peut pas etre receptionnee ");
    }
}
