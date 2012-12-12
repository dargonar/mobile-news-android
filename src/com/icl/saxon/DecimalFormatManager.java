package com.icl.saxon;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;

import org.xml.sax.*;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
  * DecimalFormatManager manages the collection of named and unnamed decimal formats
  * @version 10 December 1999: extracted from Controller
  * @author Michael H. Kay (Michael.Kay@icl.com)
  */
  
public class DecimalFormatManager {

    private DecimalFormatSymbols defaultDFS;
    private Hashtable formatTable;            // table for named decimal formats

    /**
    * create a Controller and initialise variables
    */

    public DecimalFormatManager() {
        formatTable = new Hashtable();
        DecimalFormatSymbols d = new DecimalFormatSymbols();
        setDefaults(d);
        setDefaultDecimalFormat(d);
    }

    /**
    * Set up the XSLT-defined default attributes in a DecimalFormatSymbols
    */

    public static void setDefaults(DecimalFormatSymbols d) {
        d.setDecimalSeparator('.');
        d.setGroupingSeparator(',');
        d.setInfinity("Infinity");
        d.setMinusSign('-');
        d.setNaN("NaN");
        d.setPercent('%');
        d.setPerMill('\u2030');            
        d.setZeroDigit('0');
        d.setDigit('#');
        d.setPatternSeparator(';');
    }

    /**
    * Register the default decimal-format. 
    * Note that it is an error to register the same decimal-format twice, even with different
    * precedence
    */

    public void setDefaultDecimalFormat(DecimalFormatSymbols dfs) {
        defaultDFS = dfs;
    }

    /**
    * Get the default decimal-format. 
    */

    public DecimalFormatSymbols getDefaultDecimalFormat() {
        return defaultDFS;
    }

    /**
    * Set a named decimal format.
    * Note that it is an error to register the same decimal-format twice, even with different
    * precedence.
    */

    public void setNamedDecimalFormat(String name, DecimalFormatSymbols dfs) throws SAXException {
        if (formatTable.get(name)!=null) {
            if (!dfs.equals((DecimalFormatSymbols)formatTable.get(name))) {
                throw new SAXException("Duplicate declaration of decimal-format");
            }
        }
        formatTable.put(name, dfs);
    }

    /**
    * Get a locale registered using setDefaultLocale or setNamedLocale.
    * @param name The locale name, or "*" to get the default locale
    * @return the DecimalFormatSymbols object corresponding to the named locale, if any
    * or null if not set.
    */

    public DecimalFormatSymbols getNamedDecimalFormat(String name) {
        return (DecimalFormatSymbols)formatTable.get(name);
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
