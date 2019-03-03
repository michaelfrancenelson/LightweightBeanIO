package beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import beans.sampleBeans.SimpleBean;
import beans.sampleBeans.SimpleParamBean;
import beans.sampleBeans.SimpleStaticBean;

public class AnnotatedBeanTest {

	@Test
	public void testFileFactories() 
	{
		String fileCSV = "testData/TestBeanFactory.csv";
		String fileXLSX = "testData/TestBeanFactory.xlsx";
		String fileCSVT = "testData/TestBeanFactoryTransposed.csv";
		String fileXLSXT = "testData/TestBeanFactoryTransposed.xlsx";

		AnnotatedBeanReporter.factory(SimpleBean.class, "%.4f", ",", "field1", "field2");

		List<SimpleBean> lcT =	AnnotatedBeanBuilder.factory(SimpleBean.class, fileCSVT);
		List<SimpleBean> lxT =	AnnotatedBeanBuilder.factory(SimpleBean.class, fileXLSXT);

		List<SimpleBean> lc =	AnnotatedBeanBuilder.factory(SimpleBean.class, fileCSV);
		List<SimpleBean> lx =	AnnotatedBeanBuilder.factory(SimpleBean.class, fileXLSX);
		assertEquals(lc.size(), lx.size());

		assertEquals(lcT.size(), lxT.size());
		assertEquals(lc.size(), lxT.size());

		/* Test that objects read from xlsx and csv files with the same data are identical. */
		for (int i = 0; i < lc.size(); i++)
		{
			assertTrue(AnnotatedBeanBuilder.equals(SimpleBean.class, lc.get(i), lc.get(i)));
			assertTrue(AnnotatedBeanBuilder.equals(SimpleBean.class, lc.get(i), lx.get(i)));

			assertTrue(AnnotatedBeanBuilder.equals(SimpleBean.class, lc.get(i), lxT.get(i)));

			assertTrue(AnnotatedBeanBuilder.equals(SimpleBean.class, lcT.get(i), lcT.get(i)));
			assertTrue(AnnotatedBeanBuilder.equals(SimpleBean.class, lcT.get(i), lxT.get(i)));
		}
	}

	@Test
	public void testStaticReader()
	{
		String fileX = "testData/TestStaticFieldBean.xlsx";
		String fileC = "testData/TestStaticFieldBean.csv";
		String fileSave = "testData/TestStaticFieldBeanSave.csv";
		
		AnnotatedBeanReporter<SimpleStaticBean> rep = AnnotatedBeanReporter.factory(SimpleStaticBean.class, "0.4f", ",", "sim", "batch");

		AnnotatedBeanBuilder.factory(SimpleStaticBean.class, fileX);
		List<String> s1 = AnnotatedBeanReporter.staticStringValReport(SimpleStaticBean.class, "%.4f");
		rep.appendStaticToReport(SimpleStaticBean.class, "a", 1);

		AnnotatedBeanBuilder.factory(SimpleStaticBean.class, fileC);
		List<String> s2 = AnnotatedBeanReporter.staticStringValReport(SimpleStaticBean.class, "%.4f");
		rep.appendStaticToReport(SimpleStaticBean.class, "b", 2);

		SimpleStaticBean.d1 -= .1;
		SimpleStaticBean.d2 -= .1;
		SimpleStaticBean.i1 += 2;
		SimpleStaticBean.i2 += 2;
		rep.appendStaticToReport(SimpleStaticBean.class, "c", 3);

		List<String> s3 = AnnotatedBeanReporter.staticStringValReport(SimpleStaticBean.class, "%.4f");

		for (int i = 0; i < s1.size(); i++)
		{
			assertEquals(s1.get(i), s2.get(i));
			assertNotEquals(s1.get(i), s3.get(i));
		}

		String fileUnequal = "testData/TestStaticFieldBeanUnequalRows.xlsx";
		AnnotatedBeanBuilder.factory(SimpleStaticBean.class, fileUnequal, true);
		
//		List<String> s4 = AnnotatedBeanReporter.staticStringValReport(SimpleStaticBean.class, "%.4f");
//		System.out.println(s1);
//		System.out.println(s2);
//		System.out.println(s3);
//		System.out.println(s4);

		rep.appendStaticToReport(SimpleStaticBean.class, "d", 4);
		rep.writeCSV(fileSave);
	}

	@Test
	public void testInitilize()
	{
		String fileXLSXT = "testData/TestBeanFactoryTransposed.xlsx";
		List<SimpleBean> lxT =	AnnotatedBeanBuilder.factory(SimpleBean.class, fileXLSXT);
		for (SimpleBean t : lxT)
		{
			assertTrue(AnnotatedBeanInitializer.checkInstanceInitialized(SimpleBean.class, t));
			AnnotatedBeanInitializer.initializeInstanceFieldsToNA(SimpleBean.class, t);
			assertFalse(AnnotatedBeanInitializer.checkInstanceInitialized(SimpleBean.class, t));
		}
		
		String fileC = "testData/TestStaticFieldBean.csv";
		AnnotatedBeanBuilder.factory(SimpleStaticBean.class, fileC);
		/* Should be true since all the initialized fields are also @FieldColumn s. */
		assertTrue(AnnotatedBeanInitializer.checkStaticInitialized(SimpleStaticBean.class));
		
		AnnotatedBeanInitializer.initializeStaticFieldsToNA(SimpleParamBean.class);
		assertFalse(AnnotatedBeanInitializer.checkStaticInitialized(SimpleParamBean.class));
		
		
		String filename = "testData/params.xlsx";
		AnnotatedBeanBuilder.factory(SimpleParamBean.class, filename, true);
		/* Should be false because the class has initialized params that aren't read from the file. 
		 * Some of the @Initialized are not @FieldColumn */
		assertFalse(AnnotatedBeanInitializer.checkStaticInitialized(SimpleParamBean.class));

		SimpleParamBean.setFromFile(filename);
		assertTrue(AnnotatedBeanInitializer.checkStaticInitialized(SimpleParamBean.class));
	}
}
