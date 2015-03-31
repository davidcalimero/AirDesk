package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class FileManager {

    /* MACROS */
    // Line Separator
    public static final String LINE_SEP = System.getProperty("line.separator");

    /**
     * Create File Function.
     * Receives the filename and the path to which the file will be created on.
     */
    public static boolean createFile(String path, String filename){
        try {
            File file = new File(path, filename);

            if(!file.exists()){
                file.mkdirs();
            }
            Log.e("FileSystem", "File Location: " + file.getAbsolutePath());

            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * List Directory Function.
     * Receives the path of the directory and returns the files in it.
     */
    public static ArrayList<String> listDir(String path){
        ArrayList<String> files = new ArrayList<>();
        File file = new File(path);
        if(file.exists()) {
            File[] children = file.listFiles();
            for (File f : children) {
                files.add(f.getName());
            }
        }
        else
            Log.e("FileSystem", "File does not exist");
        return files;
    }

    /**
     * Read File Function.
     * Receives the file's path, and returns the content of it.
     */
    public static String readFile(String path){
        FileInputStream fis = null;
        String content = null;
        Scanner scanner = null;
        File file = new File(path);
        try {
            fis = new FileInputStream(file);
            scanner = new Scanner(fis);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine() + LINE_SEP);
            }
            content = sb.toString();
        } catch(Exception e){
            Log.e("FileSystem","Couldn't open file " + file.getName());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.d("FileExplorer", "Close error.");
                }
            }
            if (scanner != null) {
                scanner.close();
            }
        }
        return content;
    }

    /**
     * Write File Function.
     * Receives the file's path and the content to be put, and write it over the file, replacing other existing content.
     */
    public static boolean writeFile(String path, String content){
        FileOutputStream fos = null;
        boolean returnValue = false;
        try {
            fos = new FileOutputStream(path);
            fos.write(content.getBytes());
        } catch (FileNotFoundException e) {
            Log.e("FileSystem", "File not found", e);
        } catch (IOException e) {
            Log.e("FileSystem", "IO problem", e);
        } finally {
            try {
                fos.close();
                returnValue = true;
            } catch (Exception e) {
                Log.e("FileExplorer", "Close error.");
            }
        }
        return returnValue;
    }

    /**
     * Delete File Function.
     * Receives the file's path, and delete it. If file is a directory, the function deletes all children first.
     */
    public static void deleteFile(String path){
        File file = new File(path);
        if(file.exists()){
            if(file.isDirectory()){
                File[] children = file.listFiles();
                for(File f : children)
                    deleteFile(f.getAbsolutePath());
            }
            file.delete();
        }
    }

    public static Object fileToObject(String fileName, Context context) throws FileNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream is = null;
        Object object = null;
        try {
            is = new ObjectInputStream(fis);
            object = is.readObject();
        } catch (Exception e) {
            Log.e("FileManager", "Error loading object from file");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Log.e("FileManager", "Error cloasing IS");
            }
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Log.e("FileManager", "Error cloasing FIS");
            }
        }

        return object;
    }

    public static void objectToFile(String fileName, Object object, Context context){
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(object);
        } catch (IOException e) {
            Log.e("FileManager", "Error saving object into file");
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                Log.e("FileManager", "Error cloasing OS");
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                Log.e("FileManager", "Error cloasing FOS");
            }
        }
    }


    /**
     * Get Free Space Function.
     * Returns the free space of the internal storage, in megabytes.
     */
    public static long getInternalFreeSpace(){
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long free;
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            free = statFs.getAvailableBytes() / 1048576;
        else
            free = ((long)statFs.getAvailableBlocks() * (long)statFs.getBlockSize()) / 1048576;
        return free;
    }

    /**
     * Get Used Space Function.
     * Returns space used by directory or file, in bytes.
     * If it's a directory, it will sum the size of all children recursively.
     */
    public static long getUsedSpace(String path){
        File file = new File(path);
        if(file.isDirectory()){
            File[] children = file.listFiles();
            long totalSpace = 0;
            for(File f : children)
                totalSpace += getUsedSpace(f.getAbsolutePath());
            return totalSpace;
        }
        else
            return file.length();
    }
}
