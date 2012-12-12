package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;


/**
* A NodeEnumeration is used to iterate over a list of nodes. It is very similar to
* the standard Java Enumeration interface, except that it throws exceptions and returns
* NodeInfo objects rather than general Objects. It also has extra properties allowing the
* client to determine whether the nodes are in document order, etc.
*/

public interface NodeEnumeration  {

    /**
    * Determine whether there are more nodes to come. <BR>
    * (Note the term "Element" is used here in the sense of the standard Java Enumeration class,
    * it has nothing to do with XML elements).
    * @return true if there are more nodes
    */

    public boolean hasMoreElements() throws SAXException;

    /**
    * Get the next node in sequence. <BR>
    * (Note the term "Element" is used here in the sense of the standard Java Enumeration class,
    * it has nothing to do with XML elements).
    * @return the next NodeInfo
    */

    public NodeInfo nextElement() throws SAXException;
    
    /**
    * Determine whether the nodes returned by this enumeration are known to be in document order
    * @return true if the nodes are guaranteed to be in document order.
    */

    public boolean isSorted() throws SAXException;

    /**
    * Determine whether the nodes returned by this enumeration are known to be in
    * reverse document order.
    * @return true if the nodes are guaranteed to be in document order.
    */

    public boolean isReverseSorted() throws SAXException;

    /**
    * Determine whether the nodes returned by this enumeration are known to be peers, that is,
    * no node is a descendant or ancestor of another node. This significance of this property is
    * that if a peer enumeration is applied to each node in a set derived from another peer
    * enumeration, and if both enumerations are sorted, then the result is also sorted.
    */

    public boolean isPeer() throws SAXException;

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
