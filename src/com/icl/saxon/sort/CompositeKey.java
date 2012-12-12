package com.icl.saxon.sort;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;

import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

    /**
    * Class representing a single node together with the values of its sort keys
    */

    public class CompositeKey {
        private NodeInfo node;
        private String[] key;

        public CompositeKey(Vector sortkeys, NodeInfo node, Context context) throws SAXException {
            this.node = node;
            this.key = setKeyValue(sortkeys, node, context);
        }

        /**
        * Get the key value for a particular node
        * @return an array of objects: the NodeInfo itself, then one String value for each sort key
        */

        private String[] setKeyValue(Vector sortkeys, NodeInfo node, Context context) throws SAXException {

            String[] keys = new String[sortkeys.size()];
            for (int i=0; i<sortkeys.size(); i++) {
                SortKeyDefinition sk = (SortKeyDefinition)sortkeys.elementAt(i);
                keys[i] = sk.getSortKey().evaluateAsString(context);
            }
            return keys;
        }

        /**
        * Get the NodeInfo
        */

        public NodeInfo getNode() {
            return node;
        }

        /**
        * Get the n'th key value
        */

        public String getKeyValue(int n) {
            return key[n];
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
