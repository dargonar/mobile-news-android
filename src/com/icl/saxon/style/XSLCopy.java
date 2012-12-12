package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;

import org.xml.sax.*;
import java.util.*;

/**
* Handler for xsl:copy elements in stylesheet.<BR>
*/

public class XSLCopy extends StyleElement {

    private String use;                     // value of use-attribute-sets attribute

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {

        String[] allowed = {"use-attribute-sets"};
        allowAttributes(allowed);

        AttributeList atts = attributeList;
        use = atts.getValue("use-attribute-sets");
    }

    public void validate() throws SAXException {
        checkWithinTemplate();
        if (use!=null) {
            findAttributeSets(use);         // find any referenced attribute sets
        }
    }

    public void process( Context context ) throws SAXException
    {
        NodeInfo source = context.getCurrentNode();
        Outputter out = context.getOutputter();

        // Processing depends on the node type.
        // ## Should really do this with a polymorphic method on the node class, but this is
        // ## messy because of the need to call processChildren() 

        if (source instanceof ElementInfo) {            
            out.writeStartTag(source.getExpandedName());
    
            ((ElementInfo)source).outputNamespaceNodes(out);

            processAttributeSets(context);
            processChildren(context);
            out.writeEndTag(source.getExpandedName());
            
        } else if (source instanceof AttributeInfo) {            
            out.writeAttribute(source.getExpandedName(), source.getValue(), false);
            processChildren(context);
            
        } else if (source instanceof TextInfo) {            
            out.writeContent(source.getValue());
            processChildren(context);

        } else if (source instanceof ProcInstInfo) {            
            out.writePI(source.getDisplayName(), source.getValue());
            processChildren(context);
            
        } else if (source instanceof CommentInfo) {            
            out.writeComment(source.getValue());
            processChildren(context);
            
        } else if (source instanceof NamespaceInfo) {
            NamespaceInfo ns = (NamespaceInfo)source;
            out.writeNamespaceDeclaration(ns.getNamespacePrefix(), ns.getNamespaceURI(), true);
            processChildren(context);
            
        } else if (source instanceof DocumentInfo) {
            processChildren(context);
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
