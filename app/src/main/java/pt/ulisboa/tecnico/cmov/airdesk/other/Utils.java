package pt.ulisboa.tecnico.cmov.airdesk.other;

import java.util.Collection;

public class Utils {

    //Returns true if list1 has at least one element contained in list2
    public static boolean haveElementsInCommon(Collection set1, Collection set2) {
        for (Object object1 : set1)
            if(set2.contains(object1))
                return true;
        return false;
    }

    //Trim that also replace inner string multiple spaces by a single one
    public static String trim(String text) {
        return text == null ? "" : text.replaceAll("( )+", " ").trim();
    }

    //Returns true if the string has only one word
    public static boolean isSingleWord(String text) {
        String newText = text.trim();
        return newText.split(" ").length == 1 && newText.length() != 0;
    }
}
