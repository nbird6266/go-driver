/*
 * Decompiled with CFR 0.152.
 */
package cryptix.asn1.encoding;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

public class PackageProperties {
    static final boolean GLOBAL_DEBUG = false;
    private static final String PACKAGE_NAME = "encoding";
    private static final Properties properties;
    private static final String[][] DEFAULT_PROPERTIES;
    static /* synthetic */ Class class$0;

    static {
        InputStream is;
        boolean ok;
        properties = new Properties();
        DEFAULT_PROPERTIES = new String[][]{{"Debug.Level.*", "0"}, {"asn.1.encoding.home", "cryptix.asn1.encoding"}};
        String it = "encoding.properties";
        Class<?> clazz = class$0;
        if (clazz == null) {
            try {
                clazz = class$0 = Class.forName("cryptix.asn1.encoding.PackageProperties");
            }
            catch (ClassNotFoundException classNotFoundException) {
                throw new NoClassDefFoundError(classNotFoundException.getMessage());
            }
        }
        boolean bl = ok = (is = clazz.getResourceAsStream(it)) != null;
        if (ok) {
            try {
                properties.load(is);
                is.close();
            }
            catch (Exception x) {
                ok = false;
            }
        }
        if (!ok) {
            int n = DEFAULT_PROPERTIES.length;
            int i = 0;
            while (i < n) {
                properties.put(DEFAULT_PROPERTIES[i][0], DEFAULT_PROPERTIES[i][1]);
                ++i;
            }
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String value) {
        return properties.getProperty(key, value);
    }

    public static void list(PrintStream out) {
        PackageProperties.list(new PrintWriter(out, true));
    }

    public static void list(PrintWriter out) {
        out.println("#");
        out.println("# ----- Begin encoding properties -----");
        out.println("#");
        Enumeration<?> propenum = properties.propertyNames();
        while (propenum.hasMoreElements()) {
            String key = (String)propenum.nextElement();
            String value = PackageProperties.getProperty(key);
            out.println(String.valueOf(key) + " = " + value);
        }
        out.println("#");
        out.println("# ----- End encoding properties -----");
    }

    public static Enumeration propertyNames() {
        return properties.propertyNames();
    }

    static boolean isTraceable(String label) {
        String s = PackageProperties.getProperty("Trace." + label);
        if (s == null) {
            return false;
        }
        return new Boolean(s);
    }

    static int getLevel(String label) {
        String s = PackageProperties.getProperty("Debug.Level." + label);
        if (s == null && (s = PackageProperties.getProperty("Debug.Level.*")) == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException x) {
            return 0;
        }
    }

    static PrintWriter getOutput() {
        String name = PackageProperties.getProperty("Output");
        return name != null && name.equals("out") ? new PrintWriter(System.out, true) : new PrintWriter(System.err, true);
    }
}

