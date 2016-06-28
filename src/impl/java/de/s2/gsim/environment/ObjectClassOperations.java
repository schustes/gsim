package de.s2.gsim.environment;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class ObjectClassOperations {

    private EntitiesContainer container;
    private AgentClassOperations agentClassOperations;
    private ObjectInstanceOperations objectInstanceOperations;

    ObjectClassOperations(EntitiesContainer container, AgentClassOperations agentClassOperations,
            ObjectInstanceOperations objectInstanceOperations) {
        this.container = container;
        this.objectInstanceOperations = objectInstanceOperations;
        this.agentClassOperations = agentClassOperations;
    }

    public Frame addObjectClassAttribute(Frame cls, Path<List<DomainAttribute>> path, DomainAttribute newValue) {

        Frame here = findObjectClass(cls);
        Set<Frame> objectSubClasses = container.getObjectSubClasses();

        here.addChildAttribute(path, newValue);
        here.setDirty(true);

        objectSubClasses.stream().filter(f -> f.isSuccessor(cls.getName())).forEach(f -> f.replaceAncestor(here));

        this.addChildAttributeInReferringObjects(cls, path, newValue);
        agentClassOperations.addChildAttributeInReferringAgents(cls, path, newValue);

        return (Frame) here.clone();
    }

    public void addObjectSubClass(Frame cls) {
        container.addObjectClass(cls.clone());
    }

    public Frame createObjectSubClass(String name, Frame parent) {

        Frame p;
        if (parent == null) {
            p = Frame.inherit(Arrays.asList(container.getObjectClass()), name, Optional.empty());
        } else {
            p = Frame.inherit(Arrays.asList(this.findObjectClass(parent)), name, Optional.empty());
        }
        container.addObjectClass(p);
        return (Frame) p.clone();

    }

    public Frame addChildFrame(Frame cls, Path<TypedList<Frame>> path, Frame frameToAdd) {
        Frame here = findObjectClass(cls);

        here.addChildFrame(path, frameToAdd);

        for (Frame successor : container.getObjectSubClasses(here)) {
            successor.replaceAncestor(here);
        }

        for (Instance member : container.getInstancesOfClass(here, Instance.class)) {
            Instance newInstance = new Instance(frameToAdd.getName(), frameToAdd);
            Path<TypedList<Instance>> instPath = Path.objectListPath(path.toStringArray());
            member.addChildInstance(instPath, newInstance);
        }

        addChildFrameInReferringObjects(cls, path, frameToAdd);

        return (Frame) here.clone();

    }

    public Frame getObjectClass() {
        return container.getObjectClass().clone();
    }

    public Frame getObjectClassRef() {
        return container.getObjectClass();
    }

    public Frame getObjectSubClass(String objectName) {
        return container.getObjectSubClasses().parallelStream().filter(o -> o.getName().equals(objectName)).findAny().get();
    }

    public List<Frame> getObjectSubClasses() {
        return container.getObjectSubClasses().parallelStream().map(Frame::clone).collect(Collectors.toList());
    }

    public Frame modifyObjectClassAttribute(Frame cls, Path<DomainAttribute> path, DomainAttribute a) {
        Frame here = findObjectClass(cls);

        here.replaceChildAttribute(path, a.clone());
        for (Frame successor : container.getObjectSubClasses(here)) {
            successor.replaceAncestor(here);
            successor.replaceChildAttribute(path, a.clone());
        }

        return (Frame) here.clone();
    }

    public Frame removeAttributeList(Frame owner, String listName) throws GSimDefException {
        Frame here = findObjectClass(owner);

        here.removeDeclaredAttributeList(listName);
        container.getInstancesOfClass(owner, Instance.class).parallelStream().forEach((inst) -> {
            inst.setFrame(here);
            inst.removeDeclaredAttributeList(listName);
        });

        container.getObjectSubClasses(here).parallelStream().forEach(succ -> {
            succ.replaceAncestor(here);
            succ.removeDeclaredAttributeList(listName);
            for (Instance member : container.getInstancesOfClass(succ, Instance.class)) {
                member.setFrame(succ);
                member.removeDeclaredAttributeList(listName);
            }
        });

        return (Frame) here.clone();

    }

    public Frame removeChildFrame(Frame cls, Path<Frame> path) {
        Frame here = findObjectClass(cls);
        here.removeChildFrame(path);

        container.getObjectSubClasses(here).parallelStream().forEach(sub -> {
            sub.removeChildFrame(path);
            Path<TypedList<Instance>> instList = Path.objectListPath(Path.withoutLastAttributeOrObject(path, Path.Type.LIST).toStringArray());
            for (Instance member : container.getInstancesOfClass(sub, Instance.class)) {
                TypedList<Instance> list = member.resolvePath(instList);
                if (list != null) {
                    list.clear();
                }
            }
        });

        return (Frame) here.clone();
    }

    public void removeObjectClass(Frame cls) {

        Frame here = findObjectClass(cls);
        Iterator<Frame> iter = container.getObjectSubClasses(here).iterator();
        while (iter.hasNext()) {
            iter.remove();
        }

        container.getObjectSubClasses().remove(here);

        this.removeFrameInReferringAgentClasses(here);
        this.removeFrameInReferringObjectClasses(here);

    }

    public GenericAgentClass removeObjectClassAttribute(Frame cls, Path<DomainAttribute> path) {
        Frame here = this.findObjectClass(cls);
        container.getObjectSubClasses(here).parallelStream().forEach(objectClass -> {
            objectClass.replaceAncestor(here);
            container.getInstancesOfClass(objectClass, Instance.class).forEach(inst -> {
                inst.setFrame(objectClass);
            });
        });

        container.getInstancesOfClass(here, Instance.class).forEach(member -> {
            Path<Attribute> instancePath = Path.attributePath(path.toStringArray());
            member.removeChildAttribute(instancePath);
        });

        here.removeChildAttribute(path);

        removeDeletedAttributeInReferringObjects(here, path);
        removeDeletedAttributeInReferringAgents(here, path);

        return (GenericAgentClass) here.clone();

    }

    private void removeDeletedAttributeInReferringAgents(Frame here, Path<DomainAttribute> path) {
        container.modifyChildFrame((cls, attributePath) -> {
                agentClassOperations.removeAgentClassAttribute((GenericAgentClass) cls, attributePath);
        }, here, path);
    }

    private void removeDeletedAttributeInReferringObjects(Frame here, Path<DomainAttribute> path) {
        container.modifyChildFrame((cls, attributePath) -> {
            removeObjectClassAttribute(cls, path);
        }, here, path);
    }

    private Frame findObjectClass(Frame extern) {

        if (extern.getName().equals(container.getObjectClass().getName())) {
            return container.getObjectClass();
        }

        return container.getObjectSubClasses().parallelStream().filter(a -> a.getName().equals(extern.getName())).findAny().get();

    }

    private void removeFrameInReferringObjectClasses(Frame removed) {
        container.removeFrameInReferringFrames((frame, subPath) -> {
            this.removeChildFrame(frame, subPath);
        }, removed);
    }

    private void removeFrameInReferringAgentClasses(Frame removed) {
        container.removeFrameInReferringFrames((frame, subPath) -> {
            agentClassOperations.removeFrameInReferringAgents(removed);
        }, removed);
    }

    public void addChildAttributeInReferringObjects(Frame modifiedFrame, Path<List<DomainAttribute>> path, DomainAttribute added) {
        for (Frame objectClass : getObjectSubClasses()) {
            for (String listname : objectClass.getDeclaredFrameListNames()) {
                for (Frame containedModifiedFrame : objectClass.getChildFrames(listname)) {
                    if (containedModifiedFrame.isSuccessor(modifiedFrame.getName())) {
                        Path<List<DomainAttribute>> newPath = Path.attributeListPath(listname, path.toStringArray());
                        objectClass.addChildAttribute(newPath, added);
                    }
                }
            }
        }
    }

    public void addChildFrameInReferringObjects(Frame here, Path<TypedList<Frame>> path, Frame addedObject) {
        container.getObjectSubClasses().parallelStream().forEach(object -> {
            for (String listName : object.getDeclaredFrameListNames()) {
                object.getChildFrames(listName).stream().filter(child -> child.isSuccessor(here.getName()) || child.getName().equals(here.getName()))
                        .forEach(child -> {
                            Path<TypedList<Frame>> p = Path.objectListPath(listName, child.getName(), path.toStringArray());
                            addChildFrame(object, p, addedObject);
                        });
            }
        });
    }
}
