package com.icl.saxon.sort;

// Copyright © International Computers Limited 1998
// See conditions of use

/**
 * A Comparer used with lang="en". Note this only does anything intelligent with characters
 * in ISO 8859/1, which are mapped to their unaccented equivalents
 *
 * @author Michael H. Kay, ICL (Michael.Kay@icl.com)
 *
 */

public class Compare_en extends TextComparer {

    // Following string maps Latin-1 characters in the range C0-FF to equivalent unaccented letter

    private static String supp =
    "AAAAAAACEEEEIIII[NOOOOO*OUUUUY]Saaaaaaaceeeeiiii{nooooo*ouuuuy}y";

    int caseOrder = UPPERCASE_FIRST;

    /**
    * Compare two string objects, in three phases:
    * (a) ignoring accents and case
    * (b) if still equal, ignoring case
    * (c) if still equal, taking case into account
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    */

    public int compare(Object a, Object b) {

        char[] a1 = ((String)a).toCharArray();
        char[] b1 = ((String)b).toCharArray();
        int alen = a1.length;
        int blen = b1.length;

        // strip off the accents
        for (int k=0; k<alen; k++) {
            int code = (int)a1[k];
            if (code>=192 && code<=255) {
                a1[k] = supp.charAt(code-192);
            }
        }
        for (int k=0; k<blen; k++) {
            int code = (int)b1[k];
            if (code>=192 && code<=255) {
                b1[k] = supp.charAt(code-192);
            }
        }
        int i = 0;
        int j = 0;
        
        // do an accent-blind comparison
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

        // still equal - try again with accents, but still case-blind
        
        a1 = ((String)a).toCharArray();
        b1 = ((String)b).toCharArray();
        i = 0;
        j = 0;
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

        // still equal: take case into account
        
        i = 0;
        j = 0;
        while (true) {
            if (i==alen) return 0;
            int diff = a1[i++] - b1[j++]; 
            if (diff!=0) {
                if (caseOrder==LOWERCASE_FIRST) {
                    return (Character.isLowerCase(a1[i-1]) ? -1 : +1);
                } else {
                    return (Character.isUpperCase(a1[i-1]) ? -1 : +1);
                }
            }
        }        
            
    }

    public Comparer setCaseOrder(int caseOrder) {
        this.caseOrder = caseOrder;
        return this;
    }
        
}
