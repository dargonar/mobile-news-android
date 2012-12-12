package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* GroupActivation represents one activation of a saxon:group loop. It is implemented as a
* NodeEnumeration with one extra method, the ability to test whether the value of a key on
* the current node is the same as the value of the key on the next node. Control over the
* iteration is shared between the saxon:group and saxon:item elements.
*/

public class GroupActivation implements NodeEnumeration {

    private SAXONGroup group;
    private NodeEnumeration nodes;
    private Expression groupkey;
    private Context context;
    private NodeInfo next = null;
    private Value nextValue = null;
    private NodeInfo current = null;
    private Value currentValue = null;
    private int position = 0;
            
    public GroupActivation(SAXONGroup group, Expression groupkey, NodeEnumeration nodes, Context c)
            throws SAXException
    {
        this.group = group;
        this.groupkey = groupkey;
        this.nodes = nodes;
        this.context = c;
        this.position = 0;
        this.current = null;
        this.currentValue = null;
        lookAhead();
    }

    private void lookAhead() throws SAXException {
        if (nodes.hasMoreElements()) {
            next = nodes.nextElement();
            context.setCurrentNode(next);   
            context.setContextNode(next);  
            context.setPosition(position+1);
            nextValue = groupkey.evaluate(context);
        } else {
            next = null;
        }
    }

    public boolean hasMoreElements() throws SAXException {
        return next != null;
    }

    public NodeInfo nextElement() throws SAXException {
        current = next;
        currentValue = nextValue;
        position++;     
        lookAhead();
        context.setCurrentNode(current);
        context.setContextNode(current);
        context.setPosition(position);   
        return current;
    }

    public boolean sameAsNext() throws SAXException {
        if (next==null) return false;
        return (currentValue.equals(nextValue));            
    }

    public boolean isSorted() throws SAXException {
        return nodes.isSorted();
    }

    public boolean isReverseSorted() throws SAXException {
        return nodes.isReverseSorted();
    }

    public boolean isPeer() throws SAXException {
        return nodes.isPeer();
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
