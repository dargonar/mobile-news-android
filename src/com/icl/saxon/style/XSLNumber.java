package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.number.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.util.*;

/**
* An xsl:number element in the stylesheet.<BR>
*/

public class XSLNumber extends StyleElement {

    private final static int SINGLE = 0;
    private final static int MULTI = 1;
    private final static int ANY = 2;
    private final static int SIMPLE = 3;

    private int level;
    private Pattern count = null;
    private Pattern from = null;
    private Expression expr = null;
    private Expression format = null;
    private Expression groupSize = null;
    private Expression groupSeparator = null;
    private Expression letterValue = null;
    private Expression lang = null;
    private NumberFormatter formatter = null;
    private Numberer numberer = null;  

    private static Numberer defaultNumberer = new Numberer_en();

    /**
    * Determine whether this node is an instruction.
    * @return true - it is an instruction
    */

    public boolean isInstruction() {
        return true;
    }


    public void prepareAttributes() throws SAXException {


        String[] allowed = {"value", "count", "from", "level", "format",
            "grouping-size", "grouping-separator", "lang", "letter-value"};
        allowAttributes(allowed);

        String exprAtt = getAttributeValue("value");
        if (exprAtt!=null) {
            expr = Expression.make(exprAtt, this);
        } 

        String countAtt = getAttributeValue("count");
        if (countAtt!=null) {
            count = Pattern.make(countAtt, this);
        }

        String fromAtt = getAttributeValue("from");
        if (fromAtt!=null) {
            from = Pattern.make(fromAtt, this);
        }

        String levelAtt = getAttributeValue("level");
        if (levelAtt==null) {
            level = SINGLE;
        } else if (levelAtt.equals("single")) {
            level = SINGLE;
        } else if (levelAtt.equals("multiple")) {
            level = MULTI;            
        } else if (levelAtt.equals("any")) {
            level = ANY;
        } else {
            throw styleError("Invalid value for level attribute");
        }

        if (level==SINGLE && from==null && count==null) {
            level=SIMPLE;
        }
        
        String formatAtt = getAttributeValue("format");
        if (formatAtt != null) {
            format = AttributeValueTemplate.make(formatAtt, this);
            if (format instanceof StringValue) {
                formatter = new NumberFormatter();
                formatter.prepare(((StringValue)format).asString());
            }
            // else we'll need to allocate the formatter at run-time
        } else {
            formatter = new NumberFormatter();
            formatter.prepare("1");
        }



        String groupSizeAtt = getAttributeValue("grouping-size");
        String groupSeparatorAtt = getAttributeValue("grouping-separator");
        if (groupSeparatorAtt!=null && groupSizeAtt!=null) {
            // the spec says that if only one is specified, it is ignored
            groupSize = AttributeValueTemplate.make(groupSizeAtt, this); 
            groupSeparator = AttributeValueTemplate.make(groupSeparatorAtt, this);
        }

        String langAtt = getAttributeValue("lang");
        if (langAtt==null) {
            numberer = defaultNumberer;
        } else {
            lang = AttributeValueTemplate.make(langAtt, this);
            if (lang instanceof StringValue) {
                numberer = makeNumberer(((StringValue)lang).asString());
            }   // else we allocate a numberer at run-time
        }

        String letterValueAtt = getAttributeValue("letter-value");
        if (letterValueAtt != null) {
            letterValue = AttributeValueTemplate.make(letterValueAtt, this);
        }

    }

    public void validate() throws SAXException {
        checkWithinTemplate();
        checkEmpty();
    }

    public void process( Context context ) throws SAXException
    {
        NodeInfo source = context.getCurrentNode();
        int value = -1;
        Vector vec = null;

        if (expr!=null) {
            value = (int)(expr.evaluateAsNumber(context));

        } else {

            if (level==SIMPLE) {
                value = source.getNumberSimple(context);
            } else if (level==SINGLE) {
                value = source.getNumberSingle(count, from, context);
            } else if (level==ANY) {
                value = source.getNumberAny(count, from, context);
            } else if (level==MULTI) {
                vec = source.getNumberMulti(count, from, context);               
            } 
        }

        int gpsize = 0;
        String gpseparator = "";
        String language;
        String letterVal;

        if (groupSize!=null) {
            String g = groupSize.evaluateAsString(context);
            try {
                gpsize = Integer.parseInt(g);
            } catch (NumberFormatException err) {
                throw new SAXException("group-size must be numeric");
            }
        }

        if (groupSeparator!=null) {
            gpseparator = groupSeparator.evaluateAsString(context);
        }

        // fast path for the simple case

        if (vec==null && format==null && gpsize==0 && lang==null) {
            context.getOutputter().writeContent("" + value);
            return;
        }

        if (numberer==null) {
            numberer = makeNumberer(lang.evaluateAsString(context));
        }

        if (letterValue==null) {
            letterVal = "";
        } else {
            letterVal = letterValue.evaluateAsString(context);
            if (!(letterVal.equals("alphabetic") || letterVal.equals("traditional"))) {
                throw styleError("letter-value must be \"traditional\" or \"alphabetic\"");
            }
        }

        if (vec==null) {
            vec = new Vector();
            vec.addElement(new Integer(value));
        }

        NumberFormatter nf;
        if (formatter==null) {              // format not known until run-time
            nf = new NumberFormatter();
            nf.prepare(format.evaluateAsString(context));            
        } else {
            nf = formatter;
        }
        
        String s = nf.format(vec, gpsize, gpseparator, letterVal, numberer);
        context.getOutputter().writeContent(s);
    }

    /**
    * Load a Numberer class for a given language and check it is OK.
    */

    protected static Numberer makeNumberer (String language) throws SAXException
    {
        Numberer numberer;
        if (language.equals("en")) {
            numberer = defaultNumberer;
        } else {
            String langClassName = "com.icl.saxon.number.Numberer_";
            for (int i=0; i<language.length(); i++) {
                if (Character.isLetter(language.charAt(i))) {
                    langClassName += language.charAt(i);
                }
            }    
            try {
                numberer = (Numberer)(Loader.getInstance(langClassName));
            } catch (Exception err) {
                numberer = defaultNumberer;
            }
        }

        return numberer;
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
