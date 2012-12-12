package com.icl.saxon.expr;
import org.xml.sax.SAXException;
import java.util.*;

/**
* Tokenizer for patterns.
*
* This code is copied with minor changes from James Clark's xt.
* See copyright notice at end of file.
*
*/


final class Tokenizer {
    private static final int UNKNOWN = -1;
    public static final int EOF = 0;
    public static final int NAME = 1;
    public static final int FUNCTION = 2;
    public static final int LITERAL = 3;
    public static final int VBAR = 4;
    public static final int SLASH = 5; 
    public static final int AT = 6;
    public static final int LSQB = 7;
    public static final int RSQB = 8;
    public static final int LPAR = 9;
    public static final int RPAR = 10;
    public static final int EQUALS = 11;
    public static final int DOT = 12;
    public static final int DOTDOT = 13;
    public static final int STAR = 14;
    public static final int COMMA = 15;
    public static final int SLSL = 16;
    public static final int PREFIX = 17;
    public static final int OR = 18;
    public static final int AND = 19;
    public static final int NUMBER = 20;
    public static final int GT = 21;
    public static final int LT = 22;
    public static final int GE = 23;
    public static final int LE = 24;
    public static final int PLUS = 25;
    public static final int MINUS = 26;
    public static final int MULT = 27;
    public static final int DIV = 28;
    public static final int MOD = 29;
    public static final int DOLLAR = 31;
    public static final int NODETYPE = 32;
    public static final int AXIS = 33;
    public static final int NE = 34;

    public static final int NEGATE = 99;    // unary minus: not actually a token, but we
                                            // use token numbers to identify operators.


    public static String[] tokens =
             {"EOF", "<name>", "<function>", "<literal>", "|", "/", "@", "[", "]",
                                "(", ")", "=", ".", "..", "*", ",", "//", "^",
                                "or", "and", "<number>", ">", "<", ">=", "<=", "+", "-",
                                "*", "div", "mod", "--quo--", "$", "<nodetype>()",
                                "<axis>()", "!="};

    public int currentToken = EOF;
    public String currentTokenValue = null;
    public double currentNumericValue = 0.0;

    private int currentTokenStartIndex = 0;
    public String pattern;
    private int patternIndex = 0;
    private int patternLength;

    private int precedingToken = UNKNOWN;

    //
    // Lexical analyser for patterns
    //

    public void tokenize(String pattern) throws SAXException {
        currentToken = EOF;
        currentTokenValue = null;
        currentTokenStartIndex = 0;
        patternIndex = 0;
        this.pattern = pattern;
        this.patternLength = pattern.length();
        next();
    }

    //diagnostic version of next(): change real version to realnext()
    //
    //public void next() throws SAXException {
    //    realnext();
    //    System.err.println("Token: " + currentToken + "[" + tokens[currentToken] + "]");
    //}

