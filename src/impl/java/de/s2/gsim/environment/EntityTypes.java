package de.s2.gsim.environment;

public enum EntityTypes {

	ACTION {
		public String toString() {
			return "action";
		}
	}
	,

	AGENT {
		public String toString() {
			return "agent";
		}
		
	}
	,

	BEHAVIOUR {
		public String toString() {
			return "behaviour";
		}
	}
	,

	GENERIC {
		public String toString() {
			return "generic agent";
		}
	}
	,

	OBJECT {
		public String toString() {
			return "object";
		}		
	}
	,

	REACTIVE_RULE {
		public String toString() {
			return "reactive_rule";
		}
	}
	,

	RL_RULE {
		public String toString() {
			return "rl_rule";
		}		
	};
}
