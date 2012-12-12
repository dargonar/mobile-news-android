package com.icl.saxon.handlers;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.Hashtable;

/**
 * This class is the default element handler from which
 * user-defined element handlers can inherit. It is provided for convenience:
 * use is optional. The individual methods of the default element handler
 * do nothing with the content; in a subclass it is therefore only necessary to implement
 * those methods that need to do something specific.<P>
 * The startElement() method calls applyTemplates(), so child elements will
 * always be processed.<P>
 * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A>
 * @version 16 July 1999
 */
 
public class ElementHandlerBase extends ElementHandler {

    /**
    * implement start() method
    */

    public void start(NodeInfo node, Context context) throws SAXException {
        if (!(node instanceof ElementInfo))
            throw new SAXException("Element Handler called for a node that is not an element");
        startElement((ElementInfo)node, context);
    }

    /**
    * Define action to be taken before an element of this element type.<BR>
    * Default implementation does nothing, other than causing subordinate elements
    * to be processed in the same mode as the caller
    * @param e The ElementInfo object for the current element.
    */
    
    public void startElement( ElementInfo e, Context context ) throws SAXException {
        Expression exp = null;
        Mode mode = context.getMode();
	    context.getController().applyTemplates(context, exp, mode, null);
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
