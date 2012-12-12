package com.icl.saxon.number;
import java.util.Vector;

/**
  * Class Numberer_de is designed simply to demonstrate how to write a number formatter
  * for a different language. This one will be activated for language="de", format="eins",
  * letter-value="traditional"
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 18 November 1999 
  */

public class Numberer_de extends Numberer_en {

    /**
    * Format a number into a string
    * @param number The number to be formatted
    * @param picture The format specification. This is a single component of the format attribute
    * of xsl:number, e.g. "1", "01", "i", or "a"
    * @param groupSize number of digits per group (0 implies no grouping)
    * @param groupSeparator string to appear between groups of digits
    * @param letterValue as defined in xsl:number ("alphabetic" or "traditional" or "")
    * @return the formatted number
    */

    public String format(int number, String picture,
                                 int groupSize, String groupSeparator,
                                 String letterValue) {
        if (letterValue.equals("traditional") && picture.equals("eins")) {
            switch(number) {
                case 1: return "eins";
                case 2: return "zwei";
                case 3: return "drei";
                case 4: return "vier";
                case 5: return "funf";
                case 6: return "sechs";
                case 7: return "sieben";
                case 8: return "acht";
                case 9: return "neun";
                case 10: return "zehn";
                default: return "" + number;
            }
        } else {
            return super.format(number, picture, groupSize, groupSeparator, letterValue);
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
