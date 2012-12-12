package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.sort.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A NodeSetExpression that retrieves nodes in order according to a specified sort key. <br>
* Note there is no direct XSL expression syntax that generates this. <br>
* The expression sorts a base NodeSet according to the value of a specified
* sort key. The sort key may be composite. The base NodeSet will always be in document
* order.
*/

public class SortedSelection extends NodeSetExpression {
  
    private Expression selection;
    private Vector sortkeys = new Vector(3);        // in major-to-minor order

    /**
    * Constructor
    * @param s the base nodeset to be sorted
    */

    public SortedSelection(Expression s) {
        selection = s;
    }

    /**
    * Add a sort key and other sorting parameters
    * @param sk A SortKeyDefinition
    */

    public void addSortKey(SortKeyDefinition sk) {
        sortkeys.addElement(sk);
    }

    /**
    * Simplify an expression
    * @return the simplified expression
    */

    public Expression simplify() throws SAXException {
        selection = selection.simplify();
        for (int i=0; i<sortkeys.size(); i++) {
            SortKeyDefinition sk = (SortKeyDefinition)sortkeys.elementAt(i);
            sk.setSortKey(sk.getSortKey().simplify());
            sk.setOrder(sk.getOrder().simplify());
            sk.setDataType(sk.getDataType().simplify());
            sk.setCaseOrder(sk.getCaseOrder().simplify());
            sk.setLanguage(sk.getLanguage().simplify());            
        }
        return this;    
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        int dep = selection.getDependencies();
        for (int i=0; i<sortkeys.size(); i++) {
            SortKeyDefinition sk = (SortKeyDefinition)sortkeys.elementAt(i);
            // Not all dependencies in the sort key matter, because the context node, etc,
            // are not dependent on the outer context 
            dep |= (sk.getSortKey().getDependencies() & 
                        (Context.VARIABLES | Context.CONTROLLER));
            dep |= sk.getOrder().getDependencies();
            dep |= sk.getDataType().getDependencies();
            dep |= sk.getCaseOrder().getDependencies();
            dep |= sk.getLanguage().getDependencies();
        }
        return dep;
    }

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dependencies The dependencies to be removed
    * @param context The context to be used for the partial evaluation
    * @return a new expression that does not have any of the specified
    * dependencies
    */

    public Expression reduce(int dependencies, Context context) throws SAXException {
        if ((dependencies & getDependencies()) != 0) {
            Expression newselection = selection.reduce(dependencies, context);
            SortedSelection newss = new SortedSelection(newselection);
            newss.setStaticContext(getStaticContext());
            for (int i=0; i<sortkeys.size(); i++) {
                SortKeyDefinition sk = (SortKeyDefinition)sortkeys.elementAt(i);
                SortKeyDefinition sknew = new SortKeyDefinition();
                sknew.setSortKey(
                    sk.getSortKey().reduce(
                        dependencies & (Context.VARIABLES | Context.CONTROLLER),
                        context));
                sknew.setOrder(sk.getOrder().reduce(dependencies, context));
                sknew.setDataType(sk.getDataType().reduce(dependencies, context));
                sknew.setCaseOrder(sk.getCaseOrder().reduce(dependencies, context));
                sknew.setLanguage(sk.getLanguage().reduce(dependencies, context));
                newss.addSortKey(sknew);
            }
            return newss.simplify();
        } else {
            return this;
        }
    }

    /**
    * Evaluate the expression by sorting the base nodeset using the supplied key. 
    * @param context The  context for the evaluation
    * @return the sorted nodeset
    */

    public NodeEnumeration enumerate(Context context) throws SAXException
    {
        NodeEnumeration base = selection.enumerate(context);
        SortKeyEnumeration enuma = new SortKeyEnumeration(base);
        MultiKeyComparer mkc = new MultiKeyComparer(sortkeys, context);
        enuma.setComparer(mkc);
        enuma.setSortKeys(sortkeys);
        enuma.setContext(context);
        return enuma;
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
