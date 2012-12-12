package com.icl.saxon.sql;
import com.icl.saxon.*;
import com.icl.saxon.style.*;
import com.icl.saxon.expr.*;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;
import org.w3c.dom.Node;
import java.sql.*;
import java.util.*;

/**
* An sql:insert element in the stylesheet.<BR>
*/

public class SQLInsert extends StyleElement {

    String table;
    
    public void prepareAttributes() throws SAXException {
      
		table = getAttribute("table");
		if (table==null) reportAbsence("table");

    }

    public void validate() throws SAXException {
        checkWithinTemplate();
    }

    public void process( Context context ) throws SAXException {
	    
        // Prepare the SQL statement (ideally we would only do this once)

        StringBuffer statement = new StringBuffer();
        statement.append("INSERT INTO " + table + " (");
        
        PreparedStatement ps;

		// Collect names of columns to be added 

        Node child = getFirstChild();
		int cols = 0;
		while (child!=null) {
		    if (child instanceof SQLColumn) {
    			if (cols++ > 0)	statement.append(',');		 
    			String colname = ((SQLColumn)child).getColumnName();			  
    			statement.append(colname);
		    }			
			child = child.getNextSibling();
		}
        statement.append(") VALUES (");
        
        // Add "?" marks for the variable parameters

		for(int i=0; i<cols; i++) {
			if (i!=0)
			    statement.append(',');
			statement.append('?');
		};
		
		statement.append(')');

        // Prepare the SQL statement

        Connection connection = (Connection)context.getController().getUserData(
                                                        getPrincipalStyleSheet(),
                                                        "sql:connection");
        if (connection==null) {
            throw styleError("No SQL connection has been established");
        }
        
		try {
    	    ps=connection.prepareStatement(statement.toString());        

            // Add the actual column values to be inserted
	    
    		Vector values=new Vector();

            child = getFirstChild();
            int i=1;
		    while (child!=null) {
		        if (child instanceof SQLColumn) {
		            			 
        			// Get the column value: either from the select attribute or from content
        			Value v = ((SQLColumn)child).getColumnValue(context);
			
        			// TODO: the values are all strings. There is no way of adding to a numeric column
        		    String val = v.asString();
		    
        		    // another hack: setString() doesn't seem to like single-character string values
        		    if (val.length()==1) val += " ";
        			ps.setString(i++, val);

		        }
				
    			// Get the next column and decide whether we've reached the last
    			child = child.getNextSibling();
    		}
        		
			ps.executeUpdate();

	    } catch (SQLException ex) {
			throw styleError("(SQL) " + statement + ": " + ex.getMessage());
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
