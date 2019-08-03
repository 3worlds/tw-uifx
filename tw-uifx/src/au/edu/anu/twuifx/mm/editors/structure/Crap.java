package au.edu.anu.twuifx.mm.editors.structure;

import fr.cnrs.iees.io.parsing.ValidPropertyTypes;
import fr.cnrs.iees.twcore.constants.DataElementType;
import fr.cnrs.iees.twcore.constants.DateTimeType;
import fr.cnrs.iees.twcore.constants.ExperimentDesignType;
import fr.cnrs.iees.twcore.constants.FileType;
import fr.cnrs.iees.twcore.constants.Grouping;
import fr.cnrs.iees.twcore.constants.LifespanType;
import fr.cnrs.iees.twcore.constants.SnippetLocation;
import fr.cnrs.iees.twcore.constants.StatisticalAggregates;
import fr.cnrs.iees.twcore.constants.TabLayoutTypes;
import fr.cnrs.iees.twcore.constants.TimeScaleType;
import fr.cnrs.iees.twcore.constants.TimeUnits;
import fr.cnrs.iees.twcore.constants.TwFunctionTypes;
import fr.cnrs.iees.twcore.constants.UIContainers;

public class Crap {

	public static void main(String[] args) {
		DataElementType det=DataElementType.defaultValue();
		ExperimentDesignType edt=ExperimentDesignType.defaultValue();
		Grouping g= Grouping.defaultValue();
		LifespanType lst = LifespanType.defaultValue();
		//SnippetLocation sl= SnippetLocation.defaultValue();
		StatisticalAggregates sa = StatisticalAggregates.defaultValue();
		//TabLayoutTypes tlt = TabLayoutTypes.defaultValue();
		TimeScaleType tst = TimeScaleType.defaultValue();
		TimeUnits tu = TimeUnits.defaultValue();
		TwFunctionTypes twft=TwFunctionTypes.defaultValue();
		//UIContainers uic =UIContainers.defaultValue();
		FileType ft = FileType.defaultValue();
		DateTimeType dtt = DateTimeType.defaultValue();
	
		ValidPropertyTypes.listTypes();

	}

}
