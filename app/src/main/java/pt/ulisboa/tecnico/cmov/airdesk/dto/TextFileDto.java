package pt.ulisboa.tecnico.cmov.airdesk.dto;

public class TextFileDto implements Dto {
    public String owner;
    public String workspace;
    public String title;
    public String content;

    @Override
    public String toString() {
        return owner + " " + workspace + " " + title;
    }

    public String messageType = "TEXTFILE";
}
