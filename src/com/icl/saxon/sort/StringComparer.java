package com.icl.saxon.sort;
import org.xml.sax.SAXException;


/**
 * A Comparer used for comparing keys. This comparer uses the binary Unicode value of the
 * characters.
 *
 * @author Michael H. Kay, ICL (Michael.Kay@icl.com)
 *
 */

public class StringComparer extends TextComparer {

    /**
    * Compare two string objects using default collating
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
            if (i==alen && j==blen) return 0;
            if (i==alen) return -1;
            if (j==blen) return +1;
            int diff = a1[i++] - b1[j++];
            if (diff!=0) return diff;
        }          
    }

    /**
    * Set case order
    * @param caseOrder one of DEFAULT_CASE_ORDER, LOWERCASE_FIRST, or UPPERCASE_FIRST.
    * Indicates whether upper case letters precede or follow lower case letters in the ordering
    * @return either this or a different Comparer that will be used to perform the comparisons.
    * This allows the TextComparer to delegate the comparison to a Comparer dedicated to a
    * specific case order.
    */

    public Comparer setCaseOrder(int caseOrder) throws SAXException {
        if (caseOrder==LOWERCASE_FIRST) {
            return new LowercaseFirstComparer();
        }
        if (caseOrder==UPPERCASE_FIRST) {
            return new UppercaseFirstComparer();
        }
        return this;
    }


}
