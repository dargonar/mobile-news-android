package com.icl.saxon.sort;

// Copyright © International Computers Limited 1998
// See conditions of use

/**
* A simple class for testing membership of a fixed set of ASCII strings.
* The class must be initialised with enough space for all the strings,
* it will go into an infinite loop if it fills. The string matching is case-blind,
* using an algorithm that works only for ASCII
*/

public class HashMap {

    String[] strings;
    int size;

    public HashMap(int size) {
        strings = new String[size];
        this.size = size;
    }

    public void set(String s) {
        int hash = (hashCode(s) & 0x7fffffff) % size;
        while(true) {
            if (strings[hash]==null) {
                strings[hash] = s;
                return;
            }
            if (strings[hash].equalsIgnoreCase(s)) {
                return;
            }
            hash = (hash + 1) % size;
        }
    }

    public boolean get(String s) {
        int hash = (hashCode(s) & 0x7fffffff) % size;
        while(true) {
            if (strings[hash]==null) {
                return false;
            }
            if (strings[hash].equalsIgnoreCase(s)) {
                return true;
            }
            hash = (hash + 1) % size;
        }
    }

    private int hashCode(String s) {
        // get a hashcode that doesn't depend on the case of characters.
        // This relies on the fact that char & 0xDF is case-blind in ASCII
        int hash = 0;
        int limit = s.length();
        if (limit>24) limit = 24;
        for (int i=0; i<limit; i++) {
            hash = (hash<<1) + (s.charAt(i) & 0xdf);
        }
        return hash;
    }
}

