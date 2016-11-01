package de.s2.gsim.api.objects.impl.behaviour;

import de.s2.gsim.api.objects.impl.UnitWrapper;
import de.s2.gsim.environment.ConditionFrame;
import de.s2.gsim.environment.Unit;
import de.s2.gsim.objects.Evaluator;
import de.s2.gsim.objects.Path;

public class EvaluatorClass implements Evaluator, UnitWrapper {

    private ConditionFrame real;

	public EvaluatorClass(ConditionFrame real) {
        this.real = real;
    }

    @Override
	public Path<?> getAttributeRef() {
		return Path.attributePath(real.getParameterValue().split("/"));
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
