package de.s2.gsim.environment;

import de.s2.gsim.objects.Path;

public class CommonFunctions {
	
	private CommonFunctions() {
		//prevent extending this class
	}
	
	public static <T extends Frame> boolean existsPath(T containingFrame, Path<?> path) {
    	try {
    		containingFrame.resolvePath(path, false);
    		return true;
    	} catch (GSimDefException e) {
    		return false;
    	}
	}

}
