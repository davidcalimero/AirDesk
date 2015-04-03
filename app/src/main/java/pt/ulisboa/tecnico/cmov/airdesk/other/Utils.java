package pt.ulisboa.tecnico.cmov.airdesk.other;

import java.util.List;

public class Utils {

    // MACRO
    public static final String ADD_WORKSPACE = "pt.ulisboa.tecnico.cmov.airdesk.ADD_WORKSPACE";
    public static final String REMOVE_WORKSPACE = "pt.ulisboa.tecnico.cmov.airdesk.REMOVE_WORKSPACE";
    public static final String ADD_FILE = "pt.ulisboa.tecnico.cmov.airdesk.ADD_FILE";
    public static final String REMOVE_FILE = "pt.ulisboa.tecnico.cmov.airdesk.REMOVE_FILE";

    public static final String WORKSPACE_NAME = "workspace";
    public static final String FILE_NAME = "file";
    public static final String OWNER = "owner";

    //Returns true if list1 has at least one element contained in list2
    public static boolean hasSameElement(List list1, List list2){
        for(Object object1 : list1){
            for(Object object2: list2){
                if(object1.equals(object2)){
                    return true;
                }
            }
        }
        return false;
    }
}
