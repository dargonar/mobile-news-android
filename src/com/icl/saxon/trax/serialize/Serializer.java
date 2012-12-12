package com.icl.saxon.trax.serialize;



import java.io.Writer;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ContentHandler;


/**
 * Interface to a serializer implementation. A serializer is used for
 * serializing a document through one of three interfaces. The serializer
 * object serves as an anchor point for setting the output stream and
 * output format, for obtaining any of these interfaces, and for resetting
 * the serializer.
 * <p>
 * A serializer is created from {@link SerializerFactory}.
 * <p>
 * Prior to using the serializer, the output format and output stream
 * or writer should be set. The serializer is then used in one of
 * three ways:
 * <ul>
 * <li>To serialize SAX 1 events call {@link #asDocumentHandler}
 * <li>To serialize SAX 2 events call {@link #asContentHandler}
 * <li>To serialize a DOM document call {@link #asDOMSerializer}
 *     (see {@link DOMSerializer})
 * </ul>
 * <p>
 * The serializer may be reused with the same output format and output
 * stream, or different output format and output stream, by calling
 * the {@link #reset} method after completing serialization. The
 * document handler or {@link DOMSerializer} must be re-acquired after
 * a successful return from {@link #reset}.
 * <p>
 * A serializer is not thread safe. Only one thread should call the
 * <tt>asXXX</tt> methods and use the returned document handler, or
 * DOM serializer.
 * <p>
 * Example:
 * <pre>
 * ser = SerializerFactory.getSerializer( Method.XML );
 * emptyDoc( ser, System.out );
 * emptyDoc( ser, System.err );
 * . . . 
 *
 * void emptyDoc( Serializer ser, OutputStream os )
 * {
 *   ser.setOutputStream( os );
 *   ser.asDocumentHandler().startDocument();
 *   ser.asDocumentHandler().startElement( "empty", new AttributeListImpl() );
 *   ser.asDocumentHandler().endElement( "empty" );
 *   ser.asDocumentHandler().endDocument();
 *   ser.reset();
 * }
 * </pre>
 *
 * @version Alpha
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @author <a href="mailto:Scott_Boag/CAM/Lotus@lotus.com">Scott Boag</a>
 */
public interface Serializer
{


    /**
     * Specifies an output stream to which the document should be
     * serialized. This method should not be called while the
     * serializer is in the process of serializing a document.
     * <p>
     * The encoding specified in the {@link OutputFormat} is used, or
     * if no encoding was specified, the default for the selected
     * output method.
     *
     * @param output The output stream
     * @param UnsupportedEncodingException The encoding specified in
     *  the output format is not supported
     */
    public void setOutputStream( OutputStream output )
      throws UnsupportedEncodingException;


    /**
     * Specifies a writer to which the document should be serialized.
     * This method should not be called while the serializer is in
     * the process of serializing a document.
     * <p>
     * The encoding specified for the {@link OutputFormat} must be
     * identical to the output format used with the writer.
     *
     * @param writer The output writer stream
     */
    public void setWriter( Writer writer );


    /**
     * Specifies an output format for this serializer. It the
     * serializer has already been associated with an output format,
     * it will switch to the new format. This method should not be
     * called while the serializer is in the process of serializing
     * a document.
     *
     * @param format The output format to use
     */
    public void setOutputFormat( OutputFormat format );


    /**
     * Returns the output format for this serializer.
     *
     * @return The output format in use
     */
    public OutputFormat getOutputFormat();


    /**
     * Return a {@link DocumentHandler} interface into this serializer.
     * If the serializer does not support the {@link DocumentHandler}
     * interface, it should return null.
     */
    public DocumentHandler asDocumentHandler();


    /**
     * Return a {@link ContentHandler} interface into this serializer.
     * If the serializer does not support the {@link ContentHandler}
     * interface, it should return null.
     */
    public ContentHandler asContentHandler();


    /**
     * Return a {@link DOMSerializer} interface into this serializer.
     * If the serializer does not support the {@link DOMSerializer}
     * interface, it should return null.
     */
    public DOMSerializer asDOMSerializer();


    /**
     * Resets the serializer. If this method returns true, the
     * serializer may be used for subsequent serialization of new
     * documents. It is possible to change the output format and
     * output stream prior to serializing, or to use the existing
     * output format and output stream.
     *
     * @return True if serializer has been reset and can be reused
     */
    public boolean reset();


}





