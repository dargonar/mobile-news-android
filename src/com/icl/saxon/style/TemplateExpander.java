package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* Handle a node in the source document, by applying the contents of the relevant
* template in the style sheet. An instance of TemplateExpander is created for each
* xsl:template in the style sheet; the node parameter identifies the
* xsl:template element.
*/

public class TemplateExpander extends NodeHandler {

    protected XSLTemplate template;

    /**
    * Create a Template Expander, identifying the template to be expanded
    * @param node the xsl:template element in the stylesheet
    */

    public TemplateExpander (XSLTemplate node) {
        this.template = node;
    }

    /**
    * Process a node in the source document
    */
   
    public void start( NodeInfo e, Context context ) throws SAXException {
        context.setCurrentTemplate(template);
        template.expand(context);
    }

    public void end( NodeInfo e, Context context ) throws SAXException {}

    /**
    * Get the associated template
    */

    public XSLTemplate getTemplate() {
        return template;
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
