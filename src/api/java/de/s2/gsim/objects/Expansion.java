package de.s2.gsim.objects;

import java.io.Serializable;

public interface Expansion extends Serializable {

    public void addFiller(String filler) throws GSimObjectException;

    public String[] getFillers() throws GSimObjectException;

    public String getMax() throws GSimObjectException;

    public String getMin() throws GSimObjectException;

    public String getParameterName() throws GSimObjectException;

    public boolean isNumerical() throws GSimObjectException;

    public void setMax(String parameterValue) throws GSimObjectException;

    public void setMin(String parameterValue) throws GSimObjectException;

    public void setParameterName(String parameterName) throws GSimObjectException;

}
