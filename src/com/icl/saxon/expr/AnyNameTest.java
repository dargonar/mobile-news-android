package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
  * AnyNameTest is NameTest that succeeds whatever the name of the node,
  * i.e. the XPath "*" name test.
  *
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

public class AnyNameTest implements NameTest {

    /**
    * Test whether this name test matches a given node
    * @return true always
    */

    public boolean isNameOf(NodeInfo node) {
        return true;
    }

    /**
    * Get default priority of this name test
    * @return -0.5 always
    */

    public double getDefaultPriority() {
        return -0.5;
    }

    /**
    * Get a string representation
    * @return "*"
    */

    public String toString() {
        return "*";
    }
    
    /**
    * Compile a Java boolean expression to create the name test
    */

    public String compile() throws SAXException {
        return "new AnyNameTest()"; 
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
