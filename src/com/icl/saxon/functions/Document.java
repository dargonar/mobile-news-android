package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Builder;
import com.icl.saxon.tree.DocumentImpl;
import com.icl.saxon.expr.*;
import com.icl.saxon.trax.URIResolver;
import org.xml.sax.*;
import org.w3c.dom.Node;
import java.util.*;
import java.net.*;


public class Document extends Function {

    private Controller boundController = null;

    public String getName()      { return "document"; };

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
        if (boundController == null) {
            return Context.CONTROLLER;
        } else {
            return Context.NO_DEPENDENCIES;
        }
    }

    /**
    * Remove intrinsic dependencies. 
    */

    protected Expression reduceIntrinsic(int dependencies, Context context)
            throws SAXException {
        int numArgs = checkArgumentCount(1, 2);
        if ((dependencies & Context.CONTROLLER) != 0) {
            Document doc = new Document();
            doc.addArgument((Expression)arguments.elementAt(0));
            if (numArgs==2) {
                doc.addArgument((Expression)arguments.elementAt(1));
            }
            doc.boundController = context.getController();
            doc.setStaticContext(getStaticContext());
            return doc;
        } else {
            return this;
        }
    }

    /**
    * eval() handles evaluation of the function 
    */

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(1, 2);

        Value arg0 = (Value)args.elementAt(0);
        Value arg1 = null;
        if (numArgs==2) {
            arg1 = (Value)args.elementAt(1);
            if (!(arg1 instanceof NodeSetValue)) {
                throw new SAXException("Second argument to document() must be a nodeset");
            }
        }

        String styleSheetURI = getStaticContext().getSystemId();

        return getDocuments(arg0, (NodeSetValue)arg1, styleSheetURI, context);
    }

    /**
    * getDocuments() evaluates the function.
    * @param arg0 The value of the first argument
    * @param arg1 The value of the second argument, if there is one; otherwise null
    * @param styleSheetURL The URI of the node in the stylesheet containing the expression.
    * Needed only when the first argument is not a nodeset and the second argument is omitted.
    * @param context The evaluation context
    * @return a NodeSetValue containing the root nodes of the selected documents
    */

    public NodeSetValue getDocuments(
                                Value arg0,
                                NodeSetValue arg1,
                                String styleSheetURL,
                                Context context) throws SAXException {
        String baseURL;
        if (arg0 instanceof NodeSetValue) {
            NodeInfo[] v = ((NodeSetValue)arg0).getNodes();
            Vector nv = new Vector(v.length);

            for (int i=0; i<v.length; i++) {
                NodeInfo n = v[i];
                if (arg1==null) {
                    baseURL = n.getSystemId();   
                } else {
                    NodeInfo first = arg1.getFirst();
                    if (first==null) {
                        // node set is empty; treat it as if omitted
                        baseURL = n.getSystemId();
                    }
                    else {
                        baseURL = first.getSystemId();
                    }
                }
                DocumentInfo doc = makeDoc(n.getValue(), baseURL, context);
                if (doc!=null) {
                    nv.addElement(doc);
                }
            }
            return new NodeSetExtent(nv);
                
        } else {
            
            if (arg1==null) {
                baseURL = styleSheetURL;
            } else {
                NodeInfo first = arg1.getFirst();
                if (first==null) {
                    // node set is empty; treat it as if omitted
                    baseURL = styleSheetURL;
                } else {
                    baseURL = first.getSystemId();
                }
            } 

            String href = arg0.asString();                      
            DocumentInfo doc = makeDoc(href, baseURL, context);
            return new SingletonNodeSet(doc);
        }
    }

    /**
    * Supporting routine to load one external document given a URI (href) and a baseURI
    */

    private DocumentInfo makeDoc(String href, String baseURL, Context c) throws SAXException {
     
        if (baseURL==null) {
            throw new SAXException("No base URI available for resolving relative URI");
        }

        Controller controller = boundController;
        if (controller==null) {
            controller = c.getController();
        }

        if (controller==null) {
            throw new SAXException("Internal error: no controller available for document() function");
        }
        
        URIResolver r = controller.getURIResolver();
        r.setURI(baseURL, href);

        // see if the document is already loaded
        
        String uri = r.getURI();
        DocumentInfo doc = (DocumentInfo)controller.getDocumentPool().get(uri);
        if (doc!=null) return doc;

        // load the document
        
        InputSource in = r.getInputSource();
        XMLReader parser = r.getXMLReader();

        if (in==null) {
            Node node = r.getDOMNode();
            if (node==null) {
                controller.reportRecoverableError("Cannot retrieve document " + href, null);
                return null;
            }
            parser = new DOMDriver();
            ((DOMDriver)parser).setStartNode(node);
            in = new InputSource();     // dummy
            in.setSystemId(uri);
        }

        Builder b = new Builder();
        b.setStripper(controller.getStripper());
        b.setLineNumbering(controller.isLineNumbering());
        
        if (parser!=null) {
            b.setXMLReader(parser);
        }
        
        DocumentImpl newdoc = b.build(in);

        // add the document to the pool
        controller.getDocumentPool().put(uri, newdoc);
        return newdoc;
                
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
