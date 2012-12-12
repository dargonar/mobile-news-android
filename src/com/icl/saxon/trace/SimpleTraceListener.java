package com.icl.saxon.trace;

import org.xml.sax.SAXException;
import com.icl.saxon.style.StyleElement;
import com.icl.saxon.handlers.NodeHandler;
import com.icl.saxon.*;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.om.ElementInfo;


/**
* A Simple trace listener that writes messages to System.err
*/

public class SimpleTraceListener implements TraceListener {

  String indent = "";

  /**
  * Called at start
  */

  public void open() {
    System.err.println("<trace>");
  }

  /**
  * Called at end
  */

  public void close() {
    System.err.println("</trace>");
  }
  
  

  /**
   * Called for all top level elements
   */
  public void toplevel(NodeInfo element)
    throws SAXException
  {
    StyleElement e = (StyleElement)element;
    System.err.println("<Top-level element=\"" + e.getDisplayName() + "\" line=\"" + e.getLineNumber() +
       "\" file=\"" + e.getSystemId() + "\" precedence=\"" + e.getPrecedence() +"\"/>");
  }

  /**
   * Called when a node of the source tree gets processed
   */
  public void enterSource(NodeHandler handler, Context context)
    throws SAXException
  {
    NodeInfo curr = context.getContextNode();
    System.err.println(indent + "<Source node=\""  + curr.getPath()
                        + "\" line=\"" + curr.getLineNumber()
		                + "\" mode=\"" + getModeName(context) + "\">");
    indent += " ";
  }

  /**
   * Called after a node of the source tree got processed
   */
  public void leaveSource(NodeHandler handler, Context context)
    throws SAXException
  {
    indent = indent.substring(0, indent.length() - 1);
    System.err.println(indent + "</Source><!-- "  + context.getContextNode().getPath() + " -->");
  }

  /**
   * Called when an element of the stylesheet gets processed
   */
  public void enter(NodeInfo element, Context context)
    throws SAXException
  {
    if (element instanceof ElementInfo) {
        System.err.println(indent + "<Instruction element=\"" + element.getDisplayName() + "\" line=\"" + element.getLineNumber() + "\">");
        indent += " ";
    }
  }

  /**
   * Called after an element of the stylesheet got processed
   */
  public void leave(NodeInfo element, Context context)
    throws SAXException
  {
    if (element instanceof ElementInfo) {
        indent = indent.substring(0, indent.length() - 1);
        System.err.println(indent + "</Instruction> <!-- " + element.getDisplayName() + " -->");
    }
  }

  String getModeName(Context context)
  {
    String name = context.getMode().getName();
    return (name==null ? "*default*" : name);
  }
}
