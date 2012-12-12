package com.icl.saxon.sort;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;

import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
* A SortKeyDefinition defines one component of a sort key. <BR>
*
* Note that most attributes defining the sort key can be attribute value templates,
* and can therefore vary from one invocation to another. We hold them as expressions,
* but optimise for the case where the attributes are all fixed strings: in this case
* we can use the same Comparer object each time.
*
*/


public class SortKeyDefinition  {

    private Expression sortKey;
    private Expression order;
    private Expression dataType;
    private Expression caseOrder;
    private Expression language;
    private StaticContext staticContext = new DummyStaticContext();

    private Comparer comparer = null;

    /**
    * Set the expression used as the sort key
    */

    public void setSortKey(Expression exp) {
        sortKey = exp;
    }

    /**
    * Set the order. This is supplied as an expression which must evaluate to "ascending"
    * or "descending". If the order is fixed, supply e.g. new StringValue("ascending").
    * Default is "ascending".
    */

    public void setOrder(Expression exp) {
        order = exp;
    }

    /**
    * Set the data type. This is supplied as an expression which must evaluate to "text",
    * "number", or a QName. If the data type is fixed, supply e.g. new StringValue("text").
    * Default is "text".
    */

    public void setDataType(Expression exp) {
        dataType = exp;
    }

    /**
    * Set the case order. This is supplied as an expression which must evaluate to "upper-first"
    * or "lower-first" or "#default". If the order is fixed, supply e.g. new StringValue("lower-first").
    * Default is "#default".
    */

    public void setCaseOrder(Expression exp) {
        caseOrder = exp;
    }

    /**
    * Set the language. This is supplied as an expression which evaluate to the language name.
    * If the order is fixed, supply e.g. new StringValue("de").
    * Default is "en".
    */

    public void setLanguage(Expression exp) {
        language = exp;
    }

    /**
    * Set the static context. This is used only for resolving any QName supplied in the data-type
    * property. By default a DummyStaticContext is used, which does not allow namespace-qualified
    * names.
    */

    public void setStaticContext(StaticContext sc) {
        staticContext = sc;
    }


    public Expression getSortKey() {
        return sortKey;
    }

    public Expression getOrder() {
        return (order==null ? new StringValue("ascending") : order);
    }

    public Expression getDataType() {
        return (dataType==null ? new StringValue("text") : dataType);
    }
    
    public Expression getCaseOrder() {
        return (caseOrder==null ? new StringValue("#default") : caseOrder);
    }

    public Expression getLanguage() {
        return (language==null ? new StringValue("en") : language);
    }

    /**
    * If possible, use the same comparer every time
    */

    public void bindComparer() throws SAXException {
        if ((dataType instanceof StringValue) &&
                (order instanceof StringValue) &&
                (caseOrder instanceof StringValue) &&
                (language instanceof StringValue)) {
            comparer = makeComparer(null);
        } 
    }

    /**
    * Get a Comparer which can be used to compare two values according to this sort key.
    */

    public Comparer getComparer(Context context) throws SAXException {
        if (comparer==null) {
            return makeComparer(context);
        } else {
            return comparer;
            // note, we can't save it for later use, because the context might be different next time
        }
    }

    /**
    * Create a Comparer which can be used to compare two values according to this sort key.
    */

    private Comparer makeComparer(Context context) throws SAXException {

        boolean isAscending;

        String orderAtt;
        if (order==null) {
            orderAtt="ascending";
        } else {
            orderAtt = order.evaluateAsString(context);
        }
        
        if (orderAtt.equals("ascending")) {
            isAscending = true;            
        } else if (orderAtt.equals("descending")) {
            isAscending = false;
        } else {
            throw new SAXException("order must be ascending or descending");
        }

        Name dataTypeName;

        String dataTypeAtt;
        if (dataType==null) {
            dataTypeAtt = "text";
        } else {
            dataTypeAtt = dataType.evaluateAsString(context);
        }
        
        if (dataTypeAtt.equals("text")) {
            dataTypeName = null;
        } else if (dataTypeAtt.equals("number")) {
            dataTypeName = null;
        } else {
            if (dataTypeAtt.indexOf(':')<0) {
                throw new SAXException("data-type must be text, number, or a prefixed name");
            }
            dataTypeName = staticContext.makeName(dataTypeAtt, false);
        }

        int caseOrderValue;
        String caseOrderAtt;
        if (caseOrder==null) {
            caseOrderAtt = "#default";
        } else {
            caseOrderAtt = caseOrder.evaluateAsString(context);
        }
        
        if (caseOrderAtt.equals("#default")) {
            caseOrderValue = TextComparer.DEFAULT_CASE_ORDER;
        } else if (caseOrderAtt.equals("lower-first")) {
            caseOrderValue = TextComparer.LOWERCASE_FIRST;
        } else if (caseOrderAtt.equals("upper-first")) {
            caseOrderValue = TextComparer.UPPERCASE_FIRST;
        } else {
            throw new SAXException("case-order must be lower-first or upper-first");
        }



        if (dataTypeAtt.equals("text")) {
            if (language==null) {
                comparer = new StringComparer();
            } else {
                String langValue = language.evaluateAsString(context);
                String userClassName = "com.icl.saxon.sort.Compare_";
                for (int i=0; i<langValue.length(); i++) {
                    if (Character.isLetter(langValue.charAt(i))) {
                        userClassName += langValue.charAt(i);
                    }
                }
                try {
                    comparer = loadComparer(userClassName);
                } catch (Exception err) {
                    //System.err.println("Warning: no comparer " + userClassName + " found; using default");
                    comparer = new Compare_en();
                }
            }
            
        } else if (dataTypeAtt.equals("number")) {
            comparer = new DoubleComparer();
        } else {
            String userClassName = dataTypeName.getLocalName();
            try {
                comparer = loadComparer(userClassName);
            } catch (Exception err) {
                System.err.println("Warning: no comparer " + userClassName + " found; using default");
                comparer = new StringComparer();
            }
        }

        comparer = comparer.setDataType(dataTypeName);
        comparer = comparer.setOrder(isAscending);
        if (comparer instanceof TextComparer) {
            comparer = ((TextComparer)comparer).setCaseOrder(caseOrderValue);
        }


        return comparer;
    }

    /**
    * Load a named Comparer class and check it is OK.
    */

    private static TextComparer loadComparer (String className) throws SAXException
    {
        try {
            return (TextComparer)Loader.getInstance(className);
        }
        catch (ClassCastException e) {
            throw new SAXException("Failed to load TextComparer  " + className +
                            ": it does not implement the TextComparer interface");
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
