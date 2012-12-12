package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.trace.*;  // e.g.
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import org.w3c.dom.Node;
import java.io.*;

/**
* This element is a surrogate for an extension element (or indeed an xsl element)
* for which no implementation is available.<BR>
*/

public class AbsentExtensionElement extends StyleElement {
   
    private SAXException reason;

    public void setReason(SAXException reason) {
        this.reason = reason;
    };
    
    public void prepareAttributes() throws SAXException {
    }

    public void validate() throws SAXException {
    }

    public void process( Context context ) throws SAXException {

        // process any xsl:fallback children; if there are none, report the original failure reason
        
        XSLFallback fallback = null;
        Node child = getFirstChild();
        while (child!=null) {
            if (child instanceof XSLFallback) {
                fallback = (XSLFallback)child;
                break;
            }
            child = child.getNextSibling();
        }
        
        if (fallback==null) throw reason;

        boolean tracing = context.getController().isTracing();

        while (child!=null) {
            if (child instanceof XSLFallback) {
                XSLFallback f = (XSLFallback)child;
                
                if (tracing) {
                    TraceListener listener = context.getController().getTraceListener();
            		listener.enter(f, context);
            		f.process(context);
            		listener.leave(f, context);
                } else {
                    f.process(context);
                }
            }
            child = child.getNextSibling();
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
