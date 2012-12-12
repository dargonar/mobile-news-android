package com.icl.saxon.expr;
import com.icl.saxon.om.*;
import com.icl.saxon.*;
import com.icl.saxon.pattern.*;
import com.icl.saxon.axis.*;
import com.icl.saxon.functions.*;

import org.xml.sax.SAXException;
import java.util.*;

/**
* Parser for XSL expressions and patterns.
*
* This code was originally inspired by James Clark's xt but has been totally rewritten (twice!)
* @author Michael Kay, ICL
* @version: 12 October 1999
*
*/


public final class ExpressionParser {

    private Tokenizer t;
    private StaticContext env;

    /**
    * Expect a given token, fail if the current token is different
    */

    private void expect(int token) throws SAXException {
        if (t.currentToken != token)
            grumble("expected \"" + Tokenizer.tokens[token] + "\"" +
                             ", found \"" + Tokenizer.tokens[t.currentToken] + "\"");
    }

    /**
    * Report a parsing error
    */

    private void grumble(String message) throws SAXException {
        throw new SAXException("Error in expression " + t.pattern + ": " + message);
    }

    /**
    * Parse a string representing an expression
    * @pattern the expression expressed as a String
    * @return an Expression object representing the result of parsing
    */

	public Expression parse(String expression, StaticContext env) throws SAXException {
        //System.err.println("Parse expression: " + expression);
	    this.env = env;
        t = new Tokenizer();
	    t.tokenize(expression);
        Expression exp = parseExpression();
        if (t.currentToken != Tokenizer.EOF)
            grumble("Unexpected token " + Tokenizer.tokens[t.currentToken] + " beyond end of expression");
        exp.setStaticContext(env);
        return exp;
    }

    /**
    * Parse a string representing a pattern
    * @pattern the expression expressed as a String
    * @return a Pattern object representing the result of parsing
    */

    public Pattern parsePattern(String pattern, StaticContext env) throws SAXException {
        //System.err.println("Parse pattern: " + pattern);
	    this.env = env;
        t = new Tokenizer();
	    t.tokenize(pattern);
        Pattern pat = parseUnionPattern();
        if (t.currentToken != Tokenizer.EOF)
            grumble("Unexpected token " + Tokenizer.tokens[t.currentToken] + " beyond end of pattern");
        pat.setStaticContext(env);
        return pat;
    }


    //////////////////////////////////////////////////////////////////////////////////
    //                     EXPRESSIONS                                              //
    //////////////////////////////////////////////////////////////////////////////////


    /**
    * Parse an Expression ( ::= OrExpression ): 
    * AndExpr ( 'or' AndExpr )*
    */

    private Expression parseExpression() throws SAXException {
        Expression exp = parseAndExpression();
        while (t.currentToken == Tokenizer.OR) {
            t.next();
            exp = new BooleanExpression(exp, Tokenizer.OR, parseAndExpression());
        }
        exp.setStaticContext(env);
        return exp;
    }

    /**
    * Parse an AndExpr: 
    * EqualityExpr ( 'and' EqualityExpr )*
    */

    private Expression parseAndExpression() throws SAXException {
        Expression exp = parseEqualityExpression();
        while (t.currentToken == Tokenizer.AND) {
            t.next();
            exp = new BooleanExpression(exp, Tokenizer.AND, parseEqualityExpression());
        }
        exp.setStaticContext(env);
        return exp;
    }

    /**
    * Parse an EqualityExpr:<br>
    * RelationalExpr ( '=' | '!=' RelationalExpr )*
    */

    private Expression parseEqualityExpression() throws SAXException {
        Expression exp = parseRelationalExpression();
        while (t.currentToken == Tokenizer.EQUALS ||
                t.currentToken == Tokenizer.NE) {
            int op = t.currentToken;
            t.next();
            exp = new RelationalExpression(exp, op, parseRelationalExpression());
        }
        exp.setStaticContext(env);
        return exp;
    }

    /**
    * Parse a RelationalExpr: 
    * AdditiveExpr ( ('&lt;'|'&gt;'|'&gt;='|'&lt;=') AdditiveExpr )*
    */

