package beans.sampleBeans;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import beans.AnnotatedBeanBuilder;
import beans.AnnotatedBeanBuilder.FieldColumn;
import beans.AnnotatedBeanBuilder.Initialized;
import beans.AnnotatedBeanInitializer;

public class SimpleParamBean 
{

//	static Logger logger = LoggerFactory.getLogger(SimpleParamBean.class);
	
	public static int NON_INITIALIZED_INT = Integer.MIN_VALUE;
	public static double NON_INITIALIZED_DOUBLE = Double.MIN_VALUE;
	
	/* Model run params: */
	/* The number of yearly steps to simulate. */
	@FieldColumn @Initialized public static  int    simulation_length;        
	/* An optional description of this particular model run. */
	@FieldColumn @Initialized public static  String simulation_description;    

	@FieldColumn @Initialized public static  int    start_year;

	/* The area, in hectares, of each forest patch. */
	@FieldColumn @Initialized public static  double cell_area;              
	@FieldColumn @Initialized public static  int    randomSeed1;
	@FieldColumn @Initialized public static  int    randomSeed2;
	@FieldColumn @Initialized public static  int    randomSeed3;
	@FieldColumn @Initialized public static  int    randomSeed4;
	@FieldColumn @Initialized public static  int    randomSeed5;
	@FieldColumn @Initialized public static  int    randomSeed6;

	/** Budget minimum values */
	@FieldColumn @Initialized public static  double min_timber_target, min_timber_budget, min_pol_budget;

	/** Cost/value minimum values. */
	@FieldColumn @Initialized public static  double min_harv_cost, min_poli_cost, min_timb_val;

	/* Ranger Parameters */
	/** Ranger initial budgets.  If the incremental budget model is used, the values for budget and capital are ignored. */
	@FieldColumn @Initialized public static  double
	initial_political_capital, initial_timber_target, initial_budget;   

	/* The number of years required for a previously_harvested patch to become harvestable again. */
	@FieldColumn @Initialized public static  double forest_regen_time;          

	/** Model for initial distribution of stand ages. */
	@FieldColumn @Initialized public static  String initial_stand_age_model;
	/** Initial stand age parameters. */
	@FieldColumn @Initialized public static  int    initial_stand_age, initial_stand_age_min, initial_stand_age_max;       /* Initial age to assign to all the cells. */

	@FieldColumn @Initialized public static  double initial_stand_age_mean, initial_stand_age_sd;    

	/* The radius of the area to be protected surrounding a harvested cell. */
	@FieldColumn @Initialized public static  double harvest_neighbor_embargo_radius;  
	/* The number of years patches adjacent to a harvested patch must wait before they are harvestable. */
	@FieldColumn @Initialized public static  int    harvest_neighbor_embargo_length;  

	/* Params for different budget adjustment methods. */
	/**  The type of function used to update the ranger's budgets depending
	 *  on whether they meet or miss timber targets. */
	@FieldColumn @Initialized public static  String budget_adjust_method;

	/** Exponential cost/value model budget adjustment amounts. */
	@FieldColumn @Initialized public static double
	ranger_meet_target_exponential_timb_target, ranger_miss_target_exponential_timb_target, 
	ranger_meet_target_exponential_timb_budget, ranger_miss_target_exponential_timb_budget, 
	ranger_meet_target_exponential_pol_budget, ranger_miss_target_exponential_pol_budget;

	/** Incremental cost/value model budget adjustment amounts. */
	@FieldColumn @Initialized public static double 
	ranger_meet_target_incremental_timb_target, ranger_miss_target_incremental_timb_target,
	ranger_meet_target_incremental_timb_budget, ranger_miss_target_incremental_timb_budget,
	ranger_meet_target_incremental_pol_budget, ranger_miss_target_incremental_pol_budget;

	@FieldColumn @Initialized public static double pol_budget_target_pct;
	@FieldColumn @Initialized public static double timb_budget_target_pct;

	/* Params for political cost model */
	/** The intercepts for the linear cost/value functions. */
	@FieldColumn @Initialized public static  double alpha_harv_cost, alpha_timber_value, alpha_poli_cost;           /* The real value of timber on patches are adjustments of this base value. */

	/** Do not adjust cost/value for elevations below this amount. */ 
	@FieldColumn @Initialized public static  double elevation_adjust;

