package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.output.OutputDetails;
import com.icl.saxon.expr.*;
import com.icl.saxon.trace.TraceListener;   // e.g.
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
//import java.io.*;
import java.util.*;

/**
* Handler for saxon:function elements in stylesheet. <BR>
* Attributes: <br>
* name gives the name of the function
*/

public class SAXONFunction extends StyleElement {

    Name functionName = null;
    Procedure procedure = new Procedure();
    private OutputDetails disallowedOutput = new OutputDetails();
    
    public void prepareAttributes() throws SAXException {

        disallowedOutput.setMethod("saxon:error");

        String[] allowed = {"name"};
        allowAttributes(allowed);

        functionName = getFunctionName();
    }

    public void validate() throws SAXException {
        checkTopLevel();
    }

    public void preprocess() throws SAXException {
        getPrincipalStyleSheet().allocateLocalSlots(procedure.getNumberOfVariables());
    }

    public void process(Context context) throws SAXException {}

    /**
    * Get associated Procedure (for details of stack frame)
    */

    public Procedure getProcedure() {
        return procedure;
    }

    public Name getFunctionName() throws SAXException {
        if (functionName==null) {
            String nameAtt = attributeList.getValue("name");        
            if (nameAtt==null) {
                reportAbsence("name");
            }
            functionName = new Name(nameAtt, this, false);
            if (functionName.getURI()=="") {
                throw styleError("Extension function name must have a non-null namespace URI");
            }
        }
        return functionName;
    }

    /**
    * Get the name of the n'th parameter (starting from 0). Return null if there is none such.
    */

    public String getNthParameter(int n) throws SAXException {
        NodeInfo node = (NodeInfo)getFirstChild();
        int pos = 0;
        while (node!=null) {
            if (node instanceof XSLParam) {
                if (pos==n) {
                    return ((XSLParam)node).getVariableName();
                } else {
                    pos++;
                }
            }
            node = (NodeInfo)node.getNextSibling();
        }
        return null;
    }
            
        

    /**
    * Call this function
    */

    public Value call(ParameterSet params, Context context) throws SAXException {
        Bindery bindery = context.getBindery();
        bindery.openStackFrame(params);
        Controller controller = context.getController();
        controller.setNewOutputDetails(disallowedOutput);

    	if (controller.isTracing()) { // e.g.
    	    TraceListener listener = controller.getTraceListener();		    
    	    listener.enter(this, context);
    	    processChildren(context);
    	    listener.leave(this, context);
    	} else {
    	    processChildren(context);
    	}

        controller.resetOutputDetails();
        bindery.closeStackFrame();
        Value result = context.getReturnValue();
        if (result==null) {
            result = new StringValue("");
        }
        context.setReturnValue(null);
        return result;
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
// Contributor(s): 
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//
