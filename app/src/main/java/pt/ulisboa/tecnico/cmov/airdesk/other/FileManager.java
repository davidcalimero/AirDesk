package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileManager {

    public static Object fileToObject(String fileName, Context context) throws FileNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream is = null;
        Object object = null;
        try {
            is = new ObjectInputStream(fis);
            object = is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("FileManager", "Error loading object from file");
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ex) {
                Log.e("FileManager", "Error cloasing IS");
            }
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                Log.e("FileManager", "Error cloasing FIS");
            }
        }
        return object;
    }

    public static boolean objectToFile(String fileName, Object object, Context context) {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        boolean ret = true;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FileManager", "Error saving object into file");
            ret = false;
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (IOException ex) {
                Log.e("FileManager", "Error cloasing OS");
                ret = false;
            }
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException ex) {
                Log.e("FileManager", "Error cloasing FOS");
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Get Free Space Function.
     * Returns the free space of the internal storage, in megabytes.
     */
    public static long getInternalFreeSpace() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long free;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            free = statFs.getAvailableBytes() /*/ 1048576*/;
        else
            free = ((long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize()) /*/ 1048576*/;
        return free;
    }
}
