package pt.ulisboa.tecnico.cmov.airdesk.dto;

public class MessageDto implements Dto {

    // MACROS
    public static final String HELLO_WORLD = "Hello World";
    public static final String USER_REQUEST = "User Request";

    public String message = null;

    public MessageDto(String message){
        this.message = message;
    }
}
