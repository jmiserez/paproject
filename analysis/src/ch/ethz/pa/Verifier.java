package ch.ethz.pa;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.LoopNestTree;


public class Verifier {
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java -classpath soot-2.5.0.jar:./bin ch.ethz.pa.Verifier <class to test>");
			System.exit(-1);			
		}
		String analyzedClass = args[0];
		SootClass c = loadClass(analyzedClass);
		
		boolean safe = true;
		
	/* Use the following to iterate over the class methods. */
		try{
			for (SootMethod method : c.getMethods()) {
				Body b = method.retrieveActiveBody();
				LoopNestTree loops = new LoopNestTree(b);
				LineNumberAnalysis lineNumberAnalysis = new LineNumberAnalysis(new BriefUnitGraph(b));
				System.out.println("Method " + method.getName() + "(): ");
				lineNumberAnalysis.run();
				PointsToAnalysis pointerAnalysis = new PointsToAnalysis(new BriefUnitGraph(b), loops);
				ObjectSetPerVar aliases = pointerAnalysis.run();
				System.out.println("Aliases: " + aliases);
//				Analysis analysis = new Analysis(new BriefUnitGraph(b));
//				analysis.run();
				// ....
			}
		}catch(ProgramIsUnsafeException e){
			safe = false;
			System.err.println(e.getMessage());
		}
		if (safe){
			System.out.println("Program is SAFE\n");
		}else {
			System.out.println("Program is UNSAFE\n");
		}
	}
	
	private static SootClass loadClass(String name) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		return c;
	}	
}
