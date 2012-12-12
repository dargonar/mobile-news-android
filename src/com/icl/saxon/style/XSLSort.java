package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.sort.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.io.*;
import java.util.*;

/**
* An xsl:sort element in the stylesheet.<BR>
*/

public class XSLSort extends StyleElement {

    private SortKeyDefinition sortKeyDefinition;
    
    public void prepareAttributes() throws SAXException {

        Expression select;
        Expression order;
        Expression dataType;
        Expression caseOrder;
        Expression lang;

        String[] allowed = {"order", "data-type", "case-order", "select", "lang"};
        allowAttributes(allowed);

        String selectAtt = getAttributeValue("select");
        if (selectAtt==null) {
            select = new ContextNodeExpression();
        } else { 
            select = Expression.make(selectAtt, this);
        }

        String orderAtt = getAttributeValue("order");
        if (orderAtt == null) {
            order = new StringValue("ascending");
        } else {
            order = AttributeValueTemplate.make(orderAtt, this);
        }

        String dataTypeAtt = getAttributeValue("data-type");
        if (dataTypeAtt == null) {
            dataType = new StringValue("text");
        } else {
            dataType = AttributeValueTemplate.make(dataTypeAtt, this);
        }

        String caseOrderAtt = getAttributeValue("case-order");
        if (caseOrderAtt == null) {
            caseOrder = new StringValue("#default");
        } else {
            caseOrder = AttributeValueTemplate.make(caseOrderAtt, this);
        }

        String langAtt = getAttributeValue("lang");
        if (langAtt == null) {
            lang = new StringValue(Locale.getDefault().getLanguage());
        } else {
            lang = AttributeValueTemplate.make(langAtt, this);
        }

        sortKeyDefinition = new SortKeyDefinition();
        sortKeyDefinition.setSortKey(select);
        sortKeyDefinition.setOrder(order);
        sortKeyDefinition.setDataType(dataType);        
        sortKeyDefinition.setCaseOrder(caseOrder);
        sortKeyDefinition.setLanguage(lang);
        sortKeyDefinition.setStaticContext(this);
        sortKeyDefinition.bindComparer();
    }

    public void validate() throws SAXException {
        NodeInfo parent = (NodeInfo)getParentNode();
        if (!((parent instanceof XSLApplyTemplates) ||
                 (parent instanceof XSLForEach) ||
                 (parent instanceof SAXONGroup) // inelegant, since saxon:group is an extension!
                 )) {
            throw styleError("xsl:sort must be child of xsl:apply-templates or xsl:for-each");
        }
    }

    public void process( Context context ) throws SAXException
    {}

    public SortKeyDefinition getSortKeyDefinition() {
        return sortKeyDefinition;
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
