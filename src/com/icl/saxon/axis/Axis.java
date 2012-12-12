package com.icl.saxon.axis;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import org.xml.sax.*;


/**
* An axis, that is a direction of navigation in the document structure.
*/

public abstract class Axis  {
   
    public final static int ANCESTOR = 1;
    public final static int ANCESTOR_OR_SELF = 2;
    public final static int ATTRIBUTE = 3;
    public final static int CHILD = 4;
    public final static int DESCENDANT = 5;
    public final static int DESCENDANT_OR_SELF = 6;
    public final static int FOLLOWING = 7;
    public final static int FOLLOWING_SIBLING = 8;
    public final static int NAMESPACE = 9;
    public final static int PARENT = 10;
    public final static int PRECEDING = 11;
    public final static int PRECEDING_SIBLING = 12;
    public final static int SELF = 13;

    protected int axisNumber;
    protected int nodeType;
    protected NameTest nodeName;

    public Axis() {
        nodeType = getPrincipalNodeType();
    }

    /**
    * resolve an axis name into a symbolic constant representing the axis
    */

    public static int lookup(String name) throws SAXException {
        if (name.equals("ancestor"))                return ANCESTOR;
        if (name.equals("ancestor-or-self"))        return ANCESTOR_OR_SELF;
        if (name.equals("attribute"))               return ATTRIBUTE;
        if (name.equals("child"))                   return CHILD;
        if (name.equals("descendant"))              return DESCENDANT;
        if (name.equals("descendant-or-self"))      return DESCENDANT_OR_SELF;
        if (name.equals("following"))               return FOLLOWING;
        if (name.equals("following-sibling"))       return FOLLOWING_SIBLING;
        if (name.equals("namespace"))               return NAMESPACE;   
        if (name.equals("parent"))                  return PARENT;
        if (name.equals("preceding"))               return PRECEDING;
        if (name.equals("preceding-sibling"))       return PRECEDING_SIBLING;
        if (name.equals("self"))                    return SELF;
        throw new SAXException("Unknown axis name: " + name);
    }

    /**
    * Factory method to make an axis of the specified type. The use of axisNumber is historic,
    * it would be better for the caller to say directly which Axis subclass it wants to create.
    */

    public static Axis make(int axisNumber, int nodeType, NameTest nameTest) throws SAXException {
        Axis axis = makeAxis(axisNumber);
        axis.setNodeType(nodeType);
        axis.setNameTest(nameTest);
        axis.setAxisNumber(axisNumber);
        return axis;
    }

    public static Axis makeAxis(int axisNumber) throws SAXException {        
        switch(axisNumber) {
            case ANCESTOR:
                return new AncestorAxis();
            case ANCESTOR_OR_SELF:
                return new AncestorOrSelfAxis();
            case ATTRIBUTE:
                return new AttributeAxis();
            case CHILD:
                return new ChildrenAxis();
            case DESCENDANT:
                return new DescendantsAxis();
            case DESCENDANT_OR_SELF:
                return new DescendantsOrSelfAxis();
            case FOLLOWING:
                return new FollowingAxis();
            case FOLLOWING_SIBLING:
                return new FollowingSiblingsAxis();
            case NAMESPACE:
                return new NamespaceAxis();
            case PARENT:
                return new ParentAxis();      
            case PRECEDING:
                return new PrecedingAxis();
            case PRECEDING_SIBLING:
                return new PrecedingSiblingsAxis();
            case SELF:
                return new SelfAxis();
            default:
                throw new SAXException("Invalid axis number in call to Axis.make()");
        }
    }

    /**
    * set the axis number
    */

    private void setAxisNumber(int n) {
        axisNumber = n;
    }

    /**
    * Get the axis number (e.g. PARENT or PRECEDING_SIBLING)
    */

    public int getAxisNumber() {
        return axisNumber;
    }

    /**
    * Set the node type required by the axis.
    * @param nodeType e.g. NodeInfo.ELEMENT for an element, NodeInfo.NODE for any node type
    */

    public void setNodeType(int type) {
        nodeType = type;
    }

    /**
    * Get the principal node type for the axis. This is the default node type, and is
    * Node.ELEMENT for all axes except the Attribute and Namespace axes
    */

    public int getPrincipalNodeType() {
        return NodeInfo.ELEMENT;
    }

    /**
    * Set the name test required by the axis.
    * @param nodeType e.g. NodeInfo.ELEMENT for an element, NodeInfo.NODE for any node type
    */

    public void setNameTest(NameTest test) {
        nodeName = test;
    }

    /**
    * Return an enumeration over the nodes reached by the given axis from a specified node
    * @param node NodeInfo representing the node from which the enumeration starts
    * @param nodeType the type(s) of node to be included, e.g. NodeInfo.ELEMENT, NodeInfo.TEXT.
    * The value NodeInfo.NODE means include any type of node.
    * @param nodeNameTest include only nodes with this name (e.g. an element name). Set this to null
    * to include nodes of any name.
    * @return a NodeEnumeration that scans the nodes reached by the axis in turn.
    * @throws SAXException if an invalid axis is specified
    */

    public abstract NodeEnumeration getEnumeration(NodeInfo node)
        throws SAXException;


    /**
    * An Axis has the sorted property if it returns nodes in document order
    */

    public abstract boolean isSorted();

    /**
    * If an axis is not in document order, then it is in reverse document order
    */

    public boolean isReverseSorted() {
        return !isSorted();
    }

    /**
    * An Axis has the sibling property if all the nodes it returns have the same parent
    */

    public abstract boolean isSibling() ;

    /**
    * An axis has the peer property if no node in the result is an ancestor of another
    * (redundant: if this is true the sibling property will also be true)
    */

    public abstract boolean isPeer();

    /**
    * An axis has the withinSubtree property if all the nodes it returns are in the subtree
    * rooted at the start node, that is if it is a subset of the descendants-of-self axis
    */

    public abstract boolean isWithinSubtree();

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
