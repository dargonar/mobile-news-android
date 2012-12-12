package com.icl.saxon.sort;
import java.text.*;
import java.util.*;

// Copyright © International Computers Limited 1998
// See conditions of use

/**
 * A Binary Tree used for sorting.
 *
 * Similar to the java.util.Dictionary interface except (a) that the
 * keys must be Strings rather than general Objects (b) the results
 * are returned as a Vector, not an Enumeration. <P>
 * 
 * The methods getKeys() and getValues() return values in ascending order
 * of key. The keys are compared using a default Collator which sorts
 * in alphabetical order with intelligent handling of case and accents.<P>
 *
 * Note that duplicate keys are not
 * allowed: a new entry silently overwrites any previous entry with
 * the same key. If you want to use dulicate keys, append a unique
 * value (for example, a random number) to each one.<P>
 *
 * @author Michael H. Kay, ICL (Michael.Kay@icl.com)
 * @version 9 July 1999: now returns a Vector rather than an Enumeration
 *
 */

public class BinaryTree {

    private BinaryTreeNode root;       // the root node of the tree; null if tree is empty
    private Comparer comparer;         // object used for comparing key values
    private BinaryTreeNode prev;       // temporary variable used while making new chain
    private boolean ascending;         // indicates a sort in ascending order of key value
    private boolean allowDuplicates;   // indicates duplicates are allowed (their order is retained)

    // The implementation of the binary tree uses a classic structure with
    // nodes containing a left pointer, a right pointer, and a key/value pair.

    // Removal of entries is handled by setting the value to null: the node
    // itself remains in the tree.

    /*
     * Constructor: creates an empty tree
     */
    
    public BinaryTree ()
    {
        root = null;
        ascending = true;
        allowDuplicates = false;
    }

    /**
    * Set order. This must be called before any nodes are added to the tree.
    * Default is ascending order
    * @param ascending true indicates ascending order; false indicates descending
    */

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
    * Define whether duplicate keys are allowed or not. If duplicates are allowed, objects
    * with the same key value will be sequenced in order of arrival. If duplicates are not
    * allowed, a new value will overwrite an existing value with the same key.
    */

    public void setDuplicatesAllowed(boolean allow) {
        allowDuplicates = allow;
    }

    /**
    * Set the Comparer to be used for the keys in this tree.
    * At the time this is called, the tree must be empty
    */

    public void setComparer(Comparer c) {
        if (isEmpty()) comparer = c;
    }

    /** 
    * getValues() returns the values in the tree in sorted order.
    * @return an Vector containing the values in this BinaryTree (in key order).
    */
 
    public Vector getValues() {
        BinaryTreeNode node = root;
        Vector v = new Vector();
        getValues(root, v);
        return v;
    }

    /**
     * getValues() supporting method:
     * Walk through the nodes recursively in key order adding each value to a supplied vector
     */

    private void getValues (BinaryTreeNode here, Vector v) {
        if (here!=null) {
           getValues(here.left, v);
           if (!here.isDeleted()) {
               v.addElement(here.value);
           }
           getValues(here.right, v);
        } 
    }

    /** 
    * getKeys() returns the keys in the tree in sorted order.
    * @return an Vector containing the keys in this BinaryTree (in key order).
    */
 
    public Vector getKeys() {
        BinaryTreeNode node = root;
        Vector v = new Vector();
        getKeys(root, v);
        return v;
    }

    /**
     * getKeys() supporting method
     * walk through the nodes recursively in key order adding each value to a supplied vector
     */

    private void getKeys (BinaryTreeNode here, Vector v) {
        if (here!=null) {
           getKeys(here.left, v);
           if (!here.isDeleted()) {
               v.addElement(here.key);
           }
           getKeys(here.right, v);
        } 
    }

    /**
    * get(String) returns the value corresponding to a given key, if any
    * @param key The key value being sought
    * @return the value to which the key is mapped in this binary tree, or null
    * if there is no entry with this key
    */

    public Object get(Object key)
    {
        BinaryTreeNode n = find(key);
        return (n==null ? null : n.value);
    };

    /**
    * isEmpty() 
    * Tests if this binary tree contains no keys.
    * @return true if there are no entries in the tree
    */