	/** Cost/value slope coefficient for elevation. */
	@FieldColumn @Initialized public static  double 
	beta_elev_harv_cost,  beta_elev_timb_val, beta_elev_poli_cost;  

	/** Cost/value slope coefficient for slope. */
	@FieldColumn @Initialized public static  double 
	beta_slope_harv_cost, beta_slope_timb_val, beta_slope_poli_cost;        

	/** Cost/value slope coefficient for stand age. */
	@FieldColumn @Initialized public static  double
	beta_age_harv_cost, beta_age_timb_val, beta_age_poli_cost;          

	/** Cost/value slope coefficient for distance to road. */
	@FieldColumn @Initialized public static  double 
	beta_road_harv_cost, beta_road_timb_val, beta_road_poli_cost;         

	/** Timber harves t strategies: <br> Primary strategy is used until timber target is meet, afterward the secondary strategy is used until budgets are exhausted*/
	@FieldColumn @Initialized public static  String primary_strategy, secondary_strategy;

	
	/** Test field for in itializing from values read from file. */
	@Initialized public static double testInitDouble;
	
	public static String dblFmt = "%.4f";

	public static void init()
	{
		testInitDouble = beta_road_harv_cost;
	}
	
	public static boolean isInitialized()
	{
		return AnnotatedBeanInitializer.checkStaticInitialized(SimpleParamBean.class);
	}
	
	public static void setFromFile(String filename)
	{
		new SimpleParamBean();
		AnnotatedBeanInitializer.initializeStaticFieldsToNA(SimpleParamBean.class);
		AnnotatedBeanBuilder.factory(SimpleParamBean.class, filename, true);
		init();
		if (!isInitialized()) throw new IllegalArgumentException(
				"Model parameters not initiailized from " + filename);
	}
	
	public static void setDefaults()
	{
//		logger.info("setting hardcoded parameter defaults");
		simulation_description = "general model run";
		simulation_length = 100;
		randomSeed1 = 12345;
		randomSeed2 = 54321;
		randomSeed3 = 987654321;
		randomSeed4 = 12345;
		randomSeed5 = 12345;
		randomSeed6 = 3409587;
		initial_political_capital = 2000;
		initial_timber_target = 500;
		initial_budget = 2000;
		forest_regen_time = 40;
		harvest_neighbor_embargo_radius = 1.5;
		harvest_neighbor_embargo_length = 5;
		min_timber_target = 100;
		min_timber_budget = 100;
		min_pol_budget = 10;
		alpha_harv_cost = 100;
		beta_slope_harv_cost = 0;
		beta_elev_harv_cost = 0;
		beta_age_harv_cost = 0;
		beta_road_harv_cost = -0.001;
		alpha_poli_cost = 2;
		beta_slope_poli_cost = 0;
		beta_elev_poli_cost = 0.5;
		beta_age_poli_cost = 0;
		beta_road_poli_cost = 0;
		alpha_timber_value = 10;
		beta_slope_timb_val = 0;
		beta_elev_timb_val = -0.05;
		beta_age_timb_val = 0.01;
		beta_road_timb_val = 0;
		primary_strategy = "low_political_cost";
		secondary_strategy = "low_political_cost";
		initial_stand_age_model = "normal";
		initial_stand_age_mean = 50;
		initial_stand_age_sd = 6;
		initial_stand_age_min = 50;
		initial_stand_age_max = 90;
		initial_stand_age = 90;
		cell_area = 1;
		min_harv_cost = 10;
		min_poli_cost = 0.5;
		min_timb_val = 1;
		start_year = 2000;
		elevation_adjust = 1200;
		pol_budget_target_pct = 1.5;
		timb_budget_target_pct = 1.5;
		budget_adjust_method = "incremental";
		ranger_meet_target_exponential_timb_target = 0.01;
		ranger_miss_target_exponential_timb_target = 0.01;
		ranger_meet_target_exponential_timb_budget = 0.01;
		ranger_miss_target_exponential_timb_budget = 0.01;
		ranger_meet_target_exponential_pol_budget = 0.01;
		ranger_miss_target_exponential_pol_budget = 0.01;
		ranger_meet_target_incremental_timb_target = 100;
		ranger_miss_target_incremental_timb_target = 100;
		ranger_meet_target_incremental_timb_budget = 10;
		ranger_miss_target_incremental_timb_budget = 10;
		ranger_meet_target_incremental_pol_budget = 10;
		ranger_miss_target_incremental_pol_budget = 10;
	}
}
