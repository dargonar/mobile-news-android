package com.icl.saxon.om;
import org.xml.sax.SAXException;
import com.icl.saxon.NameTest;

/**
  * An object representing a structured name, containing a Namespace URI,
  * a Namespace prefix, and a local name. <br>
  *
  * <p>The equivalence betweem names depends only on the URI and the local name.
  * The prefix is retained for documentary purposes only: it is useful when
  * reconstructing a document to use prefixes that the user is familiar with.</p>
  *
  * <p>We use the term AbsoluteName to mean the concatenation of the URI and the
  * local name (using circumflex as a separator); and DisplayName to mean the concatenation
  * of the prefix and the local name (using colon as a separator).</p>
  *
  * <p>This class assumes that any validation on names (e.g. that they contain only one
  * colon) has already been done.</p>
  *
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

public class Name implements NameTest {

// The implementation stores the display name and the absolute name.
// These are the forms of the name used most frequently; and although this requires
// storing the local part of the name redundantly, it saves space because each name
// object contains only two object references.

    public final static char SEPARATOR = '^';
    
    private String absoluteName;
    private String displayName;
    private short hash = -1;        // hashCode, calculated the first time it is used

    /**
    * Default constructor for internal use only
    */

    public Name() {}

    /**
    * Construct a name given a prefix, a uri, and a localName. The prefix and/or the uri
    * may be empty strings: they should not be null.
    * @param prefix The name prefix, as written in source XML, the part before the colon. If
    * there is no prefix, supply the empty string ""
    * @param uri The URI identifying the namespace corresponding to the prefix. If the name
    * is in the default namespace and the default namespace is unnamed, supply the empty string ""
    * @param localName The part of the name after the colon, or the entire name if there is no
    * colon. 
    */

    public Name(String prefix, String uri, String localName) throws SAXException {
        if (!isNCName(localName)) {
            throw new SAXException("Invalid local name: \"" + localName +
                     "\" (prefix=" + prefix + ", uri=" + uri + ")");
        }
        
        if (prefix=="") {
            displayName = localName;
        } else {
            if (!isNCName(prefix)) {
                throw new SAXException("Invalid character in name prefix: " + localName);
            }
            displayName = (prefix + ':' + localName).intern();
        }

        if (uri=="") {
            absoluteName = localName.intern();
        } else {
            absoluteName = (uri + SEPARATOR + localName).intern();
        }
    }

    /**
    * Construct a name from its display form, given an ElementInfo that provides the context
    * in which to look up any prefix.
    * @param displayName The name as it appears in source XML, that is (prefix ':')? localName
    * @param nsbase The ElementInfo defining the context for namespace lookup
    * @param useDefault True if the default namespace URI should be used when there is no
    * prefix; false otherwise
    * @throws SAXException if the name is malformed or if the prefix is undeclared.
    */

    public Name(String displayName, ElementInfo nsbase, boolean useDefault) throws SAXException {
        int colon = displayName.indexOf(":");
        if (colon < 0) {
            String prefix = "";
            String uri;
            
            this.displayName = displayName.intern();
            if (!isNCName(displayName)) {
                throw new SAXException("Name " + displayName + " contains invalid characters");
            }
            
            if (useDefault) {
                uri = nsbase.getURIforPrefix(prefix).intern();
            } else {
                uri = "";
            }
            
            if (uri=="") {
                this.absoluteName = this.displayName.intern();
            } else {
                this.absoluteName = (uri + SEPARATOR + displayName).intern();
            }
            
        } else if (colon==0) {
            throw new SAXException("Name " + displayName + " cannot start with a colon");
        } else if (colon==displayName.length()-1) {
            throw new SAXException("Name " + displayName + " cannot end with a colon");
            
        } else {
            String prefix = displayName.substring(0, colon);
            String localName = displayName.substring(colon+1).intern();
            if (!isNCName(prefix) || !isNCName(localName)) {
                throw new SAXException("Name " + displayName + " contains invalid characters");
            }
            String uri = nsbase.getURIforPrefix(prefix).intern();
            this.displayName = displayName.intern();
            if (uri=="") {
                this.absoluteName = localName.intern();
            } else {
                this.absoluteName = (uri + SEPARATOR + localName).intern();
            }
        }
    }

    /**
    * Construct a name with no namespace part. This is used primarily for the names of
    * Processing Instructions, which are not namespace-sensitive
    */

    public Name(String name) {
        this.displayName = name.intern();
        this.absoluteName = displayName.intern();
    }

    /**
    * Reconstruct a name given a prefix, a uri, and a localName. The prefix and/or the uri
    * may be empty strings: they should not be null. This method should only be used when the
    * names have already been validated: otherwise use the similar constructor.
    * @param prefix The name prefix, as written in source XML, the part before the colon. If
    * there is no prefix, supply the empty string ""
    * @param uri The URI identifying the namespace corresponding to the prefix. If the name
    * is in the default namespace and the default namespace is unnamed, supply the empty string ""
    * @param localName The part of the name after the colon, or the entire name if there is no
    * colon. 
    */

    public static Name reconstruct(String prefix, String uri, String localName) {
        Name n = new Name();
        if (prefix=="") {
            n.absoluteName = localName.intern();
            n.displayName = localName;
        } else {
            n.absoluteName = (uri + SEPARATOR + localName).intern();
            n.displayName = (prefix + ':' + localName).intern();
        }
        return n;
    }

    /**
    * Get the prefix of the name, the part before the ":" as used in the original XML
    * @return the prefix, or "" if there was no prefix. The returned string will always be interned.
    */
    
    public final String getPrefix() {
        int colon = displayName.indexOf(":");
        if (colon<0) {
            return "";
        } else {
            return displayName.substring(0, colon).intern();
        }
    }

    /**
    * Get the URI of the namespace to which the name belongs.
    * @return The URI of the namespace, or "" if the name is in the default.
    * The returned string will always be interned.
    */

    public final String getURI() {
        int sep = absoluteName.indexOf(SEPARATOR);
        if (sep<0) {
            return "";
        } else {
            return absoluteName.substring(0, sep).intern();
        }
    }

    /**
    * Get the local part of the name (the name after any ":").
    * @return The part of the name after the ":", if there is one; else the full name.
    * The returned string will always be interned.
    */

    public final String getLocalName() {
        int colon = displayName.indexOf(":");
        if (colon<0) {
            return displayName;
        } else {
            return displayName.substring(colon+1).intern();
        }
    }

    /**
    * Get the name in display format. This is in the form "[prefix:]localname"
    * @return The name including the display form of the namespace prefix if there is one.
    * The returned string will always be interned.
    */

    public final String getDisplayName() {
        return displayName;
    }

    /**
    * Get a representation of the absolute name. This is in the form "[URI^]localname",
    * where the URI and the circumflex are absent if the name is in the default namespace.
    * @return The absolute name including the URI of the namespace (if there is one) followed
    * by the localname, using a cicumflex as separator.
    * The returned string will always be interned.
    */

    public final String getAbsoluteName() {
        return absoluteName;
    }

    /**
    * Get a hash code for this name. We use characters from the end of the absolute name
    * because the start (the URI) is likely to be the same for many names.
    */

    public short getHashCode() {
        if (hash!=-1) {
            return hash;
        }
        int start = 0;
        int limit = absoluteName.length();
        if (limit>8) {
            start = limit - 8;
        }
        int h = limit;
        for (int i=start; i<limit; i++) {
            h += ((int)absoluteName.charAt(i));
        }
        hash = (short)(h & 0x7fff);
        return hash;
    }
            

    /**
    * Convert name to a String.
    * @return the display version of the name
    */

    public final String toString() {
        return displayName;
    }

    /**
    * Test whether two names are equal. According to the XPath rules, they are equal if they have
    * the same local part and the same URI
    */

    public final boolean equals(Name other) {
        return (this.absoluteName == other.absoluteName);
    }

    /**
    * Extract the local part from an absolute name
    */

    public static String getLocalPartOfAbsoluteName(String absName) {
        int sep = absName.indexOf(SEPARATOR);
        if (sep>=0) {
            return absName.substring(sep+1).intern();
        } else {
            return absName;
        }
    }

    /**
    * Extract the URI part of an absolute name
    */

    public static String getURIPartOfAbsoluteName(String absName) {
        int sep = absName.indexOf(SEPARATOR);
        if (sep>0) {
            return absName.substring(0,sep).intern();
        } else {
            return "";
        }
    }

    /**
    * Validate whether a given string constitutes a valid NCName, as defined in XML Namespaces
    */

    public static boolean isNCName(String name) {

        // This isn't 100% accurate, e.g. FF10 to FF19 are classified as digits in Java but not
        // in XML; 00AA is a letter in Java but not in XML.  But it's close enough.
        
        if (name.length()==0) return false;
        char first = name.charAt(0);
        if (!(first=='_' || Character.isLetter(first))) {
            return false;
        }
        for (int i=1; i<name.length(); i++) {
            char c = name.charAt(i);
            if (!(c=='_' || c=='.' || c=='-' || Character.isLetter(c) || Character.isDigit(c))) {
                return false;
            }
        }
        return true;
    }

    /**
    * Validate whether a given string constitutes a valid QName, as defined in XML Namespaces
    */

    public static boolean isQName(String name) {
        int colon = name.indexOf(':');
        if (colon<0) return isNCName(name);
        if (colon==0 || colon==name.length()-1) return false;
        if (!isNCName(name.substring(0, colon))) return false;
        if (!isNCName(name.substring(colon+1))) return false;
        return true;
    }


    /**
    * Determine whether this name matches a given node. This method is provided to satisfy
    * the NameTest interface
    * @param node The node to be tested
    * @return true if and only if this name is equal to the name of the given node
    */

    public final boolean isNameOf(NodeInfo node) throws SAXException {
        Name other = node.getExpandedName();
        if (other==null) return false;
        return (this.absoluteName == other.absoluteName);
    }

    /**
    * Get the default priority when this Name is used on its own as a pattern
    * @return 0.0 (always)
    */

    public double getDefaultPriority() {
        return 0.0;
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
