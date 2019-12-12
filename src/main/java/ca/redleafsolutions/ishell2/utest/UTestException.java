package ca.redleafsolutions.ishell2.utest;

public class UTestException extends Exception {
	private static final long serialVersionUID = 6471684575620428119L;

	public static class TestNotRunYet extends UTestException {
		private static final long serialVersionUID = -7830227433917732712L;
		private UTest uTest;

		public TestNotRunYet (UTest uTest) {
			this.uTest = uTest;
		}
		
		public UTest getTest () {
			return uTest;
		}
	}
}
