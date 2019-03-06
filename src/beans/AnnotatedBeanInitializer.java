package beans;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Tools for initializing and checking initialization status of annotated beans.
 *  This has gotten much more boilerplate-y than I had hoped.
 * 
 * @author michaelfrancenelson
 */
public class AnnotatedBeanInitializer extends AnnotatedBeanBuilder
{

	public static final int     NA_INT      = Integer.MIN_VALUE;
	public static final double  NA_DOUBLE   = Double.MIN_VALUE;
	public static final String  NA_STRING   = "NA";
	public static final char    NA_CHAR     = (char)'0';

	/* With the massively overloaded methods below, there are lots of opportunities
	 *  for misunderstandings in the calls below so hopefully these will help clarify. */
	private static boolean yesInstance = true, noInstance = false, yesStatic = true, noStatic = false;
	private static boolean yesEnforce = true, noEnforce = false;



	public static <T> void initializeStaticFieldsToNA(Class<T> clazz)
	{ initializeFieldsToNA(clazz, null); }

	public static <T> void initializeInstanceFieldsToNA(Class<T> clazz, T t)
	{ initializeFieldsToNA(clazz, t); }



	/* Instance checkers. */
	public static <T> boolean checkInstanceInitialized(Class<T> clazz, T t)
	{ return isInitialized(clazz, t, noEnforce, NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR); }

	public static <T> boolean checkInstanceInitialized( Class<T> clazz, T t, int naInt, double naDouble, String naString, char naChar)
	{ return isInitialized(clazz, t, noEnforce, naInt, naDouble, naString, naChar); }

	public static <T> boolean checkInstanceInitialized(Class<T> clazz, Iterable<T> t)
	{ return areInitialized(clazz, t, noEnforce, NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR); }
	
	public static <T> boolean checkInstanceInitialized( Class<T> clazz, Iterable<T> t, int naInt, double naDouble, String naString, char naChar)
	{ return areInitialized(clazz, t, noEnforce, naInt, naDouble, naString, naChar); }
	
	

	/* Static checkers */
	public static <T> boolean checkStaticInitialized(Class<T> clazz)
	{ return isInitialized(clazz, null, noEnforce, NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR); }

	public static <T> boolean checkStaticInitialized( Class<T> clazz, int naInt, double naDouble, String naString,char naChar)
	{ return isInitialized(clazz, null, noEnforce,naInt, naDouble, naString, naChar); }


	
	/* Instance enforcers. */
	public static <T> boolean enforceInstanceInitialized(Class<T> clazz, T t)
	{ return isInitialized(clazz, t, yesEnforce, NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR); }

	public static <T> boolean enforceInstanceInitialized(Class<T> clazz, T t, int naInt, double naDouble, String naString, char naChar)
	{ return isInitialized(clazz, t, yesEnforce, naInt, naDouble, naString, naChar); }

	public static <T> boolean enforceInstanceInitialized(Class<T> clazz, Iterable<T> t)
	{ return areInitialized(clazz, t, yesEnforce, NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR); }

	public static <T> boolean enforceInstanceInitialized( Class<T> clazz, Iterable<T> t, int naInt, double naDouble, String naString, char naChar)
	{ return areInitialized(clazz, t, yesEnforce, naInt, naDouble, naString, naChar); }



	/* Static enforcers */
	public static <T> boolean enforceStaticInitialized(Class<T> clazz)
	{ return isInitialized(clazz, null, yesEnforce, NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR); }

	public static <T> boolean enforceStaticInitialized( Class<T> clazz, int naInt, double naDouble, String naString,char naChar)
	{ return isInitialized(clazz, null, yesEnforce, naInt, naDouble, naString, naChar); }



	private static <T> void initializeFieldsToNA(Class<T> clazz, T t) {	initializeFieldsToNA(clazz, t, NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR); }

