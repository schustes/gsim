package de.s2.gsim.environment;


import android.annotation.Nullable;
import de.s2.gsim.objects.Path;
import de.s2.gsim.objects.attribute.Attribute;
import de.s2.gsim.objects.attribute.AttributeFactory;
import de.s2.gsim.objects.attribute.DomainAttribute;

import java.util.*;
import java.util.stream.Collectors;

import static de.s2.gsim.environment.CommonFunctions.existsPath;

/**
 * TODO all get methods must return a clone!
 * 
 * @author stephan
 *
 */
public class AgentClassOperations {

	private EntitiesContainer container;

	private AgentRuntimeConfig runtimeConfig;

	private ObjectClassOperations objectClassOperations;

	AgentClassOperations(EntitiesContainer container, AgentRuntimeConfig runtimeConfig) {
		this.container = container;
		this.runtimeConfig = runtimeConfig;
	}

	void setObjectClassOperations(ObjectClassOperations objectClassOperations) {
		this.objectClassOperations = objectClassOperations;
	}

	public GenericAgentClass removeChildObjectList(GenericAgentClass cls, Path<TypedList<Frame>> path) {
		GenericAgentClass here = this.findGenericAgentClass(cls);
		here.removeChildFrameList(path);

		removeChildListFromInstancesOfClass(path, here);

		for (GenericAgentClass successor : container.getAgentSubClasses(here)) {
			successor.replaceAncestor(here);
			successor.removeChildFrameList(path);
			removeChildListFromInstancesOfClass(path, successor);
		}

		return (GenericAgentClass) here.clone();

	}

	private void removeChildListFromInstancesOfClass(Path<TypedList<Frame>> path, GenericAgentClass here) {
		for (GenericAgent member : container.getAllInstancesOfClass(here, GenericAgent.class)) {
			member.setFrame(here);
			member.removeChildInstanceList(Path.objectListPath(path.toStringArray()));
		}
	}

	public GenericAgentClass setActivatedStatus(GenericAgentClass cls, String ruleName, boolean status) {

		GenericAgentClass here = this.findGenericAgentClass(cls);
		BehaviourFrame pb1 = here.getBehaviour();
		UserRuleFrame ur1 = pb1.getRule(ruleName);
		if (ur1 == null) {
			ur1 = pb1.getRLRule(ruleName);
			ur1.setActivated(status);
			pb1.addRLRule((RLRuleFrame) ur1);
		} else {
			ur1.setActivated(status);
			pb1.addOrSetRule(ur1);
		}
		here.setBehaviour(pb1);
		here.setDirty(true);

		for (GenericAgent p : container.getInstancesOfClass(here, GenericAgent.class)) {
			BehaviourDef pb = p.getBehaviour();
			UserRule ur = pb.getRule(ruleName);
			if (ur == null) {
				ur = pb.getRLRule(ruleName);
				ur.setActivated(status);
				pb.addRLRule((RLRule) ur);
			} else {
				ur.setActivated(status);
				pb.addRule(ur);
			}
			p.setBehaviour(pb);
			p.setDirty(true);
		}

		for (GenericAgentClass p : container.getAgentSubClasses(here)) {
			p.setDirty(true);
			BehaviourFrame pb = p.getBehaviour();
			UserRuleFrame ur = pb.getRule(ruleName);
			if (ur == null) {
				ur = pb.getRLRule(ruleName);
				ur.setActivated(status);
				pb.addRLRule((RLRuleFrame) ur);
			} else {
				ur.setActivated(status);
				pb.addOrSetRule(ur);
			}
			p.setBehaviour(pb);
			p.setDirty(true);
			for (GenericAgent a : container.getInstancesOfClass(p, GenericAgent.class)) {
				BehaviourDef beh = a.getBehaviour();
				UserRule ur2 = beh.getRule(ruleName);
				if (ur2 == null) {
					ur2 = beh.getRLRule(ruleName);
					ur2.setActivated(status);
					beh.addRule(ur2);
				} else {
					ur2.setActivated(status);
					beh.addRule(ur2);
				}
				a.setBehaviour(beh);
				a.setDirty(true);
			}

		}

		return (GenericAgentClass) here.clone();
	}

