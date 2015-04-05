package pt.ulisboa.tecnico.cmov.airdesk.other;

import java.util.List;

public class Utils {

    //Returns true if list1 has at least one element contained in list2
    public static boolean hasSameElement(List list1, List list2) {
        for (Object object1 : list1) {
            for (Object object2 : list2) {
                if (object1.equals(object2)) {
                    return true;
                }
            }
        }
        return false;
    }

    //Trim that also replace inner string multiple spaces by a single one
    public static String trim(String text) {
        return text.trim().replaceAll("( )+", " ");
    }

    //Returns true if the string has only one word
    public static boolean isSingleWord(String text) {
        String newText = text.trim();
        return newText.split(" ").length == 1 && newText.length() != 0;
    }
}
