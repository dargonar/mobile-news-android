package com.icl.saxon.sort;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import org.xml.sax.*;

// Copyright  International Computers Limited 1998
// See conditions of use

/**
 * A Comparer used for comparing keys
 *
 * @author Michael H. Kay, ICL (Michael.Kay@icl.com)
 *
 */

public abstract class Comparer {

    /**
    * Compare two objects.
    * @return <0 if a<b, 0 if a=b, >0 if a>b
    * @throws ClassCastException if the objects are of the wrong type for this Comparer
    */

    public abstract int compare(Object a, Object b);

    /**
    * Set data type. The comparer has the option of returning a different comparer
    * once it knows the data type
    */

    public Comparer setDataType(Name datatype) throws SAXException {
        return this;
    }

    /**
    * Set order. The comparer has the option of returning a different comparer
    */

    public Comparer setOrder(boolean isAscending) throws SAXException {
        return (isAscending ? this : new DescendingComparer(this) );
    }

}
