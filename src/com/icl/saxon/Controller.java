package com.icl.saxon;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.tree.Builder;
import com.icl.saxon.tree.Stripper;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import com.icl.saxon.trace.*;  // e.g.
import com.icl.saxon.style.XSLStyleSheet;
import com.icl.saxon.trax.*;
import com.icl.saxon.trax.serialize.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.*;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.*;
import java.io.*;

import com.icl.saxon.trax.URIResolver;


/**
  * <B>Controller</B> processes an XML file, calling registered node handlers
  * when appropriate to process its elements, character content, and attributes. <P>
  * @version 10 December 1999: methods for building the tree extracted to class Builder,
  * methods for maintaining rulesets extracted to RuleManager.<p>
  * The Controller class now incorporates the previous <b>StylesheetInstance</b> class.
  * A StyleSheetInstance represents a single execution of a prepared stylesheet.
  * A PreparedStyleSheet can be used any number of times, in series or in parallel,
  * but each use of it to render a source document requires a separate Controller
  * object, which is not reusable or shareable.
  * @author Michael H. Kay (Michael.Kay@icl.com)
  */
  
public class Controller implements com.icl.saxon.trax.Transformer {

    // Policies for handling recoverable errors

    public final static int RECOVER_SILENTLY = 0;
    public final static int RECOVER_WITH_WARNINGS = 1;
    public final static int DO_NOT_RECOVER = 2;

    private Bindery bindery;                // holds values of global and local variables
    private ContentHandler inputContentHandler = null;
    private ContentHandler outputContentHandler = null;
    private Context defaultContext;
    private DecimalFormatManager decimalFormatManager;
    private Emitter messageEmitter;
    private LexicalHandler lexicalHandler = null;
    private RuleManager ruleManager;
    private OutputDetails output;
    private OutputManager outputManager;
    private ParameterSet parameters;
    private PreparedStyleSheet preparedStyleSheet;    
    private TraceListener traceListener; // e.g.
    private URIResolver uriResolver;
    private XMLReader sourceParser;
    private XSLStyleSheet styleSheetElement;
    private int recoveryPolicy = RECOVER_WITH_WARNINGS;
    private boolean outputterIsInitialized = false;
    
    private Hashtable sourceDocumentPool;
    private Hashtable userDataTable;
    private boolean lineNumbering;
    private boolean preview;
    private SAXException transformFailure = null;
    private String diagnosticName = "";
    
    /**
    * create a Controller and initialise variables
    */

    public Controller() {

        bindery = new Bindery();
        outputManager = new OutputManager();
        uriResolver = new StandardURIResolver();
        sourceDocumentPool = new Hashtable();
        userDataTable = new Hashtable();
        
        defaultContext = makeContext(new DocumentImpl());   // this context is used only for binding variables,
                                                            // the document is a dummy

    }

    /**
    * Set a diagnostic name for this transformation (accessible through toString())
    */

    public void setDiagnosticName(String name) {
        diagnosticName = name;
    }

    public String toString() {
        return diagnosticName;
    }

    //////////////////////////////////////////////////////////////////////////
    // Methods to process the tree
    //////////////////////////////////////////////////////////////////////////


    /**
    * Process a Document.<p>
    * This method is intended for use when performing a pure Java transformation,
    * without a stylesheet. Where there is an XSLT stylesheet, use transformDocument()
    * or transform() instead: those methods set up information from the stylesheet before calling
    * run(). <p>
    * The process starts by calling the registered node
    * handler to process the document root object. Note that the same document can be processed
    * any number of times, typically with different node handlers for each pass. The Document
    * will typically have been built using com.icl.saxon.tree.Builder.<p>
    */
    
    public void run(DocumentInfo doc) throws SAXException
    {
        initializeOutputter();
        Context initialContext = makeContext(doc);
        applyTemplates(
            initialContext,
            new SingletonNodeSet(doc),
            getRuleManager().getMode(null),
            null);
    }

    /**
    * Visit a node, calling the registered node handler to process it.
    * What happens next depends on the node handler: for example, child elements will
    * be processed if the element handler calls applyTemplates().
    * Note that the same document can be processed
    * any number of times, changing element handlers if required.
    * @param context Defines the current node (the one to be processed) and the context
    * the context in which the node is to be processed. 
    * @param mode The processing mode, used to select the appropriate element handler
    */

    private void visit(Context context, Mode mode) throws SAXException {

        // find the node handler for this node

        NodeInfo node = context.getCurrentNode();
        NodeHandler eh = ruleManager.getHandler(node, mode, context);

        context.setMode(mode);

        if (eh==null) {             // use the default action for the node

            node.defaultAction(context);
            
        } else {
       
            if (traceListener!=null) { // e.g.
        	    traceListener.enterSource(eh, context);
         	    eh.start(node, context);
        	    traceListener.leaveSource(eh, context);
        	} else {
         	    eh.start(node, context);
        	}
        }
    }



