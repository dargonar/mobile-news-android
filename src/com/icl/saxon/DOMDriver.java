package com.icl.saxon;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Builder;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Enumeration;
import java.util.Locale;

import org.w3c.dom.*;

/**
* DOMDriver.java: (pseudo-)SAX driver for DOM.<BR>
* This class simulates the action of a SAX Parser, taking an already-constructed
* DOM Document and walking around it in a depth-first traversal,
* calling a SAX-compliant ContentHandler to process the children as it does so.
* @author MHK, 5 Jun 1998
* @version 20 Jan 1999 modified to use AttributeListWrapper class
* @version 3 February 2000 modified to use AttributeCollection class
* @version 24 February 2000 modified to drive SAX2, which means it has to do namespace handling
*/

public class DOMDriver implements Locator, XMLReader
{

    private ContentHandler contentHandler = new DefaultHandler();
    private LexicalHandler lexicalHandler = null;
    private NamespaceSupport nsSupport = new NamespaceSupport();
    private AttributesImpl attlist = new AttributesImpl();
    private String[] parts = new String[3];
    private String[] elparts = new String[3];
    private Node root = null;
    
    /**
    * Set the content handler.
    * @param handler The object to receive content events. If this also implements LexicalHandler,
    * it will also be notified of comments.
    */
    