    private Expression parseRelationalExpression() throws SAXException {
        Expression exp = parseAdditiveExpression();
        while (t.currentToken == Tokenizer.LT ||
                t.currentToken == Tokenizer.GT ||
                t.currentToken == Tokenizer.LE ||
                t.currentToken == Tokenizer.GE ) {
            int op = t.currentToken;
            t.next();
            exp = new RelationalExpression(exp, op, parseAdditiveExpression());
        }
        exp.setStaticContext(env);
        return exp;
    }

    /**
    * Parse an AdditiveExpr: 
    * MultiplicativeExpr ( (+|-) MultiplicativeExpr )*
    */

    private Expression parseAdditiveExpression() throws SAXException {
        Expression exp = parseMultiplicativeExpression();
        while (t.currentToken == Tokenizer.PLUS ||
                t.currentToken == Tokenizer.MINUS ) {
            int op = t.currentToken;
            t.next();
            exp = new ArithmeticExpression(exp, op, parseMultiplicativeExpression());
        }
        exp.setStaticContext(env);
        return exp;
    }

    /**
    * Parse a MultiplicativeExpr:<br>
    * UnaryExpr ( (*|div|mod) UnaryExpr )*
    */

    private Expression parseMultiplicativeExpression() throws SAXException {
        Expression exp = parseUnaryExpression();
        while (t.currentToken == Tokenizer.MULT ||
                t.currentToken == Tokenizer.DIV ||
                t.currentToken == Tokenizer.MOD ) {
            int op = t.currentToken;
            t.next();
            exp = new ArithmeticExpression(exp, op, parseUnaryExpression());
        }
        exp.setStaticContext(env);
        return exp;
    }

    /**
    * Parse a UnaryExpr:<br>
    * UnionExpr | '-' UnaryExpr
    */

    private Expression parseUnaryExpression() throws SAXException {
        Expression exp;
        if (t.currentToken == Tokenizer.MINUS) {
            t.next();
            exp = new ArithmeticExpression(new NumericValue(0),
                                          Tokenizer.NEGATE,
                                          parseUnaryExpression());
        }
        else {
            exp = parseUnionExpression();
        }
        exp.setStaticContext(env);
        return exp;
    }


    /**
    * Parse a UnionExpr:<br>
    * path ( | path )*
    */

    private Expression parseUnionExpression() throws SAXException {
        Expression exp = parsePathExpression();
        while (t.currentToken == Tokenizer.VBAR ) {
            t.next();
            exp = new UnionExpression(exp, parsePathExpression());
        }
        exp.setStaticContext(env);
        return exp;
    }

    /**
    * Parse a PathExpresssion. This includes "true" path expressions such as A/B/C, and also
    * elements that may start a path expression such as a variable reference $name or a
    * parenthesed expression (A|B). For some reason numeric and string literals also come
    * under this heading.
    */

    private Expression parsePathExpression() throws SAXException {
        Expression start;
        switch (t.currentToken) {
        case Tokenizer.SLASH:
            t.next();
            switch(t.currentToken) {
                // some of these, eg. "/@x" or "/." are technically permitted but meaningless
                case Tokenizer.NAME:
                case Tokenizer.PREFIX:
                case Tokenizer.STAR:
                case Tokenizer.AT:
                case Tokenizer.NODETYPE:
                case Tokenizer.AXIS:
                case Tokenizer.DOT:
                case Tokenizer.DOTDOT:
                    return parseRelativePath(new RootExpression());
                default:
                    return new RootExpression();
            }
            
        case Tokenizer.SLSL:
            // add in the implicit from-descendants-or-self() step
            return parsePathContinuation(new RootExpression());

        case Tokenizer.DOT:
            t.next();
            return parsePathContinuation(new ContextNodeExpression());
            
        case Tokenizer.DOTDOT:
            t.next();
            return parsePathContinuation(new ParentNodeExpression());
            
        case Tokenizer.NAME:
        case Tokenizer.PREFIX:
        case Tokenizer.STAR:
        case Tokenizer.NODETYPE:
        case Tokenizer.AXIS:
        case Tokenizer.AT:
            return parseRelativePath(new ContextNodeExpression());

        default:
            start = parseFilterExpression();
            Expression continuation = parsePathContinuation(start);
            continuation.setStaticContext(env);            
            return continuation;
        }
    }

