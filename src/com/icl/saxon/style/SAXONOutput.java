package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.*;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;

/**
* A saxon:output element in the stylesheet. <BR>
* The saxon:output element takes an attribute file="filename". The filename will
* often contain parameters, e.g. {@saxon:nr} to ensure that a different file is produced
* for each element instance. <BR>
* There is a further attribute method=xml|html|text which determines the format of the
* output file (default XML).
* Alternatively the xsl:output element may take a next-in-chain attribute in which case
* output is directed to another stylesheet.
* There is a user-data attribute which is used only to pass information to a user-specified
* Emitter (as part of the OutputDetails). It is an attribute value template.
*/

public class SAXONOutput extends StyleElement {

    Expression file;
    Expression userData;
    Expression method = null;
    Expression version = null;
    Expression indent = null;
    Expression encoding = null;
    Expression mediaType = null;
    Expression doctypeSystem = null;
    Expression doctypePublic = null;
    Expression omitDeclaration = null;
    Expression standalone = null;
    Expression cdataElements = null;
    Expression nextInChain = null;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {
        
        checkAllowedAttributes();

        String fileAtt = getAttributeValue("file");
        if (fileAtt != null) {
            file = AttributeValueTemplate.make(fileAtt, this);
        }

        String userDataAtt = getAttributeValue("user-data");
        if (userDataAtt != null) {
            userData = AttributeValueTemplate.make(userDataAtt, this);
        }

        String methodAtt = getAttributeValue("method");
        if (methodAtt != null) {
            method = AttributeValueTemplate.make(methodAtt, this);
        }

        String versionAtt = getAttributeValue("version");
        if (versionAtt != null) {
            version = AttributeValueTemplate.make(versionAtt, this);
        }

        String indentAtt = getAttributeValue("indent");
        if (indentAtt != null) {
            indent = AttributeValueTemplate.make(indentAtt, this);
        }        

        String encodingAtt = getAttributeValue("encoding");
        if (encodingAtt != null) {
            encoding = AttributeValueTemplate.make(encodingAtt, this);
        }

        String mediaTypeAtt = getAttributeValue("media-type");
        if (mediaTypeAtt != null) {
            mediaType = AttributeValueTemplate.make(mediaTypeAtt, this);
        }

        String doctypeSystemAtt = getAttributeValue("doctype-system");
        if (doctypeSystemAtt != null) {
            doctypeSystem = AttributeValueTemplate.make(doctypeSystemAtt, this);
        }

        String doctypePublicAtt = getAttributeValue("doctype-public");
        if (doctypePublicAtt != null) {
            doctypePublic = AttributeValueTemplate.make(doctypePublicAtt, this);
        }

        String omitDeclarationAtt = getAttributeValue("omit-xml-declaration");
        if (omitDeclarationAtt != null) {
            omitDeclaration = AttributeValueTemplate.make(omitDeclarationAtt, this);
        }

        String standaloneAtt = getAttributeValue("standalone");
        if (standaloneAtt != null) {
            standalone = AttributeValueTemplate.make(standaloneAtt, this);
        }

        String cdataAtt = getAttributeValue("cdata-section-elements");
        if (cdataAtt != null) {
            cdataElements = AttributeValueTemplate.make(cdataAtt, this);
        }

        String nextInChainAtt = getAttributeValue("next-in-chain");
        if (nextInChainAtt != null) {
            nextInChain = AttributeValueTemplate.make(nextInChainAtt, this);
        }
        
        if (nextInChain!=null && fileAtt!=null) {
            throw styleError("The file attribute and the next-in-chain attribute must not both be present");
        }
        

    }

    protected void checkAllowedAttributes() throws SAXException {
        String[] allowed = {"method", "version", "indent", "encoding", "media-type",
            "doctype-system", "doctype-public", "omit-xml-declaration", "standalone",
             "cdata-section-elements", "file", "next-in-chain", "user-data"};
        allowAttributes(allowed);
    }

    public void validate() throws SAXException {
        checkWithinTemplate();
    }
    
