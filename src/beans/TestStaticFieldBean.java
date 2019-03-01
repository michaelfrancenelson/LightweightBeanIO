package beans;

import beans.AnnotatedBeanBuilder.FieldColumn;

/** For testing reading/reporting of static fields
 * 
 * @author michaelfrancenelson
 *
 */
public class TestStaticFieldBean {
	@FieldColumn public static int i1;
	@FieldColumn public static int i2;
	@FieldColumn public static double d1;
	@FieldColumn public static double d2;
}
