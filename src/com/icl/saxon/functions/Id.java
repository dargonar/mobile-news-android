package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.lang.Math;
import java.text.*;



public class Id extends Function {

    private DocumentInfo boundDocument = null;

    public String getName()       { return "id"; };

    /**
    * Determine the data type of the expression, if possible
    * @return Value.NODESET
    */

    public int getDataType() {
        return Value.NODESET;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getIntrinsicDependencies() {
        if (boundDocument != null) {
            return Context.NO_DEPENDENCIES;
        } else {
            return Context.CONTEXT_NODE;    // needs to know the current document
        }
    }

    /**
    * Bind the function to a specific document
    */

    private void bindDocument(DocumentInfo doc) {
        boundDocument = doc;
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(1, 1);

        Value arg0 = (Value)args.elementAt(0);
        return findId(arg0, context);
    }

    /**
    * Remove intrinsic dependencies. 
    */

    protected Expression reduceIntrinsic(int dependencies, Context context)
            throws SAXException {
        checkArgumentCount(1, 1);
        if ((dependencies & Context.CONTEXT_NODE) != 0) {
            Id id = new Id();
            id.addArgument((Expression)arguments.elementAt(0));
            id.bindDocument(context.getContextNode().getDocumentRoot());
            id.setStaticContext(getStaticContext());
            return id;
        } else {
            return this;
        }
    }

    /**
    * This method actually evaluates the function
    */

    private NodeSetValue findId(Value arg0, Context context) throws SAXException {
        
        Vector idrefresult = null;
        DocumentInfo doc;
        if (boundDocument==null) {
            doc = context.getContextNode().getDocumentRoot();
        } else {
            doc = boundDocument;
        }
        
        if (arg0 instanceof NodeSetValue) {
            
            NodeEnumeration enuma = ((NodeSetValue)arg0).enumerate();
            while (enuma.hasMoreElements()) {
                NodeInfo node = enuma.nextElement();
                String s = node.getValue();
                StringTokenizer st = new StringTokenizer(s);
                while (st.hasMoreTokens()) {
                    NodeInfo el = doc.selectID(st.nextToken());
                    if (el!=null) {
                        if (idrefresult==null) {
                            idrefresult = new Vector(2);
                        }
                        idrefresult.addElement(el);
                    }
                }
            }

        } else {

            String s = arg0.asString();
            StringTokenizer st = new StringTokenizer(s);
            while (st.hasMoreTokens()) {
                NodeInfo el = doc.selectID(st.nextToken());
                if (el!=null) {
                    if (idrefresult==null) {
                        idrefresult = new Vector(2);
                    }
                    idrefresult.addElement(el);
                }
            }
        }

        if (idrefresult==null) {
            return new EmptyNodeSet();
        }
        if (idrefresult.size() == 1) {
            return new SingletonNodeSet((NodeInfo)idrefresult.elementAt(0));
        }
        return new NodeSetExtent(idrefresult);

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
