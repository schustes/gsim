package de.s2.gsim.util;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

// Copyright (C) 2001 Xtensible Technologies Corporation
// All rights reserved.
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class XML2Jtree implements ContentHandler {

    Vector branches = new Vector();

    int branchesIdx = 0;

    Vector endTags = new Vector();

    Vector nodeAttrs = new Vector();

    Vector nodes = new Vector();

    Vector startTags = new Vector();

    ExtendedNode treeEl = null;

    ExtendedNode treeRoot = null;

    private int numberOfAttributes;

    private int numberOfCharacters;

    private int numberOfElements;

    private int numberOfProcessingInstructions;

    @Override
    public void characters(char[] text, int start, int length) throws SAXException {
        numberOfCharacters += length;

        char[] cp = new char[length];
        System.arraycopy(text, start, cp, 0, length);
        String tempstr = new String(cp);
        ((ExtendedNode) branches.get(branches.size() - 1)).setText(tempstr);

        // /////////////////////////////////////////////////////////////

        // String tempstr=new String(text);

        // logger.debug("xml2jtree tempstr>"+tempstr);
        // logger.debug("substring "+tempstr.substring(start,start+length));

        // //////////////////////////////////////////////////////
    }

    // Now that the document is done, we can print out the final results
    @Override
    public void endDocument() throws SAXException {
        // logger.debug("Number of elements: " + numberOfElements);
        // logger.debug("Number of attributes: " + numberOfAttributes);
        // logger.debug("Number of processing instructions: "
        // + numberOfProcessingInstructions);
        // logger.debug("Number of characters of plain text: "
        // + numberOfCharacters);
        ;
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
        treeEl = new ExtendedNode(localName);
        if (localName.compareTo((branches.elementAt(branchesIdx)).toString()) == 0) {
            branches.removeElementAt(branchesIdx);
            branchesIdx--;
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public Vector getNodeAttrsVector() {
        return nodeAttrs;
    }

    public Vector getNodesVector() {
        return nodes;
    }

    public DefaultMutableTreeNode getTree() {

        Enumeration e = treeRoot.breadthFirstEnumeration();

        ExtendedNode otn = (ExtendedNode) (e.nextElement());

        while (e.hasMoreElements()) {
            ExtendedNode tn = (ExtendedNode) (e.nextElement());
            String otns = otn.getUserObject().toString();
            String tns = tn.getUserObject().toString();
            int otnsIdx = otns.indexOf("[");
            int otnsEnx = otns.indexOf("]");
            int tnsIdx = tns.indexOf("[");
            // int idx = 0;
            int idx = 1; // for new namespace. Old namespace accepts indexing at 0 as
                         // above
            String Notns, Ntns, idxs;

            if (otnsIdx > 0) {
                Notns = otns.substring(0, otnsIdx);
                idxs = otns.substring(otnsIdx + 1, otnsEnx);
                Integer I = new Integer(idxs);
                idx = I.intValue();
            } else {
                Notns = otns;
            }

            if (tnsIdx > 0) {
                Ntns = tns.substring(0, tnsIdx);
            } else {
                Ntns = tns;
            }

            if (Ntns.compareTo(Notns) == 0) {
                tn.setUserObject(Ntns + ":" + tn.getExtendedName() + "[" + String.valueOf(idx + 1) + "]");
                otn.setUserObject(Notns + ":" + otn.getExtendedName() + "[" + String.valueOf(idx) + "]");
            }

            if (otn.getText().length() > 0) {
                ExtendedNode textNode = new ExtendedNode(otn.getText());
                otn.add(textNode);
            }

            otn = tn;
        } // while

        return treeRoot;
    }

    @Override
    public void ignorableWhitespace(char[] text, int start, int length) throws SAXException {
        // We don't count the ignorable white space
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        numberOfProcessingInstructions++;
    }

    /*
     * public Vector getStartTags() { return startTags; }
     * 
     * public Vector getEndTags() { return endTags; }
     */

    // Do-nothing methods we have to implement to fulfill the interface
    // requirements but which don't need to do anything:
    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    @Override
    public void startDocument() throws SAXException {
        numberOfElements = 0;
        numberOfAttributes = 0;
        numberOfProcessingInstructions = 0;
        numberOfCharacters = 0;
    }

    // We should count either the start tag of the element or the end tag,
    // but not both. Empty elements will still be reported by each of these
    // methods.
    @Override
    public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException {
        String nodeAttrString = " ";
        numberOfElements++;
        numberOfAttributes += atts.getLength();

        // logger.debug("node localname "+localName);
        // logger.debug("node qname "+qualifiedName);

        nodes.addElement(localName);

        if (atts.getLength() != 0) {
            // logger.debug("an attr ");
            nodeAttrString = "Attributes: ";
            for (int i = 0; i < atts.getLength(); i++) {
                // logger.debug(" getType>"+atts.getType(i));
                // logger.debug(" getValue>"+atts.getValue(i));
                // logger.debug(" getLocalName>"+atts.getLocalName(i));
                // logger.debug(" getQName>"+atts.getQName(i));
                nodeAttrString += atts.getLocalName(i) + "=" + atts.getValue(i) + " ";
            } // for
        } // if

        nodeAttrs.addElement(nodeAttrString);

        if (treeRoot == null) {
            treeRoot = new ExtendedNode(localName);
            branches.addElement(treeRoot);
        } else {
            treeEl = new ExtendedNode(localName);
            treeEl.setExtendedName(nodeAttrString);
            // treeEl = new DefaultMutableTreeNode(localName+"("+nodeAttrString+")");
            ((DefaultMutableTreeNode) (branches.elementAt(branchesIdx))).add(treeEl);
            branches.addElement(treeEl);
            branchesIdx++;

            // No don't do it this way - see TreeIconDemo2.java and create all tree
            // nodes like in there as BookInfo nodes except change it to myInfo or
            // such -
            // treeEl.setUserObject(nodeAttrString);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    // Could easily have put main() method in a separate class
    public static void main(String[] args) {
    }

    private class ExtendedNode extends DefaultMutableTreeNode {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private String extendedName = "";

        private String text = "";

        public ExtendedNode(String x) {
            super(x);
        }

        public String getExtendedName() {
            return extendedName;
        }

        public String getText() {
            return text;
        }

        public void setExtendedName(String n) {
            extendedName = n;
        }

        public void setText(String text) {
            this.text = text;
        }

    }

}
