package com.icl.saxon.sort;
import com.icl.saxon.*;

import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A MultiKeyComparer compares using a composite sort key. <BR>
*
*/

public class MultiKeyComparer extends Comparer {

    private Vector sortkeys = new Vector(3);    // Vector of Comparer
    private DocumentOrderComparer documentOrder = new DocumentOrderComparer();

    public MultiKeyComparer(Vector keys, Context context) throws SAXException {
        for (int i=0; i<keys.size(); i++) {
            SortKeyDefinition sk = (SortKeyDefinition)keys.elementAt(i);
            Comparer c = sk.getComparer(context);
            sortkeys.addElement(c);
        }
    }

    /**
    * Compare two values. Each value must be an array containing the NodeInfo itself,
    * followed by a set of String values, one for each sort key.
    * The values are compared pairwise until a pair is found that are unequal
    */

    public int compare(Object a, Object b) {
        CompositeKey key1 = (CompositeKey)a;
        CompositeKey key2 = (CompositeKey)b;

        for (int i=0; i<sortkeys.size(); i++) {
            Comparer c = (Comparer)sortkeys.elementAt(i);
            int comp = c.compare(key1.getKeyValue(i), key2.getKeyValue(i));
            if (comp!=0) return comp;
        }
        // all keys equal: return the nodes in document order
        return documentOrder.compare(key1.getNode(), key2.getNode());
    }
}
    

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/ 
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License. 
//
// The Original Code is: all this file. 
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (Michael.Kay@icl.com).
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved. 
//
// Contributor(s): none. 
//
