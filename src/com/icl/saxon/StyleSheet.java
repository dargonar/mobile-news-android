package com.icl.saxon;
import com.icl.saxon.handlers.*;
import com.icl.saxon.tree.Builder;
import com.icl.saxon.tree.Stripper;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.Namespace;
import com.icl.saxon.expr.*;
import com.icl.saxon.style.*;
import com.icl.saxon.output.*;
import com.icl.saxon.trace.*;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.*;

import org.w3c.dom.Node;
import org.w3c.dom.Document;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;

import com.icl.saxon.trax.Processor;
import com.icl.saxon.trax.ProcessorException;
import com.icl.saxon.trax.Templates;
import com.icl.saxon.trax.URIResolver;
import com.icl.saxon.trax.TemplatesBuilder;
import com.icl.saxon.trax.TransformException;

/**
  * This <B>StyleSheet</B> class is the entry point to the Saxon XSLT Processor. This
  * class is provided to control the processor from the command line; it also implements
  * the TRAX Processor interface.<p>
  *
  * The XSLT syntax supported conforms to the W3C XSLT 1.0 and XPath 1.0 recommendation.
  * Only the transformation language is implemented (not the formatting objects).
  * Saxon extensions are documented in the file extensions.html
  *
  * @author M.H.Kay (Michael.Kay@icl.com)
  */
  
public class StyleSheet extends com.icl.saxon.trax.Processor {

    protected String sourceParserName = null;
    protected String styleParserName = null;
    protected XMLReader sourceParser = null;
    protected XMLReader styleParser = null;
    protected URIResolver sourceURIResolver = null;
    protected URIResolver styleURIResolver = null;
    protected Emitter messageEmitter = null;
    protected TraceListener traceListener = null;
    boolean showTime = false;
    boolean lineNumbering = false;
    int repeat = 1;
    int recoveryPolicy = Controller.RECOVER_WITH_WARNINGS;
    
    /**
    * Main program, can be used directly from the command line.
    * <p>The format is:</P>
    * <p>java com.icl.saxon.StyleSheet [options] <I>source-file</I> <I>style-file</I> &gt;<I>output-file</I></P>
    * <p>followed by any number of parameters in the form {keyword=value}... which can be
    * referenced from within the stylesheet.</p>
    * <p>This program applies the XSL style sheet in style-file to the source XML document in source-file.</p>
    */
    
    public static void main (String args[])
        throws java.lang.Exception
    {
        // the real work is delegated to another routine so that it can be used in a subclass
        (new StyleSheet()).doMain(args, new StyleSheet(), " java com.icl.saxon.StyleSheet");
    }

    /**
    * Support method for main program. This support method can also be invoked from subclasses
    * that support the same command line interface
    * @param args the command-line arguments
    * @param app instance of the StyleSheet class (or a subclass) to be invoked
    * @param name name of the class, to be used in error messages
    */

