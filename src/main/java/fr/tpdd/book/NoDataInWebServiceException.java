package fr.tpdd.book;

public class NoDataInWebServiceException extends Exception{
    private String message;
    public NoDataInWebServiceException(String message){
        this.message = message;
    }
}
