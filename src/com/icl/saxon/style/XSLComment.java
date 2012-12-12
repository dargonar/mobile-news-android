package com.icl.saxon.style;
import com.icl.saxon.*;

import com.icl.saxon.output.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* An xsl:comment elements in the stylesheet.<BR>
*/

public class XSLComment extends StyleElement {

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {
        String[] allowed = {};
        allowAttributes(allowed);
    }

    public void validate() throws SAXException {
        checkWithinTemplate();
    }

    public void process( Context context ) throws SAXException
    {
        String comment = expandChildren(context);
        while(true) {
            int hh = comment.indexOf("--");
            if (hh < 0) break;
            context.getController().reportRecoverableError("Invalid characters (--) in comment", this);
            comment = comment.substring(0, hh+1) + " " + comment.substring(hh+1);
        }
        if (comment.length()>0 && comment.charAt(comment.length()-1)=='-') {
            context.getController().reportRecoverableError("Invalid character (-) at end of comment", this);
            comment = comment + " ";
        }
        context.getOutputter().writeComment(comment);
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
