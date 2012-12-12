package com.icl.saxon;
import com.icl.saxon.om.*;
import org.xml.sax.AttributeList;


    /**
    * AttributeCollection is an implementation of the SAX interface AttributeList
    * that also provides the ability to manipulate namespaces and to convert attributes
    * into Nodes.
    *
    * It is extremely similar (both in interface and in implementation) to the SAX2 Attributes
    * class, but was defined before SAX2 was available.
    */

public class AttributeCollection implements AttributeList
{

    // we use a single array for economy. The elements of this array are arranged
    // in groups of five, being respectively the prefix, the uri, the localname, the
    // type, and the value
    
    private String[] list = null;
    private int used = 0;

    private static int RECSIZE = 5;
    private static int PREFIX = 0;
    private static int URI = 1;
    private static int LOCALNAME = 2;
    private static int TYPE = 3;
    private static int VALUE = 4;

    /**
    * Create an empty attribute list.
    */
    
    public AttributeCollection ()
    {}

    /**
    * Create an empty attribute list with space for n attributes
    */
    
    public AttributeCollection (int n) {
        list = new String[n*5];
    }

    /**
    * Create a new attribute collection as a clone of this one
    */

    public AttributeCollection copy() {
        AttributeCollection ac = new AttributeCollection();
        ac.list = new String[used];
        System.arraycopy(list, 0, ac.list, 0, used);
        ac.used = used;
        return ac;
    }

    /**
    * Add an attribute to an attribute list.
    * @param name The attribute name.
    * @param type The attribute type ("NMTOKEN" for an enumeration).
    * @param value The attribute value (must not be null).
    * @see #removeAttribute
    * @see org.xml.sax.DocumentHandler#startElement
    */
    
    public void addAttribute (Name name, String type, String value)
    {
        addAttribute(
            name.getPrefix(),
            name.getURI(),
            name.getLocalName(),
            type,
            value );
    }

    /**
    * Add an attribute to an attribute list.
    * @param prefix The namespace prefix of the attribute name.
    * @param uri The namespace uri of the attribute name.
    * @param localname The local part of the attribute name.
    * @param type The attribute type (e.g. "NMTOKEN").
    * @param value The attribute value (must not be null).
    * @see #removeAttribute
    * @see org.xml.sax.DocumentHandler#startElement
    */
    
    public void addAttribute (String prefix, String uri, String localName, String type, String value)
    {
        if (list==null) {
            list = new String[5*RECSIZE];
        }
        if (list.length == used) {
            String[] newlist = new String[used*2];
            System.arraycopy(list, 0, newlist, 0, used);
            list = newlist;
        }
        list[used++] = prefix;
        list[used++] = uri;
        list[used++] = localName;
        list[used++] = type;
        list[used++] = value;
    }


    /**
    * Remove an attribute from the list. The call has no effect if there is no attribute
    * with the given name
    * @param name The attribute name.
    */

    public void removeAttribute (Name name)
    {
        int offset = findByName(name);
        if (offset>=0) {
            if (offset+RECSIZE < used) {
                System.arraycopy(list, offset+RECSIZE, list, offset, used-(offset+RECSIZE));
            }
            used -= RECSIZE;
        }
    }

    /**
    * Set an attribute value
    * @param name the name of the attribute
    * @param value the value of the attribute
    */

    public void setAttribute(Name name, String value)
    {
        int offset = findByName(name);
        if (offset<0) {
            addAttribute(name, "CDATA", value);
        } else {
            list[offset + PREFIX] = name.getPrefix();
            list[offset + VALUE] = value;
        }
    }

    /**
    * Set an attribute value
    * @param name the name of the attribute
    * @param type the type of the attribute (e.g. CDATA)
    * @param value the value of the attribute
    */

    public void setAttribute(Name name, String type, String value)
    {
        int offset = findByName(name);
        if (offset<0) {
            addAttribute(name, type, value);
        } else {
            list[offset + PREFIX] = name.getPrefix();
            list[offset + VALUE] = value;
        }
    }

    /**
    * Clear the attribute list.
    */

    public void clear ()
    {
        used = 0;
    }

    /**
    * Compact the attribute list to avoid wasting memory
    */

    public void compact() {
        if (used==0) {
            list = null;
        } else if (list.length > used) {
            String[] newlist = new String[used];
            System.arraycopy(list, 0, newlist, 0, used);
            list = newlist;
        }
    }



    //////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.AttributeList
    //////////////////////////////////////////////////////////////////////


    /**
    * Return the number of attributes in the list.
    * @return The number of attributes in the list.
    */