    public void process( Context context ) throws SAXException
    {
        Controller c = context.getController();
        OutputDetails prev = c.getCurrentOutputDetails();
        OutputDetails details =  new OutputDetails(prev);
        OutputManager manager = c.getOutputManager();

        if (file != null) {

            // following code to create any directory that doesn't exist is courtesy of
            // Brett Knights [brett@knightsofthenet.com]
            // Modified by MHK to work with JDK 1.1
            
            String outFileName = file.evaluateAsString(context);
            try {
			    File outFile = new File(outFileName);
		        if (!outFile.exists()) {
		            String parent = outFile.getParent();        // always returns null with Microsoft JVM?
		            if (parent!=null) {
        				File parentPath = new File(parent);
        				if (parentPath != null) {
        					if (!parentPath.exists()) {
        						parentPath.mkdirs();
        					}
        				}
    				    outFile.createNewFile();
		            }
			    }

                details.setOutputStream(new FileOutputStream(outFile));
                details.setWriter(null);
            } catch (java.io.IOException err) {
                throw new SAXException("Failed to create output file " + outFileName, err);
            }
        }

        if (userData != null) {
            String data = userData.evaluateAsString(context);
            details.setUserData(data);
        }

        if (method != null) {
            String data = method.evaluateAsString(context);
            if (data.equals("xml") || data.equals("html") || data.equals("text"))  {
                // OK
            } else {
                Name methodName = new Name(data, this, false);
                if (methodName.getPrefix().equals("")) {
                    throw styleError("method must be xml, html, or text, or a prefixed name");
                }
                String localName = methodName.getLocalName();  // don't care what the prefix is
                
//                if (localName.equals("fop")) { 
//                    details.setEmitter(new FOPEmitter());
//                    data="saxon:user";
//                } else
                if (localName.equals("xhtml")) { 
                    data="xhtml";
                } else {
                    details.setEmitter(XSLOutput.makeEmitter(localName));
                    data="saxon:user";
                } 
            }
            details.setMethod(data);
        }

        if (version != null) {
            String data = version.evaluateAsString(context);
            details.setVersion(data);
        }

        if (indent != null) {
            String data = indent.evaluateAsString(context);
            if (data==null || data.equals("yes") || data.equals("no")) {
                details.setIndent(data);
            } else {
                try {
                    int indentSpaces = Integer.parseInt(data);
                    details.setIndent("yes");
                    details.setIndentSpaces(indentSpaces);    
                } catch (NumberFormatException err) {
                    throw styleError("indent must be yes or no or an integer");
                }
            }
        }         

        if (encoding != null) {
            String data = encoding.evaluateAsString(context);
            details.setEncoding(data);
        }    

        if (mediaType != null) {
            String data = mediaType.evaluateAsString(context);
            details.setEncoding(data);
        }    

        if (doctypeSystem != null) {
            String data = doctypeSystem.evaluateAsString(context);
            details.setDoctypeSystem(data);
        }
        
        if (doctypePublic != null) {
            String data = doctypePublic.evaluateAsString(context);
            details.setDoctypePublic(data);
        }   

        if (omitDeclaration != null) {
            String data = omitDeclaration.evaluateAsString(context);
            if (data.equals("yes") || data.equals("no")) {
                details.setOmitDeclaration(data);
            } else {
                throw styleError("omit-xml-declaration attribute must be yes or no");
            }
        }   

        if (standalone != null) {
            String data = standalone.evaluateAsString(context);
            if (data.equals("yes") || data.equals("no")) {
                details.setStandalone(data);
            } else {
                throw styleError("omit-xml-declaration attribute must be yes or no");
            }
        }  

        if (cdataElements != null) {
            String data = cdataElements.evaluateAsString(context);
            StringTokenizer st = new StringTokenizer(data);
            while (st.hasMoreTokens()) {
                String displayname = st.nextToken();
                Name fullname = new Name(displayname, this, true);
                details.addCdataElement(fullname);
            }
        }

        if (nextInChain != null) {
            String data = nextInChain.evaluateAsString(context);
            Controller nextStyleSheet = prepareNextStylesheet(data);
            ContentHandlerProxy emitter = new ContentHandlerProxy();
            emitter.setUnderlyingContentHandler(nextStyleSheet.getInputContentHandler());
            emitter.setRequireWellFormed(false);
            details.setEmitter(emitter);
            details.setMethod("saxon:user");
        }  
        
        if (details.getMethod()==null) {             
            details.setMethod("saxon:uncommitted");
        }

        
        manager.setOutputDetails(details);        
        processChildren(context);
        manager.resetOutputDetails();     
    }

    /**
    * Prepare another stylesheet to handle the output of this one
    */

    private Controller prepareNextStylesheet(String href) throws SAXException {

        URL baseURL;
        URL includedURL;
        try {
            baseURL = new URL(getSystemId());
        } catch (java.net.MalformedURLException err) {
            throw new SAXException("saxon:output - invalid base URL " + getSystemId());
        }
        try {
            includedURL = new URL(baseURL, href);
        } catch (java.net.MalformedURLException err) {
            throw new SAXException("xsl:include - invalid target URL " + href);
        }
        
        ExtendedInputSource nextSheet = new ExtendedInputSource(includedURL.toString());

        PreparedStyleSheet next = new PreparedStyleSheet();
                        // use the same stylesheet parser as before
        next.setXMLReader( getPreparedStyleSheet().getXMLReader() ); 
        next.prepare(nextSheet);

        return (Controller)next.newTransformer();
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
// Additional Contributor(s): Brett Knights [brett@knightsofthenet.com]
//
