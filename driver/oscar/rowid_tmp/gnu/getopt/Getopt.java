/*
 * Decompiled with CFR 0.152.
 */
package gnu.getopt;

import gnu.getopt.LongOpt;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Getopt {
    protected static final int REQUIRE_ORDER = 1;
    protected static final int PERMUTE = 2;
    protected static final int RETURN_IN_ORDER = 3;
    protected String optarg;
    protected int optind;
    protected boolean opterr = true;
    protected int optopt = 63;
    protected String nextchar;
    protected String optstring;
    protected LongOpt[] long_options;
    protected boolean long_only;
    protected int longind;
    protected boolean posixly_correct;
    protected boolean longopt_handled;
    protected int first_nonopt = 1;
    protected int last_nonopt = 1;
    private boolean endparse = false;
    protected String[] argv;
    protected int ordering;
    protected String progname;
    private ResourceBundle _messages = ResourceBundle.getBundle("gnu/getopt/MessagesBundle", Locale.getDefault());

    public Getopt(String string, String[] stringArray, String string2) {
        this(string, stringArray, string2, null, false);
    }

    public Getopt(String string, String[] stringArray, String string2, LongOpt[] longOptArray) {
        this(string, stringArray, string2, longOptArray, false);
    }

    public Getopt(String string, String[] stringArray, String string2, LongOpt[] longOptArray, boolean bl) {
        if (string2.length() == 0) {
            string2 = " ";
        }
        this.progname = string;
        this.argv = stringArray;
        this.optstring = string2;
        this.long_options = longOptArray;
        this.long_only = bl;
        if (System.getProperty("gnu.posixly_correct", null) == null) {
            this.posixly_correct = false;
        } else {
            this.posixly_correct = true;
            this._messages = ResourceBundle.getBundle("gnu/getopt/MessagesBundle", Locale.US);
        }
        if (string2.charAt(0) == '-') {
            this.ordering = 3;
            if (string2.length() > 1) {
                this.optstring = string2.substring(1);
                return;
            }
        } else if (string2.charAt(0) == '+') {
            this.ordering = 1;
            if (string2.length() > 1) {
                this.optstring = string2.substring(1);
                return;
            }
        } else {
            if (this.posixly_correct) {
                this.ordering = 1;
                return;
            }
            this.ordering = 2;
        }
    }

    public void setOptstring(String string) {
        if (string.length() == 0) {
            string = " ";
        }
        this.optstring = string;
    }

    public int getOptind() {
        return this.optind;
    }

    public void setOptind(int n) {
        this.optind = n;
    }

    public void setArgv(String[] stringArray) {
        this.argv = stringArray;
    }

    public String getOptarg() {
        return this.optarg;
    }

    public void setOpterr(boolean bl) {
        this.opterr = bl;
    }

    public int getOptopt() {
        return this.optopt;
    }

    public int getLongind() {
        return this.longind;
    }

    protected void exchange(String[] stringArray) {
        int n = this.first_nonopt;
        int n2 = this.last_nonopt;
        int n3 = this.optind;
        while (n3 > n2 && n2 > n) {
            String string;
            int n4;
            int n5;
            if (n3 - n2 > n2 - n) {
                n5 = n2 - n;
                n4 = 0;
                while (n4 < n5) {
                    string = stringArray[n + n4];
                    stringArray[n + n4] = stringArray[n3 - (n2 - n) + n4];
                    stringArray[n3 - (n2 - n) + n4] = string;
                    ++n4;
                }
                n3 -= n5;
                continue;
            }
            n5 = n3 - n2;
            n4 = 0;
            while (n4 < n5) {
                string = stringArray[n + n4];
                stringArray[n + n4] = stringArray[n2 + n4];
                stringArray[n2 + n4] = string;
                ++n4;
            }
            n += n5;
        }
        this.first_nonopt += this.optind - this.last_nonopt;
        this.last_nonopt = this.optind;
    }

    /*
     * Enabled aggressive block sorting
     */
    protected int checkLongOption() {
        LongOpt longOpt;
        block21: {
            longOpt = null;
            this.longopt_handled = true;
            boolean bl = false;
            boolean bl2 = false;
            this.longind = -1;
            int n = this.nextchar.indexOf("=");
            if (n == -1) {
                n = this.nextchar.length();
            }
            int n2 = 0;
            while (n2 < this.long_options.length) {
                if (this.long_options[n2].getName().startsWith(this.nextchar.substring(0, n))) {
                    if (this.long_options[n2].getName().equals(this.nextchar.substring(0, n))) {
                        longOpt = this.long_options[n2];
                        this.longind = n2;
                        bl2 = true;
                        break;
                    }
                    if (longOpt == null) {
                        longOpt = this.long_options[n2];
                        this.longind = n2;
                    } else {
                        bl = true;
                    }
                }
                ++n2;
            }
            if (bl && !bl2) {
                if (this.opterr) {
                    Object[] objectArray = new Object[]{this.progname, this.argv[this.optind]};
                    System.err.println(MessageFormat.format(this._messages.getString("getopt.ambigious"), objectArray));
                }
                this.nextchar = "";
                this.optopt = 0;
                ++this.optind;
                return 63;
            }
            if (longOpt == null) {
                this.longopt_handled = false;
                return 0;
            }
            ++this.optind;
            if (n != this.nextchar.length()) {
                if (longOpt.has_arg != 0) {
                    this.optarg = this.nextchar.substring(n).length() > 1 ? this.nextchar.substring(n + 1) : "";
                    break block21;
                } else {
                    if (this.opterr) {
                        if (this.argv[this.optind - 1].startsWith("--")) {
                            Object[] objectArray = new Object[]{this.progname, longOpt.name};
                            System.err.println(MessageFormat.format(this._messages.getString("getopt.arguments1"), objectArray));
                        } else {
                            Object[] objectArray = new Object[]{this.progname, new Character(this.argv[this.optind - 1].charAt(0)).toString(), longOpt.name};
                            System.err.println(MessageFormat.format(this._messages.getString("getopt.arguments2"), objectArray));
                        }
                    }
                    this.nextchar = "";
                    this.optopt = longOpt.val;
                    return 63;
                }
            }
            if (longOpt.has_arg == 1) {
                if (this.optind < this.argv.length) {
                    this.optarg = this.argv[this.optind];
                    ++this.optind;
                } else {
                    if (this.opterr) {
                        Object[] objectArray = new Object[]{this.progname, this.argv[this.optind - 1]};
                        System.err.println(MessageFormat.format(this._messages.getString("getopt.requires"), objectArray));
                    }
                    this.nextchar = "";
                    this.optopt = longOpt.val;
                    if (this.optstring.charAt(0) == ':') {
                        return 58;
                    }
                    return 63;
                }
            }
        }
        this.nextchar = "";
        if (longOpt.flag != null) {
            longOpt.flag.setLength(0);
            longOpt.flag.append(longOpt.val);
            return 0;
        }
        return longOpt.val;
    }

    public int getopt() {
        int n;
        this.optarg = null;
        if (this.endparse) {
            return -1;
        }
        if (this.nextchar == null || this.nextchar.equals("")) {
            if (this.last_nonopt > this.optind) {
                this.last_nonopt = this.optind;
            }
            if (this.first_nonopt > this.optind) {
                this.first_nonopt = this.optind;
            }
            if (this.ordering == 2) {
                if (this.first_nonopt != this.last_nonopt && this.last_nonopt != this.optind) {
                    this.exchange(this.argv);
                } else if (this.last_nonopt != this.optind) {
                    this.first_nonopt = this.optind;
                }
                while (this.optind < this.argv.length && (this.argv[this.optind].equals("") || this.argv[this.optind].charAt(0) != '-' || this.argv[this.optind].equals("-"))) {
                    ++this.optind;
                }
                this.last_nonopt = this.optind;
            }
            if (this.optind != this.argv.length && this.argv[this.optind].equals("--")) {
                ++this.optind;
                if (this.first_nonopt != this.last_nonopt && this.last_nonopt != this.optind) {
                    this.exchange(this.argv);
                } else if (this.first_nonopt == this.last_nonopt) {
                    this.first_nonopt = this.optind;
                }
                this.last_nonopt = this.argv.length;
                this.optind = this.argv.length;
            }
            if (this.optind == this.argv.length) {
                if (this.first_nonopt != this.last_nonopt) {
                    this.optind = this.first_nonopt;
                }
                return -1;
            }
            if (this.argv[this.optind].equals("") || this.argv[this.optind].charAt(0) != '-' || this.argv[this.optind].equals("-")) {
                if (this.ordering == 1) {
                    return -1;
                }
                this.optarg = this.argv[this.optind++];
                return 1;
            }
            this.nextchar = this.argv[this.optind].startsWith("--") ? this.argv[this.optind].substring(2) : this.argv[this.optind].substring(1);
        }
        if (this.long_options != null && (this.argv[this.optind].startsWith("--") || this.long_only && (this.argv[this.optind].length() > 2 || this.optstring.indexOf(this.argv[this.optind].charAt(1)) == -1))) {
            n = this.checkLongOption();
            if (this.longopt_handled) {
                return n;
            }
            if (!this.long_only || this.argv[this.optind].startsWith("--") || this.optstring.indexOf(this.nextchar.charAt(0)) == -1) {
                if (this.opterr) {
                    if (this.argv[this.optind].startsWith("--")) {
                        Object[] objectArray = new Object[]{this.progname, this.nextchar};
                        System.err.println(MessageFormat.format(this._messages.getString("getopt.unrecognized"), objectArray));
                    } else {
                        Object[] objectArray = new Object[]{this.progname, new Character(this.argv[this.optind].charAt(0)).toString(), this.nextchar};
                        System.err.println(MessageFormat.format(this._messages.getString("getopt.unrecognized2"), objectArray));
                    }
                }
                this.nextchar = "";
                ++this.optind;
                this.optopt = 0;
                return 63;
            }
        }
        n = this.nextchar.charAt(0);
        this.nextchar = this.nextchar.length() > 1 ? this.nextchar.substring(1) : "";
        String string = null;
        if (this.optstring.indexOf(n) != -1) {
            string = this.optstring.substring(this.optstring.indexOf(n));
        }
        if (this.nextchar.equals("")) {
            ++this.optind;
        }
        if (string == null || n == 58) {
            if (this.opterr) {
                if (this.posixly_correct) {
                    Object[] objectArray = new Object[]{this.progname, new Character((char)n).toString()};
                    System.err.println(MessageFormat.format(this._messages.getString("getopt.illegal"), objectArray));
                } else {
                    Object[] objectArray = new Object[]{this.progname, new Character((char)n).toString()};
                    System.err.println(MessageFormat.format(this._messages.getString("getopt.invalid"), objectArray));
                }
            }
            this.optopt = n;
            return 63;
        }
        if (string.charAt(0) == 'W' && string.length() > 1 && string.charAt(1) == ';') {
            if (!this.nextchar.equals("")) {
                this.optarg = this.nextchar;
            } else {
                if (this.optind == this.argv.length) {
                    if (this.opterr) {
                        Object[] objectArray = new Object[]{this.progname, new Character((char)n).toString()};
                        System.err.println(MessageFormat.format(this._messages.getString("getopt.requires2"), objectArray));
                    }
                    this.optopt = n;
                    if (this.optstring.charAt(0) == ':') {
                        return 58;
                    }
                    return 63;
                }
                this.nextchar = this.argv[this.optind];
                this.optarg = this.argv[this.optind];
            }
            n = this.checkLongOption();
            if (this.longopt_handled) {
                return n;
            }
            this.nextchar = null;
            ++this.optind;
            return 87;
        }
        if (string.length() > 1 && string.charAt(1) == ':') {
            if (string.length() > 2 && string.charAt(2) == ':') {
                if (!this.nextchar.equals("")) {
                    this.optarg = this.nextchar;
                    ++this.optind;
                } else {
                    this.optarg = null;
                }
                this.nextchar = null;
            } else {
                if (!this.nextchar.equals("")) {
                    this.optarg = this.nextchar;
                    ++this.optind;
                } else {
                    if (this.optind == this.argv.length) {
                        if (this.opterr) {
                            Object[] objectArray = new Object[]{this.progname, new Character((char)n).toString()};
                            System.err.println(MessageFormat.format(this._messages.getString("getopt.requires2"), objectArray));
                        }
                        this.optopt = n;
                        if (this.optstring.charAt(0) == ':') {
                            return 58;
                        }
                        return 63;
                    }
                    this.optarg = this.argv[this.optind];
                    ++this.optind;
                    if (this.posixly_correct && this.optarg.equals("--")) {
                        if (this.optind == this.argv.length) {
                            if (this.opterr) {
                                Object[] objectArray = new Object[]{this.progname, new Character((char)n).toString()};
                                System.err.println(MessageFormat.format(this._messages.getString("getopt.requires2"), objectArray));
                            }
                            this.optopt = n;
                            if (this.optstring.charAt(0) == ':') {
                                return 58;
                            }
                            return 63;
                        }
                        this.optarg = this.argv[this.optind];
                        ++this.optind;
                        this.first_nonopt = this.optind;
                        this.last_nonopt = this.argv.length;
                        this.endparse = true;
                    }
                }
                this.nextchar = null;
            }
        }
        return n;
    }
}

