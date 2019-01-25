
package ejemplos;
import static es.urjc.etsii.code.concurrency.SimpleConcurrent.*;



public class ejemplo2 {
	static volatile double x;

	public static void inc() {
		x = x + 1;
	}

	public static void dec() {
		x = x - 1;
	}

	public static void main(String[] args) {
		x = 0;

		createThread("inc");
		createThread("dec");
		startThreadsAndWait();

		println("x:" + x);
	}
}
