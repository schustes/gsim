package de.s2.gsim.api.objects.impl;

import java.util.Observable;
import java.util.Observer;

import de.s2.gsim.objects.ObjectClass;
import de.s2.gsim.objects.ObjectInstance;

public class ObserverUtils {
	
	private ObserverUtils() {
		//static helper class
	}

	public static void observeDependentObject(ObjectClass objectClass, Observer obs) {
		if (objectClass instanceof Observable) {
			((Observable)objectClass).addObserver(obs);
		}
	}

	public static void observeDependentObjectInstance(ObjectInstance objectClass, Observer obs) {
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

	public static void stopObservingDependentObjectInstance(ObjectInstance objectInstance, Observer obs) {
		if (objectInstance instanceof Observable) {
			((Observable)objectInstance).deleteObserver(obs);
		}
	}

}
