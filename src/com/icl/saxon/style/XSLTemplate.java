package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.trace.*;  // e.g.

import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

/**
* An xsl:template element in the style sheet.
*/
    
public class XSLTemplate extends StyleElement {

    protected String modeName = null;
    protected String templateName;
    protected Pattern match;
    protected boolean prioritySpecified;
    protected double priority;
    protected Procedure procedure = new Procedure();

    /**
    * Return the absolute (namespace-qualified) name of this template
    */

    public String getTemplateName() {
        return templateName;
    }

    public int getMinImportPrecedence() throws SAXException {
        return ((XSLStyleSheet)getDocumentElement()).getMinImportPrecedence();
    }

    public void prepareAttributes() throws SAXException {

        checkAllowedAttributes();
        
        String modeAttribute = getAttributeValue("mode");
        if (modeAttribute!=null) {
            Name mname = new Name(modeAttribute, this, false);
            modeName = mname.getAbsoluteName();
        }
        
        String nameAttribute = getAttributeValue("name");
        if (nameAttribute!=null) {
            Name tname = new Name(nameAttribute, this, false);
            templateName = tname.getAbsoluteName();
        }
        
        String priorityAtt = getAttributeValue("priority");
        prioritySpecified = (priorityAtt != null);
        if (prioritySpecified) { 
            try {
                priority = new Double(priorityAtt.trim()).doubleValue();
            } catch (NumberFormatException err) {
                throw styleError("Invalid numeric value for priority (" + priority + ")");
            }
        }

        String matchAtt = getAttributeValue("match");
        if (matchAtt != null) {
            match = Pattern.make(matchAtt, this);
        }
        
        if (match==null && nameAttribute==null)
            throw styleError("xsl:template must have a a name or match attribute (or both)");

    }

    /**
    * Check that only the permitted attributes are present on this element.
    * This method is overridden in the subclass SAXONHandler, which allow additional attributes
    */

    protected void checkAllowedAttributes() throws SAXException {
        String[] allowed = {"name", "mode", "match", "priority"};
        allowAttributes(allowed);
    }

    public void validate() throws SAXException {
        checkTopLevel();

        // it is in error if there is another template with the same name and precedence

        if (templateName!=null) {
            NodeInfo node = (NodeInfo)getPreviousSibling();
            while (node!=null) {
                if (node instanceof XSLTemplate) {
                    XSLTemplate t = (XSLTemplate)node;
                    if (t.getTemplateName()==this.getTemplateName() &&
                            t.getPrecedence()==this.getPrecedence()) {
                        throw styleError("There is another template with the same name and precedence");
                    }
                }
                node = (NodeInfo)node.getPreviousSibling();
            }
        }
                    
    }

    /**
    * Preprocess: this registers the template with the rule manager, and ensures
    * space is available for local variables
    */

    public void preprocess() throws SAXException
    {
        NodeHandler eh = new TemplateExpander(this);
        RuleManager mgr = getPrincipalStyleSheet().getRuleManager();
        Mode mode = mgr.getMode(modeName);
        
        if (match!=null) {
            if (prioritySpecified) { 
                mgr.setHandler(match, eh, mode, getPrecedence(), priority);
            } else {
                mgr.setHandler(match, eh, mode, getPrecedence());
            }
        }

        getPrincipalStyleSheet().allocateLocalSlots(procedure.getNumberOfVariables());
      
    }

    /**
    * Process template. This is called while all the top-level nodes are being processed in order,
    * so it does nothing.
    */

    public void process(Context context) throws SAXException {
    }

    /**
    * Expand the template. Called from TemplateExpander (for apply-templates) and directly from
    * XSLCallTemplate
    */

    public void expand(Context context) throws SAXException {

    	if (context.getController().isTracing()) { // e.g. FIXME: trace tail recursion
    	    TraceListener listener = context.getController().getTraceListener();

    	    listener.enter(this, context);
    	    realExpand(context);
    	    listener.leave(this, context);

    	} else {
    	    realExpand(context);
    	}
    }

    private void realExpand(Context context) throws SAXException {
	    ParameterSet p = null;
	    do {
    		context.setTailRecursion(null);
    		processChildren(context);
    		p = context.getTailRecursion();
    		if (p!=null) {
    		    context.getBindery().closeStackFrame();
    		    context.getBindery().openStackFrame(p);
    		}
	    } while (p!=null);
    }

    /**
    * Disallow variable references in the match pattern
    */

    public Binding bindVariable(String name) throws SAXException {
        throw styleError("The match pattern in xsl:template may not contain references to variables");
    }

    /**
    * Get associated Procedure (for details of stack frame)
    */

    public Procedure getProcedure() {
        return procedure;
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
// Contributor(s): 
// Portions marked "e.g." are from Edwin Glaser (edwin@pannenleiter.de)
//
