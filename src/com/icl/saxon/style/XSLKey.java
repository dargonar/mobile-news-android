package com.icl.saxon.style;
import com.icl.saxon.*;
import com.icl.saxon.om.Name;
import com.icl.saxon.pattern.Pattern;
import com.icl.saxon.expr.Expression;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import java.util.*;

/**
* Handler for xsl:key elements in stylesheet.<BR>
*/

public class XSLKey extends StyleElement  {

    private String keyname;     // the absolute name of the key
    private Pattern match;
    private Expression use;

    public void prepareAttributes() throws SAXException {

        String[] allowed = {"name", "match", "use"};
        allowAttributes(allowed);

        AttributeList atts = attributeList;
        String name = atts.getValue("name");
        if (name==null) reportAbsence("name");
        
        String matchAtt = atts.getValue("match");
        if (matchAtt==null) reportAbsence("match");
        
        String useAtt = atts.getValue("use");
        if (useAtt==null) reportAbsence("use");
        
        keyname = (new Name(name, this, false)).getAbsoluteName();
        match = Pattern.make(matchAtt, this);
        use = Expression.make(useAtt, this);
    }

    public void validate() throws SAXException {
        checkTopLevel();
    }

    public void preprocess() throws SAXException
    {
        KeyManager km = getPrincipalStyleSheet().getKeyManager();
        km.setKeyDefinition(new KeyDefinition(keyname, match, use));
    }

    public void process( Context context ) throws SAXException
    {}

    /**
    * Disallow variable references in the match and use patterns
    */

    public Binding bindVariable(String name) throws SAXException {
        throw new SAXException("The expressions in xsl:key may not contain references to variables");
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