    public void next() throws SAXException {
        precedingToken = currentToken;
        currentTokenValue = null;
        currentTokenStartIndex = patternIndex;
        for (;;) {
            if (patternIndex >= patternLength) {
	            currentToken = EOF;
	            return;
            }
            char c = pattern.charAt(patternIndex++);
            switch (c) {
            case '/':
	            if (patternIndex < patternLength
	                    && pattern.charAt(patternIndex) == '/') {
	                patternIndex++;
	                currentToken = SLSL;
	                return;
	            }
	            currentToken = SLASH;
	            return;
            case '@':
	            currentToken = AT;
	            return;
            case '[':
	            currentToken = LSQB;
	            return;
            case ']':
	            currentToken = RSQB;
	            return;
            case '(':
	            currentToken = LPAR;
	            return;
            case ')':
	            currentToken = RPAR;
	            return;
            case '+':
	            currentToken = PLUS;
	            return;
            case '-':
	            currentToken = MINUS;   // not detected if part of a name
	            return;
            case '=':
	            currentToken = EQUALS;
	            return;
            case '!':
	            if (patternIndex < patternLength
	                    && pattern.charAt(patternIndex) == '=') {
	                patternIndex++;
	                currentToken = NE;
	                return;
	            }
	            throw new SAXException("\"!\" without \"=\" in expression " + pattern);   
            case '*':
                if (precedingToken==EOF ||
                        precedingToken==AT ||
                        precedingToken==LPAR ||                    
                        precedingToken==LSQB ||
                        precedingToken==FUNCTION ||
                        precedingToken==AXIS ||
                        isOperator(precedingToken)) {
	                currentToken = STAR;
                } else {
                    currentToken = MULT;
                }
	            return;
            case ',':
	            currentToken = COMMA;
	            return;
            case '$':
	            currentToken = DOLLAR;
	            return;
            case '|':
	            currentToken = VBAR;
	            return;
            case '<':
	            if (patternIndex < patternLength
	                    && pattern.charAt(patternIndex) == '=') {
	                patternIndex++;
	                currentToken = LE;
	                return;
	            }
	            currentToken = LT;
	            return;
            case '>':
	            if (patternIndex < patternLength
	                    && pattern.charAt(patternIndex) == '=') {
	                patternIndex++;
	                currentToken = GE;
	                return;
	            }
	            currentToken = GT;
	            return;
            case '.':
	            if (patternIndex < patternLength
	                    && pattern.charAt(patternIndex) == '.') {
	                patternIndex++;
	                currentToken = DOTDOT;
	                return;
	            }
	            if (patternIndex == patternLength
	                    || pattern.charAt(patternIndex) < '0'
	                    || pattern.charAt(patternIndex) > '9') {
	                currentToken = DOT;
	                return;
	            }	            
                // otherwise drop through: we have a number starting with a decimal point
            case '0':           
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
	            for (;patternIndex < patternLength; patternIndex++) {
	                c = pattern.charAt(patternIndex);
	                if (!(c=='.' || Character.isDigit(c))) break;
	            }
	            currentTokenValue = pattern.substring(currentTokenStartIndex, patternIndex);
	            try {
                    currentNumericValue = new Double(currentTokenValue).doubleValue();
	            } catch (NumberFormatException err) {
	                throw new SAXException("Invalid number (" + currentTokenValue + ") in expression " + pattern);
	            }
	            currentToken = NUMBER;
	            return;            
            case '"':
            case '\'':
	            patternIndex = pattern.indexOf(c, patternIndex);
	            if (patternIndex < 0) {
	                patternIndex = currentTokenStartIndex + 1;
	                throw new SAXException("Unmatched quote in expression " + pattern);
	            }
	            currentTokenValue = pattern.substring(currentTokenStartIndex + 1,
					      patternIndex++).intern();
	            currentToken = LITERAL;
	            return;
            case ' ':
            case '\t':
            case '\r':
            case '\n':
	            currentTokenStartIndex = patternIndex;
	            break;
            default:
	            if (c < 0x80 && !Character.isLetter(c))
	                throw new SAXException("Invalid character (" + c + ") in expression " + pattern);
	            /* fall through */
            case '_':
            loop:
	            for (;patternIndex < patternLength; patternIndex++) {
	                c = pattern.charAt(patternIndex);
	                switch (c) {
                    case ':':
        	            if (patternIndex+1 < patternLength &&
    	                        pattern.charAt(patternIndex+1) == ':') {
	                        currentTokenValue = pattern.substring(currentTokenStartIndex,
					                                        patternIndex).intern();
                            currentToken = AXIS;
                            patternIndex+=2;
                            return;
        	            }
                        if (patternIndex+1 < patternLength &&
    	                        pattern.charAt(patternIndex+1) == '*') {
	                        currentTokenValue = pattern.substring(currentTokenStartIndex,
					                                        patternIndex).intern();
                            currentToken = PREFIX;
                            patternIndex+=2;
                            return;
        	            }       	            
                        break;
	                case '.':
	                case '-':
	                case '_':
	                    break;
	                case '(':
 	                    currentTokenValue = pattern.substring(currentTokenStartIndex,
						                                        patternIndex).intern();
						int op = getBinaryOp(currentTokenValue);                                        
	                    if (op != UNKNOWN) {
	                        currentToken = op;
	                        return;
	                    }
	                    patternIndex++;                         // swallows the '('
	                    currentToken = getFunctionType(currentTokenValue);
	                    return;
	                default:
	                    if (c < 0x80 && !Character.isLetterOrDigit(c))
	                        break loop;
	                    break;
	                }
	            }
	            currentTokenValue = pattern.substring(currentTokenStartIndex,
					                                    patternIndex).intern();
            lookahead:
	            for (int i = patternIndex; i < patternLength; i++) {
	                switch (pattern.charAt(i)) {
	                case ' ':
	                case '\t':
	                case '\r':
	                case '\n':
	                    break;
                    case ':':
        	            if (i+1 < patternLength && pattern.charAt(i+1) == ':') {
                            currentToken = AXIS;
                            patternIndex = i+2;
                            return;
        	            }
                        break;                    
	                case '(':
                        int oper = getBinaryOp(currentTokenValue);
                        if (oper != UNKNOWN) {
                            currentToken = oper;
                            return;
                        } else {
	                        currentToken = getFunctionType(currentTokenValue);
	                        patternIndex = i + 1;
	                        return;
	                    }
	                    /* fall through */
	                default:
	                    break lookahead;
	                }
	            }
	            int optype = getBinaryOp(currentTokenValue);
	            if (optype!=UNKNOWN && !
	                     (  precedingToken==EOF ||
                            precedingToken==AT ||
                            precedingToken==LPAR ||                    
                            precedingToken==LSQB ||
                            precedingToken==FUNCTION ||
                            precedingToken==AXIS ||
                            isOperator(precedingToken))
                        ) {
                    currentToken = optype;
                } else {
	                currentToken = NAME;
                }
	            return;
            }
        }
    }

    /**
    * Identify a binary operator
    * @param s String representation of the operator - must be interned
    */

    static private int getBinaryOp(String s) {
        if (s=="and") return AND;
        if (s=="or") return OR;        
        if (s=="div") return DIV;
        if (s=="mod") return MOD;
        return UNKNOWN;
    }

    /**
    * Distinguish axis names, nodetype names, and function names, which appear in the
    * same syntactic context
    * @param s the name - must be interned
    */

    static private int getFunctionType(String s) {
        //if (s.startsWith("from-")) return AXIS;
        if (s=="node") return NODETYPE;
        if (s=="text") return NODETYPE;        
        if (s=="comment") return NODETYPE;
        if (s=="processing-instruction") return NODETYPE;
        return FUNCTION;
    }

    /**
    * Test whether a token is an operator
    */

    static private boolean isOperator(int tok) {
        return (
            tok==SLASH || tok==SLSL || tok==VBAR ||
            tok==EQUALS || tok==OR || tok==AND || tok==GT || tok==LT || 
            tok==GE || tok==LE || tok==PLUS || tok==MINUS || tok==MULT || tok==DIV ||
            tok==MOD );
    }
}

/*

The following copyright notice is copied from the licence for xt, from which the
original version of this module was derived:
--------------------------------------------------------------------------------
Copyright (c) 1998, 1999 James Clark

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED ``AS IS'', WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL JAMES CLARK BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of James Clark shall
not be used in advertising or otherwise to promote the sale, use or
other dealings in this Software without prior written authorization
from James Clark.
---------------------------------------------------------------------------
*/
