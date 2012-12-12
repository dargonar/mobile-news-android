package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.util.*;
import java.io.*;

/**
* An xsl:call-template element in the stylesheet
*/

public class XSLCallTemplate extends StyleElement {

    private Name calledTemplateName = null;   // the name of the called template 
    private XSLTemplate template = null;
    private boolean useTailRecursion = false;
    private Expression calledTemplateExpression;    // allows name to be an AVT

    private static Name saxonAllowAVT = Name.reconstruct("saxon", Namespace.SAXON, "allow-avt");

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {


        String[] allowed = {"name"};
        allowAttributes(allowed);

        String allowAVTatt = getAttributeValue(saxonAllowAVT);
        boolean allowAVT = (allowAVTatt != null && allowAVTatt.equals("yes"));

        String nameAttribute = getAttributeValue("name");
        if (nameAttribute==null) {
            reportAbsence("name");
        }

        if (allowAVT) {
            calledTemplateExpression = AttributeValueTemplate.make(nameAttribute, this);
        } else {
            calledTemplateName = new Name(nameAttribute, this, false);
        }
    }

    public void validate() throws SAXException {
        checkWithinTemplate();

        if (calledTemplateExpression==null) {
            template = findTemplate(calledTemplateName);

            // Use tail recursion if the template is calling itself, and if neither this instruction
            // nor any ancestor instruction has a following sibling. Avoid tail recursion if called
            // within the parameters of another call (not sure this is necessary, I just have doubts),
            // or if the call is within an xsl:for-each (this is definitely wrong, see bug 5.3.1/004)

            if (template.isAncestor(this)) {
                useTailRecursion = true;
                StyleElement n = this;
                while (n!=template) {
                    if ((n.isInstruction() && n.getNextSibling()!=null) ||
                             (n instanceof XSLWithParam) ||
                             (n instanceof XSLForEach)) {
                        useTailRecursion = false;
                        break;
                    }
                    n = (StyleElement)n.getParentNode();
                }
                //System.err.println((useTailRecursion ? "" : "NOT ") + "Using tail recursion at line " + getLineNumber());
            }
        }

    }

    private XSLTemplate findTemplate(Name calledTemplateName) throws SAXException {
        
        XSLStyleSheet stylesheet = getPrincipalStyleSheet();        
        Vector toplevel = stylesheet.getTopLevel();
    
        // search for a matching template name, starting at the end in case of duplicates.
        // this also ensures we get the one with highest import precedence.

        String calledName = calledTemplateName.getAbsoluteName();
        for (int i=toplevel.size()-1; i>=0; i--) {
            if (toplevel.elementAt(i) instanceof XSLTemplate) {
                XSLTemplate t = (XSLTemplate)toplevel.elementAt(i);
                String tname = t.getTemplateName();
                if (tname!=null && tname==calledName) {       // both names are interned
                    return t;
                }
            }
        }
        throw styleError("No template exists named " + calledTemplateName.getDisplayName());
    }

    public void process( Context context ) throws SAXException
    {
        // if name is determined dynamically, determine it now

        XSLTemplate target = template;
        if (calledTemplateExpression != null) {
            String qname = calledTemplateExpression.evaluateAsString(context);
            if (!Name.isQName(qname)) {
                throw new SAXException("Invalid template name: " + qname);
            }
            Name fullname = new Name(qname, this, false);
            target = findTemplate(fullname);
        }

        // handle parameters if any
        
        ParameterSet params = null;
        
        if (getNumberOfChildren()>0) {
            NodeInfo child = (NodeInfo)getFirstChild();
            params = new ParameterSet();
            while (child != null) {
                if (child instanceof XSLWithParam) {    // currently always true
                    XSLWithParam param = (XSLWithParam)child;
                    params.put(param.getVariableName(), param.getParamValue(context));
                }
                child = (NodeInfo)child.getNextSibling();
            }
        }

        // Call the named template

        if (useTailRecursion) {
            context.setTailRecursion(params);
            // we now just let the stack unwind until we get back to the xsl:template element;
            // at that point the template will detect that there has been a tail-recursive call,
            // and iterate to achieve the effect of calling itself.
        } else {
            Bindery bindery = context.getBindery();
            bindery.openStackFrame(params);
            target.expand(context);
            bindery.closeStackFrame();
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
