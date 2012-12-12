package com.icl.saxon.expr;
import com.icl.saxon.*;
import com.icl.saxon.om.*;
import org.xml.sax.SAXException;
import java.util.*;
import java.lang.reflect.*;
import org.w3c.dom.*;


/**
* This class acts as a proxy for an extension function defined as a method
* in a user-defined class
*/

public class FunctionProxy extends Function {

    private Class theClass;
    private Object theInstance;
    private Method theMethod;
    private Class[] theParameterTypes;
    private Class theReturnType;
    private SAXException theException = null;
    private String name;
    private boolean usesContext;
    private boolean isStatic;
    private Constructor theConstructor;

    /**
    * Constructor: creates an uncommitted FunctionProxy
    */

    public FunctionProxy() {}

    /**
    * setMethod: locates the external class and method to which this function relates. At this
    * stage addArguments() will have been called, so the number of arguments is known. If no
    * method of the required name is located, an exception is saved, but it is not thrown until
    * an attempt is made to evaluate the function.
    * @param fullName This identifies both the local name of the function and its namespace.
    */

    public void setMethod(Name fullName) {
        try {
            name = fullName.getLocalName();
            String className = getClassName(fullName);
            int numArgs = arguments.size();

            theClass = Loader.getClass(className);

            // if the method name is "new", look for a matching constructor

            if (name.equals("new")) {
                Constructor[] constructors = theClass.getConstructors();
                for (int c=0; c<constructors.length; c++) {
                    theConstructor = constructors[c];
                    theParameterTypes = theConstructor.getParameterTypes();
                    theReturnType = theClass;
                    isStatic = true;
                    if (theParameterTypes.length == numArgs) {
                        return;
                    }
                }
                throw new SAXException("No constructor with " + numArgs +
                                     (numArgs==1 ? " parameter" : " parameters") +
                                      " found in class " + className);
            } else {
        
                // look through the methods of this class to find one that matches the local name

                Method[] methods = theClass.getMethods();
                for (int m=0; m<methods.length; m++) {

                    theMethod = methods[m];
                    theParameterTypes = theMethod.getParameterTypes();
                    theReturnType = theMethod.getReturnType();            
                    isStatic = Modifier.isStatic(theMethod.getModifiers());

                    // if the method is not static, the first supplied argument is the instance, so
                    // discount it

                    int significantArgs = (isStatic ? numArgs : numArgs - 1);

                    if (significantArgs>=0) {

                        //System.err.println("Looking for " + name + "(" + significantArgs +"), trying " +
                        //     methods[m].getName() + "(" + theParameterTypes.length + ")");
   
                        if (theParameterTypes.length == significantArgs &&
                                matches(theMethod.getName(), name) &&
                                (significantArgs==0 || theParameterTypes[0]!=Context.class)) {
                            usesContext = false;
                            return;
                        }
            
                        // we allow the method to have an extra parameter if the first parameter is Context
            
                        if (theParameterTypes.length == significantArgs+1 &&
                                matches(theMethod.getName(), name) &&
                                theParameterTypes[0]==Context.class) {
                            usesContext = true;
                            return;
                        }
                    }

                }
        
                // No method found.
                  
                throw new SAXException("No method matching " + name +
                                         " with " + numArgs +
                                         (numArgs==1 ? " parameter" : " parameters") +
                                          " found in class " + className);
            }
        } catch (SAXException err) {    // Don't fail until the method is called
            theException = err;
        }
    }

    private String getClassName(Name fullName) throws SAXException {

        String uri = fullName.getURI();
        String className;

        if (uri.equals(Namespace.SAXON)) {  // recognise the SAXON extension functions specially

            className = "com.icl.saxon.functions.Extensions";

        } else {

            // extract the class name as anything in the URI after the last "/"

            int slash = uri.lastIndexOf('/');
            if (slash<0) {
                className = uri;
            } else if (slash==uri.length()-1) {
                throw new SAXException("Namespace URI must contain a classname after the final slash (" + uri + ")");
            } else {
                className = uri.substring(slash+1);
            }
        }
        return className;
    }
        

    /**
    * Determine whether the function is available. This supports the extension-function-available
    * function, and as such it is done without regard to the number of arguments.
    */

    public boolean isAvailable(Name fullName) {

        try {
            name = fullName.getLocalName();
            String className = getClassName(fullName);
            theClass = Loader.getClass(className);
        } catch (Exception err) {
            return false;
        }

        // for the constructor, return true if it seems to be instantiable

        if (name.equals("new")) {
            int m = theClass.getModifiers();
            if (Modifier.isInterface(m)) return false;
            if (Modifier.isAbstract(m)) return false;
            if (theClass.getConstructors().length == 0) return false;
            return true;
        }

        // look through the methods of this class to find one that matches the local name

        Method[] methods = theClass.getMethods();
        for (int m=0; m<methods.length; m++) {
            if (matches(methods[m].getName(), name)) {
                return true;
            }
        }
        return false;
    }