    public boolean isEmpty() {
       return (size()==0);
    }

    /** 
    * put(Object, Object) puts a new entry in the tree, overwriting any previous entry with
    * the same key.
    * @param key The value of the key. Note this must be a String, and must not be null.
    * @param value The value to be associated with this key. Must not be null.
    * @return the value previously associated with this key, if there was one. Otherwise null.
    */

    public Object put(Object key, Object value)
    {
        if (key==null || value==null) throw new NullPointerException();

        BinaryTreeNode node = root;

        if (comparer==null) {
            comparer = new StringComparer();
        }
        
        if (root==null) {
            root = new BinaryTreeNode(key, value);
            return null;
        }
        while (true) {
            int w = comparer.compare(node.key, key);
            if (!ascending) w = 0 - w;
            if (w == 0 && allowDuplicates) w = -1;
            if ( w > 0 ) {
                if (node.left==null) {
                    node.left = new BinaryTreeNode(key, value);
                    return null;
                }                
                node = node.left;               
            }
            if ( w == 0 ) {
                Object old = node.value; 
                node.value = value;
                return old;
            }
            if ( w < 0 ) {
                if (node.right==null) {
                   node.right = new BinaryTreeNode(key, value);
                   return null;
                }
                node = node.right;
            }
        }
    }
        

    /**
    * remove(Object) 
    * removes the key (and its corresponding value) from this Binary Tree.
    * If duplicates are allowed it removes at most one entry with this key
    * @param key identifies the entry to be removed
    * @return the value that was associated with this key, if there was one.
    * Otherwise null.
    */
    
    public Object remove(Object key)
    {
         // implemented by setting a logical delete marker in the node
         BinaryTreeNode n = find(key);
 
         if (n==null) return null;
         Object val = n.value;
         n.delete();
         return(val);
    }


    /**
     * size() 
     * @return the number of entries in this binary tree. 
     */
     
    public int size() {
        return count(root);
    }

    /*
     * private method to count the nodes subordinate to a given node
     */

    private int count ( BinaryTreeNode here )
    {
        if (here==null) return 0;
        return count(here.left) + (here.isDeleted() ? 0 : 1) + count(here.right);
    }

    /*
     * Private method to find a node given a key value. If duplicates are allowed it finds the first.
     */

    private BinaryTreeNode find(Object s)
    {
        BinaryTreeNode node = root;
        
        if (node==null) return null;
        while (node!=null) {
            int w = comparer.compare(s, node.key);
            if ( w < 0 ) node= node.left;
            if ( w == 0 ) return (node.isDeleted() ? null : node);
            if ( w > 0 ) node= node.right;
        }
        return null;
    }
    

// inner classes

private class BinaryTreeNode {
    BinaryTreeNode left;
    BinaryTreeNode right;
    Object key;
    Object value;

    public BinaryTreeNode ( Object k, Object v ) {
        left = null;
        right = null;
        key = k;
        value = v;
    }
    
    public boolean isDeleted() {
        return (value==null);
    }
    public void delete() {
        value = null;
    }        
        
}

// end of inner classes

// main program, for testing

public static void main(String args[]) throws Exception {

    BinaryTree tree = new BinaryTree();
    tree.setComparer(new UppercaseFirstComparer());
    tree.setAscending(true);
    tree.setDuplicatesAllowed(true);
    tree.put("a", "1");
    tree.put("b", "2");
    tree.put("c", "3");
    tree.put("aa", "4");
    tree.put("ab", "5");
    tree.put("A", "6");
    tree.put("A", "6a");
    tree.put("B", "7");
    tree.put("AA", "8");
    tree.put("XYZ", "9");
    tree.put("", "10");
    System.out.println(tree.getKeys());
    System.out.println(tree.getValues());

    tree = new BinaryTree();
    tree.setComparer(new DoubleComparer());
    tree.setAscending(false);
    tree.put(new Double(1.43), "1");
    tree.put(new Double(84.2), "2");
    tree.put(new Double(-100), "3");
    tree.put(new Double(0.0), "4");
    System.out.println(tree.getKeys());
    System.out.println(tree.getValues());
    
}

}
