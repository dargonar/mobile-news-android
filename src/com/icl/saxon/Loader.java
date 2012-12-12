package com.icl.saxon;

import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.lang.SecurityException;
import java.lang.ClassCastException;

import org.xml.sax.SAXException;


/**
  * Loader is used to load a class given its name.
  * The implementation varies in different Java environments.
  *
  * @author Michael H. Kay (Michael.Kay@icl.com)
  * @version 25 Sep 1998: add facility to select DOM builder
  */

 
public class Loader {

    private static boolean tracing = false;

    /**
    * Switch tracing on or off
    */

    public synchronized static void setTracing(boolean onOrOff) {
        tracing = onOrOff;
    }
      
    /**
    * Load a class using the class name provided.<br>
    * Note that the method does not check that the object is of the right class.
    * @param className A string containing the name of the
    *   class, for example "com.microstar.sax.LarkDriver"
    * @return an instance of the class named, or null if it is not
    * loadable.
    * @throw an exception if the class cannot be loaded.
    * 
    */
    
    public static Class getClass(String className) throws SAXException
    {
        if (tracing) {
            System.err.println("Loading " + className);
        }
        if (Version.isPreJDK12()) {
            try {
                return Class.forName(className);
            }
            catch (Exception e) {
                throw new SAXException("Failed to load " + className, e );
            }
        } else {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            }
            catch (Exception e) {
                throw new SAXException("Failed to load " + className, e );
            }
        }

    }

  /**
    * Instantiate a class using the class name provided.<br>
    * Note that the method does not check that the object is of the right class.
    * @param className A string containing the name of the
    *   class, for example "com.microstar.sax.LarkDriver"
    * @return an instance of the class named, or null if it is not
    * loadable.
    * @throw an exception if the class cannot be loaded.
    * 
    */
    
    public static Object getInstance(String className) throws SAXException {
        Class theclass = getClass(className);
        try {
            return theclass.newInstance();
        } catch (Exception err) {
            throw new SAXException("Failed to instantiate class " + className, err);
        }
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
// The Original Code is: all this file, other than fragments copied from the SAX distribution
// made available by David Megginson, and the line marked PB-SYNC.
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (Michael.Kay@icl.com).
//
// The line marked PB-SYNC is by Peter Bryant (pbryant@bigfoot.com). All Rights Reserved. 
//
// Contributor(s): Michael Kay, Peter Bryant, David Megginson 
//