    public void setContentHandler (ContentHandler handler) 
    {
        this.contentHandler = handler;
        if (handler instanceof LexicalHandler) {
            lexicalHandler = (LexicalHandler)handler;
        }
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
    * <b>SAX1</b>: Sets the locale used for diagnostics; currently,
    * only locales using the English language are supported.
    * @param locale The locale for which diagnostics will be generated
    */
    
    public void setLocale (Locale locale) throws SAXException
    {}


    /**
     * <b>SAX2</b>: Returns the object used when resolving external
     * entities during parsing (both general and parameter entities).
     */
     
    public EntityResolver getEntityResolver ()
    {
	    return null;
    }

    /**
     * <b>SAX1, SAX2</b>: Set the entity resolver for this parser.
     * @param handler The object to receive entity events.
     */
     
    public void setEntityResolver (EntityResolver resolver) {}


    /**
     * <b>SAX2</b>: Returns the object used to process declarations related
     * to notations and unparsed entities.
     */
     
    public DTDHandler getDTDHandler () {
        return null;
    }

    /**
     * <b>SAX1, SAX2</b>: Set the DTD handler for this parser.
     * @param handler The object to receive DTD events.
     */
    public void setDTDHandler (DTDHandler handler) {}


    /**
     * <b>SAX1</b>: Set the document handler for this parser.  If a
     * content handler was set, this document handler will supplant it.
     * The parser is set to report all XML 1.0 names rather than to
     * filter out "xmlns" attributes (the "namespace-prefixes" feature
     * is set to true).
     *
     * @deprecated SAX2 programs should use the XMLReader interface
     *	and a ContentHandler.
     *
     * @param handler The object to receive document events.
     */
     
    public void setDocumentHandler (DocumentHandler handler) {}

    /**
     * <b>SAX1, SAX2</b>: Set the error handler for this parser.
     * @param handler The object to receive error events.
     */
     
    public void setErrorHandler (ErrorHandler handler) {}

    /**
     * <b>SAX2</b>: Returns the object used to receive callbacks for XML
     * errors of all levels (fatal, nonfatal, warning); this is never null;
     */
     
    public ErrorHandler getErrorHandler () { return null; }


    /**
    * Set the DOM Document that will be walked
    */

    public void setDocument(Document doc) {
        root = doc;
    }

    /**
    * Set the start Node to be walked
    * @param node The node whose subtree is to be traversed. Currently, this must be a Document
    * node.
    */

    public void setStartNode(Node node) throws SAXException {

        // We currently don't allow any start node other than the Document node, because
        // of complications with namespaces
        
        if (!(node instanceof Document)) {
            throw new SAXException("DOM input must be an entire Document");
        }
        root = node;
    }

    /**
    * Parse from InputSource.
    * The InputSource is ignored; it's there only to satisfy the XMLReader interface
    */

    public void parse(InputSource source) throws SAXException {
        parse();
    };

    /**
    * Parse from SystemId.
    * The SystemId is ignored; it's there only to satisfy the XMLReader interface
    */

    public void parse(String source) throws SAXException {
        parse();
    };

    /**
    * Walk a document (traversing the nodes depth first)
    * @param doc The (DOM) Document object to walk.
    * @exception SAXException On any error in the document
    */

    public void parse() throws SAXException
    {
        if (root==null) {
            throw new SAXException("DOMDriver: no start node defined");
        }
        if (contentHandler==null) {
            throw new SAXException("DOMDriver: no content handler defined");
        }

        contentHandler.setDocumentLocator(this);
        contentHandler.startDocument();
        walkNode(root);                         // walk the root node
        contentHandler.endDocument();
    }
    
  /**
    * Walk an element of a document (traversing the children depth first)
    * @param elem The DOM Element object to walk
    * @exception SAXException On any error in the document
    * 
    */
    
    private void walkNode (Node node) throws SAXException
    {        
        if (node.hasChildNodes()) {
            NodeList nit = node.getChildNodes();
            for (int i=0; i<nit.getLength(); i++) {
                Node child = nit.item(i);
                switch (child.getNodeType()) {
                    case Node.DOCUMENT_NODE:
                        break;                  // should not happen
                    case Node.ELEMENT_NODE:
                        Element element = (Element)child;
                        nsSupport.pushContext();
                        attlist.clear();   
                        NamedNodeMap atts = element.getAttributes();
                        for (int a1=0; a1<atts.getLength(); a1++) {
                            Attr att = (Attr)atts.item(a1);
                            String attname = att.getName();
                            if (attname.equals("xmlns")) {
                                nsSupport.declarePrefix("", att.getValue());
                                contentHandler.startPrefixMapping("", att.getValue());
                            } else if (attname.startsWith("xmlns:")) {
                                nsSupport.declarePrefix(attname.substring(6), att.getValue());
                                contentHandler.startPrefixMapping(attname.substring(6), att.getValue());
                            }
                        }
                        for (int a2=0; a2<atts.getLength(); a2++) {
                            Attr att = (Attr)atts.item(a2);
                            String attname = att.getName(); 
                            if (!attname.equals("xmlns") && !attname.startsWith("xmlns:")) {
                                parts = nsSupport.processName(attname, parts, true);
                                if (parts==null) {
                                    throw new SAXException("Undeclared namespace in " + attname);
                                }
                                attlist.addAttribute(
                                    parts[0], parts[1], parts[2], "CDATA", att.getValue());
                            }
                        }
                        elparts = nsSupport.processName(element.getTagName(), elparts, false);
                        if (elparts==null) {
                            throw new SAXException("Undeclared namespace in " + element.getTagName());
                        }
                        String uri = elparts[0];
                        String local = elparts[1];
                        String raw = elparts[2];

                        contentHandler.startElement(uri, local, raw, attlist);
                        
                        walkNode(element);

                        contentHandler.endElement(uri, local, raw);
                	    Enumeration prefixes = nsSupport.getDeclaredPrefixes();
                	    while (prefixes.hasMoreElements()) {
                    		String prefix = (String)prefixes.nextElement();
                    		contentHandler.endPrefixMapping(prefix);
                	    }
                        nsSupport.popContext();
                        break;
                    case Node.ATTRIBUTE_NODE:        // have already dealt with attributes
                        break;
                    case Node.PROCESSING_INSTRUCTION_NODE:
                        contentHandler.processingInstruction(
                            ((ProcessingInstruction)child).getTarget(),
                            ((ProcessingInstruction)child).getData());
                        break;
                    case Node.COMMENT_NODE:
                        if (lexicalHandler!=null) {
                            String text = ((Comment)child).getData();
                            lexicalHandler.comment(text.toCharArray(), 0, text.length());
                        }
                        break;    
                    case Node.TEXT_NODE:
                        String text = ((CharacterData)child).getData();
                        contentHandler.characters(text.toCharArray(), 0, text.length());
                        break;
                    default:
                        break;                  // should not happen
                }
            }
        }
        
    }                   
    
    //
    // Implementation of org.xml.sax.Locator.
    //

    public String getPublicId ()
    {
        return null;		// TODO
    }

    public String getSystemId ()
    {
        return null;
    }

    public int getLineNumber ()
    {
        return -1;
    }

    public int getColumnNumber ()
    {
        return -1;
    }

    // Features and properties

    static final String	FEATURE = "http://xml.org/sax/features/";
    static final String	HANDLER = "http://xml.org/sax/properties/";

    /**
     * <b>SAX2</b>: Tells the value of the specified feature flag.
     *
     * @exception SAXNotRecognizedException thrown if the feature flag
     *	is neither built in, nor yet assigned.
     */
     
    public boolean getFeature (String featureId) throws SAXNotRecognizedException
    {
	    if ((FEATURE + "validation").equals (featureId))
	        return false;

	    // external entities (both types) are currently always excluded
	    if ((FEATURE + "external-general-entities").equals (featureId)
		        || (FEATURE + "external-parameter-entities").equals (featureId))
	    return false;

	    // element/attribute names are as namespace-sensitive
	    if ((FEATURE + "namespace-prefixes").equals (featureId))
	        return true;

	    // report element/attribute namespaces?
	    if ((FEATURE + "namespaces").equals (featureId))
	        return true;

	    // always interns: no
	    if ((FEATURE + "string-interning").equals (featureId))
	        return false;

	    throw new SAXNotRecognizedException (featureId);
    }

    /**
     * <b>SAX2</b>:  Returns the specified property.
     *
     * @exception SAXNotRecognizedException thrown if the property value
     *	is neither built in, nor yet stored.
     */
     
    public Object getProperty (String name) throws SAXNotRecognizedException {
        if (name.equals("http://xml.org/sax/properties/lexical-handler")) {
            return lexicalHandler;
        } else {
            throw new SAXNotRecognizedException(name);
        }
    }

    /**
     * <b>SAX2</b>:  Sets the state of feature flags in this parser.  Some
     * built-in feature flags are mutable; all flags not built-in are
     * motable.
     */
    public void setFeature (String featureId, boolean state)
    throws SAXNotRecognizedException, SAXNotSupportedException 
    {
	    throw new SAXNotRecognizedException(featureId);
    }    

    /**
     * <b>SAX2</b>:  Assigns the specified property.  Like SAX1 handlers,
     * these may be changed at any time.
     */
     
    public void setProperty (String propertyId, Object property)
    throws SAXNotRecognizedException, SAXNotSupportedException {
        if (propertyId.equals("http://xml.org/sax/properties/lexical-handler")) {
            if (property instanceof LexicalHandler) {
                lexicalHandler = (LexicalHandler)property;     
            } else {
                throw new SAXNotSupportedException(
                    "Lexical Handler must be instance of org.xml.sax.ext.LexicalHandler");
            }
        } else {
            throw new SAXNotRecognizedException(propertyId);
        }
    }

    /**
    * Convert a DOM Document to a SAXON DocumentInfo object
    * @param doc the DOM Document
    * @return the SAXON DocumentInfo
    */

    public static DocumentInfo convert(Document dom) throws SAXException {
        Builder builder = new Builder();
        DOMDriver driver = new DOMDriver();
        driver.setContentHandler(builder);
        driver.setDocument(dom);
        driver.parse();
        return builder.getCurrentDocument();
    }

}
