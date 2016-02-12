package de.s2.gsim.objects;

import java.io.Serializable;

public interface Condition extends Serializable {

    public String getOperator() throws GSimObjectException;

    public String getParameterName() throws GSimObjectException;

    public String getParameterValue() throws GSimObjectException;

    public void setOperator(String str) throws GSimObjectException;

    public void setParameterName(String str) throws GSimObjectException;

    public void setParameterValue(String str) throws GSimObjectException;

}
