package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import com.icl.saxon.Context;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;
import org.w3c.dom.*;

/**
  * ProcInstImpl is an implementation of ProcInstInfo used by the Propagator to construct
  * its trees.
  * @author Michael H. Kay (Michael.Kay@icl.com)
  * @version 16 July 1999 
  */
  

class ProcInstImpl extends NodeImpl implements ProcInstInfo, ProcessingInstruction {
    
    String target;
    String content;
    Name name;
    String systemId;
    int lineNumber = -1;
    
    public ProcInstImpl(String target, String content) {
        this.name = new Name(target);
        this.target = target;
        this.content = content;
    }

    public final Name getExpandedName() {
        return name;
    }

    public String getDisplayName() {
        return target;
    }

    public String getValue() {
        return content;
    }

    public final short getNodeType() {
        return PI;
    }

    /**
    * Set the base URI and line number
    */

    public void setLocation(String uri, int lineNumber) {
        this.systemId = uri;
        this.lineNumber = lineNumber;
    }

    /**
    * Get the base URI for the node.
    */

    public String getSystemId() {
        return systemId;
    }

    /**
    * Get the line number of the node within its source document entity
    */

    public int getLineNumber() {
        return lineNumber;
    }

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws SAXException {
        out.writePI(target, content);
    }

    /**
    * Perform default action for this kind of node (built-in template rule)
    */

    public void defaultAction(Context c) {
        // do nothing
    }

    /**
    * Generate a path to this node
    */

    public String getPath() {
        String pre = ((NodeInfo)getParentNode()).getPath();
        return (pre.equals("/") ? "" : pre) +
               "/processing-instruction()[" + getNumberSimple() + "]";
    }

    /**
    * Get a pseudo-attribute. This is useful only if the processing instruction data part
    * uses pseudo-attribute syntax, which it does not have to. This syntax is as described
    * in the W3C Recommendation "Associating Style Sheets with XML Documents". 
    * @return the value of the pseudo-attribute if present, or null if not
    */

    public String getPseudoAttribute(String name) {
        return ProcInstParser.getPseudoAttribute(content, name);
    }

    // DOM methods
    
    /**
     * The target of this processing instruction. XML defines this as being 
     * the first token following the markup that begins the processing 
     * instruction.
     */
     
    public String getTarget() {
        return target;
    }

    /**
     *  The content of this processing instruction. This is from the first non 
     * white space character after the target to the character immediately 
     * preceding the <code>?&gt;</code> .
     */
     
    public String getData() {
        return content;
    }

    /**
     * Set the content of this PI. Always fails.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     */
    
    public void setData(String data) throws DOMException {
        disallowUpdate();
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
