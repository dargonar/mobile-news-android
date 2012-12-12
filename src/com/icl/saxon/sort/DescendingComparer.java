package com.icl.saxon.sort;
import com.icl.saxon.*;
import org.xml.sax.*;


/**
 * A Comparer used for comparing descending keys
 *
 *
 */

public class DescendingComparer extends Comparer {

    private Comparer baseComparer;

    public DescendingComparer(Comparer base) {
        baseComparer = base;
    }

    /**
    * Compare two objects.
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    * @throws ClassCastException if the objects are of the wrong type for this Comparer
    */

    public int compare(Object a, Object b) {
        return 0 - baseComparer.compare(a, b);
    }

}