    /**
    * ApplyTemplates to process selected nodes using the handlers registered for a particular
    * mode.<br>
    * @param select A node-set expression that determines which descendant elements are selected.
    * Note: if the nodes are to be sorted, the select Expression will take care of this. 
    * @param mode A String that names the processing mode. It should match the mode defined when the
    * element handler was registered using setHandler with a mode parameter. Set this parameter to
    * null to invoke the default mode.
    * @param parameters A ParameterSet containing the parameters to the handler/template being invoked.
    * Specify null if there are no parameters. 
    */

    public void applyTemplates(Context c, Expression select, Mode mode, ParameterSet parameters)
            throws SAXException
    {
        // Get an enumerator to iterate through the selected nodes

        NodeEnumeration enuma;
        if (select==null) {
            NodeInfo current = c.getCurrentNode();
            int count = current.getNumberOfChildren();
            if (count==0) return;
            if (count==1) {
                NodeInfo child = (NodeInfo)current.getFirstChild();
                Context context = c.newContext();            
                context.setLast(1);
                bindery.openStackFrame(parameters);
                context.setCurrentNode(child);
                context.setContextNode(child);
                context.setPosition(1);
                visit(context, mode);
                bindery.closeStackFrame();
            } else {                       
                NodeInfo[] children = current.getAllChildNodes();
                Context context = c.newContext();            
                context.setLast(count);
                for(int n=0; n<count; n++) {
                    bindery.openStackFrame(parameters);
                    context.setCurrentNode(children[n]);
                    context.setContextNode(children[n]);
                    context.setPosition(n+1);
                    visit(context, mode);
                    bindery.closeStackFrame();
                }
            }
            
        } else {
            
            NodeSetValue nodes = (NodeSetValue)select.evaluateAsNodeSet(c);
            enuma = nodes.enumerate();
            
            // if the enumerator can't calculate last() position, we wrap it in one that can.

            if (!(enuma instanceof LastPositionFinder)) {
                enuma = new LookaheadEnumerator(enuma);
            }            

            int position = 1;
            Context context = c.newContext();            
            context.setLastPositionFinder((LastPositionFinder)enuma);
            while(enuma.hasMoreElements()) {
                NodeInfo node = enuma.nextElement();
                bindery.openStackFrame(parameters);
                context.setCurrentNode(node);
                context.setContextNode(node);
                context.setPosition(position++);
                visit(context, mode);
                bindery.closeStackFrame();
            }
        }

    };


    /**
    * Apply a template imported from the stylesheet containing the current template
    */

    public void applyImports(Context c, Mode mode, int min, int max) throws SAXException {
        NodeInfo node = c.getCurrentNode();
        NodeHandler nh = ruleManager.getHandler(node, mode, min, max, c);
        if (nh!=null) {
            bindery.openStackFrame(null);
            nh.start(node, c);
            bindery.closeStackFrame();
        }
    }
            



    ////////////////////////////////////////////////////////////////////////////////
    // Methods for managing output destinations and formatting
    ////////////////////////////////////////////////////////////////////////////////


    /**
    * Set an Outputter to use. If none is supplied, the Controller creates one itself
    */

    public void setOutputManager(OutputManager out) {
        outputManager = out;
    }

    public OutputManager getOutputManager() {
        return outputManager;
    }

    /**
    * Initialize the outputter
    */
    
    private void initializeOutputter() throws SAXException {
        if (!outputterIsInitialized) {
            if (output==null) {
                output = new OutputDetails();
            }
                // System.err.println("Controller " + diagnosticName + ".transformDocument(), emitter = " + output.getEmitter());
            if (output.getEmitter()==null && styleSheetElement!=null) {
                styleSheetElement.updateOutputDetails(output);
            } else {
                // ignore the contents of the xsl:output element 
            }
            setNewOutputDetails(output);
        }
        outputterIsInitialized = true;
    }


    /**
    * Get the current outputter
    */

    public Outputter getOutputter() {
        return outputManager.getOutputter();
    }

    /**
    * Get the initial output details
    */

    public OutputDetails getInitialOutputDetails() {
        return output;
    }

    /**
    * Set the initial output details. This method should not be used once the
    * transformation is underway: use setNewOutputDetails() instead.
    */

    public void setOutputDetails(OutputDetails details) throws SAXException {
        output = details;
    }

    /**
    * Start a new output destination during the transformation
    */

    public void setNewOutputDetails(OutputDetails details) throws SAXException {
        outputManager.setOutputDetails(details);
    }

    /**
    * Get the current output details
    */

    public OutputDetails getCurrentOutputDetails() {
        return outputManager.getOutputDetails();
    }

    /**
    * Close the current output destination, and revert to the previous output destination. 
    * @return the new current outputter
    */

    public Outputter resetOutputDetails() throws SAXException {
        return outputManager.resetOutputDetails();
    }

    /**
    * Set the Emitter to be used for xsl:message output
    */

