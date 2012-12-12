package com.icl.saxon;
//import com.icl.saxon.expr.*;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.Name;

//import com.icl.saxon.style.*;
import java.io.*;
import java.util.*;
import org.xml.sax.SAXException;

    /**
    * A Mode is a collection of rules; the selection of a rule to apply to a given element
    * is determined by a Pattern.
    *
    * @author <A HREF="Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
    */
  
public class Mode {
    private Vector[] ruleDict = new Vector[101 + NodeInfo.NUMBER_OF_TYPES];
    private String name;


    public Mode() {
        for (int i=0; i<ruleDict.length; i++) {
            ruleDict[i] = null;
        }
    }

    /**
    * Set the name of the mode. Used only for diagnostic trace output
    */

    public void setName(String modeName) {
        name = modeName;
    }

    /**
    * Get the name of the mode. Used only for diagnostic trace output.
    * @return the mode name, or null for the default mode. The name is returned as an
    * absolute name.
    */

    public String getName() {
        return name;
    }


    /**
    * Add a rule to the Mode. <br>
    * The rule effectively replaces any other rule for the same pattern/mode at the same or a lower
    * priority.
    * @param p a Pattern
    * @param obj the Object to return from getRule() when the supplied element matches this Pattern
    */

    public void addRule(Pattern p, Object obj, int precedence, double priority) {

        //System.err.println("Add rule, pattern = " + p.toString() + " class " + p.getClass() + ", priority=" + priority);

        // for fast lookup, we maintain one list for each element name for patterns that can only
        // match elements of a given name, one list for each node type for patterns that can only
        // match one kind of non-element node, and one generic list.
        // Each list is sorted in precedence/priority order so we find the highest-priority rule first

        Name name = p.getName();
        int type = p.getType();
        int key = getList(name, type);    

        // create the list if necessary
        
        Vector list = ruleDict[key];
        if (list==null) {
            list = new Vector(5);
            ruleDict[key] = list;
        }

        // insert the new rule into this list before others of the same precedence/priority

        boolean inserted = false;
        int size = list.size();
        for (int i=0; i<size; i++) {
            if (((Rule)list.elementAt(i)).precedence < precedence ||
                ( ((Rule)list.elementAt(i)).precedence == precedence &&
                    ((Rule)list.elementAt(i)).priority <= priority)) {
                list.insertElementAt( new Rule(p, obj, precedence, priority), i);
                inserted=true;
                break;
            }
        }
        if (!inserted) {
            list.addElement( new Rule(p, obj, precedence, priority) );
        }
    
    }

    /**
    * Determine which list to use for a given pattern (we must also search the generic list)
    */

    public int getList(Name name, int type) {
    
        if (type==NodeInfo.ELEMENT) {
            if (name==null) {
                return NodeInfo.NODE;   // the generic list
            } else {
                return NodeInfo.NUMBER_OF_TYPES + (name.getHashCode() % 101);
            }
        } else {
            return type;
        }
    }

    /**
    * Get the rule corresponding to a given Node, by finding the best Pattern match.
    * @param node the NodeInfo referring to the node to be matched
    * @return the object (e.g. a NodeHandler) registered for that element, if any (otherwise null).
    */

