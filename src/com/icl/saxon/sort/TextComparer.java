package com.icl.saxon.sort;
import org.xml.sax.SAXException;

// Copyright © International Computers Limited 1998
// See conditions of use

/**
 * A Comparer used for comparing text keys
 *
 * @author Michael H. Kay, ICL (Michael.Kay@icl.com)
 *
 */

public abstract class TextComparer extends Comparer {

    public final static int DEFAULT_CASE_ORDER = 0;
    public final static int LOWERCASE_FIRST = 1;
    public final static int UPPERCASE_FIRST = 2;

    /**
    * Set case order
    * @param caseOrder one of DEFAULT_CASE_ORDER, LOWERCASE_FIRST, or UPPERCASE_FIRST.
    * Indicates whether upper case letters precede or follow lower case letters in the ordering
    * @return either this or a different Comparer that will be used to perform the comparisons.
    * This allows the TextComparer to delegate the comparison to a Comparer dedicated to a
    * specific case order.
    */

    public Comparer setCaseOrder(int caseOrder) throws SAXException{
        return this;
    }

}