    public void setMessageEmitter(Emitter emitter) {
        messageEmitter = emitter;
    }

    /**
    * Get the Emitter used for xsl:message output
    */

    public Emitter getMessageEmitter() {
       return messageEmitter;
    }

    /**
    * Set the policy for handling recoverable errors
    */

    public void setRecoveryPolicy(int policy) {
        recoveryPolicy = policy;
    }

    /**
    * Get the policy for handling recoverable errors
    */

    public int getRecoveryPolicy() {
        return recoveryPolicy;
    }

    /**
    * Report a recoverable error
    */

    public void reportRecoverableError(String message, NodeInfo location) throws SAXException {
        String loctext = (location==null ? ":": " at line " + location.getLineNumber());
        if (!("".equals(diagnosticName))) {
            loctext += " in transformation " + diagnosticName;
        }
        if (recoveryPolicy==RECOVER_SILENTLY) {
            // do nothing
        } else if (recoveryPolicy==RECOVER_WITH_WARNINGS) {
            System.err.println("Recoverable error" + loctext);
            System.err.println(message);
        } else {
            System.err.println("Recoverable error" + loctext);
            System.err.println(message);
            System.err.println("Processing terminated because error recovery is disabled");
            throw new SAXException(message);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Methods for managing the Context and Bindery objects
    /////////////////////////////////////////////////////////////////////////////////////////


    /**
    * Get the document pool. This is used only for source documents, not for stylesheet modules
    */

    public Hashtable getDocumentPool() {
        return sourceDocumentPool;
    }

    /**
    * Get the stripper to use for source documents loaded using the document() function
    */

    public Stripper getStripper() {
        return styleSheetElement.getStripper();
    }

    /**
    * Set line numbering (of the source document) on or off
    */

    public void setLineNumbering(boolean onOrOff) {
        lineNumbering = onOrOff;
    }

    /**
    * Determine whether line numbering is enabled
    */

    public boolean isLineNumbering() {
        return lineNumbering;
    }

    /**
    * Create a new context with a given node as the current node and the only node in the current
    * node list.
    * Note we use Controller as a factory class for Context objects because we expect to introduce
    * subclasses of Context for different Controllers in future.
    */

    public Context makeContext(NodeInfo node) {
        Context c = new Context(this);
        c.setBindery(bindery);
        c.setCurrentNode(node);
        c.setContextNode(node);
        c.setPosition(1);
        c.setLast(1);
        return c;
    }
        

    /**
    * Set a Bindery to use. If none is supplied, the Controller creates one itself
    */

    public void setBindery(Bindery b) {
        bindery = b;
        defaultContext.setBindery(b);
    }

    /**
    * Get the current bindery
    */

    public Bindery getBindery() {
        return bindery;
    }



    /**
    * Get the current URI resolver
    */

    public URIResolver getURIResolver() {
        return uriResolver;
    }


    /**
    * Get the KeyManager
    */

    public KeyManager getKeyManager() {
        return styleSheetElement.getKeyManager();
    }

    //////////////////////////////////////////////////////////////////////
    // Methods for handling decimal-formats 
    //////////////////////////////////////////////////////////////////////


    public void setDecimalFormatManager(DecimalFormatManager manager) {
        decimalFormatManager = manager;
    }

    public DecimalFormatManager getDecimalFormatManager() {
        return decimalFormatManager;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Methods for registering and retrieving handlers for template rules
    ////////////////////////////////////////////////////////////////////////////////

    public void setRuleManager(RuleManager r) {
        ruleManager = r;
    }

    public RuleManager getRuleManager() {
        return ruleManager;
    }

    /////////////////////////////////////////////////////////////////////////
    // Methods for tracing
    /////////////////////////////////////////////////////////////////////////

    public void setTraceListener(TraceListener trace) { // e.g.
        traceListener = trace;
    }

    public TraceListener getTraceListener() { // e.g.
        return traceListener;
    }

    public final boolean isTracing() { // e.g.
        return traceListener != null;
    }

    /**
    * Associate this Controller with a compiled stylesheet
    */

    public void setPreparedStyleSheet(PreparedStyleSheet sheet) {
        preparedStyleSheet = sheet;
        sourceParser = sheet.getXMLReader();
        styleSheetElement = (XSLStyleSheet)sheet.getStyleSheetDocument().getDocumentElement();
        if (uriResolver==null) {
            try {
                uriResolver = (URIResolver)sheet.getURIResolver().getClass().newInstance();
            } catch (InstantiationException err1) {
                uriResolver = new StandardURIResolver();
            } catch (IllegalAccessException err2) {
                uriResolver = new StandardURIResolver();
            }
        }
    }

    /**
    * Set the SAX-compliant parser to use for the source document.
    * If no parser is specified, the parser is obtained from the
    * ParserManager.properties file.
    * @param parser The XML parser to use. This must be an instance of a class
    * that implements the org.xml.sax.Parser interface.
    * @deprecated in Saxon 5.2: use setXMLReader() instead, to supply a SAX2 parser.
    */

    public void setSourceParser(Parser parser) {
        setXMLReader(new ParserAdapter(parser));
    }

    /**
    * Set parameters supplied externally (typically, on the command line).
    * @param params A ParameterSet containing the (name, value) pairs. 
    */

    public void setParams(ParameterSet params) {
        this.parameters = params;
    }

    /**
    * Get the top element of the stylesheet document
    */

    private XSLStyleSheet getXSLStyleSheet() throws SAXException {
         return styleSheetElement;
    }
    
    /**
    * Internal method to create and initialize a controller
    */

    private void initializeController() throws SAXException {
        setRuleManager(styleSheetElement.getRuleManager());
        setDecimalFormatManager(styleSheetElement.getDecimalFormatManager());

        if (traceListener!=null) {
            traceListener.open();
        }

        // get a new bindery, to clear out any variables from previous runs

        Bindery bindery = new Bindery();
        setBindery(bindery);
        styleSheetElement.initialiseBindery(bindery);

        // if parameters were supplied, set them up

        bindery.defineGlobalParameters(parameters);
    }

    /**
    * Render a source XML document supplied as a tree. <br>
    * A new output destination should be created for each source document,
    * by using setOutputDetails(). <br>
    * Note that preview mode is not supported with this interface.
    * @param document A DocumentInfo object identifying the root of the source document tree.
    * (Note, this must currently be a DocumentImpl object)
    */

    public void transformDocument(DocumentInfo sourceDoc) throws SAXException {

        transformFailure = null;

        if (styleSheetElement==null) {
            throw new SAXException("Stylesheet has not been prepared");
        }

        if (!preview) {
            initializeController();
        }

        // process the stylesheet document 
        // (The main function of this phase is to evaluate global variables)

        Context c = makeContext(sourceDoc);

        styleSheetElement.process(c);            

        // Process the source document using the handlers that have been set up

        try {
            run(sourceDoc);
        } catch (SAXException err) {
            transformFailure = err;
        }

        try {
            if (traceListener!=null) {
                traceListener.close();
            }

            // make the media type of the output document available to the caller

            String mime = styleSheetElement.getMediaType();
            if (mime!=null) {
                output.setMediaType(mime);
            }
            if (!preview) {
                resetOutputDetails();
            }
        } catch (SAXException err) {
            if (transformFailure == null) {
                transformFailure = err;
            }
        }
        
        if (transformFailure!=null) {
            throw new SAXException(transformFailure);
        }
    }


    /**
    * Strip whitespace nodes from a supplied source document (in situ) according to the
    * xsl:strip-space directives in this stylesheet
    */

    public void strip(DocumentInfo doc) throws SAXException {

        if (styleSheetElement==null) {
            throw new SAXException("Stylesheet has not been prepared");
        } 

        styleSheetElement.getStripper().strip(doc);
    }

    /**
    * Adds the specified trace listener to receive trace events from
    * this instance.
    * Must be called before the invocation of the render method.
    * @param    trace the trace listener.
    */
    
    public void addTraceListener(TraceListener trace) { // e.g.
        traceListener = SaxonEventMulticaster.add(traceListener, trace);
    }

    /**
    * Removes the specified trace listener so that the next invocation
    * of the render method will not send trace events to the listener.
    * @param    trace the trace listener.
    */
    
    public void removeTraceListener(TraceListener trace) { // e.g.
        traceListener = SaxonEventMulticaster.remove(traceListener, trace);
    }

    /////////////////////////////////////////////////////////////////////////
    // Allow user data to be associated with nodes on a tree
    /////////////////////////////////////////////////////////////////////////

    /**
    * Get the named user data property for the node
    * @param the name of the user data property to return
    * @return The value of the named user data property.
    * Returns null if no property of that name has been set using setUserData() 
    * for this NodeInfo object.
    */

    public Object getUserData(NodeInfo node, String name)  {
        String key = name + " " + node.hashCode();
        return userDataTable.get(key);
    }
    
    /**
    * Set a user data property for a node.
    * @param name The name of the user data property to be set. Any existing user data property
    * of the same name will be overwritten.
    * @param userData an object to be saved with this element, which can be
    * retrieved later using getUserData().
    */
    
    public void setUserData(NodeInfo node, String name, Object data)  {
        String key = name + " " + node.hashCode();
        if (data==null) {
            userDataTable.remove(key);
        } else {
            userDataTable.put(key, data);
        }
    }


    /////////////////////////////////////////////////////////////////////////
    // implement the com.icl.saxon.trax.Transformer methods
    /////////////////////////////////////////////////////////////////////////

    /**
    * Process the source tree to SAX parse events.
    * @param xmlSource  The input for the source tree.
    */
    
    public void transform(InputSource in) throws TransformException {
        // System.err.println("transform " + diagnosticName);
        try {
            if (preparedStyleSheet==null) {
                throw new TransformException("Stylesheet has not been prepared");
            } 

            PreviewManager pm = styleSheetElement.getPreviewManager();
            preview = (pm!=null);
        
            if (preview) {
                // run the build in preview mode
                initializeController();
                pm.setController(this);

                initializeOutputter();
            
                Builder sourceBuilder = new Builder();
                sourceBuilder.setXMLReader(sourceParser);
                sourceBuilder.setStripper(styleSheetElement.getStripper());
                sourceBuilder.setLineNumbering(lineNumbering);
                sourceBuilder.setPreviewManager(pm);
                DocumentInfo doc = sourceBuilder.build(in);
                sourceBuilder = null;   // give the garbage collector a chance
                
                transformDocument(doc);
                resetOutputDetails();

            } else {
                Builder sourceBuilder = new Builder();
                    // System.err.println(diagnosticName + " creating builder " + sourceBuilder + ", sourceParser = " + sourceParser);
                sourceBuilder.setXMLReader(sourceParser);
                sourceBuilder.setStripper(styleSheetElement.getStripper());
                sourceBuilder.setLineNumbering(lineNumbering);
                DocumentInfo doc = sourceBuilder.build(in);
                sourceBuilder = null;   // give the garbage collector a chance
                
                transformDocument(doc);
            }
        } catch (SAXException err) {
            //err.printStackTrace();              
            throw new TransformException("Transform failed: " + diagnosticName + " " + err.getMessage());
        }
    }

    /**
    * Process the source tree to the output result.
    * @param xmlSource  The input for the source tree.
    * @param outputTarget The output destination. 
    */
    
    public void transform( InputSource xmlSource, Result outputTarget)
            throws TransformException {
        if (output==null) {
            output = new OutputDetails();
        }                
        if (outputTarget.getByteStream() != null) {
            output.setOutputStream(outputTarget.getByteStream());
            output.setCloseAfterUse(false);
        } else if (outputTarget.getCharacterStream() != null) {
            output.setWriter(outputTarget.getCharacterStream());
            output.setCloseAfterUse(false);
        } else if (outputTarget.getNode() != null) {
            output.setMethod("saxon:dom");
            Node node = outputTarget.getNode();
            output.setDOMNode(node);
            if (!(node instanceof Document || node instanceof Element)) {
                throw new TransformException("Result node must be a Document or Element node");
            }
        }

        transform(xmlSource);
    }

    /**
    * Process the source node to the output result, if the 
    * processor supports the "http://xml.org/trax/features/dom/input" 
    * feature.
    * @param node  The input source node, which can be any valid DOM node.
    * ** Currently in Saxon it must be a Document node. **
    * @param outputTarget The output source target. 
    */
    
    public void transformNode( Node node, Result outputTarget)
        throws TransformException {

        try {
            DOMDriver driver = new DOMDriver();
            driver.setStartNode(node);
            sourceParser = driver;
            InputSource inp = new InputSource();        
            transform(inp, outputTarget);
        } catch (SAXException err) {
            throw new TransformException(err);
        }
    }
        

    /**
    * Process the source node to to SAX parse events, if the 
    * processor supports the "http://xml.org/trax/features/dom/input" 
    * feature.
    * @param node  The input source node, which can be any valid DOM node. ** In SAXON,
    * it must currently be a Document node **
    */
    
    public void transformNode( Node node ) throws TransformException {
        if (node instanceof DocumentInfo) {
            try {
                transformDocument((DocumentInfo)node);
            } catch (SAXException err) {
                throw new TransformException(err);
            }
        } else {
            transformNode(node, new Result(System.out));
        }
    }

    /**
    * Get a SAX2 ContentHandler to which the source document can be fed as a stream of SAX events.
    * @return A valid ContentHandler, which should never be null, as 
    * long as getFeature("http://xml.org/trax/features/sax/input") 
    * returns true.
    */
    
    public ContentHandler getInputContentHandler() {
        inputContentHandler = new ActiveBuilder();
        return inputContentHandler;
    }
  
    /**
    * Get a SAX2 DeclHandler for the input.
    * @return A valid DeclHandler, which should never be null, as 
    * long as getFeature("http://xml.org/trax/features/sax/input") 
    * returns true.
    */

    public DeclHandler getInputDeclHandler() {
        //TODO: not implemented
        return null;
    }
 
    /**
    * Get a SAX2 LexicalHandler for the input.
    * @return A valid LexicalHandler, which should never be null, as 
    * long as getFeature("http://xml.org/trax/features/sax/input") 
    * returns true.
    */
    
    public LexicalHandler getInputLexicalHandler() {
        return (LexicalHandler)inputContentHandler;
    }
        

    /**
    * Set the output properties for the transformation.  These 
    * properties will override properties set in the templates 
    * with xsl:output.
    * 
    * @see com.icl.saxon.trax.serialize.OutputFormat
    */
    
    public void setOutputFormat(OutputFormat oformat) {
        if (output==null) {
            output = new OutputDetails();
        }
        output.setMethod(oformat.getMethod());
        output.setVersion(oformat.getVersion());
        output.setIndent((oformat.getIndent() ? "yes" : "no"));
        output.setEncoding(oformat.getEncoding());
        output.setMediaType(oformat.getMediaType());
        output.setDoctypePublic(oformat.getDoctypePublicId());
        output.setDoctypeSystem(oformat.getDoctypeSystemId());
        output.setOmitDeclaration((oformat.getOmitXMLDeclaration() ? "yes" : "no"));
        // output.setStandalone(oformat.getStandalone());
        QName[] cdata = oformat.getCDataElements();
        for (int i=0; i<cdata.length; i++) {
            output.addCdataElement(
                Name.reconstruct(   cdata[i].getPrefix(),
                                    cdata[i].getNamespaceURI(),
                                    cdata[i].getLocalName() ));
        }
        
    }
    
    /**
    * Set a parameter for the templates.
    * @param name The name of the parameter.
    * @param namespace The namespace of the parameter.              
    * @value The value object.  This can be any valid Java object 
    * it follows the same conversion rules as a value returned from a Saxon extension function.
    */
    
    public void setParameter(String name, String namespace, Object value) {

        if (parameters == null) {
            parameters = new ParameterSet();
        }
        Value result;
        try {
            result = FunctionProxy.convertJavaObjectToXPath(value);
        } catch (SAXException err) {
            result = new StringValue(value.toString());
        }
        if (namespace==null || namespace.equals("")) {
            parameters.put(name.intern(), result);
        } else {
            String paramname = Name.reconstruct("prefix", namespace, name).getAbsoluteName();
            parameters.put(paramname, result);
        }           
    }
  
    /**
    * Reset the parameters to a null list.  
    */
    
    public void resetParameters() {
        parameters = null;
    }
  
    /**
    * Set an object that will be used to resolve URIs used in 
    * document(), etc.
    * @param resolver An object that implements the URIResolver interface, 
    * or null.
    */
    
    public void setURIResolver(URIResolver resolver) {
        if (resolver==null) {
            uriResolver = new StandardURIResolver();
        } else {
            uriResolver = resolver;
        }
    }
  
    /**
    * Set an XML parser for the source tree.  Note that if 
    * Transformer.setXMLReader is not called, the parser set 
    * with Processor.setXMLReader will be used. 
    */
    
    public void setXMLReader(XMLReader reader) {
        sourceParser = reader;
    }

    /**
    * Get the XML parser used for the source tree.  Note that 
    * if Transformer.setXMLReader is not called, the parser set 
    * with Processor.setXMLReader will be used.
    */
    
    public XMLReader getXMLReader() {
        return sourceParser;
    }

    //////////////////////////////////////////////////////////////////
    // Implement XMLFilter interface methods
    //////////////////////////////////////////////////////////////////

    /**
    * Set the parent reader.
    *
    * <p>This method allows the application to link the filter to
    * a parent reader (which may be another filter).  The argument
    * may not be null.</p>
    *
    * @param parent The parent reader (the supplier of SAX events).
    */
    
    public void setParent (XMLReader parent) {
        setXMLReader(parent);
    }

    /**
    * Get the parent reader.
    *
    * <p>This method allows the application to query the parent
    * reader (which may be another filter).  It is generally a
    * bad idea to perform any operations on the parent reader
    * directly: they should all pass through this filter.</p>
    *
    * @return The parent filter, or null if none has been set.
    */
    
    public XMLReader getParent() {
        return getXMLReader();
    }

    ///////////////////////////////////////////////////////////////////
    // implement XMLReader interface methods
    ///////////////////////////////////////////////////////////////////

    /**
     * Look up the value of a feature.
     *
     * <p>The feature name is any fully-qualified URI.  It is
     * possible for an XMLReader to recognize a feature name but
     * to be unable to return its value; this is especially true
     * in the case of an adapter for a SAX1 Parser, which has
     * no way of knowing whether the underlying parser is
     * performing validation or expanding external entities.</p>
     *
     * <p>All XMLReaders are required to recognize the
     * http://xml.org/sax/features/namespaces and the
     * http://xml.org/sax/features/namespace-prefixes feature names.</p>
     *
     * @param name The feature name, which is a fully-qualified URI.
     * @return The current state of the feature (true or false).
     * @exception org.xml.sax.SAXNotRecognizedException When the
     *            XMLReader does not recognize the feature name.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the feature name but 
     *            cannot determine its value at this time.
     * @see #setFeature
     */
     
    public boolean getFeature (String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/features/namespaces")) {
            return true;
        } else if (name.equals("http://xml.org/sax/features/namespace-prefixes")) {
            return false;
        } else {
            throw new SAXNotRecognizedException(name);
        }
    }


    /**
     * Set the state of a feature.
     *
     * <p>The feature name is any fully-qualified URI.  It is
     * possible for an XMLReader to recognize a feature name but
     * to be unable to set its value; this is especially true
     * in the case of an adapter for a SAX1 {@link org.xml.sax.Parser Parser},
     * which has no way of affecting whether the underlying parser is
     * validating, for example.</p>
     *
     * <p>All XMLReaders are required to support setting
     * http://xml.org/sax/features/namespaces to true and
     * http://xml.org/sax/features/namespace-prefixes to false.</p>
     *
     * <p>Some feature values may be immutable or mutable only 
     * in specific contexts, such as before, during, or after 
     * a parse.</p>
     *
     * @param name The feature name, which is a fully-qualified URI.
     * @param state The requested state of the feature (true or false).
     * @exception org.xml.sax.SAXNotRecognizedException When the
     *            XMLReader does not recognize the feature name.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the feature name but 
     *            cannot set the requested value.
     * @see #getFeature
     */
     
    public void setFeature (String name, boolean value)
	throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/features/namespaces")) {
            if (!value) {
                throw new SAXNotSupportedException(name);
            }
        } else if (name.equals("http://xml.org/sax/features/namespace-prefixes")) {
            if (value) {
                throw new SAXNotSupportedException(name);
            }
        } else {
            throw new SAXNotRecognizedException(name);
        }
	}

