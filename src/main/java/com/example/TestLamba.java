package com.example;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by rodrigo on 02/11/16.
 */
public class TestLamba {
    public static void main(String[] args) {
        List<String> stringList = Arrays.asList("A", "B", "C");

        String collect = stringList.stream()
                .filter(item -> {
                    try {
                        return filterByString(item);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        System.out.println("exception: " + e.getLocalizedMessage());
                        return false;
                    }
                })
                .collect(Collectors.joining(","));

        System.out.println(collect);
    }

    private static boolean filterByString(String content) {
        if (content.equals("A"))
            throw new IllegalArgumentException("Invalid argument");
        return true;
    }
}
