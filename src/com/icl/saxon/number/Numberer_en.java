package com.icl.saxon.number;
import java.util.Vector;

/**
  * Class Numberer_en does number formatting for language="en".
  * This supports the xsl:number element.
  * Methods and data are declared as protected, and static is avoided, to allow easy subclassing.
  * @author <A HREF="mailto:Michael.Kay@icl.com>Michael H. Kay, ICL</A> 
  * @version 4 August 2000 
  */

public class Numberer_en implements Numberer {

    /**
    * Format a number into a string
    * @param number The number to be formatted
    * @param picture The format specification. This is a single component of the format attribute
    * of xsl:number, e.g. "1", "01", "i", or "a"
    * @param groupSize number of digits per group (0 implies no grouping)
    * @param groupSeparator string to appear between groups of digits
    * @param letterValue as defined in xsl:number ("alphabetic" or "traditional" or "")
    * @return the formatted number
    */

    public String format(int number, String picture,
                                 int groupSize, String groupSeparator,
                                 String letterValue) {

        if (number<=0) return "" + number;      // not covered by the XSLT spec

        StringBuffer sb = new StringBuffer();
        char formchar = picture.charAt(0);

        switch(formchar) {

        case '0':
        case '1':
            sb.append(toRadical(number, westernDigits, picture, groupSize, groupSeparator));
            break;

        case 'A':
            sb.append(toAlphaSequence(number, latinUpper));
            break;

        case 'a':
            sb.append(toAlphaSequence(number, latinLower));
            break;

        case 'i':
            if (letterValue.equals("alphabetic")) {
                alphaDefault(number, formchar, sb);
            } else {
                sb.append(toRoman(number));
            }
            break;

        case 'I':
            if (letterValue.equals("alphabetic")) {
                alphaDefault(number, formchar, sb);
            } else {
                sb.append(toRoman(number).toUpperCase());
            }
            break;

        case 'o':
            if (picture.equals("one")) {
                sb.append(toWords(number));
            } else {
                alphaDefault(number, formchar, sb);
            }
            break;

        case 'O':
            if (picture.equals("ONE")) {
                sb.append(toWords(number).toUpperCase());
            } else {
                alphaDefault(number, formchar, sb);
            }
            break;

        case '\u0391':
            sb.append(toAlphaSequence(number, greekUpper));
            break;
            
        case '\u03b1':
            sb.append(toAlphaSequence(number, greekLower));
            break;

        case '\u0410':
            sb.append(toAlphaSequence(number, cyrillicUpper));
            break;
            
        case '\u0430':
            sb.append(toAlphaSequence(number, cyrillicLower));
            break;

        case '\u05d0':
            sb.append(toAlphaSequence(number, hebrew));
            break;

        case '\u3042':
            sb.append(toAlphaSequence(number, hiraganaA));
            break;
            
        case '\u30a2':
            sb.append(toAlphaSequence(number, katakanaA));
            break;

        case '\u3044':
            sb.append(toAlphaSequence(number, hiraganaI));
            break;

        case '\u30a4':
            sb.append(toAlphaSequence(number, katakanaI));
            break;

        case '\u4e00':
            sb.append(toRadical(number, kanjiDigits, picture, groupSize, groupSeparator));
            break;

        default:

            if (Character.isDigit(formchar)) {

                int zero = (int)formchar - Character.getNumericValue(formchar);
                String digits = "" +
                    (char)(zero) +
                    (char)(zero+1) +
                    (char)(zero+2) +
                    (char)(zero+3) +
                    (char)(zero+4) +
                    (char)(zero+5) +
                    (char)(zero+6) +
                    (char)(zero+7) +
                    (char)(zero+8) +
                    (char)(zero+9);

                sb.append(toRadical(number, digits, picture, groupSize, groupSeparator));
                break;
        
            } else {
                if (formchar < '\u1100') {
                    alphaDefault(number, formchar, sb);
                } else {
                    // fallback to western numbering
                    sb.append(
                        toRadical(number, westernDigits, picture, groupSize, groupSeparator));
                }
                break;
           
            }
        }
        
        return sb.toString();
    }


    protected String westernDigits =
        "0123456789";

    protected String latinUpper =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    protected String latinLower =
        "abcdefghijklmnopqrstuvwxyz";

