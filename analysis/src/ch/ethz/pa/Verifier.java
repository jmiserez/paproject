package ch.ethz.pa;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;


public class Verifier {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java -classpath soot-2.5.0.jar:./bin ch.ethz.pa.Verifier <class to test>");
			System.exit(-1);			
		}
		String analyzedClass = args[0];
		SootClass c = loadClass(analyzedClass);

	/* Use the following to iterate over the class methods. */
		for (SootMethod method : c.getMethods()) {
			Analysis analysis = new Analysis(new BriefUnitGraph(method.retrieveActiveBody()));
			analysis.run();
			// ....
		}
		// This will be called if none of the analysis methods exits
		System.out.println("Program is SAFE\n");
	}
	
	private static SootClass loadClass(String name) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		return c;
	}	
}