	private static <T> void initializeFieldsToNA(Class<T> clazz, T t, int naInt, double naDouble, String naString, char naChar)
	{
		boolean isStatic;
		boolean isNull = t == null;

		for (Field f : AnnotatedBeanBuilder.getAnnotatedFields(clazz, Initialized.class))
		{
			try {
				isStatic = Modifier.isStatic(f.getModifiers());
				/* There's probably a way to simplify this, it's really an xor */
				if ( (isStatic && isNull) || ((!isStatic) && (!isNull)))
					setNA(t, f,	naInt, naDouble, naString, naChar);
			}
			catch (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace();}
		}
	}


	/**
	 * @param t instance of T
	 * @param f
	 * @param naInt NA for integers
	 * @param naDouble NA for doubles
	 * @param naString NA for strings
	 * @param naChar NA for char
	 * @param <T> generic type parameter
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static <T> void setNA(T t, Field f, int naInt, double naDouble, String naString, char naChar)
			throws IllegalArgumentException, IllegalAccessException
	{
		f.setAccessible(true);
		String type = f.getType().getSimpleName();

		switch(type)
		{
		case("int"):     { f.setInt(t, naInt); break;}
		case("double"):  { f.setDouble(t, naDouble); break; }
		/* It doesn't make sense to have a na value for the primitive boolean 
		 * since true and false are both reasonable values */
		case("boolean"): { break; } 

		case("String"):  { f.set(t, naString); break; }
		case("char"):    { f.setChar(t, naChar); break;}

