package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

/**
* A saxon:handler element in the style sheet: defines a Java nodehandler that
* can be used to process a node in place of an XSLT template
*/
    
public class SAXONHandler extends XSLTemplate {

    private NodeHandler handler;

    public void prepareAttributes() throws SAXException {
       
        String handlerAtt = getAttributeValue("handler");
        if (handlerAtt==null) {
            reportAbsence("handler");
        } else {
            handler = makeHandler(handlerAtt);
        }

        super.prepareAttributes();
    }

    /**
    * Check that only the permitted attributes are present on this element.
    * This method overrides the version in the superclass
    */

    protected void checkAllowedAttributes() throws SAXException {
        String[] allowed = {"name", "mode", "match", "priority", "handler"};
        allowAttributes(allowed);
    }

    public void validate() throws SAXException {
        checkTopLevel();
    }

    /**
    * Preprocess: this registers the node handler with the controller
    */

    public void preprocess() throws SAXException
    {
        RuleManager mgr = getPrincipalStyleSheet().getRuleManager();
        Mode mode = mgr.getMode(modeName);
        if (match!=null) {
            if (prioritySpecified) { 
                mgr.setHandler(match, handler, mode, getPrecedence(), priority);
            } else {
                mgr.setHandler(match, handler, mode, getPrecedence());
            }
        }
      
    }

    /**
    * Process saxon:handler element. This is called while all the top-level nodes are being
    * processed in order, so it does nothing.
    */

    public void process(Context context) throws SAXException {
    }

    /**
    * Invoke the node handler. Called directly only when doing XSLCallTemplate
    */

    public void expand(Context context) throws SAXException {
        handler.start(context.getCurrentNode(), context);
    }

    /**
    * Load a named node handler and check it is OK.
    */

    protected static NodeHandler makeHandler (String className) throws SAXException
    {
        try {
            return (NodeHandler)(Loader.getInstance(className));
        }
        catch (ClassCastException e) {
            throw new SAXException("Failed to load node handler " + className +
                            ": it does not implement the NodeHandler interface");
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
