// Transformations for XML (TRaX)
// Copyright ©2000 Lotus Development Corporation, Exoffice Technologies,
// Oracle Corporation, Michael Kay of International Computers Limited, Apache
// Software Foundation.  All rights reserved.
package com.icl.saxon.trax;

import org.xml.sax.SAXException;


/**
 * This exception serves as a root exception of TRaX exception, and 
 * is thrown in raw form when an exceptional condition occurs in the 
 * Processor object.
 *
 * <h3>Open issues:</h3>
 * <dl>
 *    <dt><h4>Abstract exception root?</h4></dt>
 *    <dd>Should the root TRaX exception be abstract?</dd>
 *    <dt><h4>Derive from SAXException?</h4></dt>
 *    <dd>Keith Visco writes: I don't think these exceptions should extend  
 *        SAXException, but could nest a SAXException if necessary.</dd>
 * </dl>
 *
 * MHK: In the Saxon version of this class, I have changed it to derive from
 * SAXException rather than SAXParseException, because in most situations
 * no Locator is available. 
 * 
 * @version Alpha
 * @author <a href="mailto:scott_boag@lotus.com">Scott Boag</a>
 */
 
public class ProcessorException extends SAXException
{
  //////////////////////////////////////////////////////////////////////
  // Constructors.
  //////////////////////////////////////////////////////////////////////
    

  
  /**
   * Wrap an existing exception in a ProcessorException.
   *
   * <p>This is used for throwing processor exceptions before 
   * the processing has started.</p>
   *
   * @param message The error or warning message, or null to
   *                use the message from the embedded exception.
   * @param e Any exception
   * @see org.xml.sax.Locator
   * @see org.xml.sax.Parser#setLocale
   */
  public ProcessorException (String message, Exception e) 
  {
    super( message, e);
  }
  
  //////////////////////////////////////////////////////////////////////
  // Constructors added by MHK
  //////////////////////////////////////////////////////////////////////
    
  /**
   * Create a new ProcessorException from a message.
   *
   * <p>This constructor is especially useful when an application detects
   * an error directly.</p>
   *
   * @param message The error or warning message.
   * @see org.xml.sax.Locator
   * @see org.xml.sax.Parser#setLocale 
   */
  public ProcessorException (String message) 
  {
    super(message);
  }

  /**
   * Create a new ProcessorException to wrap a given exception.
   *
   * <p>This constructor is especially useful when an application receives
   * an exception from an underlying interface, e.g. an IO exception</p>
   *
   * @param exception The exception to be wrapped.
   * @see org.xml.sax.Locator
   * @see org.xml.sax.Parser#setLocale 
   */
  public ProcessorException (Exception err) 
  {
    super(err.getMessage(), err);
  }

}
