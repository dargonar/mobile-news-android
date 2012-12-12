package com.icl.saxon.sort;
import com.icl.saxon.expr.*;

// Copyright © International Computers Limited 1998
// See conditions of use

/**
 * A Comparer used for comparing keys that are Doubles
 *
 * @author Michael H. Kay, ICL (Michael.Kay@icl.com)
 * @version 24 April 2000: changed so NaN always collates last
 *
 */

public class DoubleComparer extends Comparer {

    /**
    * Compare two Double objects 
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    * @throws ClassCastException if the objects are of the wrong type for this Comparer
    */

    public int compare(Object a, Object b) {
        double a1 = Value.stringToNumber((String)a);
        double b1 = Value.stringToNumber((String)b);
        if (Double.isNaN(a1)) {
            if (Double.isNaN(b1)) {
                return 0;
            } else {
                return +1;
            }
        }
        if (Double.isNaN(b1)) {
            return -1;
        }
        if (a1 == b1) return 0;
        if (a1 < b1) return -1;
        return +1;
    }
    
        
}
