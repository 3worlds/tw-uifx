package hello;

/**
 * OpenMole test case: Testing independence of memory space through a static
 * variable. It appears that when multiple instances of this class are run by
 * OpenMole the process id is the same and the memory space is shared.
 * 
 * Do we need the singularity container system?: Singularity: some OpenMOLE
 * tasks require the Singularity container system. You must install it on your
 * system if you want to use some tasks such as Python, R, Scilab, Container.
 * 
 * @author Ian Davies - 26 Oct. 2022
 *
 */
public class Hello {
//	public static void main(String[] args) {
//		run(0);
//	}

	public static long testNumber = -1;

	public static void run(int arg) {
		System.out.println("arg: " + arg);
		System.out.println("PID: " + ProcessHandle.current().pid());
		if (testNumber != -1L)
			throw new IllegalStateException("TestNumber: " + testNumber);
		testNumber++;
	}
}
