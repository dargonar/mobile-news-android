package com.icl.saxon.number;
import java.util.Vector;

/**
  * Interface Numberer supports number formatting. There is a separate
  * implementation for each language, e.g. Numberer_en for English.
  * This supports the xsl:number element 
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 18 November 1999 
  */

public interface Numberer {

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
                                 String letterValue); 

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
