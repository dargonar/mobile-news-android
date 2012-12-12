package com.icl.saxon.style;
import org.xml.sax.SAXException;

/**
  * Interface ExtensionElementFactory. <br>
  * A "Factory" for used-defined nodes in the stylesheet tree. <br>
  * (Actually, it's not struictly a factory: it doesn't create the nodes,
  * it merely identifies what class they should be.
  */

public interface ExtensionElementFactory  {

    /**
    * Identify the class to be used for stylesheet elements with a given local name.
    * The returned class must extend com.icl.saxon.style.StyleElement
    * @throws SAXException if the local name is not a recognised element type in this
    * namespace.
    */

    public Class getExtensionClass(String localname) throws SAXException;

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
