package com.icl.saxon.output;

import java.util.*;
import java.io.*;
import org.xml.sax.*;

/**
  * This class handles the selection and configuration of an Outputter to create
  * serialized output, and maintains a stack of outputters so that when one destination
  * is closed, output reverts to the previous one.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 26 August 1999
  */

public class OutputManager {

    private Stack outputterStack = new Stack();
    private Outputter currentOutputter;
    private static Writer dummyWriter = new StringWriter();

    public OutputManager() {}
        
    
    /**
    * Set a new output writer, supplying the output format details. <BR>
    * This affects all further output until resetWriter() is called. Note that
    * it is the caller's responsibility to close the Writer after use: this is best
    * achieved by calling resetWriter(). 
    * @param outputDetails Details of the new output destination
    * @return the new current outputter
    */

    public Outputter setOutputDetails(OutputDetails outputDetails) throws SAXException {

        Outputter outputter = new Outputter();
        Emitter emitter;

        
        String format = outputDetails.getMethod();

        if (format.equals("html")) {
            emitter = new HTMLEmitter();
            if (outputDetails.isIndenting()) {
                HTMLIndenter in = new HTMLIndenter();
                in.setUnderlyingEmitter(emitter);
                emitter=in;
            }
        } else if (format.equals("xml")) {
            emitter = new XMLEmitter();
            if (outputDetails.isIndenting()) {
                XMLIndenter in = new XMLIndenter();
                in.setUnderlyingEmitter(emitter);
                emitter=in;
            }
            if (outputDetails.getCdataElements().size()>0) {
                CDATAFilter filter = new CDATAFilter();
                filter.setUnderlyingEmitter(emitter);
                emitter=filter;
            }            
        } else if (format.equals("text")) {
            emitter = new TEXTEmitter();

        } else if (format.equals("xhtml")) {
            emitter = new XHTMLEmitter();
            if (outputDetails.isIndenting()) {
                HTMLIndenter in = new HTMLIndenter();
                in.setUnderlyingEmitter(emitter);
                emitter=in;
            }
            if (outputDetails.getCdataElements().size()>0) {
                CDATAFilter filter = new CDATAFilter();
                filter.setUnderlyingEmitter(emitter);
                emitter=filter;
            }       

        } else if (format.equals("saxon:user")) {
            emitter = outputDetails.getEmitter();

        } else if (format.equals("saxon:fragment")) {
            emitter = outputDetails.getEmitter();
            outputDetails.setIndent("no");
            outputDetails.setWriter(dummyWriter);    // a kludge to stop one being created

        } else if (format.equals("saxon:uncommitted")) {
            emitter = new UncommittedEmitter();

        } else if (format.equals("saxon:dom")) {
            emitter = new DOMEmitter();    
            outputDetails.setWriter(dummyWriter);    // a kludge to stop one being created
            if (outputDetails.getDOMNode()==null) {
                throw new SAXException("Output to DOM requested, but no DOM Node supplied");
            }

        } else if (format.equals("saxon:error")) {
            emitter = new ErrorEmitter();    
            outputDetails.setWriter(dummyWriter);    // a kludge to stop one being created

        } else {
            emitter = new UncommittedEmitter();
        }

        outputter.setEmitter(emitter);
        emitter.setOutputDetails(outputDetails);

        Writer writer = outputDetails.getWriter();
        String encoding = outputDetails.getEncoding();
        if (encoding==null) encoding = "UTF8";
        if (encoding.equalsIgnoreCase("utf-8")) encoding = "UTF8";    // needed for Microsoft Java VM
        
        if (writer==null) {
            OutputStream outputStream = outputDetails.getOutputStream();
            if (outputStream==null) {
                outputStream = System.out;
                outputDetails.setCloseAfterUse(false);
            }
            while (true) {
                try {
                    writer = new BufferedWriter(
                                    new OutputStreamWriter(
                                        outputStream, encoding));
                    break;
                } catch (Exception err) {
                    if (encoding.equalsIgnoreCase("UTF8")) {
                        throw new SAXException("Failed to create a UTF8 output writer");
                    }
                    System.err.println("Encoding " + encoding + " is not supported: using UTF8");
                    encoding = "UTF8";
                }
            }
        }

        //System.err.println("setOutputDetails( emitter = " + emitter + ", writer = " + writer );
        emitter.setWriter(writer);
        outputter.setOutputDetails(outputDetails);
        
        CharacterSet charSet = CharacterSetFactory.makeCharacterSet(encoding);
        if (charSet==null) charSet = new ASCIICharacterSet();

        emitter.setCharacterSet(charSet);

        currentOutputter = outputter;
        outputterStack.push(outputter);

        outputter.open();
        return outputter;
    }

    /**
    * Get the current output details
    */

    public OutputDetails getOutputDetails() {
        return currentOutputter.getOutputDetails();
    }

    /**
    * Get the current outputter
    */

    public Outputter getOutputter() {
        return currentOutputter;
    }

    /**
    * Close the current outputter, and revert to the previous outputter.
    * @return the outputter that is now current
    */

    public Outputter resetOutputDetails() throws SAXException {
        //System.err.println("resetOutputDetails");
        if (currentOutputter==null) {
            throw new SAXException("No outputter has been allocated");
        }   
        outputterStack.pop();
        currentOutputter.close();
        if (outputterStack.isEmpty()) return null;
        currentOutputter = (Outputter)outputterStack.peek();
        return currentOutputter;
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
