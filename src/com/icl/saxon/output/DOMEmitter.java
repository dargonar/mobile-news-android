package com.icl.saxon.output;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;

import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.Writer;


/**
  * DOMEmitter is an Emitter that attaches the result tree to a specified Node in a DOM Document
  */
  
public class DOMEmitter implements Emitter
{
    protected OutputDetails outputDetails;
    protected Node currentNode;
    protected Document document;
    protected boolean canNormalize = true;  // until proved otherwise

    // We keep track of namespaces to avoid outputting duplicate declarations. The namespaces
    // vector holds a list of all namespaces currently declared (organised as pairs of entries,
    // prefix followed by URI). The stack contains an entry for each element currently open; the
    // value on the stack is an Integer giving the size of the namespaces vector on entry to that
    // element.

    private String[] namespaces = new String[30];    // all namespaces currently declared
    private int namespacesSize = 0;                  // all namespaces currently declared
    private int[] namespaceStack = new int[100];
    private int nsStackTop = 0;
    private int nsCount = 0;                // number of namespaces for current element

    /**
    * Set the Writer to use. Provided merely to satisfy the interface.
    */

    public void setWriter(Writer writer) {}

    /**
    * Set the character set to use. Provided merely to satisfy the interface.
    */

    public void setCharacterSet(CharacterSet charSet) {}

    /**
    * Set Document Locator. Provided merely to satisfy the interface.
    */

    public void setDocumentLocator(Locator locator) {}   

    /**
    * Start of the document. 
    */
    
    public void startDocument () throws SAXException 
    {

    }

    /**
    * End of the document. 
    */
    
    public void endDocument () throws SAXException
    {

    }

    /**
    * Start a namespace prefix mapping. All prefixes used in element or attribute names
    * will be notified before the relevant startElement call
    */

    public void startPrefixMapping(String prefix, String uri) throws SAXException {

        // Ignore declarations that are already present on the stack, provided they are
        // not masked by a different declaration of the same prefix

        for (int i=namespacesSize-2; i>=0; i-=2) {
            if (namespaces[i]==prefix) {
                if (namespaces[i+1]==uri) {
                    return;
                } else {
                    break;
                }
            }
        }

        // Ignore xmlns="" unless there is already a different xmlns= declaration on the stack

        if (prefix=="" && uri=="") {
            boolean found = false;
            for (int i=namespacesSize-2; i>=0; i-=2) {
                if (namespaces[i]=="") {
                    if (namespaces[i+1]!="") {
                        found=true;
                    } 
                }
            }
            if (!found) return;
        }
            
        // Add the declarations to the stack, expanding it if necessary

        if (namespacesSize+2 >= namespaces.length) {
            String[] newlist = new String[namespacesSize*2];
            System.arraycopy(namespaces, 0, newlist, 0, namespacesSize);
            namespaces = newlist;
        }
        namespaces[namespacesSize++] = prefix;
        namespaces[namespacesSize++] = uri;
        nsCount++;
    }        

    /**
    * Start of an element. Output the start tag, escaping special characters.
    */
    
    public void startElement (Name fullname, AttributeCollection attributes) throws SAXException
    {
        // May need to output a namespace undeclaration (bug 5.3.2/014)
        if (fullname.getPrefix()=="" && fullname.getURI()=="") {
            startPrefixMapping("", "");
        }
        
        String name = fullname.getDisplayName();
        try {

            Element element = document.createElement(name);
            currentNode.appendChild(element);
            currentNode = element;

            // output the namespaces

            for (int n=namespacesSize - (nsCount*2); n<namespacesSize; n+=2) {
                String prefix = namespaces[n];
                String uri = namespaces[n+1];
                if (!(uri.equals(Namespace.XML))) {
                    if (prefix.equals("")) {
                        element.setAttribute("xmlns", uri);
                    } else {
                        element.setAttribute("xmlns:" + prefix, uri);
                    }
                }
            }
            
            // remember how many namespaces there were so we can unwind the stack later

            if (nsStackTop>=namespaceStack.length) {
                int[] newstack = new int[nsStackTop*2];
                System.arraycopy(namespaceStack, 0, newstack, 0, nsStackTop);
                namespaceStack = newstack;
            }
 
            namespaceStack[nsStackTop++] = nsCount;

            nsCount = 0;

            // output the attributes

            for (int i=0; i<attributes.getLength(); i++) {
                element.setAttribute(
                    attributes.getExpandedName(i).getDisplayName(),
                    attributes.getValue(i));
            }
            
        } catch (DOMException err) {
            throw new SAXException(err);
        }            
    }
    
    /**
    * End of an element.
    */

    public void endElement (Name name) throws SAXException
    {
        // discard the namespaces declared on this element
        
        if (nsStackTop-- == 0) {
            throw new SAXException("Attempt to output end tag with no matching start tag");
        }
        
        int nscount = namespaceStack[nsStackTop];
        namespacesSize -= (nscount*2);

        if (canNormalize) {
            try {
                currentNode.normalize();
            } catch (Throwable err) {           // in case it's a Level 1 DOM
                canNormalize = false;
            }
        }
        
        currentNode = currentNode.getParentNode();

    }


    /**
    * Character data.
    */

    public void characters (char[] ch, int start, int length) throws SAXException
    {   
        try {
            Text text = document.createTextNode(new String(ch, start, length));
            currentNode.appendChild(text);
        } catch (DOMException err) {
            throw new SAXException(err);
        }            
    }


    /**
    * Handle a processing instruction.
    */
    
    public void processingInstruction (String target, String data)
        throws SAXException
    {
        try {
            ProcessingInstruction pi =
                document.createProcessingInstruction(target, data);
            currentNode.appendChild(pi);
        } catch (DOMException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Handle a comment.
    */
    
    public void comment (char ch[], int start, int length) throws SAXException
    {
        try {
            Comment comment = document.createComment(new String(ch, start, length));
            currentNode.appendChild(comment);
        } catch (DOMException err) {
            throw new SAXException(err);
        }
    }

    /**
    * Set output details
    */
    
    public void setOutputDetails (OutputDetails details) {
        outputDetails = details;
        currentNode = details.getDOMNode();
        if (currentNode instanceof Document) {
            document = (Document)currentNode;
        } else {
            document = currentNode.getOwnerDocument();
        }
    }

    /**
    * Set escaping on or off: ignored in this Emitter
    */

    public void setEscaping(boolean escaping) {}

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