    /**
    * Determine whether the XPath function name matches the Java method name.
    * This is true if the names are the same ignoring hyphens and case.
    * @param methodName the Java method name
    * @param functionName the XPath function name
    */

    private boolean matches(String methodName, String functionName) {
        if (methodName.equalsIgnoreCase(functionName)) return true;
        int m=0;
        int f=0;
        int ml = methodName.length();
        int fl = functionName.length();
        while (true) {
            if (functionName.charAt(f)=='-') {
                f++;
                if (f>fl) return false;
            }
            if (Character.toUpperCase(methodName.charAt(m)) !=
                     Character.toUpperCase(functionName.charAt(f))) return false;
            f++;
            m++;
            if (f>=fl && m>=ml) return true;
            if (f>=fl || m>=ml) return false;
        }
    }
            
    /**
    * Get the name of the function
    */

    public String getName() {
        return name;
    }

    /**
    * Determine which aspects of the context the expression depends on. The result is
    * a bitwise-or'ed value composed from constants such as Context.VARIABLES and
    * Context.CURRENT_NODE
    */

    public int getDependencies() {
        return Context.CONTEXT_NODE | Context.POSITION | Context.LAST;
    }

    /**
    * Perform a partial evaluation of the expression, by eliminating specified dependencies
    * on the context.
    * @param dependencies The dependencies to be removed
    * @param context The context to be used for the partial evaluation
    * @return a new expression that does not have any of the specified
    * dependencies
    */

    public Expression reduce(int dependencies, Context context) throws SAXException {
        // only safe thing is to evaluate it now
        if ((dependencies & (Context.CONTEXT_NODE | Context.POSITION | Context.LAST)) != 0) {
            return evaluate(context);
        } else {
            return this;
        }
    }

    /**
    * Evaluate the function. <br>
    * @param arguments A Vector, each of whose elements is a Value containing the value of a
    * supplied argument to the function.
    * @param context The context in which the function is to be evaluated
    * @return a Value representing the result of the function. 
    * @throws SAXException if the function cannot be evaluated.
    */    

    public Value eval(Vector arguments, Context context) throws SAXException {

        // Fail now if no method was found

        if (theException!=null) {
            throw theException;
        }

        if (name.equals("new")) {       
            int requireArgs = theParameterTypes.length;
            int numArgs = checkArgumentCount(requireArgs, requireArgs);
            
            Object[] params = new Object[theParameterTypes.length];
            
            setupParams(params, theParameterTypes, arguments, 0, 0);           

            try {
                Object obj = theConstructor.newInstance(params);
                return new ObjectValue(obj);
            } catch (InstantiationException err0) {
                throw new SAXException ("Cannot instantiate class", err0);
            } catch (IllegalAccessException err1) {
                throw new SAXException ("Constructor access is illegal", err1);
            } catch (IllegalArgumentException err2) {
                throw new SAXException ("Argument is of wrong type", err2);
            } catch (InvocationTargetException err3) {
                throw new SAXException ("Exception in extension function" + err3.getTargetException().toString());
            }
        } else {

            if (isStatic) {
                theInstance = null;
            } else {
                if (arguments.size()==0) {
                    throw new SAXException("Must supply an argument for an instance-level extension function");
                }
                if (!(arguments.elementAt(0) instanceof ObjectValue)) {
                    throw new SAXException("First argument is not an object instance");
                }
                theInstance = ((ObjectValue)arguments.elementAt(0)).getObject();
            }

            int requireArgs = theParameterTypes.length - (usesContext ? 1 : 0) + (isStatic ? 0 : 1);
            int numArgs = checkArgumentCount(requireArgs, requireArgs);
            Object[] params = new Object[theParameterTypes.length];

            if (usesContext) {
                context.setStaticContext(getStaticContext());
                params[0] = context;
            }

            setupParams(params, theParameterTypes, arguments, (usesContext ? 1 : 0), (isStatic ? 0 : 1));
      
            try {
                Object result = theMethod.invoke(theInstance, params);
                return convertJavaObjectToXPath(result);
            } catch (IllegalAccessException err1) {
                throw new SAXException ("Method access is illegal", err1);
            } catch (IllegalArgumentException err2) {
                throw new SAXException ("Argument is of wrong type", err2);
            } catch (InvocationTargetException err3) {
                Throwable ex = err3.getTargetException();
                if (ex instanceof SAXException) {
                    throw (SAXException)ex;
                } else {
                    throw new SAXException ("Exception in extension function " +
                                            err3.getTargetException().toString());
                }
            }
        }
    }

