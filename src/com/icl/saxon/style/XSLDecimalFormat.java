package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.text.DecimalFormatSymbols;

/**
* Handler for xsl:decimal-format elements in stylesheet.<BR>
*/

public class XSLDecimalFormat extends StyleElement {

    public void prepareAttributes() throws SAXException {

        String[] allowed = {"name", "decimal-separator", "grouping-separator", "infinity",
            "minus-sign", "NaN", "percent", "per-mille", "zero-digit", "digit", "pattern-separator" };
        allowAttributes(allowed);
    }

    public void validate() throws SAXException {
        checkTopLevel();
    }

    public void preprocess() throws SAXException
    {
        String name =               attributeList.getValue("name");
        String decimalSeparator =   attributeList.getValue("decimal-separator");
        String groupingSeparator =  attributeList.getValue("grouping-separator");
        String infinity =           attributeList.getValue("infinity");
        String minusSign =          attributeList.getValue("minus-sign");
        String NaN =                attributeList.getValue("NaN");
        String percent =            attributeList.getValue("percent");
        String perMill =            attributeList.getValue("per-mille");
        String zeroDigit =          attributeList.getValue("zero-digit");
        String digit =              attributeList.getValue("digit");
        String patternSeparator =   attributeList.getValue("pattern-separator");

        DecimalFormatSymbols d = new DecimalFormatSymbols();
        DecimalFormatManager.setDefaults(d);
        if (decimalSeparator!=null) {
            d.setDecimalSeparator(toChar(decimalSeparator));
        }
        if (groupingSeparator!=null) {
            d.setGroupingSeparator(toChar(groupingSeparator));
        }
        if (infinity!=null) {
        d.setInfinity(infinity);
        }
        if (minusSign!=null) {
            d.setMinusSign(toChar(minusSign));
        }
        if (NaN!=null) {
            d.setNaN(NaN);
        }
        if (percent!=null) {
            d.setPercent(toChar(percent)); 
        }
        if (perMill!=null) {
            d.setPerMill(toChar(perMill)); 
        }
        if (zeroDigit!=null) {
            d.setZeroDigit(toChar(zeroDigit));
        }
        if (digit!=null) {
            d.setDigit(toChar(digit));
        }
        if (patternSeparator!=null) {
            d.setPatternSeparator(toChar(patternSeparator));
        }        

        DecimalFormatManager dfm = getPrincipalStyleSheet().getDecimalFormatManager();
        if (name==null) {
            dfm.setDefaultDecimalFormat(d);
        } else {
            Name fullname = new Name(name, this, false);
            dfm.setNamedDecimalFormat(fullname.getAbsoluteName(), d);
        }
    }

    public void process(Context context) {}

    private char toChar(String s) throws SAXException {
        if (s.length()!=1)
            throw styleError("Attribute \"" + s + "\" should be a single character");
        return s.charAt(0);
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
