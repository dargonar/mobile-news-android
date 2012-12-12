package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.DocumentInfo;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.text.*;



public class Key extends Function {

    private DocumentInfo boundDocument = null;
    private Controller boundController = null;

    public String getName()      { return "key"; };

    /**
    * Determine the data type of the exprssion, if possible
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
        int dependencies = Context.NO_DEPENDENCIES;
        if (boundDocument == null) {
            dependencies |= Context.CONTEXT_NODE;
        } 
        if (boundController == null) {
            dependencies |= Context.CONTROLLER;
        }
        return dependencies;
    }

    /**
    * Remove intrinsic dependencies. 
    */

    protected Expression reduceIntrinsic(int dependencies, Context context)
            throws SAXException {
        checkArgumentCount(2, 2);
        if ((dependencies & Context.CONTEXT_NODE) != 0) {
            if (boundController==null || boundDocument==null) {
                Key key = new Key();
                key.addArgument((Expression)arguments.elementAt(0));
                key.addArgument((Expression)arguments.elementAt(1));
                if (boundDocument==null) {
                    key.boundDocument = context.getContextNode().getDocumentRoot();
                } else {
                    key.boundDocument = boundDocument;
                }
                if (boundController==null) {
                    key.boundController = context.getController();
                } else {
                    key.boundController = boundController;
                }
                key.setStaticContext(getStaticContext());
                return key;
            } else {
                return this;
            }
        } else if ((dependencies & Context.CONTROLLER) != 0) {
            if (boundController==null) {
                Key key = new Key();
                key.addArgument((Expression)arguments.elementAt(0));
                key.addArgument((Expression)arguments.elementAt(1));
                key.boundController = context.getController();
                key.setStaticContext(getStaticContext());
                return key;
            } else {
                return this;
            }
        } else {
            return this;
        }
    }

    /**
    * Make a new instance of this function
    */

    public Function newInstance() throws SAXException {
        Key key = new Key();
        key.boundDocument = boundDocument;
        key.boundController = boundController;
        key.setStaticContext(getStaticContext());
        return key;
    }


    /**
    * Evaluate the expression
    */

    public Value eval(Vector args, Context context) throws SAXException {

        int numArgs = checkArgumentCount(2, 2);

        Value arg0 = (Value)args.elementAt(0);
        Value arg1 = (Value)args.elementAt(1);

        Controller controller = boundController;
        if (controller==null) controller = context.getController();

        DocumentInfo doc = boundDocument;
        if (doc==null) doc = context.getContextNode().getDocumentRoot();
        
        String givenkeyname = arg0.asString();
        String abskeyname = getStaticContext().makeName(givenkeyname, false).getAbsoluteName();
        return findKey(controller, doc, abskeyname, arg1);
    }

    /**
    * Construct the NodeSet that satisfies the given key
    * @param controller The controller (to get the key definitions)
    * @param doc The document to search
    * @param keyname The absolute (expanded) name of the key
    * @param arg1 The value of the key (or nodeset containing the values)
    * @param context The execution context
    */

    private NodeSetValue findKey(
                            Controller controller,
                            DocumentInfo doc,
                            String keyname,
                            Value arg1) throws SAXException {
        
        KeyManager keyManager = controller.getKeyManager();

        Vector result = new Vector();
        boolean sorted;

        if (arg1 instanceof NodeSetValue) {

            NodeSetValue keyvals = (NodeSetValue)arg1;
            sorted = false;

            NodeEnumeration nodes = keyvals.enumerate();
        
            while (nodes.hasMoreElements()) {
                Vector onekeyresult =
                    keyManager.selectByKey(keyname, doc, nodes.nextElement().getValue(), controller);
                for (int r=0; r<onekeyresult.size(); r++) {
                    result.addElement(onekeyresult.elementAt(r));
                }
            }

        } else {
            result = keyManager.selectByKey(keyname, doc, arg1.asString(), controller);
            sorted = true;
        }

        switch (result.size()) {
            case 0:
                return new EmptyNodeSet();
            case 1:
                return new SingletonNodeSet((NodeInfo)result.elementAt(0));
            default:
                NodeSetValue nsv = new NodeSetExtent(result);
                nsv.setSorted(sorted);
                return nsv;
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
