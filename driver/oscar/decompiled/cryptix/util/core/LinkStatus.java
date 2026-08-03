/*
 * Decompiled with CFR 0.152.
 */
package cryptix.util.core;

public interface LinkStatus {
    public int getRequiredMajorVersion();

    public int getRequiredMinorVersion();

    public String getLibraryName();

    public int getMajorVersion();

    public int getMinorVersion();

    public boolean isLibraryLoaded();

    public boolean isLibraryCorrect();

    public boolean useNative();

    public String getLinkErrorString();

    public void checkNative() throws UnsatisfiedLinkError;

    public void setNative(boolean var1);
}

