package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import org.xml.sax.*;
import java.io.*;
import java.util.*;

/**
  * DTDEmitter is an Emitter that generates output in DTD format from special elements
  * such as dtd:doctype and dtd:element.
  */
  
public class DTDEmitter extends XMLEmitter
{
    private String current = null;
    private boolean openSquare = false;

    /**
    * Output the document type declaration. This does nothing, because the real DTD will
    * be constructed from the elements written later.
    */

    protected void writeDocType(String type, String systemId, String publicId) throws SAXException {
    }

    /**
    * Start of an element. 
    */
    
    public void startElement (Name fullname, AttributeCollection attributes) throws SAXException
    {
        String uri = fullname.getURI();
        String localname = fullname.getLocalName();
        try {
            if (uri.equals(Namespace.DTD)) {
                if ("doctype".equals(current) && !openSquare) {
                    writer.write(" [");
                    openSquare = true;
                }

                if (localname.equals("doctype")) {
                    if (current!=null) {
                        throw new SAXException("dtd:doctype can only appear at top level of DTD");
                    }
                    String name = attributes.getValue("name");
                    String system = attributes.getValue("system");
                    String publicid = attributes.getValue("public");
                    if (name==null) {
                        throw new SAXException("dtd:doctype must have a name attribute");
                    }

                    writer.write("<!DOCTYPE " + name + " ");
                    if (system!=null) {
                        if (publicid!=null) {
                            writer.write("PUBLIC \"" + publicid + "\" \"" + system + "\"");
                        } else {
                            writer.write("SYSTEM \"" + system + "\"");
                        }
                    }
                    
                } else if (localname.equals("element")) {
                    if (!("doctype".equals(current))) {
                        throw new SAXException("dtd:element can only appear as child of dtd:doctype");
                    }
                    String name = attributes.getValue("name");
                    String content = attributes.getValue("content");
                    if (name==null) {
                        throw new SAXException("dtd:element must have a name attribute");
                    }                    
                    if (content==null) {
                        throw new SAXException("dtd:element must have a content attribute");
                    }
                    writer.write("\n  <!ELEMENT " + name + " " + content + " ");

                } else if (localname.equals("attlist")) {
                    if (!("doctype".equals(current))) {
                        throw new SAXException("dtd:attlist can only appear as child of dtd:doctype");
                    }
                    String name = attributes.getValue("element");
                    if (name==null) {
                        throw new SAXException("dtd:attlist must have an attribute named 'element'");
                    }                    
                    writer.write("\n  <!ATTLIST " + name + " " );

                } else if (localname.equals("attribute")) {
                    if (!("attlist".equals(current))) {
                        throw new SAXException("dtd:attribute can only appear as child of dtd:attlist");
                    }
                    String name = attributes.getValue("name");
                    String type = attributes.getValue("type");
                    String value = attributes.getValue("value");
                    if (name==null) {
                        throw new SAXException("dtd:attribute must have a name attribute");
                    }                    
                    if (type==null) {
                        throw new SAXException("dtd:attribute must have a type attribute");
                    }
                    if (value==null) {
                        throw new SAXException("dtd:attribute must have a value attribute");
                    }                      
                    writer.write("\n    " + name + " " + type + " " + value);

                } else if (localname.equals("entity")) {
                    if (!("doctype".equals(current))) {
                        throw new SAXException("dtd:entity can only appear as child of dtd:doctype");
                    }
                    String name = attributes.getValue("name");
                    String parameter = attributes.getValue("parameter");
                    String system = attributes.getValue("system");
                    String publicid = attributes.getValue("public");
                    String notation = attributes.getValue("notation");

                    if (name==null) {
                        throw new SAXException("dtd:entity must have a name attribute");
                    }                    

                    // we could do a lot more checking now...
                                          
                    writer.write("\n  <!ENTITY ");
                    if ("yes".equals(parameter)) {
                        writer.write("% ");
                    }
                    writer.write(name + " ");
                    if (system!=null) {
                        if (publicid!=null) {
                            writer.write("PUBLIC \"" + publicid + "\" \"" + system + "\" ");
                        } else {
                            writer.write("SYSTEM \"" + system + "\" ");
                        }
                    }
                    if (notation!=null) {
                        writer.write("NDATA " + notation + " ");
                    }

                } else if (localname.equals("notation")) {
                    if (!("doctype".equals(current))) {
                        throw new SAXException("dtd:notation can only appear as a child of dtd:doctype");
                    }
                    String name = attributes.getValue("name");
                    String system = attributes.getValue("system");
                    String publicid = attributes.getValue("public");
                    if (name==null) {
                        throw new SAXException("dtd:notation must have a name attribute");
                    }
                    if ((system==null) && (publicid==null)) {
                        throw new SAXException("dtd:notation must have a system attribute or a public attribute");
                    }
                    writer.write("\n  <!NOTATION " + name);
                    if (publicid!=null) {
                        writer.write(" PUBLIC \"" + publicid + "\" ");
                        if (system!=null) {
                            writer.write("\"" + system + "\" ");
                        }
                    } else {
                        writer.write(" SYSTEM \"" + system + "\" ");
                    }
                } else {
                    throw new SAXException("Unrecognized element " + fullname + " in DTD output");
                }

            } else {
                if (!(current.equals("entity"))) {
                    throw new SAXException("Unrecognized element " + fullname + " in DTD output");
                }
                super.startElement(fullname, attributes);
            }
        

        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }
        current = localname;
    }

    /**
    * Start a namespace prefix mapping. Suppress the DTD namespace
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (!Namespace.DTD.equals(uri)) {
            super.startPrefixMapping(prefix, uri);
        }
    }
            

    /**
    * End of an element.
    */

    public void endElement (Name fullname) throws SAXException
    {
        String uri = fullname.getURI();
        String localname = fullname.getLocalName();
        try {
            if (uri.equals(Namespace.DTD)) {

                if (localname.equals("doctype")) {
                    if (openSquare) {
                        writer.write("\n]");
                        openSquare = false;
                    }
                    writer.write(">\n");
                    current=null;
                    
                } else if (localname.equals("element")) {
                    writer.write(">");
                    current="doctype";

                } else if (localname.equals("attlist")) {
                    writer.write(">");
                    current="doctype";

                } else if (localname.equals("attribute")) {
                    current="attlist";
                    
                } else if (localname.equals("entity")) {
                    writer.write(">");
                    current="doctype";

                } else if (localname.equals("notation")) {
                    writer.write(">");
                    current="doctype";
                }
            } else {
                super.endElement(fullname);
            }
            

        } catch (java.io.IOException err) {
            throw new SAXException(err);
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
