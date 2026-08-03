/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  GetoptDemo
 */
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class GetoptDemo {
    public static void main(String[] stringArray) {
        int n;
        LongOpt[] longOptArray = new LongOpt[3];
        StringBuffer stringBuffer = new StringBuffer();
        longOptArray[0] = new LongOpt("help", 0, null, 104);
        longOptArray[1] = new LongOpt("outputdir", 1, stringBuffer, 111);
        longOptArray[2] = new LongOpt("maximum", 2, null, 2);
        Getopt getopt = new Getopt("testprog", stringArray, "-:bc::d:hW;", longOptArray);
        getopt.setOpterr(false);
        while ((n = getopt.getopt()) != -1) {
            switch (n) {
                case 0: {
                    String string = getopt.getOptarg();
                    System.out.println("Got long option with value '" + (char)new Integer(stringBuffer.toString()).intValue() + "' with argument " + (string != null ? string : "null"));
                    break;
                }
                case 1: {
                    System.out.println("I see you have return in order set and that a non-option argv element was just found with the value '" + getopt.getOptarg() + "'");
                    break;
                }
                case 2: {
                    String string = getopt.getOptarg();
                    System.out.println("I know this, but pretend I didn't");
                    System.out.println("We picked option " + longOptArray[getopt.getLongind()].getName() + " with value " + (string != null ? string : "null"));
                    break;
                }
                case 98: {
                    System.out.println("You picked plain old option " + (char)n);
                    break;
                }
                case 99: 
                case 100: {
                    String string = getopt.getOptarg();
                    System.out.println("You picked option '" + (char)n + "' with argument " + (string != null ? string : "null"));
                    break;
                }
                case 104: {
                    System.out.println("I see you asked for help");
                    break;
                }
                case 87: {
                    System.out.println("Hmmm. You tried a -W with an incorrect long option name");
                    break;
                }
                case 58: {
                    System.out.println("Doh! You need an argument for option " + (char)getopt.getOptopt());
                    break;
                }
                case 63: {
                    System.out.println("The option '" + (char)getopt.getOptopt() + "' is not valid");
                    break;
                }
                default: {
                    System.out.println("getopt() returned " + n);
                    break;
                }
            }
        }
        int n2 = getopt.getOptind();
        while (n2 < stringArray.length) {
            System.out.println("Non option argv element: " + stringArray[n2] + "\n");
            ++n2;
        }
    }
}