		case("Integer"): { f.set(t, (Integer) naInt); break; }
		case("Double"):  { f.set(t, (Double) naDouble); break; }
		case("Boolean"): { f.set(t, (Boolean) null); break; }
		default:         { f.set(t, null); break; }
		}
	}



	/**
	 * 
	 * @param clazz type of the bean.
	 * @param bean instance of T
	 * @param checkInstance
	 * @param checkStatic
	 * @param enforce should an exception be thrown if field is not initialized?
	 * @param naInt NA for integers
	 * @param naDouble NA for doubles
	 * @param naString NA for strings
	 * @param naChar NA for char
	 * @param <T> generic type parameter
	 * @return
	 */
	private static <T> boolean isInitialized(Class<T> clazz, T bean, boolean enforce, int naInt, double naDouble, String naString, char naChar)
	{
		String message, typeName, modifier;
		typeName = clazz.getSimpleName();

		for (Field f : getAnnotatedFields(clazz, Initialized.class))
		{
			if (Modifier.isStatic(f.getModifiers())) modifier = "Static "; else modifier = "Instance ";
			message = modifier + "Field: " + f.getName() + " in type " + typeName + " is not initialized.";

			try {if (AnnotatedBeanReporter.isNA(clazz, bean, f, naInt, naDouble, naString, naChar)) 
			{
				if (enforce) throw new IllegalArgumentException(message);
				//						logger.debug(message);
				return false;
			}
			} catch (IllegalArgumentException | IllegalAccessException e) {	e.printStackTrace(); }
		}
		return true;
	}

	private static <T> boolean areInitialized(Class<T> clazz, Iterable<T> beans, boolean enforce, int naInt, double naDouble, String naString, char naChar)
	{
		String message, typeName, modifier;
		typeName = clazz.getSimpleName();

		for (Field f : getAnnotatedFields(clazz, Initialized.class))
		{
			if (Modifier.isStatic(f.getModifiers())) modifier = "Static "; else modifier = "Instance ";
			message = modifier + "Field: " + f.getName() + " in type " + typeName + " is not initialized.";

			for (T bean : beans)
			{

				try {if (AnnotatedBeanReporter.isNA(clazz, bean, f, naInt, naDouble, naString, naChar)) 
				{
					if (enforce) throw new IllegalArgumentException(message);
					//						logger.debug(message);
					return false;
				}
				} catch (IllegalArgumentException | IllegalAccessException e) {	e.printStackTrace(); }
			}
		}
		return true;
	}

	@Deprecated
	public static <T> boolean checkStaticInitialized(
			Class<T> clazz,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	{ return isInitialized(clazz, null, noEnforce, naInt, naDouble, naString, naChar); }

	@Deprecated //Get rid of naBoolean parameter
	public static <T> boolean checkInstanceInitialized(
			Class<T> clazz, T t,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	{
		return isInitialized(
				clazz, t, noEnforce,
				naInt, naDouble, naString, naChar); 
	}

	@Deprecated // get rid of naBoolean
	public static <T> void initializeStaticFieldsToNA(Class<T> clazz,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	{
		initializeFieldsToNA(clazz, null, noInstance, yesStatic, naInt, naDouble, naString, naChar); 
	}
	@Deprecated // get rid of naBoolean
	public static <T> void initializeInstanceFieldsToNA(Class<T> clazz, T t,
			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	{
		initializeFieldsToNA(clazz, t, yesInstance, noStatic, naInt, naDouble, naString, naChar); 
	}


	/** Initialize using custom NA values
	 * 
	 * @param clazz type of the bean. class of the bean
	 * @param t instance of T instance of T (null for initializing only static fields)
	 * @param naInt NA for integers NA for integers
	 * @param naDouble NA for doubles NA for doubles
	 * @param naString NA for strings NA for strings
	 * @param naChar NA for char NA for char
	 * @param <T> generic type parameter
	 */
	@Deprecated // Infer static/instance from arguments
	private static <T> void initializeFieldsToNA(Class<T> clazz, T t,
			boolean setInstance, boolean setStatic,
			int naInt, double naDouble, String naString, char naChar)
	{

		initializeFieldsToNA(clazz, t, naInt, naDouble, naString, naChar);

		//		
		//		boolean isInstance, isStatic;
		//
		//		for (Field f : AnnotatedBeanBuilder.getAnnotatedFields(clazz, Initialized.class))
		//		{
		//			try {
		//				isStatic = Modifier.isStatic(f.getModifiers());
		//				isInstance = !isStatic;
		//
		//				if ( (isStatic && setStatic) || (isInstance && setInstance))
		//					setNA(t, f,	naInt, naDouble, naString, naChar);
		//			}
		//			catch (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace();}
		//		}
	}

	//	
	//	
	//	
	//	
	//	
	//	
	//	/** Initialize using default NA values
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param <T> generic type parameter
	//	 */
	//	public static <T> void initializeStaticFieldsToNA(Class<T> clazz)
	//	{
	//		initializeFieldsToNA(clazz, null, noInstance, yesStatic); 
	//	}
	//
	//	/** Initialize using default NA values
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param t instance of T bean instance
	//	 * @param <T> generic type parameter
	//	 */
	//	public static <T> void initializeInstanceFieldsToNA(Class<T> clazz, T t)
	//	{
	//		initializeFieldsToNA(clazz, t, yesInstance, noStatic);
	//	}
	//
	//
	//	/** Initialize using custom NA values
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param naInt NA for integers NA for integers
	//	 * @param naDouble NA for doubles NA for doubles
	//	 * @param naString NA for strings NA for strings
	//	 * @param naChar NA for char NA for char
	//	 * @param <T> generic type parameter
	//	 */
	//	public static <T> void initializeStaticFieldsToNA(
	//			Class<T> clazz,
	//			int naInt, double naDouble, String naString, char naChar)
	//	{
	//		initializeFieldsToNA(
	//				clazz, null,
	//				noInstance, yesStatic, 
	//				naInt, naDouble, naString, naChar); 
	//	}
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param t instance of T instance of T
	//	 * @param naInt NA for integers NA for integers
	//	 * @param naDouble NA for doubles NA for doubles
	//	 * @param naString NA for strings NA for strings
	//	 * @param naChar NA for char NA for char
	//	 * @param <T> generic type parameter
	//	 */
	//	public static <T> void initializeInstanceFieldsToNA(Class<T> clazz, T t,
	//			int naInt, double naDouble, String naString, char naChar)
	//	{
	//		initializeFieldsToNA(
	//				clazz, t, 
	//				yesInstance, noStatic,
	//				naInt, naDouble, naString, naChar); 
	//	}
	//
	//
	//	/** Initialize using the default NA values.
	//	 * 
	//	 * @param clazz type of the bean. class of the bean
	//	 * @param t instance of T instance of T (null for initializing only static fields)
	//	 * @param <T> generic type parameter
	//	 */
	//	private static <T> void initializeFieldsToNA(Class<T> clazz, T t, boolean setInstance, boolean setStatic)
	//	{ 
	//		initializeFieldsToNA(
	//				clazz, t, setInstance, setStatic,
	//				NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR); 
	//	}
	//
	//	/** Initialize using custom NA values
	//	 * 
	//	 * @param clazz type of the bean. class of the bean
	//	 * @param t instance of T instance of T (null for initializing only static fields)
	//	 * @param naInt NA for integers NA for integers
	//	 * @param naDouble NA for doubles NA for doubles
	//	 * @param naString NA for strings NA for strings
	//	 * @param naChar NA for char NA for char
	//	 * @param <T> generic type parameter
	//	 */
	//	private static <T> void initializeFieldsToNA(Class<T> clazz, T t,
	//			boolean setInstance, boolean setStatic,
	//			int naInt, double naDouble, String naString, char naChar)
	//	{
	//		boolean isInstance, isStatic;
	//
	//		for (Field f : AnnotatedBeanBuilder.getAnnotatedFields(clazz, Initialized.class))
	//		{
	//			try {
	//				isStatic = Modifier.isStatic(f.getModifiers());
	//				isInstance = !isStatic;
	//
	//				if ( (isStatic && setStatic) || (isInstance && setInstance))
	//					setNA(t, f,	naInt, naDouble, naString, naChar);
	//			}
	//			catch (IllegalArgumentException | IllegalAccessException e) { e.printStackTrace();}
	//		}
	//	}
	//
	//	/**
	//	 * 
	//	 * @param t instance of T
	//	 * @param f
	//	 * @param naInt NA for integers
	//	 * @param naDouble NA for doubles
	//	 * @param naString NA for strings
	//	 * @param naChar NA for char
	//	 * @param <T> generic type parameter
	//	 * @throws IllegalArgumentException
	//	 * @throws IllegalAccessException
	//	 */
	//	public static <T> void setNA(T t, Field f, 
	//			int naInt, double naDouble, String naString, char naChar)
	//					throws IllegalArgumentException, IllegalAccessException
	//	{
	//		f.setAccessible(true);
	//		String type = f.getType().getSimpleName();
	//
	//		switch(type)
	//		{
	//		case("int"):     { f.setInt(t, naInt); break;}
	//		case("double"):  { f.setDouble(t, naDouble); break; }
	//		/* It doesn't make sense to have a na value for the primitive boolean 
	//		 * since true and false are both reasonable values */
	//		case("boolean"): { break; } 
	//
	//		case("String"):  { f.set(t, naString); break; }
	//		case("char"):    { f.setChar(t, naChar); break;}
	//
	//		case("Integer"): { f.set(t, (Integer) naInt); break; }
	//		case("Double"):  { f.set(t, (Double) naDouble); break; }
	//		case("Boolean"): { f.set(t, (Boolean) null); break; }
	//		default:         { f.set(t, null); break; }
	//		}
	//	}
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	public static <T> boolean checkStaticInitialized(Class<T> clazz)
	//	{
	//		return isInitialized(clazz, null, false, true); 
	//	}
	//
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param naInt NA for integers
	//	 * @param naDouble NA for doubles
	//	 * @param naString NA for strings
	//	 * @param naChar NA for char
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	public static <T> boolean checkStaticInitialized(
	//			Class<T> clazz,
	//			int naInt, double naDouble, String naString, char naChar)
	//	{ 
	//		return checkInitialized(
	//				clazz, null, 
	//				false, true, false,
	//				naInt, naDouble, naString, naChar); 
	//	}
	//
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param t instance of T
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	public static <T> boolean checkInstanceInitialized(Class<T> clazz, T t)
	//	{ 
	//		boolean enforce = false;
	//		return checkInitialized(clazz, t, true, false); 
	//	}
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param naInt NA for integers
	//	 * @param naDouble NA for doubles
	//	 * @param naString NA for strings
	//	 * @param naChar NA for char
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	public static <T> boolean checkStaticInitialized(
	//			Class<T> clazz,
	//			int naInt, double naDouble, String naString, char naChar)
	//	{ 
	//		return checkInstanceInitialized(
	//				clazz, null, 
	//				false, true, false,
	//				naInt, naDouble, naString, naChar); 
	//	}
	//
	//	/** Check that static and instance fields are initialized.
	//	 * 
	//	 * @param clazz type of the bean
	//	 * @param t instance of T
	//	 * @param naInt NA for integers
	//	 * @param naDouble NA for doubles
	//	 * @param naString NA for strings
	//	 * @param naChar NA for char
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	public static <T> boolean checkInitialized(
	//			Class<T> clazz, Iterable<T> t,
	//			int naInt, double naDouble, String naString, char naChar)
	//	{ 
	//		boolean enforce = false;
	//		for (T tt : t)
	//		{
	//			if	(!checkInstanceInitialized(
	//					clazz, tt, 
	//					true, false, enforce, 
	//					naInt, naDouble, naString, naChar))
	//				return false;
	//		}
	//
	//		return checkStaticInitialized(
	//				clazz, null, 
	//				false, true, enforce,
	//				naInt, naDouble, naString, naChar); 
	//	}
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param t instance of T
	//	 * @param naInt NA for integers
	//	 * @param naDouble NA for doubles
	//	 * @param naString NA for strings
	//	 * @param naChar NA for char
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	public static <T> boolean enforceInitialized(
	//			Class<T> clazz, Iterable<T> t,
	//			int naInt, double naDouble, String naString, char naChar)
	//	{
	//		boolean enforce = true;
	//		for (T tt : t)
	//		{
	//			if	(!isInitialized(
	//					clazz, tt, 
	//					yesInstance, noStatic, enforce, 
	//					naInt, naDouble, naString, naChar))
	//				return false;
	//		}
	//
	//		return isInitialized(
	//				clazz, null, 
	//				false, yesStatic, enforce,
	//				naInt, naDouble, naString, naChar); 
	//	}
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param t instance of T
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	public static <T> boolean enforceInstanceInitialized(Class<T> clazz, T t)
	//	{
	//		boolean checkInstance = true; boolean checkStatic = false; boolean enforce = true;
	//		return isInitialized(clazz, null, checkInstance, checkStatic, enforce);
	//	}
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	public static <T> boolean enforceStaticInitialized(Class<T> clazz)
	//	{
	//		boolean checkInstance = false; boolean checkStatic = true; boolean enforce = true;
	//		return isInitialized(clazz, null, checkInstance, checkStatic, enforce);
	//	}
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param t instance of T
	//	 * @param checkInstance
	//	 * @param checkStatic
	//	 * @param enforce should an exception be thrown if field is not initialized?
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	private static <T> boolean isInitialized(
	//			Class<T> clazz, T t, 
	//			boolean checkInstance, boolean checkStatic, boolean enforce)
	//	{ 
	//		return isInitialized(
	//				clazz, t, 
	//				checkInstance, checkStatic, enforce, 
	//				NA_INT, NA_DOUBLE, NA_STRING, NA_CHAR);
	//	}
	//
	//	/**
	//	 * 
	//	 * @param clazz type of the bean.
	//	 * @param bean instance of T
	//	 * @param checkInstance
	//	 * @param checkStatic
	//	 * @param enforce should an exception be thrown if field is not initialized?
	//	 * @param naInt NA for integers
	//	 * @param naDouble NA for doubles
	//	 * @param naString NA for strings
	//	 * @param naChar NA for char
	//	 * @param <T> generic type parameter
	//	 * @return
	//	 */
	//	private static <T> boolean isInitialized(
	//			Class<T> clazz, T bean, boolean enforce, 
	//			int naInt, double naDouble, String naString, char naChar
	//			)
	//	{
	//		String message, typeName, modifier;
	//		typeName = clazz.getSimpleName();
	//
	//		for (Field f : getAnnotatedFields(clazz, Initialized.class))
	//		{
	//			if (Modifier.isStatic(f.getModifiers())) modifier = "Static "; else modifier = "Instance ";
	//			message = modifier + "Field: " + f.getName() + " in type " + typeName + " is not initialized.";
	//
	//			try {if (AnnotatedBeanReporter.isNA(clazz, bean, f, naInt, naDouble, naString, naChar)) 
	//			{
	//				if (enforce) throw new IllegalArgumentException(message);
	//				//						logger.debug(message);
	//				return false;
	//			}
	//			} catch (IllegalArgumentException | IllegalAccessException e) {	e.printStackTrace(); }
	//		}
	//		return true;
	//	}
	//
	//
	//		/**
	//		 * 
	//		 * @param clazz type of the bean.
	//		 * @param bean instance of T
	//		 * @param checkInstance
	//		 * @param checkStatic
	//		 * @param enforce should an exception be thrown if field is not initialized?
	//		 * @param naInt NA for integers
	//		 * @param naDouble NA for doubles
	//		 * @param naString NA for strings
	//		 * @param naChar NA for char
	//		 * @param <T> generic type parameter
	//		 * @return
	//		 */
	//	@Deprecated
	//	private static <T> boolean isInitialized(
	//				Class<T> clazz, T bean, 
	//				boolean checkInstance, boolean checkStatic, boolean enforce,
	//				int naInt, double naDouble, String naString, char naChar
	//				)
	//		{
	//			List<Field> fields = getAnnotatedFields(clazz, Initialized.class);
	//			boolean 
	//	//		isInstance, 
	//			isStatic;
	//			String message, fieldName, typeName, modifier;
	//			for (Field f : fields)
	//			{
	//				isStatic = Modifier.isStatic(f.getModifiers());
	//	//			isInstance = !isStatic;
	//				
	//				fieldName = f.getName();
	//				typeName = f.getType().getSimpleName();
	//				if (isStatic) modifier = "Static "; else modifier = "Instance ";
	//				message = modifier + "Field: " + fieldName + " in type " + typeName + " is not initialized.";
	//				
	//	//			if ( (isStatic && checkStatic) || ((!isStatic) && checkInstance))
	//	//			{
	//				
	//				try {
	//					if (AnnotatedBeanReporter.isNA(clazz, bean, f, naInt, naDouble, naString, naChar)) 
	//					{
	//						if (enforce) throw new IllegalArgumentException(message);
	//	//						logger.debug(message);
	//						return false;
	//					}
	//				} catch (IllegalArgumentException | IllegalAccessException e) {	e.printStackTrace(); }
	//	//			}
	//			}
	//			return true;
	//		}
	//
	//
	//	@Deprecated
	//	public static <T> boolean checkInstanceInitialized(Class<T> clazz, T t,
	//			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	//	{ return checkInitialized(clazz, t, true, false, false, naInt, naDouble, naString, naChar); }
	//
	//	@Deprecated
	//	public static <T> boolean checkStaticInitialized(Class<T> clazz,
	//			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	//	{ return checkInitialized(clazz, null, false, true, false, naInt, naDouble, naString, naChar); }
	//
	//	@Deprecated
	//	public static <T> void initializeStaticFieldsToNA(
	//			Class<T> clazz,
	//			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	//	{
	//		initializeFieldsToNA(
	//				clazz, null,
	//				noInstance, yesStatic, 
	//				naInt, naDouble, naString, naChar); 
	//	}
	//
	//	@Deprecated
	//	public static <T> void initializeInstanceFieldsToNA(Class<T> clazz, T t,
	//			int naInt, double naDouble, String naString, boolean naBoolean, char naChar)
	//	{
	//		initializeFieldsToNA(
	//				clazz, t, 
	//				yesInstance, noStatic,
	//				naInt, naDouble, naString, naChar); 
	//	}

}
