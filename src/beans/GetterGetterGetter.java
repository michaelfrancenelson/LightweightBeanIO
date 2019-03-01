package beans;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Tools for retrieving and saving fields in AnnotatedBean objects
 * 
 * @author michaelfrancenelson
 *
 */
public class GetterGetterGetter
{
	@FunctionalInterface interface StringValGetter<T> { String get(T t);};

	@FunctionalInterface interface IntGetter <T> { int get(T obj); }
	@FunctionalInterface interface DoubleGetter<T> { Double get(T obj); }
	@FunctionalInterface interface BooleanGetter<T> { boolean get(T obj); }

	@FunctionalInterface interface StringGetter<T> { String get(T obj); }
	@FunctionalInterface interface CharGetter<T> { char get(T obj); }

	@FunctionalInterface interface BoxedDoubleGetter<T> { Double get(T obj); }
	@FunctionalInterface interface BoxedIntGetter<T> { Integer get(T obj); }
	@FunctionalInterface interface BoxedBooleanGetter<T> { Boolean get(T obj); }

	@FunctionalInterface interface ObjGetter <T> { Object get(T obj); }

	/** Retrieve the names of the fields for annotated bean IO
	 * 
	 * @param ff
	 * @return
	 */
	public static List<String> columnHeaderGetter(List<Field> ff)
	{
		List<String> out = new ArrayList<>();
		for (Field f : ff) out.add(f.getName());
		return out;
	}

	
	/** Get a list of getters for string representations
	 *  of all the bean's fields. 
	 * 
	 * @param t
	 * @param ff
	 * @param dblFmt
	 * @return
	 */
	public static <T extends AnnotatedCSVBean, R> List<StringValGetter<T>>
	stringValGetterGetter(Class<T> t, List<Field> ff, String dblFmt)
	{
		List<StringValGetter<T>> out = new ArrayList<>();
		for (Field f : ff) out.add(stringValGetterGetter(t, f, dblFmt));
		return out;
	}

	/** Create a getter function to return a string representation 
	 *  of an annotated bean's field
	 * 
	 * @param t
	 * @param f
	 * @param dblFmt
	 * @return
	 */
	public static <T extends AnnotatedCSVBean, R> StringValGetter<T>
	stringValGetterGetter(Class<T> t, Field f, String dblFmt)
	{
		String type = f.getType().getSimpleName();
		StringValGetter<T> out = null;

		switch(type)
		{
		case("double"):  
		{
			DoubleGetter<T> d = doubleGetterGetter(t, f);
			out = (T tt) -> {	return String.format(dblFmt, d.get(tt)); }; 
			break; 
		} 

		case("int"): 
		{
			IntGetter<T> d = intGetterGetter(t, f);
			out = (T tt) -> { return String.format("%d", d.get(tt)); };	
			break; 
		} 

		case("String"):  
		{
			StringGetter<T> d = stringGetterGetter(t, f);
			out = (T tt) -> {return d.get(tt); };
			break; 
		}

		case("boolean"): 
		{
			BooleanGetter<T> d = booleanGetterGetter(t, f);
			out = (T tt) -> {return Boolean.toString(d.get(tt)); };
			break; 
		}

		case("Integer"):
		{
			BoxedIntGetter<T> d = boxedIntGetterGetter(t, f);
			out = (T tt) -> { return String.format("%d", d.get(tt)); };	
			break; 
		} 

		case("Double"): 
		{
			BoxedDoubleGetter<T> d = boxedDoubleGetterGetter(t, f);
			out = (T tt) -> { return String.format(dblFmt, d.get(tt)); };	
			break; 
		}

		case("Boolean"): 
		{
			BoxedBooleanGetter<T> d = boxedBooleanGetterGetter(t, f);
			out = (T tt) -> {return Boolean.toString(d.get(tt)); };
			break; 
		}

		case("char"): {out = (T tt) ->
		{
			try { return String.valueOf(f.getChar(tt)); }
			catch (IllegalArgumentException | IllegalAccessException e) 
			{ e.printStackTrace(); } throw new IllegalArgumentException();};
			break;
		}

		default:
		{
			ObjGetter<T> d = objectGetterGetter(t, f);
			out = (T tt) -> { return d.get(tt).toString(); };
			break; 
		}
		}
		return out;
	}

	/** build a getter for a primitive int field
	 * 
	 * @param t
	 * @param f
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> IntGetter<T> 
	intGetterGetter(Class<T> t, Field f)
	{
		IntGetter<T> out = (T tt) -> 
		{
			try { return f.getInt(tt);}
			catch (IllegalArgumentException | IllegalAccessException e) 
			{ e.printStackTrace(); throw new IllegalArgumentException();}
		};
		return out;
	}

	/** build a getter for a primitive double field
	 * 
	 * @param t
	 * @param f
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> DoubleGetter<T> 
	doubleGetterGetter(Class<T> t, Field f)
	{
		DoubleGetter<T> out = (T tt) ->
		{
			try { return f.getDouble(tt);}
			catch (IllegalArgumentException | IllegalAccessException e) 
			{ e.printStackTrace(); throw new IllegalArgumentException();}
		};
		return out;
	}

	/** build a getter for a primitive boolean field
	 * 
	 * @param t
	 * @param f
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> BooleanGetter<T> 
	booleanGetterGetter(Class<T> t, Field f)
	{
		BooleanGetter<T> out = (T tt) ->
		{
			try { return f.getBoolean(tt);}
			catch (IllegalArgumentException | IllegalAccessException e) 
			{ e.printStackTrace(); throw new IllegalArgumentException();}
		};
		return out;
	}

	/** build a getter for a String field
	 * 
	 * @param t
	 * @param f
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> StringGetter<T> 
	stringGetterGetter(Class<T> t, Field f)
	{
		StringGetter<T> out = (T tt) ->
		{
			try { return f.get(tt).toString();}
			catch (IllegalArgumentException | IllegalAccessException e) 
			{ e.printStackTrace(); throw new IllegalArgumentException();}
		};
		return out;
	}

	/** build a getter for a general field (to be cast later)
	 * 
	 * @param t
	 * @param f
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> ObjGetter<T> 
	objectGetterGetter(Class<T> t, Field f)
	{
		ObjGetter<T> out = (T tt) -> 
		{
			try { return f.get(tt);}
			catch (IllegalArgumentException | IllegalAccessException e) 
			{ e.printStackTrace(); throw new IllegalArgumentException();}
		};
		return out;
	}

	/** build a getter for a boxed double field
	 * 
	 * @param t
	 * @param f
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> BoxedDoubleGetter<T> 
	boxedDoubleGetterGetter(Class<T> t, Field f)
	{
		ObjGetter<T> g = objectGetterGetter(t, f);
		return (T tt) -> { return (Double) g.get(tt); };
	}

	/** Build a getter for a boxed int field
	 * 
	 * @param t
	 * @param f
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> BoxedIntGetter<T> 
	boxedIntGetterGetter(Class<T> t, Field f)
	{
		ObjGetter<T> g = objectGetterGetter(t, f);
		return  (T tt) -> { return (Integer) g.get(tt); };
	}

	/** Build a getter for a boxed boolean field
	 * 
	 * @param t
	 * @param f
	 * @return
	 */
	public static <T extends AnnotatedCSVBean> BoxedBooleanGetter<T> 
	boxedBooleanGetterGetter(Class<T> t, Field f)
	{
		ObjGetter<T> g = objectGetterGetter(t, f);
		return (T tt) -> { return (Boolean) g.get(tt); };
	}
}
