package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.trace.*;  // e.g.

import org.xml.sax.SAXException;

/**
* An xsl:choose elements in the stylesheet.<BR>
*/

public class XSLChoose extends StyleElement {

    private StyleElement otherwise;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {
        String[] allowed = {};
        allowAttributes(allowed);
    }

    public void validate() throws SAXException {
        checkWithinTemplate();

        NodeInfo xslwhen = null;

        NodeInfo children[] = getAllChildNodes();
        for (int i=0; i<children.length; i++) {
            if (children[i] instanceof XSLWhen) {
                if (otherwise!=null) {
                    throw styleError("xsl:otherwise must come last");
                }
                xslwhen = children[i];
            } else if (children[i] instanceof XSLOtherwise) {
                if (otherwise!=null) {
                    throw styleError("Only one xsl:otherwise allowed in an xsl:choose");
                } else {
                    otherwise = (StyleElement)children[i];
                }
            } else {
                throw styleError("Only xsl:when and xsl:otherwise are allowed here");
            }
        }

        if (xslwhen==null)
            throw styleError("xsl:choose with no xsl:when");
    }

    public void process( Context context ) throws SAXException
    {
	    boolean isTracing = context.getController().isTracing(); // e.g.
        StyleElement option = (StyleElement)getFirstChild();

        // find the first matching "when" condition

        while (option!=null) {
            boolean go;
            
            if (option instanceof XSLWhen) {
                go = ((XSLWhen)option).getCondition().evaluateAsBoolean(context);
            } else {    // xsl:otherwise
                go = true;
            }

            if (go) {            
        		if (isTracing) { // e.g.
        		    TraceListener listener = context.getController().getTraceListener();
        		    listener.enter(option, context);
        		    option.process(context);
        		    listener.leave(option, context);
        		} else {
        		    option.process(context);
        		}
        		option = null;
            } else {
                option = (StyleElement)option.getNextSibling();
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
// Contributor(s): 
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//
