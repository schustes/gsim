package de.s2.gsim.environment;

import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ObjectInstanceOperations {

    private EntitiesContainer container;
    
    public ObjectInstanceOperations(EntitiesContainer container) {
    	this.container = container;
    }

    /**
     * Find the object reference specified by name.
     * 
     * Throws a NoSuchElementException if not existing.
     * 
     * @param name the name of the object
     * @return the instance
     */
    protected Instance findObject(String name) {
        Set<Instance> objects = container.getObjects();
        synchronized (objects) {
            return objects.stream().filter(o -> o.getName().equals(name)).findAny().get();
        }
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