	/**
	 * Unclear helper method that extracts all agent behaviour classes and adds
	 * a copy of them, without being attached to specific agent classes, to a
	 * list of behaviours in the environment.
	 */
	protected void createBehaviourClasses() {
		BehaviourFrame behaviourClass = BehaviourFrame.newBehaviour("Behaviour");
		container.getBehaviourClasses().add(behaviourClass);
		for (GenericAgentClass c : container.getAgentSubClasses()) {
			BehaviourFrame f = BehaviourFrame.inherit(c.getBehaviour().getName(), c.getBehaviour());
			f.replaceAncestor(behaviourClass);
			container.getBehaviourClasses().add(f);
		}
	}

	public BehaviourFrame activateBehaviourRule(BehaviourFrame fr, UserRuleFrame ur, boolean activated) {

		container.getBehaviourClasses().stream()
		        .filter(bf -> bf.getName().equals(fr.getName()) || bf.isSuccessor(fr.getName())).forEach(bf -> {
			        UserRuleFrame g = bf.getRule(ur.getName());
			        g.setActivated(activated);
			        bf.addOrSetRule(g);
		        });

		return this.findBehaviourClass(fr).clone();

	}

	public GenericAgentClass addAttributeList(GenericAgentClass owner, String listName) throws GSimDefException {
		GenericAgentClass here = this.findGenericAgentClass(owner);

		here.defineAttributeList(listName);

		Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses(owner);
		for (GenericAgentClass subClass : agentSubClasses) {
			subClass.replaceAncestor(here);
			container.getInstancesOfClass(subClass, GenericAgent.class).stream().forEach(succ -> {
				succ.setFrame(subClass);
			});
		}

		container.getInstancesOfClass(here, GenericAgent.class).stream().forEach(member -> {
			member.setFrame(here);
		});

		return (GenericAgentClass) here.clone();

	}

	public GenericAgentClass addAgentClassAttribute(GenericAgentClass cls, Path<List<DomainAttribute>> path,
			DomainAttribute a) {
		GenericAgentClass oldOne = this.findGenericAgentClass(cls);
		final GenericAgentClass newLocalRef = container.replaceAgentSubClass(oldOne, cls);

		if (!existsPath(newLocalRef, path) && path.isTerminal()) {
			newLocalRef.defineAttributeList(path.getName());
		} else if (!existsPath(newLocalRef, path) && !path.isTerminal()) {
			createNecessaryLists(newLocalRef, path);
			// throw new GSimDefException(
			// "Path " + path + " does not exist and is not terminal, so no list can be created!");
		}

		if (!newLocalRef.addChildAttribute(path, a)) {
			throw new GSimDefException(String.format(
			        "The attribute %s could not be added. Probably the path %s specifies an non-existing object.", a.toString(), path));
		}

		Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses();
		for (GenericAgentClass subClass : agentSubClasses) {
			if (subClass.isSuccessor(newLocalRef.getName())) {
				subClass.replaceAncestor(newLocalRef);
				container.getAllInstancesOfClass(subClass, GenericAgent.class).stream().forEach(succ -> {
					succ.setFrame(subClass);
					succ.addChildAttribute(Path.attributeListPath(path.toStringArray()),
							AttributeFactory.createDefaultAttribute(a));
				});
			}
		}

		container.getAllInstancesOfClass(newLocalRef, GenericAgent.class).stream().forEach(succ -> {
			succ.setFrame(newLocalRef);
			succ.addChildAttribute(Path.attributeListPath(path.toStringArray()),
					AttributeFactory.createDefaultAttribute(a));
		});

		this.addChildAttributeInReferringAgents(newLocalRef, path, a);
		objectClassOperations.addChildAttributeInReferringObjects(newLocalRef, path, a);

		return (GenericAgentClass) newLocalRef.clone();
	}

	// TODO this might be not enough, if the hierarchy is deeper
	private void createNecessaryLists(GenericAgentClass newLocalRef, Path<List<DomainAttribute>> path) {

		Path<?> p = path;// Path.withoutLast(path);
		String listname = path.lastAsString();
		while (!p.isTerminal()) {
			p = Path.withoutLast(p);
			if (existsPath(newLocalRef, p)) {
				Object o = newLocalRef.resolvePath(p);
				if (o instanceof Frame) {
					((Frame) o).defineAttributeList(listname);
				}
			} else {
				newLocalRef.defineAttributeList(listname);
			}
			// p = Path.withoutLast(p);
			listname = p.lastAsString();
		}

	}

