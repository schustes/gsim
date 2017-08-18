package de.s2.gsim.api.objects.impl;

import java.util.Objects;

import de.s2.gsim.GSimException;

public abstract class Invariant {
	
	public static void precondition(ManagedObject f, Object... params) {
		objectNotDestroyed(f);
		for (Object o : params) {
			Objects.requireNonNull(o);
		}
	}

	public static void intervalIntegrity(double from, double to) {
		if (from > to) {
			throw new GSimException(String.format("Interval upper bound %d is smaller than lower bound %d!", to, from));
		}
	}

	public static void setIntegrity(String[] fillers) {
		if (fillers.length == 0) {
			throw new GSimException(String.format("There are no fillers to set!"));
		}
	}

	public static void objectNotDestroyed(ManagedObject f) {
		if (f.isDestroyed()) {
			throw new GSimException("This object was removed from the runtime context.");
		}
	}

}
