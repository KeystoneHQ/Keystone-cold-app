package com.keystone.cold.util;


public class ArrayUtils {


    public static CharSequence[] remove(CharSequence[] array, int index) {
        int length = array.length;
        if (index >= 0 && index < length) {
            CharSequence[] result = new CharSequence[length - 1];
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
