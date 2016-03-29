package de.s2.gsim.util;

//--------------------------------------
// Systematically generate combinations.
//--------------------------------------

import java.math.BigInteger;

public class CombinationGenerator {

    private int[] a;

    private int n;

    private BigInteger numLeft;

    private int r;

    private BigInteger total;

    // ------------
    // Constructor
    // ------------
    // n=length of array, n length of 'selection'
    public CombinationGenerator(int n, int r) {
        if (r > n) {
            throw new IllegalArgumentException("r:" + r + ",n:" + n);
        }
        if (n < 1) {
            throw new IllegalArgumentException("r:" + r + ",n:" + n);
        }
        this.n = n;
        this.r = r;
        a = new int[r];
        BigInteger nFact = getFactorial(n);
        BigInteger rFact = getFactorial(r);
        BigInteger nminusrFact = getFactorial(n - r);
        total = nFact.divide(rFact.multiply(nminusrFact));
        reset();
    }

    // ------
    // Reset
    // ------

    public int[] getNext() {

        if (numLeft.equals(total)) {
            numLeft = numLeft.subtract(BigInteger.ONE);
            return a;
        }

        int i = r - 1;
        while (a[i] == n - r + i) {
            i--;
        }
        a[i] = a[i] + 1;
        for (int j = i + 1; j < r; j++) {
            a[j] = a[i] + j - i;
        }

        numLeft = numLeft.subtract(BigInteger.ONE);
        return a;

    }

    // ------------------------------------------------
    // Return number of combinations not yet generated
    // ------------------------------------------------

    public BigInteger getNumLeft() {
        return numLeft;
    }

    // -----------------------------
    // Are there more combinations?
    // -----------------------------

    public BigInteger getTotal() {
        return total;
    }

    // ------------------------------------
    // Return total number of combinations
    // ------------------------------------

    public boolean hasMore() {
        return numLeft.compareTo(BigInteger.ZERO) == 1;
    }

    // ------------------
    // Compute factorial
    // ------------------

    public void reset() {
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        numLeft = new BigInteger(total.toString());
    }

    // --------------------------------------------------------
    // Generate next combination (algorithm from Rosen p. 286)
    // --------------------------------------------------------

    public static void main(String[] args) {
        CombinationGenerator c = new CombinationGenerator(6, 6);
        while (c.hasMore()) {
            String s = "";
            int[] next = c.getNext();
            for (int i : next) {
                s += " " + i;
            }
            System.out.println(s);
        }
    }

    private static BigInteger getFactorial(int n) {
        BigInteger fact = BigInteger.ONE;
        for (int i = n; i > 1; i--) {
            fact = fact.multiply(new BigInteger(Integer.toString(i)));
        }
        return fact;
    }
}
