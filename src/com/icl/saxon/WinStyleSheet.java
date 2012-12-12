package com.icl.saxon;
import com.icl.saxon.handlers.*;
import com.icl.saxon.expr.*;
import com.icl.saxon.style.*;
import org.xml.sax.SAXException;
import org.xml.sax.*;
import java.util.*;
import java.io.*;

/**
  * This <B>WinStyleSheet</B> class is used in instant-saxon, a packaged
  * interface to the StyleSheet interpreter, intended for use with jexegen
  * on Windows platforms. It uses the SAXON version of the AElfred parser,
  * which has been modified to deliver comments to the application.
  */
  
public class WinStyleSheet extends StyleSheet  {

    
    /**
    * Main program, can be used directly from the command line.
    * <p>The format is:</P>
    * <p>java com.icl.saxon.StyleSheet [-u] <I>source-file</I> <I>style-file</I> &gt;<I>output-file</I></P>
    * <p>followed by any number of parameters in the form {keyword=value}... which can be
    * referenced from within the stylesheet.</p>
    * <p>The -u option indicates that the source-file and style-file are given in the form of URLs; by
    * default they are interpreted as file names.</p>
    * <p>This program applies the XSL style sheet in style-file to the source XML document in source-file.</p>
    */
    
    public static void main (String args[]) throws java.lang.Exception
    {
        WinStyleSheet w = new WinStyleSheet();
        w.sourceParser = new com.icl.saxon.aelfred.SAXDriver();
        w.styleParser = new com.icl.saxon.aelfred.SAXDriver();
        w.doMain(args, w, "saxon");
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
