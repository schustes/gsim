package de.s2.gsim.objects;

import de.s2.gsim.core.GSimException;

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
     * Get the parameter value of the condition.
     * 
     * @return the value
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
     * Sets the parameter value.
     * 
     * @param paramVal the parameter value
     * @throws GSimException if a problem occurs
     */
    void setParameterValue(String paramVal) throws GSimException;

}
