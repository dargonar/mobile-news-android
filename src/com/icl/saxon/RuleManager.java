package com.icl.saxon;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.DummyStaticContext;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.pattern.UnionPattern;
import com.icl.saxon.output.*;
import com.icl.saxon.om.*;

import org.xml.sax.*;

import java.util.*;


/**
  * <B>RuleManager</B> maintains a set of template rules, one set for each mode
  * @version 10 December 1999: carved out of the old Controller class
  * @author Michael H. Kay (Michael.Kay@icl.com)
  */
  
public class RuleManager {
   
    private Mode defaultMode;           // node handlers with default mode
    private Hashtable modes;            // tables of node handlers for non-default modes  

    /**
    * create a RuleManager and initialise variables
    */

    public RuleManager() {
        resetHandlers();
    }

    /**
    * Set up a new table of handlers.
    */

    public void resetHandlers() {
        defaultMode = new Mode();
        modes = new Hashtable();
    }

    /**
    * Get the Mode object for a named mode. If there is not one already registered.
    * a new Mode is created.
    * @param modeName The name of the mode, as an absolute name. Supply null to get the default
    * mode.
    */

    public Mode getMode(String modeName) {
        if (modeName==null) {
            return defaultMode;
        }
        Mode m = (Mode)modes.get(modeName);
        if (m==null) {
            m = new Mode();
            m.setName(modeName);
            modes.put(modeName, m);
        }
        return m;
    }

    /**
      * Register a handler for a particular pattern. This is a convenience interface
      * that calls setHandler(pattern, eh, mode, precedence) with default mode and precedence.
      * @param pattern A match pattern
      * @param eh The ElementHandler to be used
      * @see ElementHandler
      * @see Pattern
      */
        
    public void setHandler(String pattern, NodeHandler eh) throws SAXException {
        Pattern match = Pattern.make(pattern, new DummyStaticContext());
        setHandler(match, eh, defaultMode, 0);
    }

    /**
      * Register a handler for a particular pattern. 
      * @param pattern A match pattern
      * @param eh The ElementHandler to be used
      * @param mode The processing mode
      * @param precedence The import precedence (use 0 by default)
      */
        
    public void setHandler(Pattern pattern, NodeHandler eh, Mode mode, int precedence) throws SAXException {
        
        // for a union pattern, register the parts separately (each with its own priority)
        if (pattern instanceof UnionPattern) {
            UnionPattern up = (UnionPattern)pattern;
            Pattern p1 = up.getLHS();
            Pattern p2 = up.getRHS();
            setHandler(p1, eh, mode, precedence);
            setHandler(p2, eh, mode, precedence);
            return;
        }

        double priority = pattern.getDefaultPriority();
        setHandler(pattern, eh, mode, precedence, priority);
    }


    /**
      * Register a handler for a particular pattern.
      * @param pattern Must be a valid Pattern. 
      * @param eh The ElementHandler to be used
      * @param mode The processing mode to which this element handler applies
      * @param precedence The import precedence of this rule 
      * @param priority The priority of the rule: if an element matches several patterns, the
      * one with highest priority is used
      * @see NodeHandler
      * @see Pattern
      */

    public void setHandler(Pattern pattern, NodeHandler eh,
                 Mode mode, int precedence, double priority) throws SAXException {
     
        // for a union pattern, register the parts separately
        if (pattern instanceof UnionPattern) {
            UnionPattern up = (UnionPattern)pattern;
            Pattern p1 = up.getLHS();
            Pattern p2 = up.getRHS();
            setHandler(p1, eh, mode, precedence, priority);
            setHandler(p2, eh, mode, precedence, priority);
            return;
        }
        
        mode.addRule(pattern, eh, precedence, priority);
    }

    /**
      * Find the handler registered for a particular node in default mode.
      * @param node The NodeInfo for the relevant node
      * @return The handler that will process this
      * node. Returns the default handler for the type of node if there is no specific
      * one registered. 
      */

    public NodeHandler getHandler (NodeInfo node, Context c) throws SAXException {
        return getHandler(node, defaultMode, c);
    }

    /**
      * Find the handler registered for a particular node in a specific mode.
      * @param node The NodeInfo for the relevant node
      * @param mode The processing mode 
      * @return The handler that will process this node
      * Returns null if there is no specific handler registered. 
      */

    public NodeHandler getHandler (NodeInfo node, Mode mode, Context c) throws SAXException {  

        if (mode==null) {
            mode = defaultMode;
        }
        
        NodeHandler eh = (NodeHandler)mode.getRule(node, c);

        if (eh!=null) return eh;

        return null;
    }

    /**
    * Get a handler whose import precedence is in a particular range. This is used to support
    * the xsl:apply-imports function
    */

    public NodeHandler getHandler (NodeInfo node, Mode mode, int min, int max, Context c)
    throws SAXException {
        if (mode==null) mode = defaultMode;
        return (NodeHandler)mode.getRule(node, min, max, c);
    }

    /**
    * Get a list of all registered modes
    * @return an Enumeration of all modes in use, excluding the default (unnamed) mode
    */

    public Enumeration getAllModes() {
        return modes.keys();
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