    public int getLength ()
    {
        return (list==null ? 0 : used / RECSIZE );
    }


    /**
    * Get the name of an attribute (by position).
    *
    * @param i The position of the attribute in the list.
    * @return The display name of the attribute as a string, or null if there
    *         is no attribute at that position.
    */

    public String getName (int i)
    {
        int index = i*RECSIZE;
        if (list==null) return null;
        if (index > used) return null;
        String prefix = list[index+PREFIX];
        String localName = list[index+LOCALNAME];
        if (prefix=="") return localName;
        return prefix + ":" + localName;
    }


    /**
    * Get the type of an attribute (by position).
    * @param i The position of the attribute in the list.
    * @return The attribute type as a string ("NMTOKEN" for an
    *         enumeration, and "CDATA" if no declaration was
    *         read), or null if there is no attribute at
    *         that position.
    */
    
    public String getType (int i)
    {
        int offset = i*RECSIZE;
        if (list==null) return null;
        if (offset > used) return null;
        return list[offset+TYPE];
    }

    /**
    * Get the value of an attribute (by position).
    *
    * @param i The position of the attribute in the list.
    * @return The attribute value as a string, or null if
    *         there is no attribute at that position.
    */
    
    public String getValue (int i)
    {
        int offset = i*RECSIZE;
        if (list==null) return null;
        if (offset > used) return null;
        return list[offset+VALUE];
    }

    /**
    * Get the type of an attribute (by name).
    *
    * @param name The display name of the attribute.
    * @return The attribute type as a string ("NMTOKEN" for an
    *         enumeration, and "CDATA" if no declaration was
    *         read).
    */
    
    public String getType (String name)
    {
        int index = findByDisplayName(name);
        return ( index<0 ? null : list[index+TYPE]);
    }


    /**
    * Get the value of an attribute (by name).
    *
    * @param name The attribute name.
    */
    
    public String getValue (String name)
    {
        int index = findByDisplayName(name);
        return ( index<0 ? null : list[index+VALUE]);
    }

    //////////////////////////////////////////////////////////////////////
    // Additional methods for handling structured Names
    //////////////////////////////////////////////////////////////////////

    /**
    * Get the name of an attribute (by position).
    *
    * @param i The position of the attribute in the list.
    * @return The display name of the attribute as a string, or null if there
    *         is no attribute at that position.
    */

    public Name getExpandedName (int i) 
    {
        int offset = i*RECSIZE;
        if (list==null) return null;
        if (offset > used) return null;
        String prefix = list[offset + PREFIX];
        String uri = list[offset + URI];
        String localName = list[offset + LOCALNAME];
        return Name.reconstruct(prefix, uri, localName);
    }

    /**
    * Get the value of an attribute, given its full name.
    *
    * @param name The full name of the attribute in the list.
    * @return The value of the attribute as a string, or null if there
    *         is no attribute with that name.
    */

    public String getValue (Name name)
    {
        int offset = findByName(name);
        return ( offset<0 ? null : list[offset + VALUE]);
    }

    /**
    * Find the position of an attribute with a given name
    * @return the position of the attribute in the list, or -1 if not present
    */

    public int getPosition(Name name) {
        if (list==null) return -1;
        int pos = findByName(name);
        if (pos<0) return -1;
        return pos/RECSIZE;
    }


    //////////////////////////////////////////////////////////////////////
    // Supporting methods
    //////////////////////////////////////////////////////////////////////

    /**
    * Find an attribute by name
    * @return the index of the attribute (NB not the position)
    */

    private int findByName(Name name) {
        if (list==null) return -1;
        String uri = name.getURI();
        String localName = name.getLocalName();
        for (int i=0; i<used; i+=RECSIZE) {
            if (uri.equals(list[i+URI]) &&
                     localName.equals(list[i+LOCALNAME])) {
                return i;
            }
        }
        return -1;
    }

    /**
    * Find an attribute by display name
    * @return the index of the attribute (NB not the position)
    */

    private int findByDisplayName(String name) {
        if (list==null) return -1;
        int colon = name.indexOf(":");
        if (colon<0) {
            for (int i=0; i<used; i+=RECSIZE) {
                if (name.equals(list[i+LOCALNAME])) {
                    return i;
                }
            }
            return -1;
        } else {
            String prefix=name.substring(0, colon);
            String localName = name.substring(colon+1);
            for (int i=0; i<used; i+=RECSIZE) {
                if (localName.equals(list[i+LOCALNAME]) &&
                        prefix.equals(list[i+PREFIX])) {
                    return i;
                }
            }
            return -1;
        }
    }
    

}