    protected String greekUpper =
        "\u0391\u0392\u0393\u0394\u0395\u0396\u0397\u0398\u0399\u039a" +
        "\u039b\u039c\u039c\u039d\u039e\u039f\u03a0\u03a1\u03a3\u03a4" +
        "\u03a5\u03a6\u03a7\u03a8\u03a9";

    protected String greekLower =
        "\u03b1\u03b2\u03b3\u03b4\u03b5\u03b6\u03b7\u03b8\u03b9\u03ba" +
        "\u03bb\u03bc\u03bc\u03bd\u03be\u03bf\u03c0\u03c1\u03c3\u03c4" +
        "\u03c5\u03c6\u03c7\u03c8\u03c9";

    // Cyrillic information from Dmitry Kirsanov [dmitry@kirsanov.com]
    // (based on his personal knowledge of Russian texts, not any authoritative source)    

    protected String cyrillicUpper =
        "\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417\u0418" +
        "\u041a\u041b\u041c\u041d\u041e\u041f\u0420\u0421\u0421\u0423" +
        "\u0424\u0425\u0426\u0427\u0428\u0429\u042b\u042d\u042e\u042f";

    protected String cyrillicLower =
        "\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438" +
        "\u043a\u043b\u043c\u043d\u043e\u043f\u0440\u0441\u0441\u0443" +
        "\u0444\u0445\u0446\u0447\u0448\u0449\u044b\u044d\u044e\u044f";

    protected String hebrew =
        "\u05d0\u05d1\u05d2\u05d3\u05d4\u05d5\u05d6\u05d7\u05d8\u05d9\u05db\u05dc" +
        "\u05de\u05e0\u05e1\u05e2\u05e4\u05e6\u05e7\u05e8\u05e9\u05ea";
        

    // The following Japanese sequences were supplied by
    // MURAKAMI Shinyu [murakami@nadita.com]

    protected String hiraganaA = 
        "\u3042\u3044\u3046\u3048\u304a\u304b\u304d\u304f\u3051\u3053" +
        "\u3055\u3057\u3059\u305b\u305d\u305f\u3061\u3064\u3066\u3068" +
        "\u306a\u306b\u306c\u306d\u306e\u306f\u3072\u3075\u3078\u307b" +
        "\u307e\u307f\u3080\u3081\u3082\u3084\u3086\u3088\u3089\u308a" +
        "\u308b\u308c\u308d\u308f\u3092\u3093";

    protected String katakanaA = 

        "\u30a2\u30a4\u30a6\u30a8\u30aa\u30ab\u30ad\u30af\u30b1\u30b3" +
        "\u30b5\u30b7\u30b9\u30bb\u30bd\u30bf\u30c1\u30c4\u30c6\u30c8" +
        "\u30ca\u30cb\u30cc\u30cd\u30ce\u30cf\u30d2\u30d5\u30d8\u30db" +
        "\u30de\u30df\u30e0\u30e1\u30e2\u30e4\u30e6\u30e8\u30e9\u30ea" +
        "\u30eb\u30ec\u30ed\u30ef\u30f2\u30f3";

    protected String hiraganaI = 

        "\u3044\u308d\u306f\u306b\u307b\u3078\u3068\u3061\u308a\u306c" +
        "\u308b\u3092\u308f\u304b\u3088\u305f\u308c\u305d\u3064\u306d" +
        "\u306a\u3089\u3080\u3046\u3090\u306e\u304a\u304f\u3084\u307e" +
        "\u3051\u3075\u3053\u3048\u3066\u3042\u3055\u304d\u3086\u3081" +
        "\u307f\u3057\u3091\u3072\u3082\u305b\u3059";

    protected String katakanaI =
    
        "\u30a4\u30ed\u30cf\u30cb\u30db\u30d8\u30c8\u30c1\u30ea\u30cc" +
        "\u30eb\u30f2\u30ef\u30ab\u30e8\u30bf\u30ec\u30bd\u30c4\u30cd" +
        "\u30ca\u30e9\u30e0\u30a6\u30f0\u30ce\u30aa\u30af\u30e4\u30de" +
        "\u30b1\u30d5\u30b3\u30a8\u30c6\u30a2\u30b5\u30ad\u30e6\u30e1" +
        "\u30df\u30b7\u30f1\u30d2\u30e2\u30bb\u30b9";


    protected String kanjiDigits =
        "\u3007\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d";
    

