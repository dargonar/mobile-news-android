package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.ElementInfo;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import org.w3c.dom.Node;

/**
* Handler for xsl:text elements in stylesheet. <BR>
*/

public class XSLText extends StyleElement {

    private boolean disable = false;
    private String value = null;
    

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }

    public void prepareAttributes() throws SAXException {

        String[] allowed = {"disable-output-escaping"};
        allowAttributes(allowed);
        
        String disableAtt = getAttributeValue("disable-output-escaping");
        if (disableAtt != null) {
            if (disableAtt.equals("yes")) {
                disable = true;
            } else if (disableAtt.equals("no")) {
                disable = false;
            } else {
                throw styleError("disable-output-escaping attribute must be either yes or no");
            }
        }
    }

    public void validate() throws SAXException {
        checkWithinTemplate();
        Node node = getFirstChild();
        while (node!=null) {
            if (node instanceof ElementInfo) {
                throw styleError("xsl:text must not have any child elements");
            }
            node = node.getNextSibling();
        }
    }

    public void process(Context context) throws SAXException {
        if (value==null) value = getValue();
        if (!value.equals("")) {
            Outputter out = context.getOutputter();
            if (disable) {
                out.setEscaping(false);
                out.writeContent(value);
                out.setEscaping(true);
            } else {
                out.writeContent(value);
            }
        }
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
