/*
 * Decompiled with CFR 0.152.
 */
package xjava.security;

class IJCE_Java10Support {
    private static Class Object_class = new Object().getClass();

    private IJCE_Java10Support() {
    }

    public static boolean isAssignableFrom(Class target, Class cl) {
        if (target.isInterface()) {
            return IJCE_Java10Support.interfaceIsAssignableFrom(target, cl);
        }
        if (target.getName().charAt(0) == '[') {
            return IJCE_Java10Support.arrayIsAssignableFrom(target, cl);
        }
        if (cl.isInterface()) {
            return false;
        }
        return IJCE_Java10Support.classIsAssignableFrom(target, cl);
    }

    private static boolean interfaceIsAssignableFrom(Class target, Class cl) {
        if (target == cl) {
            return true;
        }
        if (cl == Object_class) {
            return false;
        }
        Class<?>[] interfaces = cl.getInterfaces();
        int i = 0;
        while (i < interfaces.length) {
            if (IJCE_Java10Support.interfaceIsAssignableFrom(target, interfaces[i])) {
                return true;
            }
            ++i;
        }
        Class superclass = cl.getSuperclass();
        if (superclass == null) {
            return false;
        }
        return IJCE_Java10Support.interfaceIsAssignableFrom(target, superclass);
    }

    private static boolean classIsAssignableFrom(Class target, Class cl) {
        if (target == cl) {
            return true;
        }
        if (cl == Object_class) {
            return false;
        }
        Class superclass = cl.getSuperclass();
        if (superclass == null) {
            return cl.isInterface() && target == Object_class;
        }
        return IJCE_Java10Support.classIsAssignableFrom(target, superclass);
    }

    private static boolean arrayIsAssignableFrom(Class target, Class cl) {
        return false;
    }
}

