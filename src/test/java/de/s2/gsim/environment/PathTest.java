package de.s2.gsim.environment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.junit.Test;

import de.s2.gsim.objects.attribute.AttributeType;
import de.s2.gsim.objects.attribute.DomainAttribute;

public class PathTest {

    @Test
    public void verify_to_string() {
        Path<DomainAttribute> path = Path.attributePath("list-1", "C1", "list-2", "C2", "attr-list", "attr");

        String pathString = path.toString();
        assertThat("Path string is returned", pathString, notNullValue());
        assertThat("Path string matches expected value", pathString, equalTo("/list-1/C1/list-2/C2/attr-list/attr"));

    }

    @Test
    public void verify_domain_attribute_path_flat() {

        String attrName = "attr";
        Path<DomainAttribute> path = Path.attributePath("list-1", "C1", "list-2", "C2", "attr-list", attrName);

        Frame f = new Frame("Test", "");
        Frame c1 = new Frame("C1", "");
        Frame c2 = new Frame("C2", "");
        DomainAttribute a1 = new DomainAttribute("attr", AttributeType.STRING);
        a1.setDefault("default-value");
        c2.addOrSetAttribute("attr-list", a1);
        c1.addChildFrame("list-2", c2);
        f.addChildFrame("list-1", c1);

        DomainAttribute k = f.resolvePath(path);

        assertThat("Attribute is found", k, notNullValue());
        assertThat("Attribute matches the expected one", k.getName(), equalTo(attrName));

    }

    @Test
    public void verify_domain_attribute_path_via_inheritance() {
        String attrName = "attr";
        Path<DomainAttribute> path = Path.attributePath("list-2", "C2", "attr-list", attrName);

        Frame f = new Frame("Test", "");
        Frame c1 = new Frame(new Frame[] { f }, "C1", "");
        Frame c2 = new Frame("C2", "");
        DomainAttribute a1 = new DomainAttribute("attr", AttributeType.STRING);
        a1.setDefault("default-value");
        c2.addOrSetAttribute("attr-list", a1);
        f.addChildFrame("list-2", c2);

        DomainAttribute k = c1.resolvePath(path);

        assertThat("Attribute is found", k, notNullValue());
        assertThat("Attribute matches the expected one", k.getName(), equalTo(attrName));

    }

    @Test
    public void verify_resolve_domain_attr_list_path() {

        String listName = "list";
        Path<List<DomainAttribute>> path = Path.attributePath("list-1", "C1", listName);

        Frame f = new Frame("Test", "");
        Frame c1 = new Frame("C1", "");
        DomainAttribute a1 = new DomainAttribute("attr", AttributeType.STRING);
        DomainAttribute a2 = new DomainAttribute("attr2", AttributeType.STRING);
        a1.setDefault("default-value");
        c1.addOrSetAttribute(listName, a1);
        c1.addOrSetAttribute(listName, a2);
        f.addChildFrame("list-1", c1);

        List<DomainAttribute> list = f.resolvePath(path);

        assertThat("Attribute list is found", list, notNullValue());
        assertThat("Attribute is in list", list, hasItem(a1));

    }

    @Test
    public void verify_resolve_object_class_list_path() {

        String listName = "list-1";
        String objectName = "C1";
        Path<TypedList> path = Path.objectListPath(listName);

        Frame f = new Frame("Test", "");
        Frame c1 = new Frame(objectName, "");
        f.addChildFrame(listName, c1);

        TypedList list = f.resolvePath(path);

        assertThat("Object list is found", list, notNullValue());
        assertThat("Object is in list", list, hasItem(c1));

    }

    @Test
    public void verify_resolve_object_class_path() {

        String listName = "list-1";
        String objectName = "C1";
        Path<Frame> path = Path.objectPath(listName, objectName);

        Frame f = new Frame("Test", "");
        Frame c1 = new Frame(objectName, "");
        f.addChildFrame(listName, c1);

        Frame child = f.resolvePath(path);

        assertThat("Object is found", child, notNullValue());
        assertThat("Object is identical", child, equalTo(c1));

    }

    @Test(expected = GSimDefException.class)
    public void incorrect_path_throws_gsimDef_exception() {

        Path<List<DomainAttribute>> path = Path.attributePath("list-1", "C1", "listName");

        Frame f = new Frame("Test", "");
        Frame c1 = new Frame("C1", "");
        f.addChildFrame("list-1", c1);

        List<DomainAttribute> list = f.resolvePath(path);

        assertThat("Attribute is found", list, notNullValue());

    }

}
