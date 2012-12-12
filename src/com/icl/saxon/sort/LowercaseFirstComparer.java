package com.icl.saxon.sort;

// Copyright © International Computers Limited 1998
// See conditions of use

/**
 * A Comparer used for comparing keys
 *
 * @author Michael H. Kay, ICL (Michael.Kay@icl.com)
 *
 */

public class LowercaseFirstComparer extends Comparer {

    /**
    * Compare two string objects: case is irrelevant, unless the strings are equal ignoring
    * case, in which case lowercase comes first.
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    * @throws ClassCastException if the objects are of the wrong type for this Comparer
    */

    public int compare(Object a, Object b) {
        char[] a1 = ((String)a).toCharArray();
        char[] b1 = ((String)b).toCharArray();
        int alen = a1.length;
        int blen = b1.length;
        int i = 0;
        int j = 0;

        while (true) {
            if (i==alen && j==blen) break;
            if (i==alen) return -1;
            if (j==blen) return +1;
            int diff = Character.toLowerCase(a1[i]) -
                             Character.toLowerCase(b1[j]);
            i++;
            j++;
            if (diff!=0) return diff;
        }

        i = 0;
        j = 0;
        while (true) {
            if (i==alen) return 0;
            int diff = a1[i++] - b1[j++]; 
            if (diff!=0) {
                return (Character.isLowerCase(a1[i-1]) ? -1 : +1);
            }
        }        
            
    }


}
