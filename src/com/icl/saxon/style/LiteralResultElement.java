package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Builder;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import java.util.*;
import java.text.*;

/**
* This class represents a literal result element in the style sheet
* (typically an HTML element to be output). <br>
* It is also used to represent unknown top-level elements, which are ignored.
*/

public class LiteralResultElement extends StyleElement {

    private Name resultName;
    private Name[] attributeNames;
    private Expression[] attributeValues;
    private boolean[] attributeChecked;
    private int numberOfAttributes;
    private boolean toplevel;
    private Vector namespaceNodes = new Vector();

    /**
    * On standard attributes such as extension-element-prefixes, say that an XSL prefix is required
    */

    public boolean requiresXSLprefix() {
        return true;
    }

    /**
    * Process the attribute list
    */

    public void prepareAttributes() throws SAXException {
        // processing of the attribute list is deferred until validate() time, so that
        // namespaces can be translated if necessary using the namespace aliases in force
        // for the stylesheet.
    }

    /**
    * Validate that this node is OK
    */

    public void validate() throws SAXException {
        toplevel = (getParentNode() instanceof XSLStyleSheet);

        String uri = getURI();
        if (toplevel) {
            // A top-level element can never be a "real" literal result element, 
            // but this class gets used for them anyway
            
            if (uri=="") {
                throw new SAXException("Top level elements must have a non-null namespace URI");
            }
        } else {
            
            // Build the list of output namespace nodes

            // Up to 5.3.1 we listed the namespace nodes associated with this element that were not also
            // associated with an ancestor literal result element (because those will already
            // have been output). Unfortunately this isn't true if the namespace was present on an outer
            // LRE, and was excluded at that level using exclude-result-prefixes, and is now used in an
            // inner element: bug 5.3.1/006

            addNamespaceNodes(this, namespaceNodes, null);

            // apply any aliases required to create the list of output namespaces

            XSLStyleSheet sheet = getPrincipalStyleSheet();
            for (int i=0; i<namespaceNodes.size(); i++) {
                NamespaceInfo ns = (NamespaceInfo)namespaceNodes.elementAt(i);
                XSLNamespaceAlias nsalias = sheet.getNamespaceAlias(ns.getNamespaceURI());
                if (nsalias != null) {
                    //just remove the namespace node because it will always be a duplicate
                    namespaceNodes.removeElementAt(i);
                    i--;
                }
            }                    
       
            // determine if there is an alias for the namespace of the element name

            resultName = getExpandedName();
            XSLNamespaceAlias xna = sheet.getNamespaceAlias(uri);

            if (xna != null) {
                String newPrefix = xna.getResultPrefix();
                String newURI = xna.getResultURI();
                resultName = new Name(newPrefix, newURI, getLocalName());
            }               

            // establish the names to be used for all the output attributes

            int num = attributeList.getLength();
            attributeNames = new Name[num];
            attributeValues = new Expression[num];
            attributeChecked = new boolean[num];
            numberOfAttributes = 0;
            
            for (int i=0; i<num; i++) {
                Name aname = attributeList.getExpandedName(i);

                if (aname.getURI().equals(Namespace.XSLT)) {
                    if (aname.getLocalName().equals("use-attribute-sets")) {
                        findAttributeSets(attributeList.getValue(i));
                    }
                    // the only others, xsl:extension-element-prefixes and xsl:exclude-result-prefixes,
                    // were dealt with earlier
                } else {
                    Name alias = aname;
                    if (aname.getPrefix()!="") {
                        XSLNamespaceAlias axna = sheet.getNamespaceAlias(aname.getURI());
                        if (axna != null) {
                            String newPrefix = axna.getResultPrefix();
                            String newURI = axna.getResultURI();
                            alias = new Name(newPrefix, newURI, aname.getLocalName());
                        }
                    }
                    attributeNames[numberOfAttributes] = alias;
                    Expression exp = AttributeValueTemplate.make(attributeList.getValue(i), this);
                    attributeValues[numberOfAttributes] = exp;

                    // if we can be sure the attribute value contains no special XML/HTML characters,
                    // we can save the trouble of checking it each time it is output.
                    // Note that the check includes characters that are special in a URL, including space.

                    attributeChecked[numberOfAttributes] = false;
                    boolean special = false;
                    if (exp instanceof StringValue) {
                        String val = ((StringValue)exp).asString();
                        for (int k=0; k<val.length(); k++) {
                            char c = val.charAt(k);
                            if ((int)c<33 || (int)c>126 ||
                                     c=='<' || c=='>' || c=='&' || c=='\"' || c=='%') {
                                special = true;
                                break;
                             }
                        }
                        attributeChecked[numberOfAttributes] = !special;
                    }
                    numberOfAttributes++;
                }
            }

            // remove any namespaces that are on the exclude-result-prefixes list, unless it is
            // the namespace of the element or an attribute

            for (int n=0; n<namespaceNodes.size(); ) {
                String nsuri = ((NamespaceInfo)namespaceNodes.elementAt(n)).getNamespaceURI();
                if (isExcludedNamespace(nsuri)) {
                    boolean exclude = true;

                    // check the element name

                    if (nsuri.equals(resultName.getURI())) {
                        exclude = false;
                    }

                    // check the attribute names

                    for (int a=0; a<numberOfAttributes; a++) {
                        Name attName = attributeNames[a];
                        if (nsuri.equals(attName.getURI())) {
                            exclude = false;
                            break;
                        }
                    }

                    // if the name isn't in use, exclude it from the output namespace list

                    if (exclude) {                    
                        namespaceNodes.removeElementAt(n);
                    } else {
                        n++;
                    }
                } else {
                    n++;
                }
            }  
        }
    }



