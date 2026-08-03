/*
 * Decompiled with CFR 0.152.
 */
package cryptix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.Security;
import java.util.Enumeration;
import java.util.Properties;
import netscape.security.PrivilegeManager;

public class CryptixProperties {
    private static final int MAJOR_VERSION = 3;
    private static final int MINOR_VERSION = 1;
    private static final int INTER_VERSION = 2;
    private static final boolean IS_SNAPSHOT = false;
    private static final String CVS_DATE = "$Date: 2000/08/16 17:55:25 $";
    public static final boolean NATIVE_ALLOWED = true;
    static final String HTML_INFO = "<h1>" + CryptixProperties.getVersionString() + "</h1>\n" + "<p>\n" + "<b>Copyright</b> &copy; 1995-1999\n" + "<a href=\"http://www.systemics.com/\">Systemics Ltd</a> on behalf of the\n" + "<a href=\"http://www.cryptix.org/\">Cryptix Development Team</a>.\n" + "<br>All rights reserved.\n" + "<p>\n" + "This library includes, or is derived from software developed by\n" + "(and owned by):\n" + "<blockquote>\n" + "  Jill&nbsp;Baker, Paulo&nbsp;Barreto, George&nbsp;Barwood,\n" + "  Antoon&nbsp;Bosselaers, Ian&nbsp;Brown, Lawrence&nbsp;Brown,\n" + "  Joan&nbsp;Daemen, Richard&nbsp;De&nbsp;Moliner,\n" + "  John&nbsp;F.&nbspDumas, Jeroen&nbsp;Van&nbsp;Gelderen,\n" + "  Peter&nbsp;Gutmann, Ian&nbsp;Grigg,\n" + "  Mark&nbsp;A.&nbsp;Herschberg, Uwe&nbsp;Hollerbach,\n" + "  David&nbsp;Hopwood, Gary&nbsp;Howland, Geoffrey&nbsp;Keating,\n" + "  Sascha&nbsp;Kettler, Jonathon&nbsp;Knudsen, A.M.&nbsp;Kuchling,\n" + "  Matthew&nbsp;Kwan, Jerry&nbsp;McBride, Andrew&nsbp;E.&nsbp;Mileski,\n" + "  Raif&nbsp;Naffah, NIST, Bryan&nbsp;Olson, Zoran&nbsp;Rajic,\n" + "  Vincent&nbsp;Rijmen, RSA&nbsp;Data&nbsp;Security&nbsp;Inc.,\n" + "  Bruce&nbsp;Schneier, Systemics&nbsp;Ltd., Mike&nbsp;Wynn,\n" + "  Edwin&nbsp;Woudt, Thomas&nbsp;Wu, Eric&nbsp;Young, Yuliang&nbsp;Zheng.\n" + "</blockquote>\n" + "<p>\n" + "See the <a href=\"http://www.cryptix.org/\">site</a>\n" + "for further details.\n";
    static final String PRODUCT_NAME = "Cryptix";
    static final String LIB_DIRNAME = "cryptix-lib";
    static final String[] PROPERTIES_FILES = new String[]{"Cryptix.properties", "Local.properties"};
    private static final Properties properties = new Properties();
    private static String lib_path;
    static /* synthetic */ Class class$0;

    static {
        CryptixProperties.setProperties();
    }

    private CryptixProperties() {
    }

    public static int getMajorVersion() {
        return 3;
    }

    public static int getMinorVersion() {
        return 1;
    }

    public static int getIntermediateVersion() {
        return 2;
    }

    public static boolean isVersionAtLeast(int major, int minor, int intermediate) {
        if (3 > major) {
            return true;
        }
        if (3 < major) {
            return false;
        }
        if (1 > minor) {
            return true;
        }
        if (1 < minor) {
            return false;
        }
        return 2 >= intermediate;
    }

    public static String getReleaseDate() {
        try {
            return CVS_DATE.substring(7, 17);
        }
        catch (StringIndexOutOfBoundsException e) {
            return "unknown";
        }
    }

