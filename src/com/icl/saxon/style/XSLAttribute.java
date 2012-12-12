package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.*;
import java.util.*;

/**
* xsl:attribute element in stylesheet.<BR>
*/

public class XSLAttribute extends StyleElement {

    private static Name saxonDisable = Name.reconstruct("saxon", Namespace.SAXON, "disable-output-escaping");

    private Expression attributeName;
    private Expression namespace=null;
    private boolean disable = false;
    
    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {


        String[] allowed = {"name", "namespace"};
        allowAttributes(allowed);

        String nameAtt = getAttributeValue("name");
        if (nameAtt==null)
            reportAbsence("name");
        attributeName = AttributeValueTemplate.make(nameAtt, this);

        String namespaceAtt = getAttributeValue("namespace");
        if (namespaceAtt!=null) {
            namespace = AttributeValueTemplate.make(namespaceAtt, this);
        }

        String disableAtt = getAttributeValue(saxonDisable);
        disable = (disableAtt != null && disableAtt.equals("yes"));

    }

    public void validate() throws SAXException {
        NodeInfo anc = (NodeInfo)getParentNode();
        while (anc!=null) {
            if (anc instanceof XSLAttributeSet) return;
            if (anc instanceof XSLTemplate) return;
            if (anc instanceof XSLVariable) return;
            if (anc instanceof XSLParam) return;
            if (anc instanceof SAXONFunction) return;
            anc = (NodeInfo)anc.getParentNode();
        }
        throw styleError("Element must only be used within a template or within an attribute-set");
    }

    public void process( Context context ) throws SAXException
    {       
        String expandedName = attributeName.evaluateAsString(context);
        
        if (!Name.isQName(expandedName)) {
            context.getController().reportRecoverableError(
                "Invalid attribute name: " + expandedName, this);
            return;
        }

        int colon = expandedName.indexOf(":");

        Name fullName;
        String prefix;
        String uri;
        if (namespace!=null) {

            // generate a name using the supplied namespace URI
            
            uri = namespace.evaluateAsString(context).intern();
            if (colon<0) {
                prefix=getPrefixForURI(uri);
                if (prefix==null) {
                    prefix="ns0";       // arbitrary generated prefix; will be changed later if
                                        // not unique
                }
            } else {
                prefix = expandedName.substring(0, colon).intern();
            }
            
            String localName = (colon<0 ? expandedName : expandedName.substring(colon+1)).intern();
            fullName = new Name(prefix, uri, localName);

        } else {
            
            fullName = new Name(expandedName, this, false);
            prefix = fullName.getPrefix();
            uri = fullName.getURI();
        }

        if ((prefix=="" && expandedName.equals("xmlns")) || prefix=="xmlns") {
            context.getController().reportRecoverableError(
                "Invalid attribute name: " + expandedName, this);
            return;
        }
        
        Outputter out = context.getOutputter();
        if (prefix!="") {
            out.writeNamespaceDeclaration(prefix, uri, true);
        }
        out.writeAttribute(fullName, expandChildren(context), disable);
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
