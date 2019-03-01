package beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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

		AnnotatedBeanReporter.factory(TestBean.class, "%.4f", ",", "field1", "field2");

		List<TestBean> lcT =	AnnotatedBeanBuilder.factory(TestBean.class, fileCSVT);
		List<TestBean> lxT =	AnnotatedBeanBuilder.factory(TestBean.class, fileXLSXT);

		List<TestBean> lc =	AnnotatedBeanBuilder.factory(TestBean.class, fileCSV);
		List<TestBean> lx =	AnnotatedBeanBuilder.factory(TestBean.class, fileXLSX);
		assertEquals(lc.size(), lx.size());

		assertEquals(lcT.size(), lxT.size());
		assertEquals(lc.size(), lxT.size());

		/* Test that objects read from xlsx and csv files with the same data are identical. */
		for (int i = 0; i < lc.size(); i++)
		{
			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lc.get(i), lc.get(i)));
			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lc.get(i), lx.get(i)));

			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lc.get(i), lxT.get(i)));

			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lcT.get(i), lcT.get(i)));
			assertTrue(AnnotatedBeanBuilder.equals(TestBean.class, lcT.get(i), lxT.get(i)));
		}
	}

	@Test
	public void testStaticReader()
	{
		String fileX = "testData/TestStaticFieldBean.xlsx";
		String fileC = "testData/TestStaticFieldBean.csv";
		String fileSave = "testData/TestStaticFieldBeanSave.csv";

		AnnotatedBeanReporter<TestStaticFieldBean> rep = AnnotatedBeanReporter.factory(TestStaticFieldBean.class, "0.4f", ",", "sim", "batch");

		AnnotatedBeanBuilder.factory(TestStaticFieldBean.class, fileX);
		List<String> s1 = AnnotatedBeanReporter.staticStringValReport(TestStaticFieldBean.class, "%.4f");
		rep.appendStaticToReport(TestStaticFieldBean.class, "a", 1);

		AnnotatedBeanBuilder.factory(TestStaticFieldBean.class, fileC);
		List<String> s2 = AnnotatedBeanReporter.staticStringValReport(TestStaticFieldBean.class, "%.4f");
		rep.appendStaticToReport(TestStaticFieldBean.class, "b", 2);

		TestStaticFieldBean.d1 -= .1;
		TestStaticFieldBean.d2 -= .1;
		TestStaticFieldBean.i1 += 2;
		TestStaticFieldBean.i2 += 2;
		rep.appendStaticToReport(TestStaticFieldBean.class, "c", 3);

		List<String> s3 = AnnotatedBeanReporter.staticStringValReport(TestStaticFieldBean.class, "%.4f");

		for (int i = 0; i < s1.size(); i++)
		{
			assertEquals(s1.get(i), s2.get(i));
			assertNotEquals(s1.get(i), s3.get(i));
		}

		rep.writeCSV(fileSave);

		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s3);
	}

}
