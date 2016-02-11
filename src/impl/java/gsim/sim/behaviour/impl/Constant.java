package gsim.sim.behaviour.impl;

public class Constant implements java.io.Serializable {

    private static final long serialVersionUID = 7594202672886051100L;

    private String name = "test";

    private String value = "";

    public Constant(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Constant) {
            Constant c = (Constant) o;
            return c.getValue().equals(getValue()) && c.getName().equals(getName());
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return 89;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