    /**
    * Parse filter expression
    */

    private Expression parseFilterExpression() throws SAXException {
        Expression primary = parsePrimaryExpression();
        while (t.currentToken == Tokenizer.LSQB) {
            t.next();
            Expression predicate = parseExpression();
            expect(Tokenizer.RSQB);
            primary = new FilterExpression(primary, predicate);
            primary.setStaticContext(env);
            t.next();
        }
        return primary;
    }

    /**
    * Parse a primary expression. One of:
    *   variable-reference:   $ name
    *   parentheses:          ( expr )
    *   string literal:       'fred'
    *   numeric literal:      93.7
    *   function call:        name(args,...)
    */

    private Expression parsePrimaryExpression() throws SAXException {
        switch (t.currentToken) {
        case Tokenizer.DOLLAR:
            t.next();
            expect(Tokenizer.NAME);
            String var = t.currentTokenValue;
            t.next();
            return new VariableReference(env.makeName(var, false), env);

        case Tokenizer.LPAR:
            t.next();
            Expression exp = parseExpression();
            expect(Tokenizer.RPAR);
            t.next();
            return exp;

        case Tokenizer.LITERAL:
            Expression literal = new StringValue(t.currentTokenValue);
            t.next();
            return literal;
            
        case Tokenizer.NUMBER:
            Expression number = new NumericValue(t.currentTokenValue);
            t.next();
            return number;
            
        case Tokenizer.FUNCTION:
            return parseFunctionCall();

        default:
            grumble("Unexpected token " + Tokenizer.tokens[t.currentToken] + " in expression");
            return null;
        }
    }


    /**
    * Parse path continuation. Called when the current token is a separator (/ or //);
    * if it is not a separator, this is effectively a null operation.
    */

    private Expression parsePathContinuation(Expression start)  throws SAXException {
        switch (t.currentToken) {
        case Tokenizer.SLASH:
            t.next();
            return parseRelativePath(start);

        case Tokenizer.SLSL:
            // add in the implicit from-descendants-or-self() step
            Expression exp = new PathExpression(
                                    start,
                                    new Step(Axis.DESCENDANT_OR_SELF, NodeInfo.NODE, new AnyNameTest()));
            exp.setStaticContext(env);
            t.next();
            return parseRelativePath(exp);

        default:
            return start;
        }
    }

    /**
    * Parse a relative path (a sequence of steps). Called when the current token immediately
    * follows a separator (/ or //), or an implicit separator (XYZ is equivalent to ./XYZ)
    */

    private Expression parseRelativePath(Expression start)  throws SAXException {
        Step step = parseStep();
        Expression exp = new PathExpression(start, step);
        exp.setStaticContext(env);
        return parsePathContinuation(exp);
    }

    /**
    * Parse a step (including an optional sequence of qualifiers)
    */

