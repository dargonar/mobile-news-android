package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
  * Base URIs are not held in nodes in the tree, because they are usually the same
  * for a whole document.
  * This class provides a map from element sequence numbers to base URIs: it is
  * linked to the root node of the tree.
  *
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

class BaseURIMap {

    private int[] sequenceNumbers;
    private String[] uris;
    private int allocated;

    public BaseURIMap() {
        sequenceNumbers = new int[10];
        uris = new String[10];
        allocated = 0;
    }

    /**
    * Set the base uri corresponding to a given sequence number
    */

    public void setBaseURI(int sequence, String uri) {
        // ignore it if same as previous
        if (allocated>0 && uri.equals(uris[allocated-1])) {
            return;
        }
        if (sequenceNumbers.length <= allocated + 1) {
            int[] s = new int[allocated * 2];
            String[] u = new String[allocated * 2];
            System.arraycopy(sequenceNumbers, 0, s, 0, allocated);
            System.arraycopy(uris, 0, u, 0, allocated);
            sequenceNumbers = s;
            uris = u;
        }
        sequenceNumbers[allocated] = sequence;
        uris[allocated] = uri;
        allocated++;
    }

    /**
    * Get the uri corresponding to a given sequence number
    */

    public String getBaseURI(int sequence) {
        // could use a binary chop, but it's not important
        for (int i=1; i<allocated; i++) {
            if (sequenceNumbers[i] > sequence) {
                return uris[i-1];
            }
        }
        return uris[allocated-1];
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