    protected void doMain(String args[], StyleSheet app, String name) {
        
        
        String sourceFileName = null;
        String styleFileName = null;
        File sourceFile = null;
        File styleFile = null;
        File outputFile = null;
        boolean useURLs = false;
        ParameterSet params = new ParameterSet();
        OutputDetails output = new OutputDetails();
        String outputFileName = null;
        boolean useAssociatedStylesheet = false;
        boolean wholeDirectory = false;
        
				// Check the command-line arguments.

        try {
            int i = 0;
            while (true) {
                if (i>=args.length) badUsage(name, "No source file name");

                if (args[i].charAt(0)=='-') {

                    if (args[i].equals("-a")) {
                        useAssociatedStylesheet = true;
                        i++;
                    }

                    else if (args[i].equals("-l")) {
                        lineNumbering = true;
                        i++;
                    }

                    else if (args[i].equals("-u")) {
                        useURLs = true;
                        i++;
                    }
        
                    else if (args[i].equals("-t")) {
                        System.err.println(Version.getProductName());
                        System.err.println("Java version " + System.getProperty("java.version"));
                        Loader.setTracing(true);
                        showTime = true;
                        i++;
                    }

                    else if (args[i].equals("-3")) {    // undocumented option: do it thrice
                        i++;
                        repeat = 3;
                    }
        
                    else if (args[i].equals("-o")) {
                        i++;
                        if (args.length < i+2) badUsage(name, "No output file name");
                        outputFileName = args[i++];
                    }

                    else if (args[i].equals("-x")) {
                        i++;
                        if (args.length < i+2) badUsage(name, "No source parser class");
                        sourceParserName = args[i++];
                        sourceParser = ParserManager.makeParser(sourceParserName);
                    }

                    else if (args[i].equals("-y")) {
                        i++;
                        if (args.length < i+2) badUsage(name, "No style parser class");
                        styleParserName = args[i++];
                        styleParser = ParserManager.makeParser(styleParserName);
                    }

                    else if (args[i].equals("-r")) {
                        i++;
                        if (args.length < i+2) badUsage(name, "No URIResolver class");
                        String r = args[i++];
                        sourceURIResolver = makeURIResolver(r);
                        styleURIResolver = makeURIResolver(r);
                    }

                    else if (args[i].equals("-T")) {
                        i++;
                        lineNumbering = true;
                        traceListener = new com.icl.saxon.trace.SimpleTraceListener();
                    }

                    else if (args[i].equals("-TL")) {
                        i++;
                        if (args.length < i+2) badUsage(name, "No TraceListener class");
                        lineNumbering = true;
                        traceListener = makeTraceListener(args[i++]);
                    }

                    else if (args[i].equals("-w0")) {
                        i++;
                        recoveryPolicy = Controller.RECOVER_SILENTLY;
                    }
                    else if (args[i].equals("-w1")) {
                        i++;
                        recoveryPolicy = Controller.RECOVER_WITH_WARNINGS;
                    }
                    else if (args[i].equals("-w2")) {
                        i++;
                        recoveryPolicy = Controller.DO_NOT_RECOVER;
                    }
                    
                    else if (args[i].equals("-m")) {
                        i++;
                        if (args.length < i+2) badUsage(name, "No message Emitter class");
                        messageEmitter = makeMessageEmitter(args[i++]);
                    }

                    else badUsage(name, "Unknown option " + args[i]);
                }
            
                else break;
            }

            if (args.length < i+1 ) badUsage(name, "No source file name");        
            sourceFileName = args[i++];

            if (!useAssociatedStylesheet) {
                if (args.length < i+1 ) badUsage(name, "No stylesheet file name");        
                styleFileName = args[i++];
            }
        
            for (int p=i; p<args.length; p++) {
                String arg = args[p];
                int eq = arg.indexOf("=");
                if (eq<1 || eq>=arg.length()-1) badUsage(name, "Bad param=value pair on command line");
                params.put(arg.substring(0,eq).intern(), new StringValue(arg.substring(eq+1)));
            }


            if (sourceURIResolver==null) {
                sourceURIResolver = new StandardURIResolver();
                ((StandardURIResolver)sourceURIResolver).setParserClass(sourceParserName);
            }

            if (styleURIResolver==null) {
                styleURIResolver = new StandardURIResolver();
                ((StandardURIResolver)styleURIResolver).setParserClass(styleParserName);
            }

            InputSource sourceInput = null;

            if (useURLs || sourceFileName.startsWith("http:") || sourceFileName.startsWith("file:")) {
                sourceURIResolver.setURI(null, sourceFileName);
                sourceInput = sourceURIResolver.getInputSource();
                if (sourceInput==null) {
                    quit("URIResolver for source file must return a SAX InputSource");
                }
                XMLReader r = sourceURIResolver.getXMLReader();
                if (r != null) {
                    sourceParser = r;
                }
            } else {
                sourceFile = new File(sourceFileName);
                if (!sourceFile.exists()) {
                    quit("Source file " + sourceFile + " does not exist");
                }
                if (sourceFile.isDirectory()) {
                    wholeDirectory = true;
                    if (outputFileName==null) {
                        quit("To process a directory, -o must be specified");
                    } else if (outputFileName.equals(sourceFileName)) {
                        quit("Output directory must be different from input");
                    } else {
                        outputFile = new File(outputFileName);
                        if (!outputFile.isDirectory()) {
                            quit("Input is a directory, but output is not");
                        }
                    }
                } else {
                    sourceInput = new ExtendedInputSource();
                    ((ExtendedInputSource)sourceInput).setFile(sourceFile);
                    ((ExtendedInputSource)sourceInput).setEstimatedLength((int)sourceFile.length());
                }
            }

            if (outputFileName!=null && !wholeDirectory) {
                 outputFile = new File(outputFileName);
                 if (outputFile.isDirectory()) {
                            quit("Output is a directory, but input is not");
                 }
            }

            if (useAssociatedStylesheet) {
                if (wholeDirectory) {
                    processDirectoryAssoc(sourceFile, outputFile, params);
                } else {
                    processFileAssoc(sourceInput, null, outputFile, params);
                }
            } else {
                
                long startTime = (new Date()).getTime();
                
                String styleURI;
                if (useURLs || styleFileName.startsWith("http:")
                                 || styleFileName.startsWith("file:")) {
                    styleURI = styleFileName;
                    
                } else {
                    File sheetFile = new File(styleFileName);
                    if (!sheetFile.exists()) {
                        quit("Stylesheet file " + sheetFile + " does not exist");
                    }
                    styleURI = ExtendedInputSource.createURL(sheetFile);
                }
                    
                styleURIResolver.setURI(null, styleURI);
                InputSource ins = styleURIResolver.getInputSource();
                if (ins==null) {
                    quit("URIResolver for stylesheet file must return a SAX InputSource");
                }
                XMLReader r = styleURIResolver.getXMLReader();
                if (r != null) {
                    styleParser = r;
                }

                PreparedStyleSheet sheet = new PreparedStyleSheet();
                sheet.setURIResolver(styleURIResolver);
                sheet.setXMLReader(styleParser);
                try {
                    sheet.prepare(ins);
                } catch (SAXException err) {
                    System.err.println("Failed to compile style sheet");
                    throw err;
                }

                if (showTime) {
                    long endTime = (new Date()).getTime();
                    System.err.println("Preparation time: " + (endTime-startTime) + " milliseconds");
                    startTime = endTime;
                }
                
                if (wholeDirectory) {        
                    processDirectory(sourceFile, sheet, outputFile, params);
                } else {
                    processFile(sourceInput, sheet, outputFile, params);
                }
            }
        } catch (SAXException err) {
            System.err.println(err.getMessage());
            System.exit(2);
        } catch (Exception err2) {
            err2.printStackTrace();
        }
        

        System.exit(0);
    }

