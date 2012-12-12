package com.icl.saxon.output;

/**
* This class creates a CharacterSet object for a given named encoding.
*/


public class CharacterSetFactory {

	public static CharacterSet makeCharacterSet(String encoding) {

        if (encoding.equalsIgnoreCase("ASCII")) {
            return new ASCIICharacterSet();
        } else if (encoding.equalsIgnoreCase("US-ASCII")) {
            return new ASCIICharacterSet();
        } else if (encoding.equalsIgnoreCase("iso-8859-1")) {
            return new Latin1CharacterSet();
        } else if (encoding.equalsIgnoreCase("utf-8")) {
            return new UnicodeCharacterSet();
        } else if (encoding.equalsIgnoreCase("UTF8")) {
            return new UnicodeCharacterSet();
        } else if (encoding.equalsIgnoreCase("KOI8-R")) {
            return new KOI8RCharacterSet();
        } else if (encoding.equalsIgnoreCase("cp1251")) {
            return new CP1251CharacterSet();
        } else return null;

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