    public static Value convertJavaObjectToXPath(Object result) throws SAXException {
        if (result instanceof Boolean) {
            return new BooleanValue(((Boolean)result).booleanValue());
        } else if (result instanceof Double) {
            return new NumericValue(((Double)result).doubleValue());
        } else if (result instanceof Float) {
            return new NumericValue((double)((Float)result).floatValue());
        } else if (result instanceof Short) {
            return new NumericValue((double)((Short)result).shortValue());
        } else if (result instanceof Integer) {
            return new NumericValue((double)((Integer)result).intValue());
        } else if (result instanceof Long) {
            return new NumericValue((double)((Long)result).longValue());
        } else if (result instanceof String) {
            return new StringValue((String)result);
        } else if (result instanceof Value) {
            return (Value)result;
        } else if (result instanceof NodeInfo) {
            return new SingletonNodeSet((NodeInfo)result);
        } else if (result instanceof org.w3c.dom.NodeList) {
            NodeList list = ((NodeList)result);
            NodeInfo[] nodes = new NodeInfo[list.getLength()];
            for (int i=0; i<list.getLength(); i++) {
                if (list.item(i) instanceof NodeInfo) {
                    nodes[i] = (NodeInfo)list.item(i);
                } else {
                    throw new SAXException("Result NodeList contains non-Saxon DOM Nodes");
                }
            }
            return new NodeSetExtent(nodes);
        } else if (result instanceof org.w3c.dom.Node) {
            throw new SAXException("Result is a non-Saxon DOM Node");
        } else {
            return new ObjectValue(result);
        }
    }
                
    private void setupParams(Object[] params,
                             Class[] paramTypes,
                             Vector arguments,
                             int firstParam,
                             int firstArg) throws SAXException {
        int j=firstParam;
        for (int i=firstArg; i<arguments.size(); i++) {
            Value arg = (Value)arguments.elementAt(i);
            Class pclass = paramTypes[j];

            // try direct mappings of scalar values
        
            if (pclass==boolean.class) {
                params[j++] = new Boolean(arg.asBoolean());
            } else if (pclass==double.class) {
                params[j++] = new Double(arg.asNumber());
            } else if (pclass==float.class) {
                params[j++] = new Float((float)arg.asNumber());
            } else if (pclass==int.class) {
                params[j++] = new Integer((int)arg.asNumber());
            } else if (pclass==long.class) {
                params[j++] = new Long((long)arg.asNumber());
            } else if (pclass==short.class) {
                params[j++] = new Short((short)arg.asNumber());
            } else if (pclass==String.class) {
                params[j++] = new String(arg.asString());
            } else if (pclass==Value.class) {
                params[j++] = arg;


            // try the DOM types
            
            } else if (arg instanceof NodeSetValue &&
                            (pclass==org.w3c.dom.Node.class ||
                             pclass==org.w3c.dom.Document.class ||
                             pclass==org.w3c.dom.Element.class ||
                             pclass==org.w3c.dom.Attr.class ||
                             pclass==org.w3c.dom.Text.class ||
                             pclass==org.w3c.dom.Comment.class ||
                             pclass==org.w3c.dom.ProcessingInstruction.class)) {
                params[j++] = ((NodeSetValue)arg).getFirst();
            } else if (arg instanceof NodeSetValue &&
                             pclass==org.w3c.dom.NodeList.class) {
                params[j++] = arg;
            //} else if (arg instanceof FragmentValue &&
            //                 pclass==org.w3c.dom.DocumentFragment.class) {
            //    params[j++] = ((FragmentValue)arg);

            // following attempts will fail if the parameter is declared with the wrong type,
            // but they are useful where the type is, e.g. "Object"
            
            } else if (arg instanceof BooleanValue) {
                params[j++] = new Boolean(arg.asBoolean());
            } else if (arg instanceof NumericValue) {
                params[j++] = new Double(arg.asNumber());
            } else if (arg instanceof StringValue) {
                params[j++] = new String(arg.asString());
            } else if (arg instanceof ObjectValue) {
                Object obj = ((ObjectValue)arg).getObject();
                params[j++] = obj;
            } else {
                params[j++] = arg;
            }
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
// The Original Code is: all this file. 
//
// The Initial Developer of the Original Code is
// Michael Kay of International Computers Limited (Michael.Kay@icl.com).
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved. 
//
// Contributor(s): none. 
//
