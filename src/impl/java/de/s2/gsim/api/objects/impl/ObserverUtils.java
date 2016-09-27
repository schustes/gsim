package de.s2.gsim.api.objects.impl;

import java.util.Observable;
import java.util.Observer;

import de.s2.gsim.objects.ObjectClass;

public class ObserverUtils {
	
	private ObserverUtils() {
		//static helper class
	}

	public static void observeDependentObject(ObjectClass objectClass, Observer obs) {
		if (objectClass instanceof Observable) {
			((Observable)objectClass).addObserver(obs);
		}
	}

	public static void stopObservingDependent(Observable objectClass, Observer obs) {
		objectClass.deleteObserver(obs);
	}

	public static void stopObservingDependentObject(ObjectClass objectClass, Observer obs) {
		if (objectClass instanceof Observable) {
			((Observable)objectClass).deleteObserver(obs);
		}
	}
}