    private Step parseStep()  throws SAXException {    
        Step step=null;
        
        switch(t.currentToken) {

        case Tokenizer.DOT:
            step = new Step(Axis.SELF, NodeInfo.NODE, new AnyNameTest());
            t.next();
            break;

        case Tokenizer.DOTDOT:
            step = new Step(Axis.PARENT, NodeInfo.NODE, new AnyNameTest());
            t.next();
            break;
            
        case Tokenizer.NAME:
            step = new Step(Axis.CHILD, NodeInfo.ELEMENT, env.makeName(t.currentTokenValue, false));
            t.next();
            while (t.currentToken == Tokenizer.LSQB) {
                step = parseStepPredicate(step);
            }
            break;

        case Tokenizer.PREFIX:
            step = new Step(Axis.CHILD, NodeInfo.ELEMENT, env.makePrefixTest(t.currentTokenValue));
            t.next();
            while (t.currentToken == Tokenizer.LSQB) {
                step = parseStepPredicate(step);
            }
            break;

        case Tokenizer.STAR:
            step = new Step(Axis.CHILD, NodeInfo.ELEMENT, new AnyNameTest());
            t.next();
            while (t.currentToken == Tokenizer.LSQB) {
                step = parseStepPredicate(step);
            }
            break;
            
        case Tokenizer.AT:
            t.next();
            switch(t.currentToken) {

            case Tokenizer.NAME:
                step = new Step(Axis.ATTRIBUTE, NodeInfo.ATTRIBUTE,
                                env.makeName(t.currentTokenValue, false));
                t.next();
                break;

            case Tokenizer.PREFIX:
                step = new Step(Axis.ATTRIBUTE, NodeInfo.ATTRIBUTE,
                                env.makePrefixTest(t.currentTokenValue));
                t.next();
                break;

            case Tokenizer.STAR:
                step = new Step(Axis.ATTRIBUTE, NodeInfo.ATTRIBUTE, new AnyNameTest());
                t.next();
                break;

            case Tokenizer.NODETYPE:
                String nodetype = t.currentTokenValue;      // already interned
                t.next();

                // most of these nodes can't occur in the attribute axis but the syntax allows them
                if (nodetype=="text") {
                    step = new Step(Axis.ATTRIBUTE, NodeInfo.TEXT, new AnyNameTest());
                } else if (nodetype=="node") {
                    step = new Step(Axis.ATTRIBUTE, NodeInfo.NODE, new AnyNameTest());
                } else if (nodetype=="comment") {
                    step = new Step(Axis.ATTRIBUTE, NodeInfo.COMMENT, new AnyNameTest());
                } else if (nodetype=="processing-instruction") {
                    // optional PI name allowed (as a literal)
                    if (t.currentToken==Tokenizer.LITERAL) {
                        step = new Step(Axis.ATTRIBUTE, NodeInfo.PI,
                                        new Name(t.currentTokenValue));
                        t.next();
                    } else {
                        step = new Step(Axis.ATTRIBUTE, NodeInfo.PI, new AnyNameTest());
                    }
                }
                expect(Tokenizer.RPAR);
                t.next();
                break;

            default:
                grumble("@ must be followed by a NameTest or NodeTest");
            }
            while (t.currentToken == Tokenizer.LSQB) {
                step = parseStepPredicate(step);
            }
            break;
            
        case Tokenizer.NODETYPE:
            String nodetype = t.currentTokenValue;      // already interned
            t.next();

            if (nodetype=="text") {
                step = new Step(Axis.CHILD, NodeInfo.TEXT, new AnyNameTest());
            } else if (nodetype=="node") {
                step = new Step(Axis.CHILD, NodeInfo.NODE, new AnyNameTest());
            } else if (nodetype=="comment") {
                step = new Step(Axis.CHILD, NodeInfo.COMMENT, new AnyNameTest());
            } else if (nodetype=="processing-instruction") {
                // optional PI name allowed (as a literal)
                if (t.currentToken==Tokenizer.LITERAL) {
                    step = new Step(Axis.CHILD, NodeInfo.PI,
                                    new Name(t.currentTokenValue));
                    t.next();
                } else {
                    step = new Step(Axis.CHILD, NodeInfo.PI, new AnyNameTest());
                }
            }
            expect(Tokenizer.RPAR);
            t.next();

            while (t.currentToken == Tokenizer.LSQB) {
                step = parseStepPredicate(step);
            }
            break;

        case Tokenizer.AXIS:
            int axis = Axis.lookup(t.currentTokenValue);
            int principalNodeType = Axis.makeAxis(axis).getPrincipalNodeType();
            t.next();
            switch (t.currentToken) {

            case Tokenizer.NAME:               
                step = new Step(axis, principalNodeType,
                                env.makeName(t.currentTokenValue, false));
                t.next();
                break;

            case Tokenizer.PREFIX:               
                step = new Step(axis, principalNodeType,
                                env.makePrefixTest(t.currentTokenValue));
                t.next();
                break;

            case Tokenizer.STAR:
                step = new Step(axis, principalNodeType, new AnyNameTest());
                t.next();
                break;
                
            case Tokenizer.NODETYPE:
                String nt = t.currentTokenValue;        // already interned
                t.next();
                
                if (nt=="node") {
                    step = new Step(axis, NodeInfo.NODE, new AnyNameTest());
                } else if (nt=="text") {
                    step = new Step(axis, NodeInfo.TEXT, new AnyNameTest());
                } else if (nt=="comment") {
                    step = new Step(axis, NodeInfo.COMMENT, new AnyNameTest());
                } else if (nt=="processing-instruction") {
                    // optional PI name allowed, as a literal
                    if (t.currentToken==Tokenizer.LITERAL) {
                        step = new Step(axis, NodeInfo.PI,
                                        new Name(t.currentTokenValue));
                        t.next();
                    } else {
                        step = new Step(axis, NodeInfo.PI, new AnyNameTest());
                    }
                } else {
                    grumble("Unsupported node type");
                }

                expect(Tokenizer.RPAR);
                t.next();
                break;
            default:
                grumble("Unexpected token [" + Tokenizer.tokens[t.currentToken] + "] after axis name");
            }
            while (t.currentToken == Tokenizer.LSQB) {
                step = parseStepPredicate(step);
            }
            break;
            
        default:
            grumble("Unexpected token [" + Tokenizer.tokens[t.currentToken] + "] in path expression");
            //break;
        }
        return step;
    }