    public void process(Context context) throws SAXException
    {
        // top level elements in the stylesheet are ignored
        if (toplevel) return;
        
        // output the start tag
        Outputter o = context.getOutputter();
        o.writeStartTag(resultName);

        // output the namespace list
        for (int i=0; i<namespaceNodes.size(); i++) {
            NamespaceInfo ns = (NamespaceInfo)namespaceNodes.elementAt(i);
            o.writeNamespaceDeclaration(ns.getNamespacePrefix(), ns.getNamespaceURI(), false);
        }

        // output any attributes from xsl:use-attribute-set

        processAttributeSets(context);

        // evaluate AVT expressions and output the attributes (these may be overwritten later)

        for (int i=0; i<numberOfAttributes; i++) {
            Name attname = attributeNames[i];
            String attval = attributeValues[i].evaluateAsString(context);
            o.writeAttribute(attname, attval, attributeChecked[i]);
        }

        // process the child elements in the stylesheet

        processChildren(context);

        // write the end tag
        
        o.writeEndTag(resultName);

    }

    /**
    * Make a top-level literal result element into a stylesheet. This implements
    * the "Literal Result Element As Stylesheet" facility.
    */

    public DocumentInfo makeStyleSheet() throws SAXException {

        // the implementation grafts the LRE node onto a containing xsl:template and
        // xsl:stylesheet
        
        String xslPrefix = getPrefixForURI(Namespace.XSLT);
        if (xslPrefix==null) {
            throw styleError("Not a stylesheet (the xsl namespace is not declared on the top-level element)");
        }

        // check there is an xsl:version attribute (it's mandatory), and copy
        // it to the new xsl:stylesheet element
        
        Name xslVersion = new Name(xslPrefix, Namespace.XSLT, "version");
        String version = getAttributeValue(xslVersion);
        if (version==null) {
            throw styleError("There must be an xsl:version attribute on the outermost element");
        }

        Builder builder = new Builder();
        builder.setDocumentLocator(null);
        builder.setNodeFactory(new StyleNodeFactory());
        builder.setSystemId(this.getSystemId());

        builder.startDocument();
        builder.startPrefixMapping("xsl", Namespace.XSLT);
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "version", "version", "CDATA", version);
        builder.startElement(Namespace.XSLT, "stylesheet", "xsl:stylesheet", atts);

        atts.clear();
        atts.addAttribute("", "match", "match", "CDATA", "/");
        builder.startElement(Namespace.XSLT, "template", "xsl:template", atts);

        builder.graftElement(this);

        builder.endElement(Namespace.XSLT, "template", "xsl:template");
        builder.endElement(Namespace.XSLT, "stylesheet", "xsl:stylesheet");
        builder.endPrefixMapping("xsl");
        builder.endDocument();

        return builder.getCurrentDocument();

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
