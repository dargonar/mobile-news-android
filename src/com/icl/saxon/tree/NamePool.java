package com.icl.saxon.tree;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;

/**
  * An object representing a collection of XML names, each containing a Namespace URI,
  * a Namespace prefix, and a local name. <br>
  *
  * <p>The equivalence betweem names depends only on the URI and the local name.
  * The prefix is retained for documentary purposes only: it is useful when
  * reconstructing a document to use prefixes that the user is familiar with.</p>
  *
  * <p>The NamePool eliminates duplicate names if they have the same prefix, uri,
  * and local part. It retains duplicates if they have different prefixes</p>
  *
  *
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  */

class NamePool {

    Name[] names;
    int size;
    int count=0;

    public NamePool() {
        init(501);
    }

    public NamePool(int size) {
        init(size);
    }

    private void init(int size) {
        names = new Name[size];
        for (int i=0; i<size; i++) {
            names[i] = null;
        }
        this.size = size;
    }

    /**
    * Allocate a name from the pool, or a new Name if there is not a matching one there
    * @param prefix - must be interned
    * @param uri - must be interned
    * @param localName - must be interned
    */
    
    public Name allocate(String prefix, String uri, String localName) throws SAXException {
        count++;
        int hash = localName.hashCode();
        hash = (hash & 0x7fffffff) % size;
        int tries = 0;
        while(tries<size) {
            Name name = names[hash];
            if (name==null) {
                // name not found - create a new one
                name = new Name(prefix, uri, localName);
                names[hash] = name;
                return name;
            }

            // see if we've found the right name
            if (name.getPrefix()==prefix && name.getLocalName()==localName && name.getURI()==uri) {
                return name;
            }

            // try the next slot
            hash = (hash+1) % size;
            tries++;
        }
        
        // the hash table is full! Rehash into a bigger one

        //System.err.println("Performance advice: creating additional space for name pool");

        Name[] oldnames = names;
        int oldsize = size;
        init(oldsize*3);
        for (int i=0; i<oldsize; i++) {
            Name n = oldnames[i];
            if (n!=null) {
                allocate(n.getPrefix(), n.getURI(), n.getLocalName() );
            }
        }
        return allocate(prefix, uri, localName);
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
