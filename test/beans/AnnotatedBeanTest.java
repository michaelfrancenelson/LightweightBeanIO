package beans;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class AnnotatedBeanTest {


	@Test
	public void testFileFactories() 
	{

		String fileCSV = "testData/TestBeanFactory.csv";
		String fileXLSX = "testData/TestBeanFactory.xlsx";
		String fileCSVT = "testData/TestBeanFactoryTransposed.csv";
		String fileXLSXT = "testData/TestBeanFactoryTransposed.xlsx";

		AnnotatedBeanReporter<TestBean> rep =
				AnnotatedBeanReporter.factory(TestBean.class, "%.4f", ",", "field1", "field2");

		List<TestBean> lcT =	AnnotatedBeanBuilder.csvFactory(TestBean.class, fileCSVT, true);
		List<TestBean> lxT =	AnnotatedBeanBuilder.xlsxFactory(TestBean.class, fileXLSXT, true);

		List<TestBean> lc =	AnnotatedBeanBuilder.csvFactory(TestBean.class, fileCSV);
		List<TestBean> lx =	AnnotatedBeanBuilder.xlsxFactory(TestBean.class, fileXLSX);
		assertEquals(lc.size(), lx.size());
		

		assertEquals(lcT.size(), lxT.size());
		assertEquals(lc.size(), lxT.size());
		
		/* Test that objects read from xlsx and csv files with the same data are identical. */
		for (int i = 0; i < lc.size(); i++)
		{
			System.out.println(i);
			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lc.get(i), lc.get(i)));
			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lc.get(i), lx.get(i)));

			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lc.get(i), lxT.get(i)));

			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lcT.get(i), lcT.get(i)));
			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lcT.get(i), lxT.get(i)));
		}

	}

}
