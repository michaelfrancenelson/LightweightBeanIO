package beans;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class AnnotatedBeanInitializer extends AnnotatedBeanBuilder
{

	public static final int     NA_INT      = Integer.MIN_VALUE;
	public static final double  NA_DOUBLE   = Double.MIN_VALUE;
	public static final String  NA_STRING   = "NA";
	public static final char    NA_CHAR     = (char)'0';

	public static <T> void initializeStaticFieldsToNA(Class<T> clazz)
	{
		boolean inst = false, stat = true;
		initializeFieldsToNA(clazz, null, inst, stat); 
	}

	public static <T> void initializeStaticFieldsToNA(Class<T> clazz,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	{
		boolean inst = false, stat = true;
		initializeFieldsToNA(clazz, null, inst, stat, naInt, naDouble, naString, naChar); 
	}

	public static <T> void initializeInstanceFieldsToNA(Class<T> clazz, T t)
	{
		boolean inst = true, stat = false;
		initializeFieldsToNA(clazz, t, inst, stat);
	}

	public static <T> void initializeInstanceFieldsToNA(Class<T> clazz, T t,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	{
		boolean inst = true, stat = false;
		initializeFieldsToNA(clazz, t, inst, stat, naInt, naDouble, naString, naChar); 
	}

	/**
	 * 
	 * @param clazz class of the bean
	 * @param t instance of T (null for initializing only static fields)
	 * @param <T>
	 */
	private static <T> void initializeFieldsToNA(Class<T> clazz, T t, boolean setInstance, boolean setStatic)
	{ initializeFieldsToNA(clazz, t, setInstance, setStatic, NA_INT, NA_DOUBLE, NA_STRING, 
			NA_CHAR); }

	/**
	 * 
	 * @param clazz class of the bean
	 * @param t instance of T (null for initializing only static fields)
	 * @param naInt NA for integers
	 * @param naDouble NA for doubles
	 * @param naString NA for strings
	 * @param naChar NA for char
	 * @param <T>
	 */
	private static <T> void initializeFieldsToNA(Class<T> clazz, T t,
			boolean setInstance, boolean setStatic,
			int naInt, double naDouble, String naString,
			char naChar)
	{
		List<Field> fields = new ArrayList<>();
		for (Field f : clazz.getDeclaredFields())
		{	f.setAccessible(true);
		if (f.isAnnotationPresent(Initialized.class)) fields.add(f);}

		getAnnotatedFields(clazz);
		for (Field f : fields)
			try {

				boolean isInstance, isStatic;
				isStatic = Modifier.isStatic(f.getModifiers());
				isInstance = !isStatic;

				if ( (isStatic && setStatic) || (isInstance && setInstance))
				{

					setNA(t, f,
							naInt, naDouble, naString, 
							naChar);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
	}

	public static <T> void setNA(T t, Field f, 
			int naInt, double naDouble, String naString,
			char naChar) throws IllegalArgumentException, IllegalAccessException
	{
		f.setAccessible(true);

		String type = f.getType().getSimpleName();

		switch(type)
		{
		case("int"):     { f.setInt(t, naInt); break;}
		case("double"):  { f.setDouble(t, naDouble); break; }
		case("boolean"): { break; } /* It doesn't make sense to have a na value for the primitive bool... */
		//			case("boolean"): { f.setBoolean(t, naBoolean); break; }

		case("String"):  { f.set(t, naString); break; }
		case("char"):    { f.setChar(t, naChar); break;}

		case("Integer"): { f.set(t, (Integer) naInt); break; }
		case("Double"):  { f.set(t, (Double) naDouble); break; }
		case("Boolean"): { f.set(t, (Boolean) null); break; }
		default:         { f.set(t, null); break; }
		}
	}


	public static <T> boolean checkInstanceInitialized(Class<T> clazz, T t)
	{ return checkInitialized(clazz, t, true, false); }
	
	public static <T> boolean checkInstanceInitialized(Class<T> clazz, T t,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	{ return checkInitialized(clazz, t, true, false, naInt, naDouble, naString,
			naChar); }
	
	
	public static <T> boolean checkStaticInitialized(Class<T> clazz)
	{ return checkInitialized(clazz, null, false, true); }

	public static <T> boolean checkStaticInitialized(Class<T> clazz,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	{ return checkInitialized(clazz, null, false, true, naInt, naDouble, naString,
			naChar); }

	
	private static <T> boolean checkInitialized(Class<T> clazz, T t, 
			boolean checkInstance, boolean checkStatic)
	{ return checkInitialized(clazz, t, checkInstance, checkStatic, NA_INT, NA_DOUBLE, NA_STRING, 
			NA_CHAR); }

	private static <T> boolean checkInitialized(Class<T> clazz, T t, 
			boolean checkInstance, boolean checkStatic,
			int naInt, double naDouble, String naString,
			char naChar)
	{
		List<Field> fields = getAnnotatedFields(clazz, Initialized.class);
		for (Field f : fields)
		{

			boolean isInstance, isStatic;

			isStatic = Modifier.isStatic(f.getModifiers());
			isInstance = !isStatic;

			if ( (isStatic && checkStatic) || (isInstance && checkInstance))
			{

				try {
					if (AnnotatedBeanReporter.isNA(clazz, t, f, naInt, naDouble, naString, naChar)) 
					{
//						logger.debug("Field: " + f.getName() + " (" + f.getType().getSimpleName() 
//								+ ", " + f.get(t).toString() + ") is not initialized."); 
						return false;
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
}
