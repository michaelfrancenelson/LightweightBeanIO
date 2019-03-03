package beans.sampleBeans;

import beans.AnnotatedBeanBuilder.FieldColumn;
import beans.AnnotatedBeanBuilder.Initialized;

/** Test class for the bean reader/writers
 * 
 * @author michaelfrancenelson
 *
 */
public class SimpleBean
{
	@FieldColumn @Initialized public static int iSt = -12345;
	@FieldColumn @Initialized private static double dSt = 0.987654321;
	
	@FieldColumn @Initialized private int i;
	@FieldColumn @Initialized private double d;
	@FieldColumn @Initialized private Integer ii;
	@FieldColumn @Initialized private Double dd;
	@FieldColumn @Initialized private char c;
	@FieldColumn @Initialized private String s;
	@FieldColumn @Initialized private boolean b;
	@FieldColumn @Initialized private Boolean bb;
}
