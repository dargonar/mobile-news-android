package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.*;
import java.util.*;

/**
* Handler for saxon:preview elements in stylesheet. <BR>
* Attributes: <br>
* mode identifies the mode in which preview templates will be called. <br>
* elements is a space-separated list of element names which are eligible for preview processing.
*/

public class SAXONPreview extends StyleElement {

    String previewMode = null;
    String elements = null;

    
    public void prepareAttributes() throws SAXException {

        String[] allowed = {"mode", "elements"};
        allowAttributes(allowed);

        previewMode = attributeList.getValue("mode");        
        if (previewMode==null) {
            reportAbsence("mode");
        }
        elements = attributeList.getValue("elements");
        if (elements==null) {
            reportAbsence("elements");
        }        
    }

    public void validate() throws SAXException {
        checkTopLevel();
    }

    public void preprocess() throws SAXException
    {
        XSLStyleSheet sheet = getPrincipalStyleSheet();
        PreviewManager pm = sheet.getPreviewManager();
        if (pm==null) {
            pm = new PreviewManager();
            sheet.setPreviewManager(pm);
        }
        pm.setPreviewMode(previewMode);

        StringTokenizer st = new StringTokenizer(elements);
        while (st.hasMoreTokens()) {
            String elementName = st.nextToken();
            pm.setPreviewElement(new Name(elementName, this, true));
        }
    }

    public void process(Context context) throws SAXException {}

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
