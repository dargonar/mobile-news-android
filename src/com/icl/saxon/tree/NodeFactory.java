package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.AttributeCollection;

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import java.util.Vector;

/**
  * Interface NodeFactory. <br>
  * A Factory for nodes used to build a tree. <br>
  * Currently only allows Element nodes to be user-constructed.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 25 February 2000
  */

public interface NodeFactory {

    /**
    * Create an Element node
    * @param parent The parent element
    * @param name The element name
    * @param attlist The attribute collection, excluding any namespace attributes
    * @param namespaces List of new namespace declarations for this element, as a sequence
    * of pairs of strings: (prefix1, uri1, prefix2, uri2...)
    * @param namespacesUsed the number of elemnts of the namespaces array actually used
    * @param locator Indicates the source document and line number containing the node
    * @param sequenceNumber Sequence number to be assigned to represent document order.
    */

    public ElementImpl makeElementNode(
            NodeInfo parent,
            Name name,
            AttributeCollection attlist,
            String[] namespaces,
            int namespacesUsed,
            Locator locator,
            int sequenceNumber)          throws SAXException;
    
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
