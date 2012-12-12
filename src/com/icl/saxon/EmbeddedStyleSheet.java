package com.icl.saxon;
import com.icl.saxon.om.*;
import com.icl.saxon.tree.Builder;
import com.icl.saxon.tree.Stripper;

import com.icl.saxon.style.*;
import com.icl.saxon.output.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;

/**
  * The EmbeddedStyleSheet class is responsible for building a stylesheet from an xsl:stylesheet
  * element found in the middle of a source document. <br>
  * It is necessary to copy the subtree to a new tree, because the original document will not
  * have been built using the StyleNodeFactory.
  * @author Michael H. Kay (Michael.Kay@icl.com)
  */
  
public abstract class EmbeddedStyleSheet {

    /**
    * Given an element node in a tree that represents an embedded stylesheet, create a
    * StyleSheet document and prepare it for use, returning it as a PreparedStyleSheet.
    */

    public static PreparedStyleSheet build(ElementInfo element) throws SAXException {

        // we do our best to strip spaces, but it might be too late if the nodes have already
        // been stripped from the source document
        
        Stripper styleStripper = new Stripper();
        styleStripper.setPreserveSpace(new AnyNameTest(), false);
        styleStripper.setPreserveSpace(new Name("xsl", Namespace.XSLT, "text"), true);

        // the technique is to create a Builder, then to create an Outputter that feeds
        // SAX events to this Builder, then use the copy() method (which normally copies to
        // a result tree fragment) to copy the subtree to this Outputter. 

        Builder styleBuilder = new Builder();
        styleBuilder.setStripper(styleStripper);
        styleBuilder.setNodeFactory(new StyleNodeFactory());
        styleBuilder.setDiscardCommentsAndPIs(true);

        styleStripper.setNextHandler(styleBuilder);

        Outputter out = new Outputter();
        ContentHandlerProxy proxy = new ContentHandlerProxy();
        proxy.setUnderlyingContentHandler(styleStripper);
        out.setEmitter(proxy);

        styleStripper.startDocument();
        element.copy(out);
        styleStripper.endDocument();

        DocumentInfo doc = styleBuilder.getCurrentDocument();

        PreparedStyleSheet pss = new PreparedStyleSheet();
        pss.setStyleSheetDocument(doc);

        return pss;
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
