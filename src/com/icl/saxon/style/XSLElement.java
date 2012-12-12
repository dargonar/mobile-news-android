package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

/**
* An xsl:element element in the stylesheet.<BR>
*/

public class XSLElement extends StyleElement {

    private Expression elementName;
    private Expression namespace = null;
    private String use;
    private boolean declared = false;       // used by compiler

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {


        String[] allowed = {"name", "namespace", "use-attribute-sets"};
        allowAttributes(allowed);

        String nameAtt = getAttributeValue("name");
        if (nameAtt==null)
            reportAbsence("name");
        elementName = AttributeValueTemplate.make(nameAtt, this);

        String namespaceAtt = getAttributeValue("namespace");
        if (namespaceAtt!=null) {
            namespace = AttributeValueTemplate.make(namespaceAtt, this);
        }

        use = getAttributeValue("use-attribute-sets");
       
    }

    public void validate() throws SAXException {
        checkWithinTemplate();
        if (use!=null) {
            findAttributeSets(use);        // find any referenced attribute sets
        }
    }

    public void process( Context context ) throws SAXException
    {         
        // produce (pending) output
        
        String expandedName = elementName.evaluateAsString(context);
        String prefix;
        String uri;

        if (!Name.isQName(expandedName)) {
            context.getController().reportRecoverableError(
                "Invalid element name: " + expandedName, this);
            processChildren(context);
            return;
        }

        Name fullName;
        if (namespace!=null) {
            uri = namespace.evaluateAsString(context).intern();
            

            
            int colon = expandedName.indexOf(":");
            prefix = (colon<0 ? "" : expandedName.substring(0, colon).intern());
            String localName = (colon<0 ? expandedName : expandedName.substring(colon+1));
            fullName = new Name(prefix, uri, localName);

        } else {
            fullName = new Name(expandedName, this, true);
            prefix = fullName.getPrefix();
            uri = fullName.getURI();
        }

        Outputter out = context.getOutputter();
        out.writeStartTag(fullName);
        if (uri!="") {
            out.writeNamespaceDeclaration(prefix, uri, false);
        }

        // apply the content of any attribute sets mentioned in use-attribute-sets
        processAttributeSets(context);

        // process subordinate elements in stylesheet
        processChildren(context);

        // output the element end tag
        out.writeEndTag(fullName);
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
