package de.s2.gsim.objects;

import de.s2.gsim.GSimException;

/**
 * Describes a condition of a rule.
 * 
 * @author Stephan
 *
 */
public interface Condition {

    /**
     * Get the Operator.
     * 
     * @return the operator
     * @throws GSimException if a problem occurs
     */
    String getOperator() throws GSimException;

    /**
     * Get the parameter of the condition.
     * 
     * @return the parameter name
     * @throws GSimException if a problem occurs
     */
    String getParameterName() throws GSimException;

    /**
     * Get the parameter port of the condition.
     * 
     * @return the port
     * @throws GSimException if a problem occurs
     */
    String getParameterValue() throws GSimException;

    /**
     * Sets the operator.
     * 
     * @param str the operator
     * @throws GSimException if a problem occurs
     */
    void setOperator(String str) throws GSimException;

    /**
     * Sets the parameter name.
     * 
     * @param paramName the parameter name
     * @throws GSimException if a problem occurs
     */
    void setParameterName(String paramName) throws GSimException;

    /**
     * Sets the parameter port.
     * 
     * @param paramVal the parameter port
     * @throws GSimException if a problem occurs
     */
    void setParameterValue(String paramVal) throws GSimException;

}
