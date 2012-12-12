package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;

import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.*;

/**
* An xsl:with-param element in the stylesheet.<BR>
* The xsl:with-param element has mandatory attribute name and optional attribute select
*/

public class XSLWithParam extends XSLGeneralVariable {

    public void validate() throws SAXException {
        super.validate();

        NodeInfo parent = (NodeInfo)getParentNode();
        if (!((parent instanceof XSLApplyTemplates) || (parent instanceof XSLCallTemplate))) {
            throw styleError("xsl:with-param must be child of xsl:apply-templates or xsl:call-template");
        }

    }

    public void process( Context context ) throws SAXException
    {}


    public Value getParamValue( Context context ) throws SAXException
    {
        return getSelectValue(context);
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
