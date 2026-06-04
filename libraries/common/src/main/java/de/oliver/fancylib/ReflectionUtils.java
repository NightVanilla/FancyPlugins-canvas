package de.oliver.fancylib;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

public class ReflectionUtils {

    public static Object getValue(Object instance, String name) {
        Object result = null;

        try {
            Field field = instance.getClass().getDeclaredField(name);

            field.setAccessible(true);
            result = field.get(instance);
            field.setAccessible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Object getStaticValue(Class clazz, String name) {
        Object result = null;

        try {
            Field field = clazz.getDeclaredField(name);

            field.setAccessible(true);
            result = field.get(clazz);
            field.setAccessible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void setValue(Object instance, String name, Object value) {
        try {
            Field field = instance.getClass().getDeclaredField(name);

            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets a field directly, resolving it in a version-tolerant way.
     * <p>
     * Minecraft field names change between versions (obfuscation / Mojang
     * remaps), so a hardcoded name can throw {@link NoSuchFieldException} on a
     * version it was not written for. This first looks the field up by
     * {@code name} across the class hierarchy and, if that fails, falls back to
     * the {@code typeIndex}-th non-static field (in declaration order) whose
     * type is assignable to {@code fieldType}. The fallback needs no knowledge
     * of the current field name.
     *
     * @param instance   object whose field to set
     * @param name       expected field name (used as the primary lookup)
     * @param fieldType  declared type the field is assignable to
     * @param typeIndex  zero-based index among fields of that type (for the
     *                   name-less fallback when several fields share the type)
     * @param value      value to assign
     * @return {@code true} if a field was found and written
     */
    public static boolean setValueResilient(Object instance, String name, Class<?> fieldType, int typeIndex, Object value) {
        Field field = findFieldByName(instance.getClass(), name, fieldType);
        if (field == null) {
            field = findFieldByType(instance.getClass(), fieldType, typeIndex);
        }

        if (field == null) {
            System.err.println("[FancyLib] ReflectionUtils: could not resolve field '" + name + "' ("
                    + fieldType.getSimpleName() + ") on " + instance.getClass().getName());
            return false;
        }

        try {
            field.setAccessible(true);
            field.set(instance, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Field findFieldByName(Class<?> clazz, String name, Class<?> fieldType) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(name);
                if (fieldType == null || fieldType.isAssignableFrom(field.getType())) {
                    return field;
                }
            } catch (NoSuchFieldException ignored) {
                // try the superclass, then fall back to a type-based lookup
            }
        }

        return null;
    }

    private static Field findFieldByType(Class<?> clazz, Class<?> fieldType, int typeIndex) {
        int matches = 0;
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (fieldType.isAssignableFrom(field.getType()) && matches++ == typeIndex) {
                    return field;
                }
            }
        }

        return null;
    }

    public static Method getMethod(Object instance, String methodName) {
        try {
            return instance.getClass().getDeclaredMethod(methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
