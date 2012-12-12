package com.icl.saxon.handlers;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
 * This abstract class defines the node handler interface used by SAXON.
 * This is used to handle all kinds of nodes: elements, character data, and attributes
 * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A>
 * @version 7 April 1999: generalisation of old ElementHandler
 */
 
public abstract class NodeHandler {

    /**
    * Define action to be taken at the start of a node.<BR>
    * This method must be implemented in a subclass.
    * @param node The NodeInfo object for the current node.
    * @exception SAXException Aborts the parse
    * @see NodeInfo
    */
    
    public abstract void start( NodeInfo node, Context context ) throws SAXException;

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
