package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.*;
import java.util.*;

/**
* An xsl:message element in the stylesheet.<BR>
*/

public class XSLMessage extends StyleElement {

    boolean terminate = false;

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {

        String[] allowed = {"terminate"};
        allowAttributes(allowed);

        String terminateAtt = getAttributeValue("terminate");
        if (terminateAtt!=null) {
            if (terminateAtt.equals("yes")) {
                terminate = true;
            } else if (terminateAtt.equals("no")) {
                terminate = false;
            } else {
                styleError("terminate must be \"yes\" or \"no\"");
            }
        }
    }

    public void validate() throws SAXException {
        checkWithinTemplate();
    }

    public void process( Context context ) throws SAXException
    {
        Controller c = context.getController();
        OutputDetails details = new OutputDetails();
        Emitter emitter = c.getMessageEmitter();
        if (emitter==null) {
            details.setEmitter(new MessageEmitter());
        } else {
            details.setEmitter(emitter);
        }
        details.setWriter(new PrintWriter(System.err));
                    // the Emitter can ignore this if it wants to
        details.setMethod("saxon:user");
        details.setCloseAfterUse(false);
        details.setOmitDeclaration("yes");
        c.setNewOutputDetails(details);

        processChildren(context);

        c.resetOutputDetails();
                
        if (terminate) {
            throw styleError("Processing terminated");
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