    /**
    * Parse a predicate appearing as part of a Step
    */

    private Step parseStepPredicate(Step start) throws SAXException {
        t.next();
        Expression predicate = parseExpression();
        expect(Tokenizer.RSQB);
        t.next();
        return start.addFilter(predicate);
    }

    /**
    * Parse a function call
    * function-name '(' ( Expression (',' Expression )* )? ')'
    */

    private Expression parseFunctionCall() throws SAXException {
        
        String fname = t.currentTokenValue;
        Function f=null;

        int colon = fname.indexOf(":");
        if (colon<0) {
            Expression e = makeSystemFunction(fname);
            if (e==null) grumble("Unknown system function: " + fname);
            
            if (e instanceof Function) {
                f = (Function)e;
            } else {                    // shortcut for true() and false()
                t.next();
                expect(Tokenizer.RPAR);
                t.next();
                return e;
            }            
        } else {        // try a saxon:function
            f = env.getStyleSheetFunction(env.makeName(t.currentTokenValue, false));
        }
        if (f==null) {  // try a Java function
            f = new FunctionProxy();
        }
        f.setStaticContext(env);

        // the "(" has already been read by the Tokenizer

        t.next();
        if (t.currentToken!=Tokenizer.RPAR) {
            // parse arguments
            Expression arg = parseExpression();
            f.addArgument(arg);
            while(t.currentToken==Tokenizer.COMMA) {
                t.next();
                arg = parseExpression();
                f.addArgument(arg);
            }
            expect(Tokenizer.RPAR);
        }
        t.next();
        if (f instanceof FunctionProxy) {
            ((FunctionProxy)f).setMethod(env.makeName(fname, false));
        }
        return f;
    }

    /**
    * Make a system function (one whose name has no prefix). Note this is static and public
    * so it can also be used from extension-function-available()
    */

