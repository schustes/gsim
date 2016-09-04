package de.s2.gsim.environment;

import java.util.Iterator;
import java.util.List;

import de.s2.gsim.objects.attribute.Attribute;

public class ObjectInstanceOperations {

    private EntitiesContainer container;

    /**
     * Find the object reference specified by name.
     * 
     * Throws a NoSuchElementException if not existing.
     * 
     * @param name the name of the object
     * @return the instance
     */
    protected Instance findObject(String name) {
        return container.getObjects().parallelStream().filter(o -> o.getName().equals(name)).findAny().get();
    }
    
    public List<Instance> getInstancesOfClass(Frame parent) {
    	return container.getAllInstancesOfClass(parent, Instance.class);
    }
    

    public Instance modifyObjectAttribute(Instance inst, Path<Attribute> attrPath, Attribute att) {
        Instance here = findObject(inst.getName());
        here.replaceChildAttribute(attrPath, att);
        return (Instance) here.clone();
    }

    public void removeObject(Instance object) {
        Iterator<Instance> iter = container.getObjects().iterator();
        while (iter.hasNext()) {
            Instance a = iter.next();
            if (a.getName().equals(object.getName())) {
                iter.remove();
            }
        }
    }


	public Instance instanciateObject(Frame f, String name) {
		Instance newInstance = Instance.instanciate(name, f);
		container.addObject(newInstance);
		return newInstance.clone();
	}


}