    /**
    * Exit with a message
    */

    protected static void quit(String message) {
        System.err.println(message);
        System.exit(2);
    }

    /**
    * Process each file in the source directory using its own associated stylesheet
    */

    public void processDirectoryAssoc(
        File sourceDir, File outputDir, ParameterSet params)
        throws Exception {

        String[] files = sourceDir.list();
        for (int f=0; f<files.length; f++) {
            try {
                File file = new File(sourceDir, files[f]);
                if (!file.isDirectory()) {
                    String localName = file.getName();
                    //File outputFile = new File(outputDir, localName);
                    ExtendedInputSource source = new ExtendedInputSource(file);
                    processFileAssoc(source, localName, outputDir, params);
                }
            } catch (SAXException err) {
                System.err.println(err.getMessage());
            }
        }
    }

    /**
    * Make an output file in the output directory, with filename extension derived from the
    * media-type produced by the stylesheet
    */

    private File makeOutputFile(File directory, String localName,
                                 PreparedStyleSheet sheet) throws SAXException{
        String mediaType = sheet.getMediaType();
        String suffix = ".xml";
        if (mediaType.equals("text/html")) {
            suffix = ".html";
        } else if (mediaType.equals("text/plain")) {
            suffix = ".txt";
        }
        String prefix = localName;
        if (localName.endsWith(".xml") || localName.endsWith(".XML")) {
            prefix = localName.substring(0, localName.length()-4);
        }
        return new File(directory, prefix+suffix);
    }
            

    /**
    * Process a single source file using its associated stylesheet(s)
    */

