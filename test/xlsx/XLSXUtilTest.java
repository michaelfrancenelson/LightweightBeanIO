package xlsx;

import java.util.List;

import org.junit.Test;

import beans.AnnotatedBeanBuilder;
import beans.AnnotatedBeanReporter;
import beans.sampleBeans.SimpleBean;

public class XLSXUtilTest {

	@Test
	public void test() {
		
    	String filename = "testData/annotatedTestBean.xlsx";
    	
    	filename = "testData/annotatedTestBean.xlsx";

    	List<List<String>> lll = 
    	XLSXHelper.readXLSX(filename);
    	
		AnnotatedBeanReporter<SimpleBean> rep = AnnotatedBeanReporter.factory(SimpleBean.class, "%.4f", ",", "field1", "field2");
    	List<SimpleBean> lb = AnnotatedBeanBuilder.factory(SimpleBean.class, lll, false);
    
    	rep.appendListToReport(lb);
    	
    		for (int i = 0; i < lll.size(); i++)
    	{
//    		System.out.println(lll.get(i).get(5));
//    		for (String s : l1) System.out.print(s + "  ");
//    		System.out.println();
    	}
    	
//    	for (TestBean tb : lb) rep.consoleReport(tb); 
		
//		fail("Not yet implemented");
	}
}
