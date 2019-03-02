package csvIO;

import java.util.List;

import org.junit.Test;

import beans.AnnotatedBeanBuilder;
import beans.AnnotatedBeanReporter;
import beans.TestBean;

public class CSVHelperTest {

	@Test
	public void test() {
		
		
		String filename = "testData/annotatedTestBean.csv";
		String filenameTr = "testData/annotatedTestBeanTransposed.csv";

		List<List<String>> ll = CSVHelper.readFile(filename);
		List<TestBean> lb = AnnotatedBeanBuilder.factory(TestBean.class, ll, false);
		
		AnnotatedBeanReporter<TestBean> rep =
				AnnotatedBeanReporter.factory(TestBean.class, "%.4f", ",", "field1", "field2");
		for (TestBean b : lb)	rep.consoleReport(b);
		
		List<List<String>> lt = CSVHelper.transpose(CSVHelper.readFile(filename));
		
		System.out.println("old row count: " + ll.size() + ", old column count: " + ll.get(0).size());
		System.out.println("new row count: " + lt.size() + ", new column count: " + lt.get(0).size());
		
		CSVHelper.writeFile(lt, filenameTr);
		
//		fail("Not yet implemented");
	}

}
