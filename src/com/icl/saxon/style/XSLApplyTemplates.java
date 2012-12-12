package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.TextInfo;
import com.icl.saxon.expr.*;
import com.icl.saxon.axis.Axis;

import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import org.w3c.dom.Node;
import java.util.*;
import java.io.*;

/**
* An xsl:apply-templates element in the stylesheet
*/

public class XSLApplyTemplates extends StyleElement {

    private Expression select;
    private boolean usesParams;
    private String modeName = null;            // null if no mode specified
    private Mode mode;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {

        String[] allowed = {"mode", "select"};
        allowAttributes(allowed);

        String modeAttribute = getAttributeValue("mode");
        if (modeAttribute!=null) {
            Name mname = new Name(modeAttribute, this, false);
            modeName = mname.getAbsoluteName();
        }
        
        String selectAtt = getAttributeValue("select");        
        if (selectAtt!=null) {
            select = Expression.make(selectAtt, this);
        }
    }

    public void validate() throws SAXException {

        checkWithinTemplate();

        // get the Mode object
        mode = getPrincipalStyleSheet().getRuleManager().getMode(modeName);

        // handle sorting if requested

        boolean sorted = false;
        Node child = getFirstChild();
        while (child!=null) {
            if (child instanceof XSLSort) {
                sorted = true;
            } else if (child instanceof XSLWithParam) {
                usesParams = true;
            } else {
                if (child instanceof TextInfo) {
                    // with xml:space=preserve, white space nodes may still be there
                    if (!((TextInfo)child).isWhite()) {
                        throw styleError(
                            "No character data allowed within xsl:apply-templates");
                    }
                } else {                
                    throw styleError(
                        "Invalid element within xsl:apply-templates: ");
                }
            }
            child = child.getNextSibling();
        }
        
        if (select==null && sorted) {
            select = new PathExpression(
                            new ContextNodeExpression(),
                            new Step(Axis.CHILD, NodeInfo.NODE, new AnyNameTest()));
        }
        if (select!=null) {
            select = handleSortKeys(select);
        }
    }
        
    public void process( Context context ) throws SAXException
    {
        // handle parameters if any
        
        ParameterSet params = null;
        if (usesParams) {
            params = new ParameterSet();
            Node child = getFirstChild();
            while (child!=null) {
                if (child instanceof XSLWithParam) {
                    XSLWithParam param = (XSLWithParam)child;
                    params.put(param.getVariableName(), param.getParamValue(context));                    
                }
                child = child.getNextSibling();
            }
        }

        // Process the selected nodes in the source document

        context.getController().applyTemplates(context, select, mode, params);

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