    public static Expression makeSystemFunction(String name)  {
        if (name=="last") return new Last();
        if (name=="position") return new Position();

        if (name=="count") return new Count();
        if (name=="current") return new Current();
        if (name=="id") return new Id();
        if (name=="key") return new Key();
        if (name=="document") return new Document();
        if (name=="local-name") return new LocalName();
        if (name=="namespace-uri") return new NamespaceURI();
        if (name=="name") return new NameFn();
        if (name=="generate-id") return new GenerateId();

        if (name=="not") return new Not();
        if (name=="true") return new BooleanValue(true);
        if (name=="false") return new BooleanValue(false);
        if (name=="boolean") return new BooleanFn();
        if (name=="lang") return new Lang();

        if (name=="number") return new NumberFn();
        if (name=="floor") return new Floor();
        if (name=="ceiling") return new Ceiling();
        if (name=="round") return new Round();
        if (name=="sum") return new Sum();

        if (name=="string") return new StringFn();

        if (name=="starts-with") return new StartsWith();
        if (name=="string-length") return new StringLength();
        if (name=="substring") return new Substring();
        if (name=="contains") return new Contains();
        if (name=="substring-before") return new SubstringBefore();
        if (name=="substring-after") return new SubstringAfter();
        if (name=="normalize-space") return new NormalizeSpace();
        if (name=="translate") return new Translate();
        if (name=="concat") return new Concat();
        if (name=="format-number") return new FormatNumber();
        
        if (name=="system-property") return new SystemProperty();
        if (name=="function-available") return new FunctionAvailable();
        if (name=="element-available") return new ElementAvailable();
        if (name=="unparsed-entity-uri") return new UnparsedEntityURI();        
        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////
    //                     PATTERNS                                                 //
    //////////////////////////////////////////////////////////////////////////////////


    /**
    * Parse a Union Pattern:<br>
    * pathPattern ( | pathPattern )*
    */

    private Pattern parseUnionPattern() throws SAXException {
        Pattern exp1 = parsePathPattern();

        while (t.currentToken == Tokenizer.VBAR ) {
            t.next();
            Pattern exp2 = parsePathPattern();        
            exp1 = new UnionPattern(exp1, exp2);
            exp1.setStaticContext(env);
        }

        return exp1;
    }

    /**
    * Parse a Location Path Pattern:
    */

    private Pattern parsePathPattern() throws SAXException {

        LocationPathPattern lppat = new LocationPathPattern();
        lppat.setStaticContext(env);
        Pattern pat = lppat;        
        Pattern prev = null;
        int connector = -1;
        boolean rootonly = false;

        // special handling of stuff before the first component
        
        switch(t.currentToken) {
            case Tokenizer.SLASH:
                connector = t.currentToken;
                t.next();
                prev = new DocumentPattern();
                rootonly = true;
                break;
            case Tokenizer.SLSL:            // leading double slash can't be ignored
                                            // because it changes the default priority
                connector = t.currentToken;
                t.next();
                prev = new DocumentPattern();
                rootonly = false;
                break;
            default:
                break;
        }
        
        boolean more = true;
        while(more) {

            switch(t.currentToken) {
                case Tokenizer.AXIS:
                    int ax = Axis.lookup(t.currentTokenValue);
                    if (ax==Axis.CHILD) { 
                        t.next();
                        pat = patternStep(ax, lppat, prev, connector);
                    } else if (ax==Axis.ATTRIBUTE) {
                        t.next();
                        pat = patternStep(ax, lppat, prev, connector);
                    } else {
                        grumble("Axis in pattern must be child or attribute");
                    }
                    break;

                case Tokenizer.STAR:                   
                case Tokenizer.NAME:
                case Tokenizer.PREFIX:
                case Tokenizer.NODETYPE:
                    pat = patternStep(Axis.CHILD, lppat, prev, connector);
                    break;

                case Tokenizer.AT:
                    t.next();
                    pat = patternStep(Axis.ATTRIBUTE, lppat, prev, connector);
                    break;

                case Tokenizer.FUNCTION:        // must be id(literal) or key(literal,literal)
                    if (prev!=null) {
                        grumble("Function may appear only at the start of a pattern");
                    }
                    if (t.currentTokenValue.equals("id")) {
                        t.next();
                        expect(Tokenizer.LITERAL);
                        pat = new IDPattern(t.currentTokenValue);
                        pat.setStaticContext(env);
                        t.next();
                        expect(Tokenizer.RPAR);
                        t.next();
                    } else if (t.currentTokenValue.equals("key")) {
                        t.next();
                        expect(Tokenizer.LITERAL);
                        String keyname = t.currentTokenValue;
                        t.next();
                        expect(Tokenizer.COMMA);
                        t.next();
                        expect(Tokenizer.LITERAL);
                        pat = new KeyPattern(keyname, t.currentTokenValue);
                        pat.setStaticContext(env);
                        t.next();
                        expect(Tokenizer.RPAR);
                        t.next();
                    } else {
                        grumble("The only functions allowed in a pattern are id() and key()");
                    }
                    break;

                default:
                    if (rootonly) return prev;
                    grumble("Unxpected token in pattern, found " + Tokenizer.tokens[t.currentToken]);
            }
            
            connector = t.currentToken;
            rootonly = false;
            more = (connector == Tokenizer.SLASH || connector == Tokenizer.SLSL);
            if (more) {
                prev = pat;
                lppat = new LocationPathPattern();
                lppat.setStaticContext(env);
                if (connector==Tokenizer.SLASH) {
                     lppat.parentPattern = prev;
                } else {                        // connector == SLSL
                     lppat.ancestorPattern = prev;
                }
                t.next();
            }
        }
        pat.setStaticContext(env);
        return pat;

    }

    private Pattern patternStep(int axis, LocationPathPattern lppat, Pattern prev, int connector)
        throws SAXException {

        if (axis==Axis.CHILD) {
            if (t.currentToken==Tokenizer.STAR) {
                lppat.nodeType = NodeInfo.ELEMENT;
                lppat.nameTest = new AnyNameTest();
            } else if (t.currentToken==Tokenizer.NAME) {
                lppat.nodeType = NodeInfo.ELEMENT;
                lppat.nameTest = env.makeName(t.currentTokenValue, false);
            } else if (t.currentToken==Tokenizer.PREFIX) {
                lppat.nodeType = NodeInfo.ELEMENT;
                lppat.nameTest = env.makePrefixTest(t.currentTokenValue);
            } else if (t.currentToken==Tokenizer.NODETYPE) {
                String nodetype = t.currentTokenValue;      // already interned
                t.next();
                if (nodetype=="text") {
                    lppat.nodeType = NodeInfo.TEXT;
                } else if (nodetype=="node") {
                    lppat.nodeType = NodeInfo.NODE;
                } else if (nodetype=="comment") {
                    lppat.nodeType = NodeInfo.COMMENT;
                } else if (nodetype=="processing-instruction") {
                    // optional PI name allowed, as a literal
                    lppat.nodeType = NodeInfo.PI;
                    if (t.currentToken == Tokenizer.LITERAL) {
                        lppat.nameTest = new Name(t.currentTokenValue);
                        t.next();
                    }
                }
                expect(Tokenizer.RPAR);
            } else {
                grumble("Unxpected token in pattern, found " + Tokenizer.tokens[t.currentToken]);
            }
            
            if (prev!=null) {
                if (connector==Tokenizer.SLASH) {
                    lppat.parentPattern = prev;
                } else {                        // connector == SLSL
                    lppat.ancestorPattern = prev;
                }
            }
            t.next();
            parseFilters(lppat);
            return lppat;

        } else if (axis==Axis.ATTRIBUTE) {

            lppat.nodeType = NodeInfo.ATTRIBUTE;
            if (t.currentToken==Tokenizer.STAR) {
                lppat.nameTest = new AnyNameTest();
            } else if (t.currentToken==Tokenizer.NAME) {
                lppat.nameTest = env.makeName(t.currentTokenValue, false);
            } else if (t.currentToken==Tokenizer.PREFIX) {
                lppat.nameTest = env.makePrefixTest(t.currentTokenValue);
            } else if (t.currentToken==Tokenizer.NODETYPE) {
                String nodetype = t.currentTokenValue;      // already interned
                t.next();
                // the syntax allows a test for any kind of node, but text, comment and PI nodes
                // can't appear on the attribute axis, so we create a test that is never satisfied
                if (nodetype=="text") {
                    lppat.nodeType = NodeInfo.NONE;
                } else if (nodetype=="node") {
                    lppat.nodeType = NodeInfo.ATTRIBUTE;
                } else if (nodetype=="comment") {
                    lppat.nodeType = NodeInfo.NONE;
                } else if (nodetype=="processing-instruction") {
                    // optional PI name allowed, as a literal
                    lppat.nodeType = NodeInfo.NONE;
                    if (t.currentToken == Tokenizer.LITERAL) {
                        //lppat.nameTest = new Name(t.currentTokenValue); // ignore the name
                        t.next();
                    }
                }
                expect(Tokenizer.RPAR);

            } else {
                grumble("@ in pattern not followed by NameTest or NodeTest");
            }
            t.next();
            parseFilters(lppat);
            return lppat;

        } else {
            grumble("Axis in pattern must be child or attribute");
            return null;
        }
    }

    /**
    * Test to see if there are filters for a Pattern, if so, parse them
    */

    private void parseFilters(LocationPathPattern path) throws SAXException {
        while (t.currentToken == Tokenizer.LSQB) {
            t.next();
            Expression qual = parseExpression();
            expect(Tokenizer.RSQB);
            t.next();
            path.addFilter(qual);
            if (qual.usesCurrent()) {
                grumble("The current() function may not be used in a pattern");
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
