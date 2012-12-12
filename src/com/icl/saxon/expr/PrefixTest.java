package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;

/**
  * PrefixTest is class that performs a test as to whether a node has a given namespace prefix.
  * The ttest is on a matching URI, not necessarily the prefix as written.
  *
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

public class PrefixTest implements NameTest {

    private String uri;

    public PrefixTest(String uri) {
        this.uri = uri;
    }

    public String getURI() {
        return uri;
    }

    public boolean isNameOf(NodeInfo node) throws SAXException {
        return node.getURI().equals(uri);
    }

    public double getDefaultPriority() {
        return -0.25;
    }

    public String toString() {
        return uri + ":*";
    }

    /**
    * Compile a Java boolean expression to create the name test
    */

    public String compile() throws SAXException {
        return "new PrefixTest(\"" + uri + "\")"; 
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