    public Object getRule(NodeInfo node, Context context) throws SAXException {
        Name name = node.getExpandedName();
        int type = node.getNodeType();
        int key = getList(name, type);
        int policy = context.getController().getRecoveryPolicy();

        Vector list;
        Rule specificRule = null;
        Rule generalRule = null;
        int specificPrecedence = -1;
        double specificPriority = Double.NEGATIVE_INFINITY;

        // search the specific list for this node type

        if (key!=NodeInfo.NODE) {
            list = ruleDict[key];
            if (list!=null) {
                for (int i=0; i<list.size(); i++) {
                    Rule r = (Rule)list.elementAt(i);
                    if (r.pattern.matches(node, context)) {
                        // is this a second match?
                        if (specificRule != null) {
                            if (r.precedence==specificPrecedence && r.priority==specificPriority) {
                                reportAmbiguity(node, specificRule.pattern, r.pattern, context);
                            } 
                            break;
                        }
                        specificRule = r;
                        specificPrecedence = r.precedence;
                        specificPriority = r.priority;
                        if (policy==Controller.RECOVER_SILENTLY) {
                            break;                      // find the first; they are in priority order
                        }
                    }
                }
            }
        }

        // search the general list
    
        list = ruleDict[NodeInfo.NODE];
        if (list!=null) {
            for (int i=0; i<list.size(); i++) {
                Rule r = (Rule)list.elementAt(i);
                if (r.precedence < specificPrecedence ||
                     (r.precedence == specificPrecedence && r.priority < specificPriority)) {
                    break;      // no point in looking at a lower priority rule than the one we've got
                }
                if (r.pattern.matches(node, context)) {
                    // is it a second match?
                    if (generalRule != null) {
                        if (r.precedence == generalRule.precedence && r.priority ==generalRule.priority) {
                            reportAmbiguity(node, r.pattern, generalRule.pattern, context);
                        } 
                        break;
                    } else {
                        generalRule = r;
                        if (policy==Controller.RECOVER_SILENTLY) {
                            break;                      // find only the first; they are in priority order
                        }
                    }
                }

            }
        }

        if (specificRule!=null && generalRule==null)
            return specificRule.object;
        if (specificRule==null && generalRule!=null)
            return generalRule.object;
        if (specificRule!=null && generalRule!=null) {
            if (specificRule.precedence == generalRule.precedence &&
                specificRule.priority == generalRule.priority &&
                policy != Controller.RECOVER_SILENTLY) {
                    reportAmbiguity(node, specificRule.pattern, generalRule.pattern, context);
                }
            if (specificRule.precedence > generalRule.precedence ||
                 (specificRule.precedence == generalRule.precedence &&
                    specificRule.priority >= generalRule.priority)) {
                return specificRule.object;
            } else {
                return generalRule.object;
            }
        }
        return null;
    }

    /**
    * Get the rule corresponding to a given Node, by finding the best Pattern match, subject to a minimum
    * and maximum precedence. (This supports xsl:apply-imports)
    * @param node the NodeInfo referring to the node to be matched
    * @return the object (e.g. a NodeHandler) registered for that element, if any (otherwise null).
    */

    public Object getRule(NodeInfo node, int min, int max, Context context) throws SAXException {
        Name name = node.getExpandedName();
        int type = node.getNodeType();
        int key = getList(name, type);

        Vector list;
        Rule specificRule = null;
        Rule generalRule = null;

        // search the the specific list for this node type / name

        if (key!=NodeInfo.NODE) {
            list = ruleDict[key];
            if (list!=null) {
                for (int i=0; i<list.size(); i++) {
                    Rule r = (Rule)list.elementAt(i);
                    if (r.precedence >= min && r.precedence <= max &&
                             r.pattern.matches(node, context)) {
                        specificRule = r;
                        break;                      // find the first; they are in priority order
                    }
                }
            }
        }

        // search the generic list
    
        list = ruleDict[NodeInfo.NODE];
        if (list!=null) {
            for (int i=0; i<list.size(); i++) {
                Rule r = (Rule)list.elementAt(i);

                if (r.precedence >= min && r.precedence <= max && r.pattern.matches(node, context)) {
                    generalRule = r;
                    break;                      // find only the first; they are in priority order
                }

            }
        }
        if (specificRule!=null && generalRule==null)
            return specificRule.object;
        if (specificRule==null && generalRule!=null)
            return generalRule.object;
        if (specificRule!=null && generalRule!=null) {
            if (specificRule.precedence > generalRule.precedence ||
                 (specificRule.precedence == generalRule.precedence &&
                    specificRule.priority >= generalRule.priority)) {
                return specificRule.object;
            } else {
                return generalRule.object;
            }
        }
        return null;
    }

    /**
    * Report an ambiguity
    */

    private void reportAmbiguity(NodeInfo node, Pattern pat1, Pattern pat2, Context c)
        throws SAXException
    {
        c.getController().reportRecoverableError(
            "Ambiguous rule match for node at " + node.getPath() + "\n" +
            "Matches both " + pat1 + " and " + pat2, null);
        
    }


    /**
    * Inner class Rule used to support the implementation
    */

    private class Rule {
        public Pattern pattern;
        public Object object;
        public int precedence;
        public double priority;
    
        public Rule( Pattern p, Object o, int prec, double prio ) {
            pattern = p;
            object = o;
            precedence = prec;
            priority = prio;
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
