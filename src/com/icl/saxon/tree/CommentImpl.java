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
  * CommentImpl is an implementation of CommentInfo used by the Propagator to construct
  * its trees.
  * @author Michael H. Kay (Michael.Kay@icl.com)
  * @version 2 August 1999 
  */
  

class CommentImpl extends NodeImpl implements CommentInfo, Comment {
    
    String comment;

    public CommentImpl(String content) {
        this.comment = content;
    }

    /**
    * Get the name of this node, following the XPath rules 
    * @return null
    */

    public final Name getExpandedName() {
        return null;
    }

    /**
    * Get the name of this node, following the DOM rules 
    * @return "#comment"
    */

    public final String getNodeName() {
        return "#comment";
    }

    public final String getValue() {
        return comment;
    }

    public final short getNodeType() {
        return NodeInfo.COMMENT;
    }

    /**
    * Generate a path to this node
    */

    public String getPath() {
        String pre = ((NodeInfo)getParentNode()).getPath();
        return (pre.equals("/") ? "" : pre) + "/comment()[" + getNumberSimple() + "]";
    }

    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws SAXException {
        out.writeComment(comment);
    }

    /**
    * Perform default action for this kind of node (built-in template rule)
    */

    public void defaultAction(Context c) {
        // do nothing
    }

    // implement DOM methods

    /**
     *  The character data of the node that implements this interface. 
     */
     
    public String getData() throws DOMException {
        return getValue();       
    }
    
    public void setData(String data) throws DOMException {
        disallowUpdate();
    }

    public int getLength() {
        return getValue().length();
    }

    /**
     *  Extracts a range of data from the node.
     * @param offset  Start offset of substring to extract.
     * @param count  The number of 16-bit units to extract.
     * @return  The specified substring. If the sum of <code>offset</code> and 
     *   <code>count</code> exceeds the <code>length</code> , then all 16-bit 
     *   units to the end of the data are returned.
     * @exception DOMException
     *    INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is 
     *   negative or greater than the number of 16-bit units in 
     *   <code>data</code> , or if the specified <code>count</code> is 
     *   negative.
     */
     
    public String substringData(int offset, int count) throws DOMException {
        try {
            return getValue().substring(offset, offset+count);
        } catch (IndexOutOfBoundsException err2) {
            throw new DOMExceptionImpl(DOMException.INDEX_SIZE_ERR,
                             "substringData: index out of bounds");
        }
    }

    /**
     * Append the string to the end of the character data of the node.
     * Always fails.
     * @param arg  The <code>DOMString</code> to append.
     * @exception DOMException
     *    NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
     
    public void appendData(String arg) throws DOMException {
        disallowUpdate();
    }

    /**
     *  Insert a string at the specified character offset. Always fails.
     * @param offset  The character offset at which to insert.
     * @param arg  The <code>DOMString</code> to insert.
     * @exception DOMException
     *    INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is 
     *   negative or greater than the number of 16-bit units in 
     *   <code>data</code> .
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
     
    public void insertData(int offset, String arg) throws DOMException {
        disallowUpdate();
    }

    /**
     *  Remove a range of  16-bit units from the node. Always fails.
     * @param offset  The offset from which to start removing.
     * @param count  The number of 16-bit units to delete. 
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
     
    public void deleteData(int offset, int count) throws DOMException {
        disallowUpdate();
    }

    /**
     *  Replace the characters starting at the specified  16-bit unit offset 
     * with the specified string. Always fails.
     * @param offset  The offset from which to start replacing.
     * @param count  The number of 16-bit units to replace. 
     * @param arg  The <code>DOMString</code> with which the range must be 
     *   replaced.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
     
    public void replaceData(int offset, 
                            int count, 
                            String arg) throws DOMException {
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
