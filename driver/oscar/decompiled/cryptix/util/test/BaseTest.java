/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.test;

import cryptix.util.test.TestException;
import cryptix.util.test.TestGUI;
import java.applet.Applet;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class BaseTest
extends Applet {
    private static final String SEPARATOR = "\n===========================================================================";
    private static PrintWriter defaultOutput = new PrintWriter(System.out, true);
    protected PrintWriter status;
    protected PrintWriter out;
    private String name = this.getClass().getName();
    private StringWriter sw;
    private int failures;
    private int errors;
    private int passes;
    private int skipped;
    private int expectedPasses;
    private boolean overallPass;
    private boolean verbose;
    private boolean commandLine;
    private TestGUI gui;

    protected BaseTest() {
        this.status = defaultOutput;
    }

    public synchronized void init() {
        this.initGui();
        try {
            this.test();
        }
        catch (TestException e) {
            e.printStackTrace(this.status);
        }
    }

    private void initGui() {
        this.gui = new TestGUI(this);
        this.add(this.gui);
        this.setOutput(this.gui.getOutput());
    }

    public synchronized void setGuiEnabled(boolean flag) {
        if (flag && this.gui == null) {
            this.initGui();
            if (this.commandLine) {
                this.gui.useAppFrame(true);
            }
        } else if (!flag && this.gui != null) {
            if (this.commandLine) {
                this.gui.useAppFrame(false);
            }
            this.setOutput(defaultOutput);
            this.gui = null;
        }
    }

    public boolean isGuiEnabled() {
        return this.gui != null;
    }

    public synchronized void waitForExit() {
        if (this.gui != null) {
            this.gui.waitForExit();
        }
    }

    protected void commandline(String[] args) {
        this.commandline(args, 0);
    }

    protected void commandline(String[] args, int offset) {
        this.commandLine = true;
        try {
            this.parseOptions(args, offset);
            this.test();
            this.waitForExit();
            System.exit(10);
        }
        catch (TestException e) {
            this.status.println(e.getMessage());
            this.waitForExit();
            System.exit(e.getErrorCode());
        }
    }

    protected void parseOption(String option) throws TestException {
        if (option.equalsIgnoreCase("-verbose")) {
            this.setVerbose(true);
        } else if (option.equalsIgnoreCase("-gui")) {
            this.setGuiEnabled(true);
        } else {
            System.err.println(this.describeUsage());
            throw new TestException("Unrecognised option: '" + option + "'", 2);
        }
    }

    public void parseOptions(String[] args, int offset) throws TestException {
        int i = offset;
        while (i < args.length) {
            this.parseOption(args[i]);
            ++i;
        }
    }

    public String describeUsage() {
        return "Usage:\n    java " + this.getClass().getName() + " [options...]\n" + this.describeOptions();
    }

    public String describeOptions() {
        return "Options:\n    -verbose: print output even if all tests pass.\n";
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getName() {
        return this.name;
    }

    public void setOutput(PrintWriter pw) {
        this.status = pw;
    }

    public void setVerbose(boolean flag) {
        this.verbose = flag;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public void test() throws TestException {
        block30: {
            String andSkipped;
            if (this.verbose) {
                this.sw = null;
                this.out = this.status;
            } else {
                this.sw = new StringWriter();
                this.out = new PrintWriter(this.sw);
            }
            try {
                try {
                    this.expectedPasses = 0;
                    this.skipped = 0;
                    this.passes = 0;
                    this.errors = 0;
                    this.failures = 0;
                    this.overallPass = false;
                    this.status.print("Running tests for " + this.getName());
                    if (this.verbose) {
                        this.status.println();
                    } else {
                        this.status.flush();
                    }
                    this.engineTest();
                }
                catch (Throwable e) {
                    String andSkipped2;
                    this.error(e);
                    String string = andSkipped2 = this.skipped > 0 ? " and skipped tests" : "";
                    if (this.passes + this.skipped < this.expectedPasses) {
                        this.error("Number of passes" + andSkipped2 + " is less than expected");
                    } else if (this.passes < 1) {
                        this.error("At least one pass is required");
                    } else if (this.expectedPasses > 0 && this.expectedPasses < this.passes + this.skipped) {
                        this.error("Number of passes" + andSkipped2 + " is more than expected\n" + "(therefore the expected number is wrong)");
                    }
                    this.report();
                    if (this.failures > 0 || this.errors > 0) {
                        if (this.passes == 0) {
                            throw new TestException(String.valueOf(this.getName()) + " failed completely", 1);
                        }
                        throw new TestException(String.valueOf(this.getName()) + " failed partially", 4);
                    }
                    this.overallPass = true;
                    break block30;
                }
            }
            catch (Throwable throwable) {
                String andSkipped3;
                String string = andSkipped3 = this.skipped > 0 ? " and skipped tests" : "";
                if (this.passes + this.skipped < this.expectedPasses) {
                    this.error("Number of passes" + andSkipped3 + " is less than expected");
                } else if (this.passes < 1) {
                    this.error("At least one pass is required");
                } else if (this.expectedPasses > 0 && this.expectedPasses < this.passes + this.skipped) {
                    this.error("Number of passes" + andSkipped3 + " is more than expected\n" + "(therefore the expected number is wrong)");
                }
                this.report();
                if (this.failures > 0 || this.errors > 0) {
                    if (this.passes == 0) {
                        throw new TestException(String.valueOf(this.getName()) + " failed completely", 1);
                    }
                    throw new TestException(String.valueOf(this.getName()) + " failed partially", 4);
                }
                this.overallPass = true;
                throw throwable;
            }
            String string = andSkipped = this.skipped > 0 ? " and skipped tests" : "";
            if (this.passes + this.skipped < this.expectedPasses) {
                this.error("Number of passes" + andSkipped + " is less than expected");
            } else if (this.passes < 1) {
                this.error("At least one pass is required");
            } else if (this.expectedPasses > 0 && this.expectedPasses < this.passes + this.skipped) {
                this.error("Number of passes" + andSkipped + " is more than expected\n" + "(therefore the expected number is wrong)");
            }
            this.report();
            if (this.failures > 0 || this.errors > 0) {
                if (this.passes == 0) {
                    throw new TestException(String.valueOf(this.getName()) + " failed completely", 1);
                }
                throw new TestException(String.valueOf(this.getName()) + " failed partially", 4);
            }
            this.overallPass = true;
        }
    }

    protected void fail(String msg) {
        ++this.failures;
        this.out.println("\nFailed: " + msg);
        if (this.sw != null) {
            this.switchStream();
        }
    }

    protected void error(String msg) {
        ++this.errors;
        this.out.println("\nError: " + msg);
        if (this.sw != null) {
            this.switchStream();
        }
    }

    protected void skip(String msg) {
        ++this.skipped;
        this.out.println("\nTest skipped: " + msg);
        if (this.sw != null) {
            this.switchStream();
        }
    }

    private void switchStream() {
        this.out.flush();
        this.out = this.status;
        this.out.println();
        this.out.print(this.sw.getBuffer());
        this.out.flush();
        this.sw = null;
    }

    protected void error(Exception e) {
        this.error("Exception Unexpected " + e.getClass().getName());
        e.printStackTrace(this.out);
    }

    protected void error(Throwable e) {
        this.error("Throwable Unexpected " + e.getClass().getName());
        e.printStackTrace(this.out);
    }

    protected void pass(String msg) {
        ++this.passes;
        this.out.println("\nPassed: " + msg);
        if (this.sw != null) {
            this.status.print(".");
            this.status.flush();
        }
    }

    protected void passIf(boolean pass, String msg) {
        if (pass) {
            this.pass(msg);
        } else {
            this.fail(msg);
        }
    }

    protected void setExpectedPasses(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n < 0");
        }
        this.expectedPasses = n;
    }

    protected void report() {
        this.status.println(SEPARATOR);
        this.status.println("Number of passes:        " + this.passes);
        this.status.println("Number of failures:      " + this.failures);
        if (this.errors > 0) {
            this.status.println("Number of errors:        " + this.errors);
        }
        if (this.skipped > 0) {
            this.status.println("Number of skipped tests: " + this.skipped);
        }
        this.status.println("Expected passes:         " + (this.expectedPasses > 0 ? Integer.toString(this.expectedPasses) : "unknown"));
    }

    public int getFailures() {
        return this.failures;
    }

    public int getErrors() {
        return this.errors;
    }

    public int getPasses() {
        return this.passes;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public int getExpectedPasses() {
        return this.expectedPasses;
    }

    public boolean isOverallPass() {
        return this.overallPass;
    }

    protected abstract void engineTest() throws Exception;
}

