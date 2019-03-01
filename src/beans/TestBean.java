package beans;

import java.util.Random;

import beans.AnnotatedBeanBuilder.FieldColumn;

/** Test class for the bean reader/writers
 * 
 * @author michaelfrancenelson
 *
 */
public class TestBean
{
	static Random r = new Random();
	@FieldColumn public static int iSt = -12345;
	@FieldColumn private static double dSt = 0.987654321;
	
	@FieldColumn private int i;
	@FieldColumn private double d;
	@FieldColumn private Integer ii;
	@FieldColumn private Double dd;
	@FieldColumn private char c;
	@FieldColumn private String s;
	@FieldColumn private boolean b;
	@FieldColumn private Boolean bb;

//	public static void main(String[] args) {
//		
//		AnnotatedBeanCSVReporter<TestBean> rep =
//				AnnotatedBeanCSVReporter.factory(TestBean.class, "%.4f", ",", "field1", "field2");
//		
//		List<TestBean> ll = randomFactory(5);
//		
//		System.out.println(rep.headerReportLine());
//		System.out.println(rep.stringReportLine(ll.get(0)));
//		
//		rep.appendListToReport(ll, 9, "bb");
//		rep.write("testOutput/annotatedTestBean.csv");
//	}
}