    public void processFileAssoc(
        InputSource sourceInput, String localName, File outputFile, ParameterSet params)
        throws Exception {
                    
        if (showTime) {
            System.err.println("Processing " + sourceInput.getSystemId());
        }
        long startTime = (new Date()).getTime();
        
        DocumentInfo sourceDoc = null;
        PreparedStyleSheet sheet = null;
        File outFile = outputFile;

        Builder builder = new Builder();
        builder.setLineNumbering(lineNumbering);
        sourceDoc = builder.build(sourceInput);                
        String[] hrefs = sourceDoc.getAssociatedStylesheets(null, null);
            
        if (hrefs==null || hrefs.length==0) {
            throw new SAXException("No xml-stylesheet processing instruction was found");
        }

        if (hrefs.length>1) {
            throw new SAXException("More than one xml-stylesheet processing instruction was found");
        }

        String href = hrefs[0];

        if (href.charAt(0)=='#') {                
            String id = href.substring(1);
            sheet = sourceDoc.getEmbeddedStylesheet(id);
            if (sheet==null) {
                throw new SAXException("No stylesheet with id=#" + id + " was found");
            }

        } else {
            String styleFileName;
            try {
                URL styleURL = new URL(new URL(sourceDoc.getSystemId()), href);
                styleFileName = styleURL.toString();
            } catch (MalformedURLException err) {
                throw new SAXException(err);
            }

            ExtendedInputSource sheetInput = new ExtendedInputSource();
            sheetInput.setSystemId(styleFileName);
            sheet = new PreparedStyleSheet();
            sheet.setXMLReader(styleParser);
            sheet.setURIResolver(styleURIResolver);
            sheet.prepare(sheetInput);
        }

        Controller instance = (Controller)sheet.newTransformer();
        if (outFile!=null && outFile.isDirectory()) {
            outFile = makeOutputFile(outFile, localName, sheet);
        }
        instance.setXMLReader(sourceParser);
        instance.setURIResolver(sourceURIResolver);
        instance.setOutputDetails(makeDetails(sheet.getOutputDetails(), outFile));
        instance.setMessageEmitter(messageEmitter);
        instance.setLineNumbering(lineNumbering);
        instance.setRecoveryPolicy(recoveryPolicy);
        instance.setParams(params);
        instance.strip(sourceDoc);
        if (traceListener!=null) {
            instance.addTraceListener(traceListener);
        }
        instance.transformDocument(sourceDoc);

        if (showTime) {
            long endTime = (new Date()).getTime();
            System.err.println("Execution time: " + (endTime-startTime) + " milliseconds");
            startTime = endTime;
        }
    }

    /**
    * Process each file in the source directory using the same supplied stylesheet
    */

    public void processDirectory(
        File sourceDir, PreparedStyleSheet sheet, File outputDir, ParameterSet params)
        throws Exception {
            
        String[] files = sourceDir.list();
        for (int f=0; f<files.length; f++) {
            try {
                File file = new File(sourceDir, files[f]);
                if (!file.isDirectory()) {
                    String localName = file.getName();
                    File outputFile = makeOutputFile(outputDir, localName, sheet);
                    ExtendedInputSource source = new ExtendedInputSource(file);
                    processFile(source, sheet, outputFile, params);
                }
            } catch (SAXException err) {
                System.err.println(err.getMessage());
            }                
        }
    }

    /**
    * Process a single file using a supplied stylesheet
    */

    public void processFile(
        InputSource source, PreparedStyleSheet sheet, File outputFile, ParameterSet params)
        throws Exception {

        for (int r=0; r<repeat; r++) {      // repeat is for internal testing/timing
            if (showTime) {
                System.err.println("Processing " + source.getSystemId());                
            }
            long startTime = (new Date()).getTime();
            Controller instance = (Controller)sheet.newTransformer();
            instance.setXMLReader(sourceParser);
            instance.setURIResolver(sourceURIResolver);
            instance.setOutputDetails(makeDetails(sheet.getOutputDetails(), outputFile));
            instance.setMessageEmitter(messageEmitter);
            instance.setLineNumbering(lineNumbering);
            instance.setRecoveryPolicy(recoveryPolicy);
            if (traceListener!=null) {
                instance.addTraceListener(traceListener);
            }
            instance.setParams(params);
            instance.transform(source);

            if (showTime) {
                long endTime = (new Date()).getTime();
                //System.err.println("\nSource XML parsed using " + source.getXMLReader().getClass());
                System.err.println("Execution time: " + (endTime-startTime) + " milliseconds");
                startTime = endTime;
            }
        }
    }

