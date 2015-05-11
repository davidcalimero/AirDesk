package pt.ulisboa.tecnico.cmov.airdesk.utility;

import java.io.Serializable;

import pt.ulisboa.tecnico.cmov.airdesk.dto.Dto;

public class MessagePack implements Serializable {
    // MACROS
    public static final String HELLO_WORLD = "Hello World";

    public static final String USER_REQUEST = "User Request";

    public static final String UNINVITE_FROM_WORKSPACE = "Uninvite From Workspace";
    public static final String MOUNT_WORKSPACE = "Mount Workspace";
    public static final String UNMOUNT_WORKSPACE = "Unmount Workspace";
    public static final String ADD_FILE = "Add File";
    public static final String REMOVE_FILE = "Remove File";
    public static final String EDIT_FILE = "Edit File";

    public String receiver;

    public String request;
    public Dto dto;


    @Override
    public String toString() {
        return request + " to " + receiver + ": " + dto.toString();
    }
}
