package de.s2.gsim.environment;

import java.util.List;

import de.s2.gsim.objects.attribute.DomainAttribute;

public class CommonFunctions {
	
	private CommonFunctions() {
		//prevent extending this class
	}
	
    public static boolean existsPath(Frame containingFrame, Path<List<DomainAttribute>> path) {
    	try {
    		containingFrame.resolvePath(path, false);
    		return true;
    	} catch (GSimDefException e) {
    		return false;
    	}
	}

}
