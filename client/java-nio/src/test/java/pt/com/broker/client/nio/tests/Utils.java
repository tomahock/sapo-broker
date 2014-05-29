package pt.com.broker.client.nio.tests;

import java.lang.reflect.Field;

/**
 * Created by luissantos on 29-05-2014.
 */
public class Utils {

    private final static String vmname = System.getProperty("java.vm.name");
    private final static String osname = System.getProperty("os.name");

    public static boolean isAndroid(){
        return "Dalvik".equals(vmname);
    }

    public static boolean isUnix() {
        String OS = osname.toLowerCase();
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }

    public static boolean isLinux() {
        return osname.toLowerCase().equals("linux");
    }

    public static Integer getPlatformVersion() {

        if(!isAndroid()){
            return null;
        }


        try {

            Field verField = Class.forName("android.os.Build$VERSION").getField("SDK_INT");
            int ver = verField.getInt(verField);
            return ver;

        } catch (Exception e) {

            try {

                Field verField = Class.forName("android.os.Build$VERSION").getField("SDK");
                String verString = (String) verField.get(verField);
                return Integer.parseInt(verString);

            } catch(Exception e2) {
                return null;
            }

        }

    }
}