	public GenericAgentClass addAgentClassRule(GenericAgentClass cls, UserRuleFrame f) {

		GenericAgentClass here = this.findGenericAgentClass(cls);
		BehaviourFrame b = here.getBehaviour();

		if ((f instanceof RLRuleFrame)) {
			b.addRLRule((RLRuleFrame) f);
		} else {
			b.addOrSetRule(f);
		}
		here.setBehaviour(b);

		Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses();
		for (GenericAgentClass subClass : agentSubClasses) {
			if (subClass.isSuccessor(cls.getName())) {
				BehaviourFrame beh = subClass.getBehaviour();
				if (!(f instanceof RLRuleFrame)) {
					beh.addOrSetRule(UserRuleFrame.inheritFromUserRuleFrames(Arrays.asList(f), f.getName(), f.getCategory()));
				} else {
					beh.addRLRule(RLRuleFrame.inheritFromRLRuleFrames(Arrays.asList(f), f.getName(), f.getCategory()));
				}

				subClass.replaceAncestor(here);
				container.getAllInstancesOfClass(subClass, GenericAgent.class).stream().forEach(succ -> {
					instanciateAndSetRule(f, succ);
				});
			}
		}

		container.getAllInstancesOfClass(cls, GenericAgent.class).stream().forEach(succ -> {
			instanciateAndSetRule(f, succ);
		});

		return (GenericAgentClass) here.clone();
	}

	private void instanciateAndSetRule(UserRuleFrame f, GenericAgent succ) {
		if (!(f instanceof RLRuleFrame)) {
			UserRule urInst = UserRule.instanciate(f, f.getName());
			succ.getBehaviour().addRule(urInst);
		} else {
			RLRule cf = RLRule.instanciate(f, f.getName());
			succ.getBehaviour().addRLRule(cf);
		}
	}

	public void addAgentSubClass(GenericAgentClass cls) {
		container.getAgentSubClasses().add(GenericAgentClass.copy(cls));
		if (!this.runtimeConfig.getAgentOrder().containsKey(cls.getName())) {
			runtimeConfig.addAgentOrder(cls.getName(), -1);
		}
	}

	/**
	 * Adds the frame addedObject to all frames of the same or inherited type of
	 * 'here' in all agent classes that refer to this type. For example, agent
	 * has objectClass A1; A1 is modified by adding a child object A11, then all
	 * agent classes having A1 will be added A11 to their contained A1.
	 * 
	 * @param here
	 *            the frame that was modified
	 * @param path
	 *            the path in the modified frame that was added
	 * @param addedObject
	 *            the object that is added in path
	 */
	public void addChildFrameInReferringAgents(Frame here, Path<TypedList<Frame>> path, Frame addedObject) {
		container.getAgentSubClasses().stream().forEach(agent -> {
			for (String listName : agent.getDeclaredFrameListNames()) {
				agent.getChildFrames(listName).stream()
				        .filter(child -> child.isSuccessor(here.getName()) || child.getName().equals(here.getName())).forEach(child -> {
					        Path<TypedList<Frame>> p = Path.objectListPath(listName, child.getName()).append(path);
					        addChildObject(agent, p, addedObject);
				        });
			}
		});
	}

