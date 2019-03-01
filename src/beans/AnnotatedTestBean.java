package beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Test class for the bean reader/writers
 * 
 * @author michaelfrancenelson
 *
 */
public class AnnotatedTestBean extends AnnotatedCSVBean
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

	public static void main(String[] args) {
		
		AnnotatedBeanCSVReporter<AnnotatedTestBean> rep =
				AnnotatedBeanCSVReporter.factory(AnnotatedTestBean.class, "%.4f", ",", "field1", "field2");
		
		List<AnnotatedTestBean> ll = randomFactory(5);
		
		System.out.println(rep.headerReportLine());
		System.out.println(rep.stringReportLine(ll.get(0)));
		
		rep.appendListToReport(ll, 9, "bb");
		rep.write("testOutput/annotatedTestBean.csv");
	}
	
	
	public static List<AnnotatedTestBean> randomFactory(int n)
	{
		List<AnnotatedTestBean> l = new ArrayList<>();
		for (int i = 0; i < n; i++) l.add(randomFactory());
		return l;
	}
	
	public static AnnotatedTestBean randomFactory()
	{
		AnnotatedTestBean b = new AnnotatedTestBean();
		b.d = r.nextDouble();
		b.i = r.nextInt(200) - 100;
		b.dd = r.nextDouble() * 20.0 + 100.0;
		b.ii = r.nextInt(100) + 1000;
		b.c = (char)(r.nextInt('z' - 'a') + 'a');
		b.s = randomString(r.nextInt(12) + 1, '9', 'Z');
		if (r.nextDouble() < 0.5) { b.b = true; b.bb = false;}
		else {b.b = false; b.bb = true;}
		return b;
	}
}
