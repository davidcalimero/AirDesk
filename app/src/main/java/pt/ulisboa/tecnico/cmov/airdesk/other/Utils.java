package pt.ulisboa.tecnico.cmov.airdesk.other;

public class Utils {

    /*public static byte[] objectoToByteArray(Object object){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            bytes = bos.toByteArray();

        } catch (Exception e) {
            Log.e("Utils", "Error transforming object into byte[]");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {}
            try {
                bos.close();
            } catch (IOException ex) {}
        }
        return bytes;
    }

    public static Object byteArrayToObject(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        Object object = null;
        try {
            in = new ObjectInputStream(bis);
            object = in.readObject();
        } catch (Exception e) {
            Log.e("Utils", "Error transforming byte[] into object");
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {}
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {}
        }
        return object;
    }*/
}
