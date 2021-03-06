package pt.ulisboa.tecnico.cmov.airdesk.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.util.Collection;

public class Utils {

    private static final String DRIVER_DB = "org.postgresql.Driver";
    private static final String URL_DB = "db.ist.utl.pt:5432";
    private static final String USERNAME_DB = "ist166392";
    private static final String PASSWORD_DB = "inrq1320";

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

    public static long minMaxNormalization(long value, long min_old, long max_old, long min_new, long max_new){
        return (value - min_old) * (max_new - min_new) / (max_old - min_old) + min_new;
    }

    public static String formatNumber(String format, float number){
        return new DecimalFormat(format).format(number);
    }

    public static byte[] objectToBytes(Object object){
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(buffer);
            oos.writeObject(object);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }

    public static Object bytesToObject(byte[] data){
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(in);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection generateConnection() {
        //Connects to database
        Connection connection;
        try {
            Class.forName(DRIVER_DB);
            connection = DriverManager.getConnection("jdbc:postgresql://" + URL_DB + "/", USERNAME_DB, PASSWORD_DB);
        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
            return null;
        }

        //Check connection success
        if (connection == null) {
            System.out.println("Failed to make database connection!");
            return null;
        }

        return connection;
    }
}
