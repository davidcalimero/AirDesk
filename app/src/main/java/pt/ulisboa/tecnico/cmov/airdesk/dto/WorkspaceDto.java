package pt.ulisboa.tecnico.cmov.airdesk.dto;

import java.util.ArrayList;

public class WorkspaceDto implements Dto {
    public String owner;
    public String name;
    public ArrayList<TextFileDto> files;

    @Override
    public String toString() {
        return owner + " " + name + " " + (files != null ? files.size() : 0);
    }
}
