package com.icl.saxon;

/**
* The Version class holds the SAXON version information.
*/

public class Version
{
    public static String javaVersion = System.getProperty("java.version");
    public static boolean preJDK12 = javaVersion.startsWith("1.1") || javaVersion.startsWith("1.0");

    public static final boolean isPreJDK12() {
        return preJDK12;
    }
    
    public static String getVersion() {
        return "5.5.1";
    }

    public static double getXSLVersion() {
        return 1.0;
    }

    public static String getProductName() {
        return "SAXON " + getVersion() + " from Michael Kay of ICL";
    }

    public static String getWebSiteAddress() {
        return "http://users.iclway.co.uk/mhkay/saxon/index.html";
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
