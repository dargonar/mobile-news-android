package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.util.*;

/**
* An xsl:namespace-alias element in the stylesheet.<BR>
*/

public class XSLNamespaceAlias extends StyleElement {

    private String stylesheetPrefix;
    private String resultPrefix;
    private String stylesheetURI;
    private String resultURI;

    public void prepareAttributes() throws SAXException {

        String[] allowed = {"stylesheet-prefix", "result-prefix"};
        allowAttributes(allowed);

        stylesheetPrefix = attributeList.getValue("stylesheet-prefix").intern();
        if (stylesheetPrefix==null) {
            reportAbsence("stylesheet-prefix");
        }
        if (stylesheetPrefix=="#default") {
            stylesheetPrefix="";
        }

        resultPrefix = attributeList.getValue("result-prefix").intern();
        if (resultPrefix==null) {
            reportAbsence("result-prefix");
        }
        if (resultPrefix=="#default") {
            resultPrefix="";
        }

    }

    public void validate() throws SAXException {
        checkTopLevel();
        stylesheetURI = getURIforPrefix(stylesheetPrefix);
        resultURI = getURIforPrefix(resultPrefix);

    }

    public void preprocess() throws SAXException {

    }

    public void process(Context c) {}

    public String getResultPrefix() {
        return resultPrefix;
    }

    public String getStylesheetURI() throws SAXException {
        if (stylesheetURI==null) validate();
        return stylesheetURI;
    }

    public String getResultURI() throws SAXException {
        if (resultURI==null) validate();
        return resultURI;
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
