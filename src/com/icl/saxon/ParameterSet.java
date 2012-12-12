package com.icl.saxon;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;

/**
* A ParameterSet is a set of parameters supplied when calling a template.
* It is a collection of name-value pairs
*/

public class ParameterSet 
{

    // we use a single array for economy. The elements of this vector are arranged
    // in groups of two, being respectively the name and the value
    
    private Object[] list = new Object[10];
    private int used = 0;


    /**
    * Add a parameter to the ParameterSet
    * @param name The name of the parameter. The string must be interned.
    * @param value The value of the parameter
    */
    
    public void put (String name, Value value) {
        if (used+2 > list.length) {
            Object[] newlist = new Object[used*2];
            System.arraycopy(list, 0, newlist, 0, used);
            list = newlist;
        }
        list[used++] = name;
        list[used++] = value;
    }

    /**
    * Get a parameter
    * @param name The name. The value must be interned.
    * @return The value of the parameter, or null if not defined
    */

    public Value get (String name) {
        for (int i=0; i<list.length; i+=2) {
            if ((String)list[i]==name) {
                return (Value)list[i+1];
            }
        }
        return null;
    }

    /**
    * Clear all values
    */

    public void clear() {
        used = 0;
    }

}
