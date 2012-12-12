package com.icl.saxon;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
  * NameTest is an interface that enables a test of whether a node has a particular
  * name. There are three kinds of name test, a full name test, a prefix test, and an
  * "any node" test.
  *
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

public interface NameTest {

    /**
    * Test whether this name test is satisfied by a given node
    */

    public boolean isNameOf(NodeInfo node) throws SAXException;

    /**
    * Determine the default priority of this name test when used on its own as a Pattern
    */

    public double getDefaultPriority();

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
