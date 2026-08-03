/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

import java.security.InvalidParameterException;
import xjava.security.InvalidParameterTypeException;
import xjava.security.NoSuchParameterException;

public interface Parameterized {
    public void setParameter(String var1, Object var2) throws NoSuchParameterException, InvalidParameterException, InvalidParameterTypeException;

    public Object getParameter(String var1) throws NoSuchParameterException, InvalidParameterException;
}

