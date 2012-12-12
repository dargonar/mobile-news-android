package com.icl.saxon.om;

/**
  * A node in the XML parse tree representing a Processing Instruction. <p>
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

public interface ProcInstInfo extends NodeInfo {

    /**
    * Get a pseudo-attribute. This is useful only if the processing instruction data part
    * uses pseudo-attribute syntax, which it does not have to. This syntax is as described
    * in the W3C Recommendation "Associating Style Sheets with XML Documents".
    * @return the value of the pseudo-attribute if present, or null if not
    */

    public String getPseudoAttribute(String name);
    
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
