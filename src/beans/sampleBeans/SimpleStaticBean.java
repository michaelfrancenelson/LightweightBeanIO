package beans.sampleBeans;

import beans.AnnotatedBeanBuilder.FieldColumn;
import beans.AnnotatedBeanBuilder.Initialized;

/** For testing reading/reporting of static fields
 * 
 * @author michaelfrancenelson
 *
 */
public class SimpleStaticBean {
	@FieldColumn @Initialized public static int i1;
	@FieldColumn @Initialized public static int i2;
	@FieldColumn @Initialized public static double d1;
	@FieldColumn @Initialized public static double d2;
}
