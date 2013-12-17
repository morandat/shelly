package demo;

import fr.labri.shelly.Parser;
import fr.labri.shelly.Shell;
import fr.labri.shelly.ShellyException;
import fr.labri.shelly.annotations.Command;
import fr.labri.shelly.annotations.Error;
import fr.labri.shelly.annotations.Group;
import fr.labri.shelly.annotations.Option;
import fr.labri.shelly.impl.HelpFactory;

@Group
public class TestStatic {
	
	@Option	public static boolean s0;
	@Option	public boolean n0;
	
	@Group
	public class Test1 {
		@Option	public boolean n1;

		@Command
		public void foo () {
			printInfo(getClass());
			System.out.printf("%b %b %b\n", s0, TestStatic.this.n0, n1);
		}
		
		@Group
		public class Test7 {
			@Option	public boolean n7;

			@Command
			public void foo () {
				printInfo(getClass());
				System.out.printf("%b %b %b %b\n", s0, TestStatic.this.n0,Test1.this.n1, n7);
			}	
		}
	}
	@Command
	public static void foo1() {
		printInfo(Test2.class);
	}
	@Command
	public void foo () {
		printInfo(getClass());
		System.out.printf("%b %b\n", s0, n0);
	}

	@Group
	public static class Test2 {
		@Option	public static boolean s2;
		@Option	public boolean n2;

		@Command
		public static void foo1() {
			printInfo(Test2.class);
		}
		@Command
		public void foo () {
			printInfo(getClass());
			System.out.printf("%b %b\n", s2, n2);
		}

		@Group
		public class Test3 {
			@Option	public boolean n3;

			//			static void foo1() {
//				
//			}
			@Command
			public void foo () {
				printInfo(getClass());
				System.out.printf("%b %b %b\n", Test2.s2, Test2.this.n2, n3);
			}
				
//			static class Test4 {
//				
//			}
			@Group
			public class Test5 {
				@Option	public boolean n5;
				
				@Command
				public void foo () {
					printInfo(getClass());
					System.out.printf("%b %b %b %b\n", Test2.s2, Test2.this.n2, Test3.this.n3, n5);
				}
			}
		}
		@Group
		public static class Test4 {
			@Option	public static boolean s4;
			@Option	public boolean n4;

			@Command
			public static void foo1 () {
				printInfo(Test4.class);
			}
			@Command
			public void foo () {
				printInfo(getClass());
				System.out.printf("%b %b %b\n", Test2.s2, s4, n4);
			}
			@Group
			public static class Test5 {
				@Option	public static boolean s5;
				@Option	public boolean n5;

				@Command
				public static void foo1 () {
					printInfo(Test5.class);
				}
				@Command
				public static void foo2 () {
					foo1();
				}
				@Command
				public void foo () {
					printInfo(getClass());
					System.out.printf("%b %b %b\n", Test2.Test4.s4, n5, s5);
				}

			}
			@Group
			public class Test6 {
				@Option	public boolean n6;

				@Command
				public void foo () {
					printInfo(getClass());
					System.out.printf("%b %b %b %b %b\n", Test2.Test4.this.n4, Test2.Test4.this.s4, Test4.this.n4, Test4.this.s4, n6);
				}
			}
		}
	}
	
	@Error
	@Option(factory=HelpFactory.Factory.class)
	@Command(factory=HelpFactory.Factory.class)
	public void help(ShellyException e, String [] args) throws Exception {
		System.err.println(e);
		e.printStackTrace(System.err);
		HelpFactory.printHelp(Shell.createShell(TestStatic.class).getRoot(), System.err);
		throw e;
	}
		
	public static void printInfo(Class<?> clazz) {
		System.out.printf(">> %s\n", clazz.getName());
		System.out.printf("%s\n%s\n%b\n", clazz.getEnclosingClass(), clazz.getDeclaringClass(), clazz.isMemberClass());
	}
	
	public static void main(String[] args) throws Exception {
		Shell.createShell(TestStatic.class).loop(System.in, Parser.GNUNonStrict);;
	}
}