    /**
     * Look up the value of a property.
     *
     * <p>The property name is any fully-qualified URI.  It is
     * possible for an XMLReader to recognize a property name but
     * to be unable to return its state; this is especially true
     * in the case of an adapter for a SAX1 {@link org.xml.sax.Parser
     * Parser}.</p>
     *
     * <p>XMLReaders are not required to recognize any specific
     * property names, though an initial core set is documented for
     * SAX2.</p>
     *
     * <p>Some property values may be available only in specific
     * contexts, such as before, during, or after a parse.</p>
     *
     * <p>Implementors are free (and encouraged) to invent their own properties,
     * using names built on their own URIs.</p>
     *
     * @param name The property name, which is a fully-qualified URI.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException When the
     *            XMLReader does not recognize the property name.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the property name but 
     *            cannot determine its value at this time.
     * @see #setProperty
     */
     
    public Object getProperty (String name)
	throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/properties/lexical-handler")) {
            return lexicalHandler;
        } else {
	        throw new SAXNotRecognizedException(name);
        }
	}


    /**
     * Set the value of a property.
     *
     * <p>The property name is any fully-qualified URI.  It is
     * possible for an XMLReader to recognize a property name but
     * to be unable to set its value; this is especially true
     * in the case of an adapter for a SAX1 {@link org.xml.sax.Parser
     * Parser}.</p>
     *
     * <p>XMLReaders are not required to recognize setting
     * any specific property names, though a core set is provided with 
     * SAX2.</p>
     *
     * <p>Some property values may be immutable or mutable only 
     * in specific contexts, such as before, during, or after 
     * a parse.</p>
     *
     * <p>This method is also the standard mechanism for setting
     * extended handlers.</p>
     *
     * @param name The property name, which is a fully-qualified URI.
     * @param state The requested value for the property.
     * @exception org.xml.sax.SAXNotRecognizedException When the
     *            XMLReader does not recognize the property name.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the property name but 
     *            cannot set the requested value.
     */
     
    public void setProperty (String name, Object value)
    throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals("http://xml.org/sax/properties/lexical-handler")) {
            if (value instanceof LexicalHandler) {
                Emitter emitter = null;
                if (output!=null) {
                    emitter = output.getEmitter();
                }
                if (emitter==null || !(emitter instanceof ContentHandlerProxy)) {
                    throw new SAXNotSupportedException(
                        "Controller: cannot register a lexical handler unless a content handler has been registered");
                }
                lexicalHandler = (LexicalHandler)value;
                ((ContentHandlerProxy)emitter).setLexicalHandler(lexicalHandler);       
            } else {
                throw new SAXNotSupportedException(
                    "Lexical Handler must be instance of org.xml.sax.ext.LexicalHandler");
            }
        } else {
            throw new SAXNotRecognizedException(name);
        }
    }

    /**
     * Allow an application to register an entity resolver.
     *
     * <p>If the application does not register an entity resolver,
     * the XMLReader will perform its own default resolution.</p>
     *
     * <p>Applications may register a new or different resolver in the
     * middle of a parse, and the SAX parser must begin using the new
     * resolver immediately.</p>
     *
     * @param resolver The entity resolver.
     * @exception java.lang.NullPointerException If the resolver 
     *            argument is null.
     * @see #getEntityResolver
     */
     
    public void setEntityResolver (EntityResolver resolver) {
        // XSLT output does not use entities, so the resolver is never used
    }


    /**
     * Return the current entity resolver.
     *
     * @return The current entity resolver, or null if none
     *         has been registered.
     * @see #setEntityResolver
     */

    public EntityResolver getEntityResolver () {
        return null;
    }


    /**
     * Allow an application to register a DTD event handler.
     *
     * <p>If the application does not register a DTD handler, all DTD
     * events reported by the SAX parser will be silently ignored.</p>
     *
     * <p>Applications may register a new or different handler in the
     * middle of a parse, and the SAX parser must begin using the new
     * handler immediately.</p>
     *
     * @param handler The DTD handler.
     * @exception java.lang.NullPointerException If the handler 
     *            argument is null.
     * @see #getDTDHandler
     */
     
    public void setDTDHandler (DTDHandler handler) {
        // XSLT output does not include a DTD
    }


    /**
     * Return the current DTD handler.
     *
     * @return The current DTD handler, or null if none
     *         has been registered.
     * @see #setDTDHandler
     */
     
    public DTDHandler getDTDHandler () {
        return null;
    }


    /**
     * Allow an application to register a content event handler to process the result
     * of the transformation.
     *
     * <p>In SAX2, Applications may register a new or different handler in the
     * middle of a parse, and the SAX parser must begin using the new
     * handler immediately. ** Saxon does not permit this **</p>
     *
     * @param handler The content handler.
     * @exception java.lang.NullPointerException If the handler 
     *            argument is null.
     * @see #getContentHandler
     */
     
    public void setContentHandler(ContentHandler handler) {
        // System.err.println(diagnosticName + " setContentHandler to " + handler);
        if (output==null) {
            output = new OutputDetails();
        }
        outputContentHandler = handler;
        ContentHandlerProxy proxy = new ContentHandlerProxy();
        proxy.setUnderlyingContentHandler(handler);
        output.setMethod("saxon:user");
        output.setEmitter(proxy);
    }
            
    /**
    * Return the content handler to which transformation output will be directed.
    *
    * @return The current content handler, or null if none
    *         has been registered.
    * @see #setContentHandler
    */

    public ContentHandler getContentHandler() {
        return outputContentHandler;
    }

    /**
     * Allow an application to register an error event handler.
     *
     * <p>If the application does not register an error handler, all
     * error events reported by the SAX parser will be silently
     * ignored; however, normal processing may not continue.  It is
     * highly recommended that all SAX applications implement an
     * error handler to avoid unexpected bugs.</p>
     *
     * <p>Applications may register a new or different handler in the
     * middle of a parse, and the SAX parser must begin using the new
     * handler immediately.</p>
     *
     * @param handler The error handler.
     * @exception java.lang.NullPointerException If the handler 
     *            argument is null.
     * @see #getErrorHandler
     */
     
    public void setErrorHandler (ErrorHandler handler) {
        // No effect
    }

    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none
     *         has been registered.
     * @see #setErrorHandler
     */
    public ErrorHandler getErrorHandler () {
        return null;
    }

    /**
     * "Parse an XML document." In the context of a Transformer, this means
     * perform a transformation. The method is equivalent to transform().
     *
     * @param source The input source (the XML document to be transformed)
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException An IO exception from the parser,
     *            possibly from a byte stream or character stream
     *            supplied by the application.
     * @see org.xml.sax.InputSource
     * @see #parse(java.lang.String)
     * @see #setEntityResolver
     * @see #setDTDHandler
     * @see #setContentHandler
     * @see #setErrorHandler 
     */
     
    public void parse (InputSource input) throws IOException, SAXException {
        transform(input);
    }

    /**
     * Parse (i.e. transform) an XML document from a system identifier (URI).
     *
     * <p>This method is a shortcut for the common case of reading a
     * document from a system identifier.  It is the exact
     * equivalent of the following:</p>
     *
     * <pre>
     * parse(new InputSource(systemId));
     * </pre>
     *
     * <p>If the system identifier is a URL, it must be fully resolved
     * by the application before it is passed to the parser.</p>
     *
     * @param systemId The system identifier (URI).
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @exception java.io.IOException An IO exception from the parser,
     *            possibly from a byte stream or character stream
     *            supplied by the application.
     * @see #parse(org.xml.sax.InputSource)
     */
     
    public void parse (String systemId)	throws IOException, SAXException {
        transform(new InputSource(systemId));
    }

    /**
    * Inner class: a special Builder for the source document,
    * that initiates the transformation as soon as the parsing is complete
    */

    private class ActiveBuilder extends Builder {

        public void endDocument () throws SAXException {
            if (!failed) {
                try {
                    // System.err.println("ActiveBuilder.endDocument()");
                    super.endDocument();
                    DocumentInfo sourceDoc = getCurrentDocument();
                    transformDocument(sourceDoc);
                } catch (SAXException err) {
                    // messy exception handling is due to differences between SAX parsers
                    transformFailure = err;
                    throw err;
                }
            }
        }
    }

}   // end of outer class Controller

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
// Contributor(s): 
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//
