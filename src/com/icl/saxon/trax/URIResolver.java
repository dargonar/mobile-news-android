// Transformations for XML (TRaX)
// Copyright ©2000 Lotus Development Corporation, Exoffice Technologies,
// Oracle Corporation, Michael Kay of International Computers Limited, Apache
// Software Foundation.  All rights reserved.
package com.icl.saxon.trax;

//import java.io.IOException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.w3c.dom.Node;



/**
 * <p><i>This version of URIResolver reflects the proposal made by Michael Kay to revise
 * the interface as defined in TRAX 0.6.</i></p>
 *
 * <p>An interface that can be called by the processor to for turning the
 * URIs used in document() and xsl:import etc into an InputSource or a 
 * Node if the processor supports the "http://xml.org/trax/features/dom/input" feature.</p>
 *
 * Node that the URIResolver is stateful (it remembers the most recent URI) so separate
 * instances must be used in each thread.
 * 
 * 
 * @version Alpha
 * @author <a href="mailto:scott_boag@lotus.com">Scott Boag</a>
 */
 
public interface URIResolver
{
  /**
   * This will be called by the processor when it encounters 
   * an xsl:include, xsl:import, or document() function.
   * 
   * @param base The base URI that should be used.
   * @param uri Value from an xsl:import or xsl:include's href attribute, 
   * or a URI specified in the document() function.
   */
   
  public void setURI (String base, String uri)
    throws TransformException;

  /**
   * Get the absolute URI. This method must not be called
   * unless setURI() has been called first.
   */
   
  public String getURI ();


  /**
   * This will be called by the processor when it encounters 
   * an xsl:include, xsl:import, or document() function, if it needs 
   * a DOM tree. The URIResolver must be prepared to return either a
   * DOM tree, or a SAX InputSource, or both. This method must not be called
   * unless setURI() has been called first.
   * 
   * @returns a DOM node that represents the resolution of the URI to a tree, if the
   * URI resolver is capable of returning a DOM Node; or null otherwise
   */
   
  public Node getDOMNode () throws TransformException;

  /**
   * This will be called by the processor when it encounters 
   * an xsl:include, xsl:import, or document() function, if it needs 
   * a SAX InputSource. The URIResolver must be prepared to return either a
   * DOM tree, or a SAX InputSource, or both. This method must not be called
   * unless setURI() has been called first.
   * 
   * @returns a SAX InputSource that represents the resolution of the URI, if the
   * URI resolver is capable of returning a SAX InputSourcee; or null otherwise
   */
   
  public InputSource getInputSource () throws TransformException;

  /**
  * This method returns the SAX2 parser to use with the InputSource obtained from this URI.
  * It may return null if any SAX2-conformant XML parser can be used,
  * or if getInputSource() will also return null. The parser must be free for use (i.e.
  * not currently in use for another parse().
  */

  public XMLReader getXMLReader() throws TransformException;

    
}
