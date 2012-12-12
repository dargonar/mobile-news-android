package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.AttributeCollection;
import com.icl.saxon.PreviewManager;
import com.icl.saxon.ExtendedInputSource;
import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.ParserManager;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import java.io.*;
import java.net.URL;

/**
  * The Builder class is responsible for taking a stream of SAX events and constructing
  * a Document tree. 
  * @author Michael H. Kay (Michael.Kay@icl.com)
  */
  
public class Builder implements org.xml.sax.ContentHandler,
                                   org.xml.sax.ext.LexicalHandler,
                                   org.xml.sax.ErrorHandler,
                                   org.xml.sax.DTDHandler,
                                   org.xml.sax.Locator

{
    private static AttributeCollection emptyAttributeCollection = new AttributeCollection();

    private static int nextSequenceNr = 0;
    
    private Writer errorOutput;                 // Destination for error messages
    private Stripper stripper;                  // manages list of elements to be stripped
    private PreviewManager previewManager = null;
    private boolean discardComments;            // true if comments and PIs should be ignored

    private final static Vector emptyVector = new Vector(0);

    private ParentNodeImpl currentNode;
    private DocumentImpl currentDocument;
    private XMLReader parser;                   // SAX2-compliant XML parser
    private ErrorHandler errorHandler;          // SAX-compliant XML error handler
    private Locator locator = this;             // SAX-compliant locator
    private String baseURI = null;
    private NodeFactory nodeFactory;
    private NamePool namePool = new NamePool();
    private String[] namespaces = new String[20];
    private int namespacesUsed = 0;
    private int[] size = new int[100];          // stack of number of children for each open node
    private int depth = 0;
    private Vector arrays = new Vector();       // reusable arrays for creating nodes
    private boolean previousText;
    private int nodeSequenceNr;
    protected boolean failed = false;
    private boolean started = false;
    private StringBuffer charBuffer;
    private int estimatedLength;
    private boolean inDTD = false;
    private boolean lineNumbering = false;

    private long startTime;

    /**
    * create a Builder and initialise variables
    */

    public Builder() {
        //documentPool = new Hashtable();        
        discardComments = false;
        parser = null;
        errorHandler = this;
        errorOutput = new PrintWriter(System.err);
        nodeFactory = new DefaultNodeFactory();
        synchronized(Boolean.TRUE) {                    
            nodeSequenceNr = nextSequenceNr;
        }         
    }

    /////////////////////////////////////////////////////////////////////////
    // Methods concerned with building the tree
    /////////////////////////////////////////////////////////////////////////

    /**
    * Set the SAX-compliant parser to use. If no parser is specified, the
    * parser is obtained from the ParserManager.properties file.
    * @param parser The XML parser to use. This must be an instance of a class
    * that implements the org.xml.sax.Parser interface.
    * @see ParserManager
    */

    public void setParser(Parser parser) {
        this.parser = new ParserAdapter(parser);
    }

    /**
    * Set the SAX2-compliant parser to use. If no parser is specified, the
    * parser is obtained from the ParserManager.properties file.
    * @param parser The XML parser to use. This must be an instance of a class
    * that implements the org.xml.sax.XMLReader interface.
    * @see ParserManager
    */

    public void setXMLReader(XMLReader parser) {
        this.parser = parser;
    }

    /**
    * Get the SAX2 parser in use.
    */

    public XMLReader getXMLReader() {
        return parser;
    }

    /**
    * Set line numbering on or off
    */

    public void setLineNumbering(boolean onOrOff) {
        lineNumbering = onOrOff;
    }

    /**
    * Set the Stripper to use
    */

    public void setStripper(Stripper s) {
        stripper = s;
    }


    /**
    * Request stripping of all whitespace text nodes. This is a simple shortcut to
    * avoid allocating a separate Stripper object
    */

    public void setStripAll() {
        stripper = new AllElementsStripper();
    }

    /**
    * Set the PreviewManager to use
    */

    public void setPreviewManager(PreviewManager pm) {
        previewManager = pm;
    }


    /**
    * Indicate whether comments and Processing Instructions should be discarded
    * @params discard true if comments and PIs are to be discarded, false if
    * they are to be added to the tree
    */

    public void setDiscardCommentsAndPIs(boolean discard) {
        discardComments = discard;
    }

    /**
    * Set the error handler to use. If none is specified, SAXON supplies its own,
    * which writes error messages to the selected error output writer.
    * @param eh The error handler to use. It must conform to the interface
    * org.xml.sax.ErrorHandler
    */

    public void setErrorHandler(ErrorHandler eh) {
        this.errorHandler = eh;
    }

    /**
    * Set output for error messages produced by the default error handler.<BR>
    * The default error handler does not throw an exception
    * for parse errors or input I/O errors, rather it returns a result code and
    * writes diagnostics to a user-specified output writer, which defaults to
    * System.err<BR>
    * This call has no effect if setErrorHandler() has been called to supply a
    * user-defined error handler
    * @param writer The Writer to use for error messages
    */

    public void setErrorOutput(Writer writer) {
        errorOutput = writer;
    }

    /**
    * Set the Node Factory to use. If none is specified, the Builder uses its own.
    */

    public void setNodeFactory(NodeFactory factory) {
        nodeFactory = factory;
    }

    /**
    * Build the tree from an input source. After building the tree, it can
    * be walked as often as required using run(Document doc).
    * @param source The InputSource to use. InputSource is a SAX-defined class that
    * allows input from a URL, a byte stream, or a character stream. SAXON also
    * provides a subclass, ExtendedInputSource, that allows input directly from a File.
    * @return The DocumentInfo object that results from parsing the input.
    * @throws SAXException if the input document could not be read or if it was not parsed
    * correctly.
    */

    public DocumentImpl build(InputSource in) throws SAXException
    {
        // System.err.println("Builder " + this + " build using parser " + parser);
        failed = true;  // until startDocument() called
        started = false;
        String uri = in.getSystemId();
        baseURI = uri;

        if (in instanceof ExtendedInputSource) {
            estimatedLength = ((ExtendedInputSource)in).getEstimatedLength();
            if (estimatedLength < 1) estimatedLength = 4096;
            if (estimatedLength > 1000000) estimatedLength = 1000000;
        } else {
            estimatedLength = 4096;
        }

        // parse the document

        if (parser==null) {
            parser = ParserManager.makeParser();
        }
        
        try {
            if (stripper!=null && !stripper.getPreserveAll()) {
                parser.setContentHandler(stripper);
                stripper.setNextHandler(this);
                if (!discardComments) {
                    parser.setProperty("http://xml.org/sax/properties/lexical-handler", stripper);
                }
            } else {
                parser.setContentHandler(this);
                if (!discardComments) {
                    parser.setProperty("http://xml.org/sax/properties/lexical-handler", this);
                }
            }
        } catch (SAXNotSupportedException err) {    // this just means we won't see the comments
        } catch (SAXNotRecognizedException err) {
        }
        
        parser.setDTDHandler(this);
        parser.setErrorHandler(errorHandler);
        
        
        try {
            parser.parse(in);
        } catch (java.io.IOException err) {
            throw new SAXException(err);
        }

        if (!started) {
            // System.err.println("Builder " + this + " failed");
            throw new SAXException("Source document not supplied");
        }

        if (failed) {
            // System.err.println("Builder " + this + " failed");
            throw new SAXException("XML Parsing failed");
        }

        return currentDocument;
    }

    /**
    * Get the current document
    * @return the document that has been most recently built using this builder
    */

    public DocumentImpl getCurrentDocument() {
        return currentDocument;
    }

  ////////////////////////////////////////////////////////////////////////////////////////
  // Implement the org.xml.sax.ContentHandler interface.
  ////////////////////////////////////////////////////////////////////////////////////////

    /**
    * Callback interface for SAX: not for application use
    */

    public void startDocument () throws SAXException
    {
        // System.err.println("Builder: " + this + " Start document");
        failed = false;
        started = true;
        currentDocument = new DocumentImpl();
        currentDocument.setSystemId(baseURI);
        currentNode = currentDocument;
        depth = 0;
        size[depth] = 0;
        previousText = false;
        currentDocument.sequence = nodeSequenceNr++;
        charBuffer = new StringBuffer(estimatedLength);
        currentDocument.setCharacterBuffer(charBuffer);
        if (lineNumbering) {
            currentDocument.setLineNumbering();
        }
        //startTime = (new Date()).getTime();
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endDocument () throws SAXException 
    {
        // System.err.println("Builder: " + this + " End document");
        currentNode.compact(size[depth]);
        currentNode = null;
        previousText = false;
        
        // allocate sequence number for the next document to be built
        synchronized(Boolean.TRUE) {                    
            nextSequenceNr = nodeSequenceNr + 1;
        }

        // we're not going to use this Builder again so give the garbage collector
        // something to play with
        arrays = null;

        //long endTime = (new Date()).getTime();
        //System.err.println("Build time: " + (endTime-startTime) + " milliseconds");

    }

    /**
    * Callback interface for SAX: not for application use
    */
    
    public void setDocumentLocator (Locator locator)
    {
        this.locator = locator;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (namespaces.length < namespacesUsed + 2) {
            String[] temp = new String[namespaces.length * 2];
            System.arraycopy(namespaces, 0, temp, 0, namespacesUsed);
            namespaces = temp;
        }            
        namespaces[namespacesUsed++] = prefix;
        namespaces[namespacesUsed++] = uri;
    }

    public void endPrefixMapping(String prefix) {}

    /**
    * Callback interface for SAX: not for application use
    */

    public void startElement (
        String uri, String localname, String rawname, Attributes attributes) throws SAXException
    {
        // System.err.println("Start element (" + uri + ", " + localname + ", " + rawname + ")");

        // Construct element name as a Name object
        
        String prefix = rawNameToPrefix(rawname);
        String lname = localname.intern();
        if (lname=="") {
            lname = rawNameToLocalName(rawname);
        }
        Name elementName = namePool.allocate(prefix, uri.intern(), lname);

        // Convert SAX2 Attributes object into an AttributeCollection
                    // the difference is historic, both classes perform the same function
                    // and could be combined.

        AttributeCollection atts;
        int numAtts = attributes.getLength();
        if (numAtts==0) {
            atts = emptyAttributeCollection;
        } else {
            atts = new AttributeCollection(numAtts);
            for (int i=0; i<numAtts; i++) {
                String aprefix = rawNameToPrefix(attributes.getQName(i));
                String alocalname = attributes.getLocalName(i);
                String auri = attributes.getURI(i);
                String atype = attributes.getType(i);
                String avalue = attributes.getValue(i);

                // System.err.println("Attribute: " + aprefix + ":" + alocalname + "(" + atype + ")=" + avalue);

                atts.addAttribute(aprefix, auri, alocalname, atype, avalue);
            }
        }

        ElementImpl elem = nodeFactory.makeElementNode( currentNode,
                                                        elementName,
                                                        atts,
                                                        namespaces,
                                                        namespacesUsed,
                                                        locator,
                                                        nodeSequenceNr++ );
                                                        
        // the initial aray used for pointing to children will be discarded when the exact number
        // of children in known. Therefore, it can be reused. So we allocate an initial array from
        // a pool of reusable arrays. A nesting depth of >20 is so rare that we don't bother.

        while (depth >= arrays.size()) {
            arrays.addElement(new NodeImpl[20]);
        }
        elem.useChildrenArray((NodeImpl[])arrays.elementAt(depth));

        currentNode.addChild(elem, size[depth]++);
        if (depth >= size.length - 1) {
            int[] newsize = new int[size.length * 2];
            System.arraycopy(size, 0, newsize, 0, size.length);
            size = newsize;
        }
        size[++depth] = 0;
           	
        namespacesUsed = 0;
        
    	if (currentNode instanceof DocumentInfo) {
    	    ((DocumentImpl)currentNode).setDocumentElement(elem);
    	}


        currentNode = elem;
        previousText = false;        
    }

    /**
    * Extract the prefix from a QName
    * @return the prefix as an interned string
    */

    private String rawNameToPrefix(String rawname) {
        int colon = rawname.indexOf(':');
        if (colon<0) {
            return "";
        } else {
            return rawname.substring(0, colon).intern();
        }
    }

    /**
    * Extract the local name from a QName
    * @return the local name as an interned string    
    */

    private String rawNameToLocalName(String rawname) {
        int colon = rawname.indexOf(':');
        if (colon<0) {
            return rawname.intern();
        } else {
            return rawname.substring(colon+1).intern();
        }
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void endElement (String uri, String localname, String rawname) throws SAXException
    {
        // System.err.println("End element " + rawname);
        currentNode.compact(size[depth]);

        // if a preview handler is registered, call it now
        if (previewManager != null) {
            
            if (previewManager.isPreviewElement(currentNode.getExpandedName())) {
                Controller c = previewManager.getController();
                Context context = c.makeContext(currentNode);
                c.applyTemplates(
                    context,
                    new SingletonNodeSet(currentNode),
                    c.getRuleManager().getMode(previewManager.getPreviewMode()),
                    null);
                currentNode.dropChildren();
            }
        }
        
        depth--;
        currentNode = (ParentNodeImpl)currentNode.getParentNode();
        previousText = false;        
    }

    /**
    * Callback interface for SAX: not for application use
    */

    public void characters (char ch[], int start, int length) throws SAXException
    {
        // System.err.println("Characters: " + new String(ch, start, length));
        if (length>0) {
            int bufferStart = charBuffer.length();
            charBuffer.append(ch, start, length);
               
            if (previousText) {
                // normalize adjacent text nodes (only necessary if no Stripper is in use)
                TextImpl prev = (TextImpl)currentNode.getAllChildNodes()[size[depth]-1];
                prev.increaseLength(length);
            } else {            
                TextImpl n = new TextImpl(currentDocument, bufferStart, length);
                currentNode.addChild(n, size[depth]++);
                previousText = true;
            }
        }
    }

    /**
    * Callback interface for SAX: not for application use
    */
 
    public void ignorableWhitespace (char ch[], int start, int length) throws SAXException
    {
        characters(ch, start, length);
    }

    
    /**
    * Callback interface for SAX: not for application use<BR>
    * Note: because SAX1 does not deliver comment nodes, we get these in the form of a processing
    * instruction with a null name. This requires a specially-adapted SAX driver.
    */

    public void processingInstruction (String name, String remainder)
    {
        if (!discardComments) {
            if (name==null) {
                // name==null in some modified SAX1 parsers means it's really a comment.
                CommentImpl comment = new CommentImpl(remainder);
                currentNode.addChild(comment, size[depth]++);
            } else {
                ProcInstImpl pi = new ProcInstImpl(name.intern(), remainder);
                currentNode.addChild(pi, size[depth]++);
                if (locator!=null) {
                    pi.setLocation(locator.getSystemId(), locator.getLineNumber());
                }
            }
            previousText = false;
        }
    }

    /**
    * Callback interface for SAX (part of LexicalHandler interface): not for application use
    */
 
    public void comment (char ch[], int start, int length) throws SAXException
    {
        if (!discardComments && !inDTD) {
            CommentImpl comment = new CommentImpl(new String(ch, start, length));
            currentNode.addChild(comment, size[depth]++);
            previousText = false;
        }
    }

    // Methods from lexical handler interface

    public void skippedEntity(String name) throws SAXException {}

    public void startDTD (String name, String publicId, String systemId)
	throws SAXException {
	    inDTD = true;
	}

    public void endDTD () throws SAXException {
        inDTD = false;
    }

    public void startEntity (String name) throws SAXException {};

    public void endEntity (String name)	throws SAXException {};

    public void startCDATA () throws SAXException {};

    public void endCDATA ()	throws SAXException {};

    /**
    * graftElement() allows an element node to be transferred from one tree to another.
    * This is a dangerous internal interface which is used only to contruct a stylesheet
    * tree from a stylesheet using the "literal result element as stylesheet" syntax.
    * The supplied element is grafted onto the current element as its only child.
    */

    public void graftElement(ElementImpl element) throws SAXException {
        currentNode.addChild(element, size[depth]++);
    }


    ////////////////////////////////////////////////////////////////////////////
    // Implement the org.xml.sax.ErrorHandler interface.
    // The user can supply an alternative implementation using setErrorHandler()
    ////////////////////////////////////////////////////////////////////////////

    /**
    * Callback interface for SAX: not for application use
    */

    public void warning (SAXParseException e) 
    {}

    /**
    * Callback interface for SAX: not for application use
    */

    public void error (SAXParseException e) throws SAXException {
        reportError(e, false);
        failed = true;
        //throw e;        // added 14 Mar 2000 to stop after namespace problems
    }

    /**
    * Callback interface for SAX: not for application use
    */
    
    public void fatalError (SAXParseException e) throws SAXException {
        reportError(e, true);
        failed = true;
        throw e;
    }

    /**
    * Common routine for errors and fatal errors
    */

    private void reportError (SAXParseException e, boolean isFatal) {
        try {
            String errcat = (isFatal ? "Fatal error" : "Error");
            errorOutput.write(errcat + " reported by XML parser: " + e.getMessage() + "\n");
            errorOutput.write("  URL:    " + e.getSystemId() + "\n");
            errorOutput.write("  Line:   " + e.getLineNumber() + "\n");
            errorOutput.write("  Column: " + e.getColumnNumber() + "\n");
            errorOutput.flush();
        } catch (Exception e2) {
            System.err.println(e);
            System.err.println(e2);
            e2.printStackTrace();
        };
    }    

    //////////////////////////////////////////////////////////////////////////////
    // Implement DTDHandler interface
    //////////////////////////////////////////////////////////////////////////////


    public void notationDecl(       String name,
                                    String publicId,
                                    String systemId) throws SAXException
    {}


    public void unparsedEntityDecl( String name,
                                    String publicId,
                                    String systemId,
                                    String notationName) throws SAXException
    {
        //System.err.println("Unparsed entity " + name + "=" + systemId);

        // Some SAX parsers report the systemId as written. We need to turn it into
        // an absolute URL.

        String uri = systemId;
        if (locator!=null) {
            try {
                String baseURI = locator.getSystemId();
                URL absoluteURI = new URL(new URL(baseURI), systemId);
                uri = absoluteURI.toString();
            } catch (Exception err) {}
        }
        currentDocument.setUnparsedEntity(name, uri);
    }

    //////////////////////////////////////////////////////////////////////////////
    // Implement Locator interface (default implementation)
    //////////////////////////////////////////////////////////////////////////////

    public void setSystemId(String uri) {
        baseURI = uri;
    }

    public String getSystemId() {
        return baseURI;
    }

    public String getPublicId() {
        return null;
    }

    public int getLineNumber() {
        return -1;
    }

    public int getColumnNumber() {
        return -1;
    }
    

    //////////////////////////////////////////////////////////////////////////////
    // Inner class DefaultNodeFactory. This creates the nodes in the tree.
    // It can be overridden, e.g. when building the stylesheet tree
    //////////////////////////////////////////////////////////////////////////////

    private class DefaultNodeFactory implements NodeFactory {

        public ElementImpl makeElementNode(
                NodeInfo parent,
                Name name,
                AttributeCollection attlist,
                String[] namespaces,
                int namespacesUsed,
                Locator locator,
                int sequenceNumber)
                    throws SAXException
        {
            if (attlist.getLength()==0 && namespacesUsed==0) {
                
                // for economy, use a simple ElementImpl node

                ElementImpl e = new ElementImpl();
                String baseURI = null;
                int lineNumber = -1;

                if (locator!=null) {
                    baseURI = locator.getSystemId();
                    lineNumber = locator.getLineNumber();
                }

                e.initialise(name, attlist, parent, baseURI, lineNumber, sequenceNumber);

                return e;

            } else {
                ElementWithAttributes e = new ElementWithAttributes();
                String baseURI = null;
                int lineNumber = -1;

                if (locator!=null) {
                    baseURI = locator.getSystemId();
                    lineNumber = locator.getLineNumber();
                }
            
                e.setNamespaceDeclarations(namespaces, namespacesUsed);

                e.initialise(name, attlist, parent, baseURI, lineNumber, sequenceNumber);

                return e;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // Inner class AllElementsStripper
    // A Stripper that removes all whitespace text nodes.
    ////////////////////////////////////////////////////////////////////////////////////


    private class AllElementsStripper extends Stripper {

        /**
        * Decide whether an element is in the set of white-space preserving element types
        * @param name The element name
        * @return true if the element is in the set of white-space preserving element types
        */

        public boolean isSpacePreserving(Name name) {
            return false;
        }

        /**
        * Return true if all whitespace nodes are to be preserved
        */

        public boolean getPreserveAll() {
            return false;
        }
    }


}   // end of outer class Builder

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
