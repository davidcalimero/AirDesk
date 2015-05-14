package pt.ulisboa.tecnico.cmov.airdesk.utility;

import java.io.Serializable;

public class MessagePack implements Serializable {
    // MACROS
    public static final String USER_REQUEST = "User Request";

    public static final String SUBSCRIBE = "Subscribe";
    public static final String UNINVITE_FROM_WORKSPACE = "Uninvite From Workspace";

    public static final String MOUNT_WORKSPACE = "Mount Workspace";
    public static final String UNMOUNT_WORKSPACE = "Unmount Workspace";

    public static final String ADD_FILE = "Add File";
    public static final String REMOVE_FILE = "Remove File";
    public static final String EDIT_FILE = "Edit File";

    public static final String FILE_CONTENT = "File Content";
    public static final String ASK_TO_EDIT = "Ask to edit";
    public static final String STOP_EDITING = "Stop editing";

    public enum Type {REQUEST, REPLY}

    public String sender;
    public String receiver;
    public String request;
    public Object data;
    public Type type;

    @Override
    public String toString() {
        return request + " to " + receiver + ": " + (data == null ? null : data.toString());
    }
}