	public GenericAgentClass addChildObject(GenericAgentClass cls, Path<TypedList<Frame>> path, Frame f) {

		GenericAgentClass here = findGenericAgentClass(cls);

		Path<TypedList<Frame>> frameList = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT);
		TypedList<Frame> list = here.resolvePath(frameList);
		list.add(f);
		here.setDirty(true);

		Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses();
		agentSubClasses.stream().filter(subClass -> subClass.isSuccessor(cls.getName())).forEach(subClass -> {
			subClass.replaceAncestor(here);
			container.getAllInstancesOfClass(subClass, GenericAgent.class).stream().forEach(succ -> {
				succ.setFrame(subClass);
				Path<TypedList<Instance>> instListPath = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT);
				TypedList<Instance> instList = succ.resolvePath(instListPath);
				instList.add(new Instance(f.getName(), f));
			});
		});

		container.getAllInstancesOfClass(cls, GenericAgent.class).stream().forEach(succ -> {
			succ.setFrame(cls);
			Path<TypedList<Instance>> instListPath = Path.withoutLastAttributeOrObject(path, Path.Type.OBJECT);
			TypedList<Instance> instList = succ.resolvePath(instListPath);
			instList.add(new Instance(f.getName(), f));
		});

		addChildFrameInReferringAgents(here, path, f);

		return (GenericAgentClass) here.clone();
	}

	public BehaviourFrame addRuleToBehaviour(BehaviourFrame fr, UserRuleFrame ur) {
		BehaviourFrame here = null;
		for (BehaviourFrame f : container.getBehaviourClasses()) {
			if (f.getName().equals(fr.getName())) {
				f.addOrSetRule(ur);
				here = f;
			}
		}
		for (BehaviourFrame f : container.getBehaviourClasses()) {
			if (f.isSuccessor(fr.getName())) {
				f.addOrSetRule(ur);
				f.replaceAncestor(here);
			}
		}
		return (BehaviourFrame) here.clone();
	}

	public GenericAgentClass changeAgentClassBehaviour(GenericAgentClass c, BehaviourFrame behaviourFrame) {

		GenericAgentClass here = this.findGenericAgentClass(c);

		ArrayList<String> removed = new ArrayList<String>(behaviourFrame.removed);
		behaviourFrame.removed.clear();

		BehaviourFrame newBehaviour = BehaviourFrame.copy(behaviourFrame);
		here.setBehaviour(newBehaviour);

		for (GenericAgentClass p : container.getAgentSubClasses()) {
			if (p.isSuccessor(c.getName())) {
				p.replaceAncestor(here);
				BehaviourFrame sb = p.getBehaviour();
				sb.replaceAncestor(newBehaviour);

				if (behaviourFrame.isDirty()) {
					sb.setMaxNodes(behaviourFrame.getMaxNodes());
					sb.setRevalProb(behaviourFrame.getRevalProb());
					sb.setRevisitCost(behaviourFrame.getRevisitCost());
					sb.setStateUpdateInterval(behaviourFrame.getStateUpdateInterval());
				}

				for (UserRuleFrame f : behaviourFrame.getRules()) {
					if (f.isDirty() && sb.getDeclaredRule(f.getName()) != null) {
						sb.addOrSetRule(f);
					}
				}
				for (RLRuleFrame f : behaviourFrame.getRLRule()) {
					if (f.isDirty() && sb.getDeclaredRLRule(f.getName()) != null) {
						sb.addOrSetRule(f);
					}
				}
				for (ActionFrame f : behaviourFrame.getAvailableActions()) {
					if (f.isDirty()) {
						sb.addAction(f);
					}
				}
				for (String s : removed) {
					try {
						sb.removeRLRule(s);
						sb.removeRule(s);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				p.setBehaviour(sb);

				for (GenericAgent agent : container.getAllInstancesOfClass(p, GenericAgent.class)) {
					agent.setDirty(true);
					BehaviourDef np = BehaviourDef.instanciate(sb);
					agent.setFrame(p);
					agent.setBehaviour(np);
				}
			}
		}

		for (UserRuleFrame reactiveRule : behaviourFrame.getRules()) {
			reactiveRule.setDirty(false);
		}
		for (RLRuleFrame rlRule : behaviourFrame.getRLRule()) {
			rlRule.setDirty(false);
		}
		for (ActionFrame actionFrame : behaviourFrame.getAvailableActions()) {
			actionFrame.setDirty(false);
		}

		behaviourFrame.setDirty(false);
		here.setBehaviour(newBehaviour);

		for (GenericAgent a : container.getAllInstancesOfClass(here, GenericAgent.class)) {
			a.setDirty(true);
			BehaviourDef np = BehaviourDef.instanciate(behaviourFrame);
			a.setFrame(here);
			a.setBehaviour(np);
		}

		return (GenericAgentClass) here.clone();
	}

	public GenericAgentClass createAgentSubclass(String name, GenericAgentClass parent) {

		GenericAgentClass successor = null;
		if (parent == null) {
			successor = GenericAgentClass.inherit(container.getAgentClass(), name);
		} else {
			successor = GenericAgentClass.inherit(parent, name);
		}

		container.addAgentClass(successor);

		if (!runtimeConfig.getAgentOrder().containsKey(name)) {
			runtimeConfig.addAgentOrder(name, -1);
		}

		return (GenericAgentClass) successor.clone();

	}

	public BehaviourFrame createBehaviour(String name, @Nullable BehaviourFrame parent) {
		BehaviourFrame behaviourFrame = null;

		if (parent != null) {
			BehaviourFrame here = getBehaviour(parent.getName());
			behaviourFrame = BehaviourFrame.inherit(name, here);
		} else {
			behaviourFrame = BehaviourFrame.newBehaviour(name);
			for (ActionFrame actionFrame : container.getAgentClass().getBehaviour().getAvailableActions()) {
				behaviourFrame.addAction((ActionFrame) actionFrame.clone());
			}
		}

		container.addBehaviourClass(behaviourFrame);

		return (BehaviourFrame) behaviourFrame.clone();
	}

	public GenericAgentClass extendAgentClassRole(GenericAgentClass original, Frame role) {

		GenericAgentClass newRoleType = (GenericAgentClass) role.clone();

		GenericAgentClass c = this.findGenericAgentClass(original);
		if (c == null) {
			c = original;
		}
		GenericAgentClass c2 = (GenericAgentClass) c.clone();

		GenericAgentClass nc = GenericAgentClass.copyFromAndExtendWith(c2, newRoleType, original.getName());
		container.replaceAgentSubClass(c, nc);
		return (GenericAgentClass) nc.clone();

	}

	public List<String> getAgentNames(String ofClass) {

		return container.getAgents().stream().filter(agent -> agent.inheritsFromOrIsOfType(ofClass)).map(GenericAgent::getName)
				.collect(Collectors.toList());
	}

	public Map<String, Integer> getAgentOrdering() {
		return runtimeConfig.getAgentOrder();
	}

	public List<GenericAgent> getAgents(String ofClass) {
		return container.getAgents().stream().filter(agent -> agent.inheritsFromOrIsOfType(ofClass)).map(GenericAgent::clone)
				.collect(Collectors.toList());
	}

	public GenericAgentClass getAgentSubClass(String className) {
		return container.getAgentSubClasses()
				.stream()
				.filter(sub -> sub.getName().equals(className)).map(GenericAgentClass::clone).findAny().orElse(null);
	}

	public boolean containsAgentSubClass(String className) {
		return container.getAgentSubClasses()
				.stream()
				.anyMatch(sub -> sub.getName().equals(className));
	}

	public List<GenericAgentClass> getAgentSubClasses() {
		return container.getAgentSubClasses().stream().map(GenericAgentClass::clone).collect(Collectors.toList());
	}

	/**
	 * Returns all successors (not only the immediate ones).
	 * 
	 * @param agentClassName
	 * @return list of successors
	 */
	public List<GenericAgentClass> getAllAgentClassSuccessors(String agentClassName) {
		return container.getAgentSubClasses().stream().filter(c -> c.isSuccessor(agentClassName))
				.map(GenericAgentClass::clone).collect(Collectors.toList());
	}

	public BehaviourFrame getBehaviour(String behaviourName) {
		return container.getBehaviourClasses().stream().filter(b -> b.getName().equals(behaviourName))
				.map(BehaviourFrame::clone).findAny().get();
	}

	public List<BehaviourFrame> getBehaviours() {
		return container.getBehaviourClasses().stream().map(BehaviourFrame::clone).collect(Collectors.toList());
	}

	public GenericAgentClass getGenericAgentClass() {
		return container.getAgentClass().clone();
	}

	public List<GenericAgentClass> getImmediateAgentClassSuccessors(String frame) {
		return container.getAgentSubClasses().stream()
				.filter(p -> p.getParentFrame(frame) != null && p.getName().equals(frame)).map(GenericAgentClass::clone)
				.collect(Collectors.toList());
	}

	public GenericAgentClass modifyAgentClassAttribute(GenericAgentClass cls, Path<DomainAttribute> path,
			DomainAttribute domainAttribute) {

		GenericAgentClass actualRef = this.findGenericAgentClass(cls);

		createOrModifyAttribute(path, domainAttribute, actualRef);

		Iterator<GenericAgentClass> iter = container.getAgentSubClasses().iterator();

		while (iter.hasNext()) {
			GenericAgentClass c = iter.next();

			if (c.isSuccessor(actualRef.getName())) {
				c.replaceAncestor(actualRef);

				createOrModifyAttribute(path, domainAttribute, c);

				replaceMemberAttributes(c, path, domainAttribute);
			}
		}

		replaceMemberAttributes(actualRef, path, domainAttribute);

		return (GenericAgentClass) actualRef.clone();
	}

	private void createOrModifyAttribute(Path<DomainAttribute> path, DomainAttribute domainAttribute,
			GenericAgentClass agentClass) {
		String listName = Path.withoutLastAttributeOrObject(path, Path.Type.ATTRIBUTE).getName();
		String attrName = domainAttribute.getName();
		if (agentClass.isDeclaredAttribute(listName, attrName)) {
			agentClass.replaceChildAttribute(path, domainAttribute);
		} else {
			agentClass.addOrSetAttribute(listName, domainAttribute);
		}
	}

	/**
	 * Replaces attributes of all members (immediate successors) with the
	 * default value of the given domain attribute.
	 * 
	 * @param agentClass
	 *            the class of the members to replace.
	 * @param attributePath
	 *            the path
	 */
	private void replaceMemberAttributes(GenericAgentClass agentClass, Path<DomainAttribute> path,
			DomainAttribute replacement) {
		Path<Attribute> attributePath = Path.attributePath(path.toStringArray());
		Iterator<GenericAgent> members = container.getInstancesOfClass(agentClass, GenericAgent.class).iterator();
		while (members.hasNext()) {
			GenericAgent c = (GenericAgent) members.next();
			c.replaceChildAttribute(attributePath, AttributeFactory.createDefaultAttribute(replacement));
		}
	}

	public void removeAgentClass(GenericAgentClass cls) {

		GenericAgentClass here = this.findGenericAgentClass(cls);

		for (Iterator<GenericAgentClass> iter = container.getAgentSubClasses().iterator(); iter.hasNext();) {
			GenericAgentClass cc = iter.next();
			if (cc.isSuccessor(cls.getName()) || cc.equals(here)) {
				container.remove(cc);
				removeFrameInReferringAgents(cc);
			}

			for (Iterator<GenericAgent> iter2 = container.getInstancesOfClass(cc, GenericAgent.class).iterator(); iter2
					.hasNext();) {
				iter2.next();
				iter2.remove();
			}
		}

		for (Iterator<Frame> iter = container.getObjectSubClasses().iterator(); iter.hasNext();) {
			Frame cc = (Frame) iter.next();
			if (cc.isSuccessor(cls.getName()) || cc.equals(here)) {
				this.removeFrameInReferringObjectClasses(cc);
				iter.remove();
			}
			for (Iterator<Instance> iter2 = container.getInstancesOfClass(cc, Instance.class).iterator(); iter2
					.hasNext();) {
				iter2.remove();
			}
		}

	}

	private void removeFrameInReferringObjectClasses(Frame removed) {
		container.removeFrameInReferringFrames((frame, subPath) -> {
			objectClassOperations.removeChildFrame(frame, subPath);
		}, removed);
	}

	public GenericAgentClass removeAgentClassAttribute(GenericAgentClass cls, Path<DomainAttribute> path) {
		GenericAgentClass here = this.findGenericAgentClass(cls);
		container.getAgentSubClasses().stream().filter(agentClass -> agentClass.isSuccessor(cls.getName()))
		        .forEach(agentClass -> {
			        agentClass.replaceAncestor(here);
			        container.getInstancesOfClass(agentClass, Instance.class).forEach(inst -> {
				        inst.setFrame(agentClass);
			        });
		        });

		container.getInstancesOfClass(here, Instance.class).forEach(member -> {
			Path<Attribute> instancePath = Path.attributePath(path.toStringArray());
			member.removeChildAttribute(instancePath);
		});

		here.removeChildAttribute(path);

		removeDeletedAttributeInReferringAgents(cls, path);
		removeDeletedAttributeInReferringObjects(cls, path);

		return (GenericAgentClass) here.clone();

	}

	public GenericAgentClass removeAttributeList(GenericAgentClass owner, String listName) throws GSimDefException {

		GenericAgentClass here = this.findGenericAgentClass(owner);

		here.removeDeclaredAttributeList(listName);

		container.getInstancesOfClass(here, GenericAgent.class).stream().forEach(a -> {
			a.setFrame(here);
			a.removeDeclaredAttributeList(listName);
		});


		container.getAgentSubClasses(here).stream().forEach(a -> {
			a.replaceAncestor(here);
			a.removeDeclaredAttributeList(listName);
			for (GenericAgent member : container.getInstancesOfClass(here, GenericAgent.class)) {
				member.setFrame(a);
				member.removeDeclaredAttributeList(listName);
			}
		});

		return (GenericAgentClass) here.clone();

	}

	public GenericAgentClass removeChildFrame(GenericAgentClass cls, Path<Frame> path) {

		GenericAgentClass here = this.findGenericAgentClass(cls);

		container.getAgentSubClasses(here).stream().forEach(sub -> {
			sub.replaceAncestor(here);
			// sub.removeChildFrame(path);
			for (GenericAgent member : container.getInstancesOfClass(sub, GenericAgent.class)) {
				member.setFrame(sub);
				// member.removeChildInstance(Path.<Instance>objectPath(path.toStringArray()));
			}
		});

		// delete defining class only after subclasses and members were checked/deleted
		here.removeChildFrame(path);
		container.getInstancesOfClass(here, GenericAgent.class).forEach(member -> {
			member.setFrame(here);
			member.removeChildInstance(Path.<Instance>objectPath(path.toStringArray()));
		});

		return (GenericAgentClass) here.clone();
	}

	private GenericAgentClass findGenericAgentClass(GenericAgentClass extern) {
		if (extern.getName().equals(GenericAgentClass.NAME)) {
			return container.getAgentClass();
		}
		return container.getAgentSubClasses().stream().filter(a -> a.getName().equals(extern.getName())).findAny()
				.get();
	}

	private BehaviourFrame findBehaviourClass(BehaviourFrame extern) {
		if (extern.getName().equals(GenericAgentClass.NAME)) {
			return container.getBehaviourClass();
		}
		return container.getBehaviourClasses().stream().filter(a -> a.getName().equals(extern.getName())).findAny()
				.get();
	}

	private void removeDeletedAttributeInReferringAgents(Frame here, Path<DomainAttribute> path) {
		container.modifyChildFrame((cls, attributePath) -> {
			this.removeAgentClassAttribute((GenericAgentClass) cls, attributePath);
		}, here, path);
	}

	private void removeDeletedAttributeInReferringObjects(Frame here, Path<DomainAttribute> path) {
		container.modifyChildFrame((cls, attributePath) -> {
			objectClassOperations.removeObjectClassAttribute(cls, path);
		}, here, path);
	}

	protected void removeFrameInReferringAgents(Frame removed) {
		container.getAgentSubClasses().stream().filter(a -> a.hasDeclaredChildFrame(removed.getName())).forEach(a -> {
			for (String list : a.getListNamesWithDeclaredChildFrame(removed.getName())) {
				Path<Frame> path = Path.objectPath(list, removed.getName());
				this.removeChildFrame(a, path);
			}
		});
	}

	public void addChildAttributeInReferringAgents(Frame here, Path<List<DomainAttribute>> path,
			DomainAttribute added) {
		for (GenericAgentClass agentClass : getAgentSubClasses()) {
			for (String listname : agentClass.getDeclaredFrameListNames()) {
				for (Frame f : agentClass.getChildFrames(listname)) {
					if (f.isSuccessor(here.getName()) || f.getName().equals(here.getName())) {
						Path<List<DomainAttribute>> newPath = Path.attributeListPath(listname, f.getName()).append(path);
						addAgentClassAttribute(agentClass, newPath, added);
					}
				}
			}
		}
	}

	Set<GenericAgentClass> getGenericAgentSubClassesRef() {
		return container.getAgentSubClasses();
	}

	void setAgentClass(GenericAgentClass c) {
		container.setAgentClass(c);
	}

	public GenericAgentClass addObjectList(GenericAgentClass owner, String list, Frame type) {
		GenericAgentClass here = this.findGenericAgentClass(owner);

		here.defineObjectList(list, type);

		Set<GenericAgentClass> agentSubClasses = container.getAgentSubClasses(owner);
		for (GenericAgentClass subClass : agentSubClasses) {
			subClass.replaceAncestor(here);
			container.getInstancesOfClass(subClass, GenericAgent.class).stream().forEach(succ -> {
				succ.setFrame(subClass);
			});
		}

		container.getInstancesOfClass(here, GenericAgent.class).stream().forEach(member -> {
			member.setFrame(here);
		});

		return (GenericAgentClass) here.clone();
	}

}
