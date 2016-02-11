package de.s2.gsim.objects;

import java.util.HashMap;

public interface AppAgent {

    String getName();

    String getNameSpace();

    void post();

    void pre(HashMap<String, Object> simProps);

}