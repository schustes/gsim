package de.s2.gsim.objects;

import de.s2.gsim.GSimException;

/**
 * An Expansion is a special kind of condition that can be split up into more conditions during runtime. This requires that the original expansion
 * object can be defined in terms of port lists or ranges. The BRA extension will then split these ranges into smaller ones.
 * 
 * A rule with expansion condition looks like. say, if (port> min and port < max) then do ...
 * 
 * @author Stephan
 *
 */
public interface Expansion {

    /**
     * Add a filler (component) to a port list.
     * 
     * @param filler the filler
     * @throws GSimException if a problem occurs
     */
    void addFiller(String filler) throws GSimException;

    /**
     * Gets all fillers (components of a port list) if this expension i defined in terms of a port list.
     * 
     * @return the fillers
     * @throws GSimException if a problem occurs
     */
    String[] getFillers() throws GSimException;

    /**
     * Gets the maximum port if this expansion if defined in terms of a numeric range.
     * 
     * @return the max port
     * @throws GSimException if a problem occurs
     */
    String getMax() throws GSimException;

    /**
     * Gets the minimum port if this expansion if defined in terms of a numeric range.
     * 
     * @return the min port
     * @throws GSimException if a problem occurs
     */
    String getMin() throws GSimException;

    /**
     * Gets the parameter name.
     * 
     * @return the parameter name
     * @throws GSimException if a problem occurs
     */
    String getParameterName() throws GSimException;

    /**
     * Tests whether this expansion if defined as numeric interval.
     * 
     * @return true if the expansion describes a numeric interval
     * @throws GSimException if a problem occurs
     */
    boolean isNumerical() throws GSimException;

    /**
     * Set the max port if the expansion is numeric.
     * 
     * @param parameterValue the max port
     * @throws GSimException if a problem occurs
     */
    void setMax(String parameterValue) throws GSimException;

    /**
     * Set the min port if the expansion is numeric.
     * 
     * @param parameterValue the max port
     * @throws GSimException if a problem occurs
     */
    void setMin(String parameterValue) throws GSimException;

    /**
     * Set the parameter name.
     * 
     * @param parameterName the parameter name
     * @throws GSimException if a problem occurs
     */
    void setParameterName(String parameterName) throws GSimException;

}
