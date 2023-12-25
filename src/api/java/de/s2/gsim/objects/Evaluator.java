package de.s2.gsim.objects;

/**
 * Evaluator is a type holding reward information: The attribute reference in
 * which the current reward is stored, and the learning rate alpha.
 * 
 * @author Stephan
 *
 */
public interface Evaluator {

	/**
	 * Gets the path to the attribute in which the reward is stored. This port
	 * is used by the RL mechanism to compute the action port to which it is
	 * attached.
	 * 
	 * The path may reference a domain or instance attribute.
	 * 
	 * @return the attribute path
	 */
	Path<?> getAttributeRef();

	/**
	 * Get the learning rate for the action node this evaluator is attached to.
	 * 
	 * @return the alpha port
	 */
	double getAlpha();

}