    private OutputDetails makeDetails(OutputDetails details, File outputFileName) throws SAXException {
        if (outputFileName!=null) {
            OutputStream stream;
            try {
                stream = new FileOutputStream(outputFileName);
                details.setOutputStream(stream);
            } catch (java.io.IOException err) {
                System.err.println("Cannot write to file " + outputFileName);
                throw new SAXException(err);
            }
        }
        return details;
    }

    protected void badUsage(String name, String message) {
        System.err.println(message);
        System.err.println(Version.getProductName());
        System.err.println("Usage: " + name + " [options] source-doc style-doc {param=value}...");
        System.err.println("Options: ");
        System.err.println("  -a              Use xml-stylesheet PI, not style-doc argument ");
        System.err.println("  -o filename     Send output to named file or directory ");
        System.err.println("  -m classname    Use specified Emitter class for xsl:message output ");
        System.err.println("  -r classname    Use specified URIResolver class ");
        System.err.println("  -t              Display version and timing information ");        
        System.err.println("  -T              Set standard TraceListener");  
        System.err.println("  -TL classname   Set a specific TraceListener");  
        System.err.println("  -u              Names are URLs not filenames ");
        System.err.println("  -x classname    Use specified SAX parser for source file ");
        System.err.println("  -y classname    Use specified SAX parser for stylesheet ");
        System.err.println("  -?              Display this message ");
        System.exit(2);
    }

    public static URIResolver makeURIResolver (String className) throws SAXException
    {
        Object obj = Loader.getInstance(className);
        if (obj instanceof URIResolver) {
            return (URIResolver)obj;
        }        
        throw new SAXException("Class " + className + " is not a URIResolver");

    }

    public static TraceListener makeTraceListener (String className) throws SAXException
    {
        Object obj = Loader.getInstance(className);
        if (obj instanceof TraceListener) {
            return (TraceListener)obj;
        }        
        throw new SAXException("Class " + className + " is not a TraceListener");

    }



    public static Emitter makeMessageEmitter (String className) throws SAXException
    {
        Object obj = Loader.getInstance(className);
        if (obj instanceof Emitter) {
            return (Emitter)obj;
        }        
        throw new SAXException("Class " + className + " is not an Emitter");

    }

    //////////////////////////////////////////////////////////////////////////////////
    // Implement the com.icl.saxon.trax.Processor interface methods
    //////////////////////////////////////////////////////////////////////////////////

    /**
    * Process the source stylesheet into a templates object.
    * 
    * @param source An object that holds a URL, input stream, etc. for the stylesheet
    * @returns A Templates object capable of being used for transformation purposes.
    * @exception ProcessorException May throw this during the parse when it 
    *            is constructing the Templates object and fails.
    */
    
    public Templates process(InputSource source) throws ProcessorException {
        try {
            PreparedStyleSheet pss = new PreparedStyleSheet();
            pss.setXMLReader(styleParser);
            pss.prepare(source);
            return pss;
        } catch (SAXException err) {
            throw new ProcessorException(err.getMessage(), err);
        }
    }
        

    /**
    * Process the stylesheet from a DOM tree, if the 
    * processor supports the "http://xml.org/trax/features/dom/input" 
    * feature.    
    * 
    * @param node A DOM tree which must contain 
    * valid transform instructions that this processor understands.
    * @returns A Templates object capable of being used for transformation purposes.
    */
    
    public Templates processFromNode(Node node) throws ProcessorException {
        try {
            DOMDriver driver = new DOMDriver();
            driver.setStartNode(node);
            styleParser = driver;
            InputSource dummy = new InputSource();
            return process(dummy);
        } catch (SAXException err) {
            throw new ProcessorException(err);
        }
    }
        
    /**
    * Process a series of stylesheet inputs, treating them in import or cascade 
    * order.  This is mainly for support of the getAssociatedStylesheets
    * method, but may be useful for other purposes.
    * 
    * @param sources An array of SAX InputSource objects.
    * @returns A Templates object capable of being used for transformation purposes.
    */
    
