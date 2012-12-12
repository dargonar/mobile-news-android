package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.output.*;
import com.icl.saxon.Version;
import com.icl.saxon.Context;

import java.util.*;
import java.io.Writer;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.w3c.dom.*;

/**
  * A node in the XML parse tree representing character content<P>
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

class TextImpl extends NodeImpl implements TextInfo, Text {   

    private int start;
    private int length;
    private DocumentImpl root;

    public TextImpl(DocumentImpl root, int start, int length) {
        this.root = root;
        this.start = start;
        this.length = length;
    }

    public DocumentInfo getDocumentRoot() {
        return root;
    }

    /**
    * Return the character value of the node. 
    * @return the string value of the node
    */

    public String getValue() {
        if (Version.isPreJDK12()) {
            char[] dest = new char[length];
            root.getCharacterBuffer().getChars(start, start+length, dest, 0);
            return new String(dest, 0, length);
        } else {
            return root.getCharacterBuffer().substring(start, start+length);
        }
    }   

    /**
    * Append the string value of the node to a StringBuffer
    * @param buffer The StringBuffer to which the value will be appended
    */

    public void appendValue(StringBuffer buffer) {
        char[] dest = new char[length];
        root.getCharacterBuffer().getChars(start, start+length, dest, 0);
        buffer.append(dest, 0, length);
    }   


    /**
    * Return the type of node.
    * @return Node.TEXT
    */

    public final short getNodeType() {
        return TEXT;
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
    * @return "#text"
    */

    public final String getNodeName() {
        return "#text";
    }
    
    /**
    * Copy this node to a given outputter
    */

    public void copy(Outputter out) throws SAXException {
        out.writeContent(root.getCharacterBuffer(), start, length);
    }

    /**
    * Copy the string-value of this node to a given outputter
    */

    public void copyStringValue(Outputter out) throws SAXException {
        out.writeContent(root.getCharacterBuffer(), start, length);
    }

    /**
    * Perform default action for this kind of node (built-in template rule)
    */

    public void defaultAction(Context c) throws SAXException {
        c.getOutputter().writeContent(root.getCharacterBuffer(), start, length);
    }

    /**
    * Determine if node is all-whitespace
    */

    public boolean isWhite() {
        StringBuffer buffer = root.getCharacterBuffer();
        for (int i=0; i<length; i++) {
            char c = buffer.charAt(start+i);
            // all valid XML whitespace characters, and only whitespace characters, are <= 0x20
            if (((int)c) > 32) {        
                return false;
            }
        }
        return true;
    }

    /**
    * Get start position in buffer
    */

    public int getStartPosition() {
        return start;
    }

    /**
    * Get length of value (in Java characters)
    */

    public int getLength() {
        return length;
    }

    /**
    * Generate a path to this node
    */

    public String getPath() {
        String pre = ((NodeInfo)getParentNode()).getPath();
        return (pre.equals("/") ? "" : pre) + "/text()[" + getNumberSimple() + "]";
    }

    /**
    * Return a string representation of the node
    */

    public String toString() {
        try {
            String n = parent.getDisplayName();
            String v = getValue();
            if (v.length()>40) {
                v = v.substring(0,40) + " ...";
            }
            return "<" + n + ">" + v + "</" + n + ">";
        }
        catch (Exception err) {
            return "#text";
        }
    }

    /**
    * Append extra content to the value of the node. For system use only, while building the tree
    */

    public void increaseLength(int extra) {
        length += extra;
    }

    /**
    * Delete string content of this and all subsequent nodes. For use when deleting
    * an element in preview mode
    */

    public void truncateToStart() {
        root.getCharacterBuffer().setLength(start);
    }

    //////////////////////////////////////////////////////////////////////////
    // implement DOM methods
    //////////////////////////////////////////////////////////////////////////
    
    /**
     *  The character data of the text node. (DOM) 
     */
     
    public String getData() {
        return getValue();       
    }

    /**
     *  Change the character data of the text node. DOM method. Always fails. 
     */

    public void setData(String data) throws DOMException {
        disallowUpdate();
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

    /**
     *  Breaks this node into two  nodes at the specified <code>offset</code> 
     * , keeping both in the tree as siblings. Always fails.
     * @param offset  The  16-bit unit offset at which to split, starting from 
     *   <code>0</code> .
     * @return  The new node, of the same type as this node.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
     
    public Text splitText(int offset)
                          throws DOMException {
        disallowUpdate();
        return null;
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
