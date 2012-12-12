package com.icl.saxon.functions;
import com.icl.saxon.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.lang.Math;
import java.text.*;



public class FormatNumber extends Function {

    private DecimalFormat decimalFormat = new DecimalFormat();
    private String previousFormat = "[null]";
    private DecimalFormatSymbols previousDFS = null;

    public String getName()      { return "format-number"; };

    /**
    * Determine the data type of the exprssion, if possible
    * @return Value.STRING
    */

    public int getDataType() {
        return Value.STRING;
    }

    public Value eval(Vector args, Context context) throws SAXException {
        int numArgs = checkArgumentCount(2, 3);

        DecimalFormatSymbols dfs;

        Value arg0 = (Value)args.elementAt(0);
        Value arg1 = (Value)args.elementAt(1);
        double n = arg0.asNumber();
        String format = arg1.asString();

        DecimalFormatManager dfm = context.getController().getDecimalFormatManager();
        if (numArgs==2) {
            dfs = dfm.getDefaultDecimalFormat(); 
        } else {
            Value arg2 = (Value)args.elementAt(2);
            String dfname = getStaticContext().makeName(arg2.asString(), false).getAbsoluteName();
            dfs = dfm.getNamedDecimalFormat(dfname);
            if (dfs==null) {
                throw new SAXException(
                    "format-number function: decimal-format " + dfname + " not registered");
            }
        }
        return new StringValue(formatNumber(n, format, dfs));
    }

    public String formatNumber(double n, String format, DecimalFormatSymbols dfs) throws SAXException {
        try {
            DecimalFormat df = decimalFormat;
            if (!(dfs==previousDFS && format.equals(previousFormat))) {
                df.setDecimalFormatSymbols(dfs);
                df.applyLocalizedPattern(format);
                previousDFS = dfs;
                previousFormat = format;
            }
            return df.format(n);
        } catch (Exception err) {
            throw new SAXException("Unable to interpret format pattern " + format + "(" + err + ")");
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