    public Templates processMultiple(InputSource[] sources) throws ProcessorException {
        if (sources.length == 1) {
            return process(sources[0]);
        } else if (sources.length == 0) {
            throw new ProcessorException("No stylesheets were supplied",
                        new SAXException("No stylesheets were supplied"));
        }
        
        // create a new top-level stylesheet that imports all the others

        try {       
            Builder builder = new Builder();
            builder.setSystemId("http://icl.com/saxon");        // not used but must be valid URL
            builder.setNodeFactory(new StyleNodeFactory());
            builder.setLineNumbering(true);

            builder.startDocument();
            builder.startPrefixMapping("xsl", Namespace.XSLT);
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "version", "version", "CDATA", "1.0");
            builder.startElement(Namespace.XSLT, "stylesheet", "xsl:stylesheet", atts);

            for (int i=0; i<sources.length; i++) {
                atts.clear();
                atts.addAttribute("", "href", "href", "CDATA", sources[i].getSystemId());
                builder.startElement(Namespace.XSLT, "import", "xsl:import", atts);        
                builder.endElement(Namespace.XSLT, "import", "xsl:import");
            }
            builder.endElement(Namespace.XSLT, "stylesheet", "xsl:stylesheet");
            builder.endPrefixMapping("xsl");
            builder.endDocument();

            PreparedStyleSheet pss = new PreparedStyleSheet();
            pss.setStyleSheetDocument(builder.getCurrentDocument());
            return pss;
        } catch (SAXException err) {
            err.printStackTrace();
            throw new ProcessorException("Failed to combine multiple stylesheets: " + err.getMessage(), err);
        }

    }
        

    /**
    * Get InputSource specification(s) that are associated with the 
    * given document specified in the source param,
    * via the xml-stylesheet processing instruction 
    * (see http://www.w3.org/TR/xml-stylesheet/), and that matches 
    * the given criteria.  Note that it is possible to return several stylesheets 
    * that match the criteria, in which case they are applied as if they were 
    * a list of imports or cascades.
    * <p>Note that DOM2 has its own mechanism for discovering stylesheets. 
    * Therefore, there isn't a DOM version of this method.</p>
    * 
    * @param media The media attribute to be matched.  May be null, in which 
    *              case the prefered templates will be used (i.e. alternate = no).
    * TODO: ### if media is null, all stylesheets are returned, alternate is not used ###
    * @param title The value of the title attribute to match.  May be null.
    * @param charset The value of the charset attribute to match.  May be null.
    * @returns An array of InputSources that can be passed to processMultiple method.
    */
    
    public InputSource[] getAssociatedStylesheets(InputSource source,
                                                      String media, 
                                                      String title,
                                                      String charset)
    throws ProcessorException
    {
        PIGrabber grabber = new PIGrabber();
        grabber.setCriteria(media, title, charset);
        grabber.setBaseURI(source.getSystemId());

        XMLReader parser = sourceParser;
        if (parser==null) {
            parser = styleParser;
        }
        if (parser==null) {
            try {
                parser = ParserManager.makeParser();
            } catch (SAXException err) {
                throw new ProcessorException(err);
            }
        }

        parser.setContentHandler(grabber);
        try {
            parser.parse(source);   // this parse will be aborted when the first start tag is found
        } catch (Exception err) {
            if (!(err instanceof SAXException && err.getMessage().equals("#start#"))) { 
                throw new ProcessorException("Failed while looking for xml-stylesheet PI", err);
            }
        }
        try {
            return grabber.getAssociatedStylesheets();
        } catch (SAXException err) {
            throw new ProcessorException(err.getMessage());
        }            
    }
  
    /**
    * Get a TemplatesBuilder object that can process SAX 
    * events into a Templates object, if the processor supports the 
    * "http://xml.org/trax/features/sax/input" feature.
    * @return A TemplatesBuilder object, or null if not supported.
    * @exception May throw a ProcessorException if a TemplatesBuilder 
    * can not be constructed for some reason.
    */
    
    public TemplatesBuilder getTemplatesBuilder() throws ProcessorException {
        return new StyleSheetBuilder();
    }
   
    /**
    * Set an XML parser for the stylesheet.  This may also 
    * be used for the XML input for the source tree, if 
    * the setXMLReader method on the Transformation 
    * method is not set.
    */
    
    public void setXMLReader(XMLReader reader) {
        styleParser = reader;
    }

    /**
    * Get the XML parser used for the templates.  This may also 
    * be used for the XML input for the source tree, if 
    * the setXMLReader method on the Transformation 
    * method is not set.
    * @return Valid XMLReader object, or null if none has been set.
    */
    
    public XMLReader getXMLReader()
    {
        return styleParser;
    }

  /**
   * Look up the value of a feature.
   * @param name The feature name, which is a fully-qualified
   *        URI. The only names recognized are
   * "http://xml.org/trax/features/dom/input" and
   * "http://xml.org/trax/features/sax/input", and in both cases
   * the value is always true. 
   * @return The current state of the feature (true or false).
   * @exception org.xml.sax.SAXNotRecognizedException When the
   *            Processor does not recognize the feature name.
   * @exception org.xml.sax.SAXNotSupportedException When the
   *            Processor recognizes the feature name but 
   *            cannot determine its value at this time.
   */
   
    public boolean getFeature (String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (name.equals("http://xml.org/trax/features/dom/input")) {
            return true;
        } else if (name.equals("http://xml.org/trax/features/sax/input")) {
            return true;
        } else {
            throw new SAXNotRecognizedException(name);
        }
    }
  
  /**
   * Set the state of a feature.
   *
   * @param name The feature name, which is a fully-qualified
   *        URI. The only names recognized are
   * "http://xml.org/trax/features/dom/input" and
   * "http://xml.org/trax/features/sax/input", and in both cases
   * the only permitted value is true. 
   * @param state The requested state of the feature (true or false).
   * @exception org.xml.sax.SAXNotRecognizedException When the
   *            Processor does not recognize the feature name.
   * @exception org.xml.sax.SAXNotSupportedException When the
   *            Processor recognizes the feature name but 
   *            cannot set the requested value.
   */
   
    public void setFeature (String name, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (name.equals("http://xml.org/trax/features/dom/input")) {
            if (!value) throw new SAXNotSupportedException(name);
        } else if (name.equals("http://xml.org/trax/features/sax/input")) {
            if (!value) throw new SAXNotSupportedException(name);
        } else {
            throw new SAXNotRecognizedException(name);
        }
    }
  

    /**
    * Inner class: a special Builder for the stylesheet document
    */

    private class StyleSheetBuilder extends Builder implements TemplatesBuilder {

        public StyleSheetBuilder() {
            Stripper styleStripper = new Stripper();
            styleStripper.setPreserveSpace(new AnyNameTest(), false);
            styleStripper.setPreserveSpace(Name.reconstruct("xsl", Namespace.XSLT, "text"), true);

            setStripper(styleStripper);
            setNodeFactory(new StyleNodeFactory());
            setDiscardCommentsAndPIs(true);
            setLineNumbering(true);
        }

        /**
        * When this object is used as a ContentHandler or DocumentHandler, it will 
        * create a Templates object, which the caller can get once 
        * the SAX events have been completed.
        * @return The stylesheet object that was created during 
        * the SAX event process, or null if no stylesheet has 
        * been created.
        *
        * @version Alpha
        * @author <a href="mailto:scott_boag@lotus.com">Scott Boag</a>
        */
    
        public Templates getTemplates() throws TransformException {
            try {
                PreparedStyleSheet pss = new PreparedStyleSheet();
                pss.setStyleSheetDocument(getCurrentDocument());
                return pss;
            } catch(SAXException err) {
                throw new TransformException(err);
            }
        }
  
        /**
        * Set the base ID (URL or system ID) for the stylesheet 
        * created by this builder.  This must be set in order to 
        * resolve relative URLs in the stylesheet.
        * @param baseID Base URL for this stylesheet.
        */

        // TODO: this method is unnecessary and is likely to be removed from TRAX
        
        public void setBaseID(String baseID) {
            setSystemId(baseID);
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