    public static String getVersionString() {
        StringBuffer version = new StringBuffer("Cryptix-Java ").append(3).append(".").append(1);
        version.append(".").append(2);
        return version.toString();
    }

    public static String getHtmlInfo() {
        return HTML_INFO;
    }

    public static void main(String[] args) {
        System.out.println(CryptixProperties.getVersionString());
        System.out.println();
        if (Security.getProvider(PRODUCT_NAME) == null) {
            System.out.println("Cryptix is not installed as a provider in the java.security file.");
            System.out.println("Enter \"java cryptix.provider.Install\" to correct this.");
        } else {
            System.out.println("Cryptix is correctly installed in the java.security file.");
        }
        try {
            String libPath = CryptixProperties.getLibraryPath();
            System.out.println("The library directory is");
            System.out.println("  " + libPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLibraryPath() throws IOException {
        if (lib_path == null) {
            throw new IOException("Cryptix library directory (cryptix-lib) could not be found");
        }
        return lib_path;
    }

    private static void setProperties() {
        try {
            PrivilegeManager.enablePrivilege("UniversalPropertyRead");
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        String fs = System.getProperty("file.separator");
        try {
            PrivilegeManager.revertPrivilege("UniversalPropertyRead");
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        try {
            PrivilegeManager.enablePrivilege("UniversalFileRead");
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        boolean loaded = false;
        int i = 0;
        while (i < PROPERTIES_FILES.length) {
            Class<?> clazz;
            Class<?> clazz2;
            Class<?> clazz3;
            InputStream props;
            Class<?> clazz4 = class$0;
            if (clazz4 == null) {
                try {
                    clazz4 = Class.forName("cryptix.CryptixProperties");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new NoClassDefFoundError(classNotFoundException.getMessage());
                }
            }
            if ((props = clazz4.getResourceAsStream(String.valueOf(fs) + LIB_DIRNAME + fs + PROPERTIES_FILES[i])) != null) {
                try {
                    properties.load(props);
                    loaded = true;
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if ((clazz3 = class$0) == null) {
                try {
                    clazz3 = Class.forName("cryptix.CryptixProperties");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new NoClassDefFoundError(classNotFoundException.getMessage());
                }
            }
            if ((props = clazz3.getResourceAsStream(String.valueOf(fs) + PROPERTIES_FILES[i])) != null) {
                try {
                    properties.load(props);
                    loaded = true;
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if ((clazz2 = class$0) == null) {
                try {
                    clazz2 = Class.forName("cryptix.CryptixProperties");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new NoClassDefFoundError(classNotFoundException.getMessage());
                }
            }
            if ((props = clazz2.getResourceAsStream(String.valueOf(fs) + "META-INF" + fs + PROPERTIES_FILES[i])) != null) {
                try {
                    properties.load(props);
                    loaded = true;
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if ((clazz = class$0) == null) {
                try {
                    clazz = Class.forName("cryptix.CryptixProperties");
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new NoClassDefFoundError(classNotFoundException.getMessage());
                }
            }
            if ((props = clazz.getResourceAsStream(PROPERTIES_FILES[i])) != null) {
                try {
                    properties.load(props);
                    loaded = true;
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            ++i;
        }
        try {
            PrivilegeManager.revertPrivilege("UniversalFileRead");
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            // empty catch block
        }
        if (!loaded) {
            System.err.println("Warning: failed to load the Cryptix properties file.\nMake sure that the CLASSPATH entry for Cryptix is an absolute path.");
        }
    }

    public static void save(OutputStream os, String comment) {
        properties.save(os, comment);
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static Enumeration propertyNames() {
        return properties.propertyNames();
    }

    public static void list(PrintStream out) {
        properties.list(out);
    }

    public static void list(PrintWriter out) {
        properties.list(out);
    }

    public static Properties getAllCryptix() {
        return properties;
    }
}

