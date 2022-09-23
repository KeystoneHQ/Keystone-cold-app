package com.keystone.cold.util;


import java.lang.reflect.Array;

public class ArrayUtils {

    @SuppressWarnings("unchecked")
    public static <T> T[] remove(Class<T> c, T[] array, int index) {
        int length = array.length;
        if (index >= 0 && index < length) {
            T[] result = (T[]) Array.newInstance(c, length - 1);
            System.arraycopy(array, 0, result, 0, index);
            if (index < length - 1) {
                System.arraycopy(array, index + 1, result, index, length - index - 1);
            }
            return result;
        } else {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }
    }


}
