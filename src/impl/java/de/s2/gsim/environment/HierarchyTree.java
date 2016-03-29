package de.s2.gsim.environment;

/**
 * Root class for representing a tree of frame inheritance relationships.
 * 
 * @author stephan
 *
 */
public class HierarchyTree {

    /**
     * Root node.
     */
    private HierarchyNode root;

    /**
     * Constructor initialising the tree.
     * 
     * @param rootFrame the root frame
     */
    public HierarchyTree(Frame rootFrame) {
        this.root = new HierarchyNode(rootFrame);
    }

    /**
     * Constructor initialising the tree with a node.
     * 
     * @param root the root node
     */
    public HierarchyTree(HierarchyNode root) {
        this.root = root;
    }

    /**
     * Get the root node.
     * 
     * @return the root
     */
    public HierarchyNode getRoot() {
        return root;
    }

}
