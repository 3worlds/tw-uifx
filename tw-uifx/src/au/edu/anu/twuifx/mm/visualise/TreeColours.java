package au.edu.anu.twuifx.mm.visualise;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.cnrs.iees.twcore.constants.Configuration;
import javafx.scene.paint.Color;

public class TreeColours implements Configuration{
	private static Set<String> labelSet = new HashSet<>();
	static {
		labelSet.add(N_ECOLOGY);
		labelSet.add(N_CODESOURCE);
		labelSet.add(N_DATAIO);
		labelSet.add(N_EXPERIMENT);
		labelSet.add(N_UI);		
	}
	private static Map<String,Color> nodeColours= new HashMap<>();
	static {
		nodeColours.put(N_ECOLOGY, Color.CHARTREUSE);
		nodeColours.put(N_CODESOURCE, Color.LIGHTGREY);
		nodeColours.put(N_DATAIO, Color.SKYBLUE);
		nodeColours.put(N_EXPERIMENT, Color.GOLDENROD);
		nodeColours.put(N_UI, Color.THISTLE);
	}
	public static Color getCategoryColor(String key){
		if (!nodeColours.containsKey(key))
			return Color.BLACK;
		return nodeColours.get(key);
	}

}
