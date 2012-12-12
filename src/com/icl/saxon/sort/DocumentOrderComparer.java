package com.icl.saxon.sort;
import com.icl.saxon.om.NodeInfo;

// Copyright © International Computers Limited 1998
// See conditions of use

/**
 * A Comparer used for comparing nodes in document order
 *
 * @author Michael H. Kay, ICL (Michael.Kay@icl.com)
 *
 */

public class DocumentOrderComparer extends Comparer {
    
    public int compare(Object a, Object b) {
        long p = ((NodeInfo)a).getSequenceNumber();
        long q = ((NodeInfo)b).getSequenceNumber();
        return (p==q ? 0 : (p<q ? -1 : +1));
    }
}

