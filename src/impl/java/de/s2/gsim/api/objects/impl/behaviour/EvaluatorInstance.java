package de.s2.gsim.api.objects.impl.behaviour;

import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ConditionDef;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.Evaluator;
import de.s2.gsim.objects.Path;

public class EvaluatorInstance implements Evaluator, UnitWrapper {

    private ConditionDef real;

	public EvaluatorInstance(ConditionDef real) {
        this.real = real;
    }

    @Override
	public Path<?> getAttributeRef() {
		return Path.attributePath(real.getParameterName());
    }

    @Override
	public double getAlpha() {
		return Double.parseDouble(real.getParameterValue());
    }

    @Override
	public Unit<?, ?> toUnit() {
        return real;
    }
}