    /**
    * Default processing with an alphabetic format token: use the contiguous
    * range of Unicode letters starting with that token.
    */

    protected void alphaDefault(int number, char formchar, StringBuffer sb) {
        int min = (int)formchar;
        int max = (int)formchar;
        // use the contiguous range of letters starting with the specified one
        while (Character.isLetterOrDigit((char)(max+1))) max++;
        sb.append(toAlpha(number, min, max));
    }

    /**
    * Format the number as an alphabetic label using the alphabet consisting
    * of consecutive Unicode characters from min to max
    */

    protected String toAlpha(int number, int min, int max) {
        if (number<=0) return "" + number;
        int range = max - min + 1;
        char last = (char)(((number-1) % range) + min);
        if (number>range) {
            return toAlpha((number-1)/range, min, max) + last;
        } else {
            return "" + last;
        }
    }

    /**
    * Convert the number into an alphabetic label using a given alphabet.
    * For example, if the alphabet is "xyz" the sequence is x, y, z, xx, xy, xz, ....
    */

    protected String toAlphaSequence(int number, String alphabet) {
        if (number<=0) return "" + number;
        int range = alphabet.length();
        char last = alphabet.charAt((number-1) % range);
        if (number>range) {
            return toAlphaSequence((number-1)/range, alphabet) + last;
        } else {
            return "" + last;
        }
    }

    /**
    * Convert the number into a decimal or other representation using the given set of
    * digits.
    * For example, if the digits are "01" the sequence is 1, 10, 11, 100, 101, 110, 111, ...
    * @param number the number to be formatted
    * @param digits the set of digits to be used
    * @param picture the formatting token, e.g. 001 means include leading zeroes to give at least
    * three decimal places
    * @param groupsize the number of digits in each group
    * @param groupSeparator the separator to use between groups of digits.
    */

    protected String toRadical(int number, String digits, String picture,
                                 int groupSize, String groupSeparator) {
                                    
        StringBuffer sb = new StringBuffer();
        StringBuffer temp = new StringBuffer();
        int base = digits.length();
        
        String s = "";
        int n = number;
        while (n>0) {
            s = digits.charAt(n % base) + s;
            n = n / base;
        }
            
        for (int i=0; i<(picture.length()-s.length()); i++) {
            temp.append(digits.charAt(0));
        }
        temp.append(s);
        
        if (groupSize>0) {
            for (int i=0; i<temp.length(); i++) {
                if (i!=0 && ((temp.length()-i) % groupSize) == 0) {
                    sb.append(groupSeparator);
                }
                sb.append(temp.charAt(i));
            }
        } else {
            sb = temp;
        }

        return sb.toString();
    }

    /**
    * Generate a Roman numeral (in lower case)
    */

    protected String toRoman(int n) {
        if (n<=0 || n>9999) return "" + n;
        return romanThousands[n/1000] +
               romanHundreds[(n/100) % 10] +
               romanTens[(n/10) % 10] +
               romanUnits[n % 10];
    }

    // Roman numbers beyond 4000 use overlining and other conventions which we won't
    // attempt to reproduce. We'll go high enough to handle present-day Gregorian years.

    private static String[] romanThousands =
        {"", "m", "mm", "mmm", "mmmm", "mmmmm", "mmmmmm", "mmmmmmm", "mmmmmmmm", "mmmmmmmmm"};
    private static String[] romanHundreds =
        {"", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm"};
    private static String[] romanTens =
        {"", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc"};
    private static String[] romanUnits =
        {"", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix"};


    /**
    * Show the number as English words
    */

    public String toWords(int number) {
        if (number >= 1000000000) {
            return toWords(number / 1000000000) + " billion " + toWords(number % 1000000000);
        } else if (number >= 1000000) {
            return toWords(number / 1000000) + " million " + toWords(number % 1000000);
        } else if (number >= 1000) {
            return toWords(number / 1000) + " thousand " + toWords(number % 1000);
        } else if (number >= 100) {
            int rem = number % 100;
            return toWords(number / 100) + " hundred" +
                (rem==0 ? "" : " and " + toWords(rem));
        } else {
            if (number < 20) return englishUnits[number];
            int rem = number % 10;
            return englishTens[number / 10] +
                (rem==0 ? "" : " " + englishUnits[rem]);
        }
    }

    protected String[] englishUnits = {
        "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen"};

    protected String[] englishTens = {
        "", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
        
        

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
