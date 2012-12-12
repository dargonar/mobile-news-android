package com.icl.saxon.number;
import java.util.Vector;

/**
  * Class NumberFormatter defines a method to format a Vector of integers as a character
  * string according to a supplied format specification.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 17 November 1999 
  */

public class NumberFormatter {

    private Numberer numberer;
    private Vector formatTokens;
    private Vector separators;
    private boolean startsWithSeparator;

    /**
    * Allocate a numberer appropriate to the selected language
    */

    public void setNumberer(Numberer numberer) {
        this.numberer = numberer;
    }

    /**
    * Tokenize the format pattern.
    * @param numbers the number to be formatted (a sequence of integer value)
    * @param form the format specification. Contains one of the following values:<ul>
    * <li>"1": conventional decimal numbering</li>
    * <li>"a": sequence a, b, c, ... aa, ab, ac, ...</li>    
    * <li>"A": sequence A, B, C, ... AA, AB, AC, ...</li>
    * <li>"i": sequence i, ii, iii, iv, v ...</li>
    * <li>"I": sequence I, II, III, IV, V, ...</li>
    * </ul>
    * This symbol may be preceded and followed by punctuation (any other characters) which is
    * copied to the output string.
    * @return the formatted output string. Note that the fallback representation (e.g. for negative
    * numbers in roman notation) is decimal.
    */
    
    public void prepare(String format) {

        // Tokenize the format string into alternating alphanumeric and non-alphanumeric tokens

        if (format.length()==0) format="1";
        
        formatTokens = new Vector();
        separators = new Vector();
        
        int len = format.length();
        int i=0;
        int t=0;
        boolean first = true;
        startsWithSeparator = true;
        
        while (i<len) {
            char c = format.charAt(i);
            t=i;
            while (Character.isLetterOrDigit(c)) {
                i++;
                if (i==len) break;
                c = format.charAt(i);
            }
            if (i>t) {
                String tok = format.substring(t, i);
                formatTokens.addElement(tok);
                if (first) {
                    separators.addElement(".");
                    startsWithSeparator = false;
                    first = false;
                }
            }
            if (i==len) break;
            t=i;
            c = format.charAt(i);
            while (!Character.isLetterOrDigit(c)) {
                first = false;
                i++;
                if (i==len) break;
                c = format.charAt(i);
            }
            if (i>t) {
                String sep = format.substring(t, i);
                separators.addElement(sep);
            }
        }
        
    }

    /**
    * Format a vector of numbers.
    * @param numbers the numbers to be formatted (a sequence of integer values)
    * @return the formatted output string. 
    */
    
    public String format(Vector numbers, int groupSize, String groupSeparator,
                        String letterValue, Numberer numberer) {
       
        StringBuffer sb = new StringBuffer();
        int num = 0;
        int tok = 0;
        // output first punctuation token
        if (startsWithSeparator) {
            sb.append((String)separators.elementAt(tok));
        }
        // output the list of numbers
        while (num<numbers.size()) {
            if (num>0) {
                sb.append((String)separators.elementAt(tok));
            }                
            int nr = ((Integer)numbers.elementAt(num++)).intValue();
            String s = numberer.format(nr, (String)formatTokens.elementAt(tok),
                         groupSize, groupSeparator, letterValue);
            sb.append(s);
            tok++;
            if (tok==formatTokens.size()) tok--;
        }
        // output the final punctuation token
        if (separators.size()>formatTokens.size()) {
            sb.append((String)separators.elementAt(separators.size()-1));
        }
        return sb.toString();
    }

    /**
    * Format a single number. The format/template has the same syntax as for a Vector
    * of numbers.
    */

    public String format(int number, int groupSize,
             String groupSeparator, String letterValue, Numberer numberer) {
        Vector v = new Vector();
        v.addElement(new Integer(number));
        return format(v, groupSize, groupSeparator, letterValue, numberer);
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
