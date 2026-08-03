/*
 * Decompiled with CFR 0.152.
 */
package gnu.getopt;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class LongOpt {
    public static final int NO_ARGUMENT = 0;
    public static final int REQUIRED_ARGUMENT = 1;
    public static final int OPTIONAL_ARGUMENT = 2;
    protected String name;
    protected int has_arg;
    protected StringBuffer flag;
    protected int val;
    private ResourceBundle _messages = ResourceBundle.getBundle("gnu/getopt/MessagesBundle", Locale.getDefault());

    public LongOpt(String string, int n, StringBuffer stringBuffer, int n2) throws IllegalArgumentException {
        if (n != 0 && n != 1 && n != 2) {
            Object[] objectArray = new Object[]{new Integer(n).toString()};
            throw new IllegalArgumentException(MessageFormat.format(this._messages.getString("getopt.invalidValue"), objectArray));
        }
        this.name = string;
        this.has_arg = n;
        this.flag = stringBuffer;
        this.val = n2;
    }

    public String getName() {
        return this.name;
    }

    public int getHasArg() {
        return this.has_arg;
    }

    public StringBuffer getFlag() {
        return this.flag;
    }

    public int getVal() {
        return this.val;
    }
}

