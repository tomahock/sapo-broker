package pt.com.broker.http.views;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class MustacheViewMapper {
	
	private static final MustacheViewMapper INSTANCE = new MustacheViewMapper();
	
	//This object allready has a built in Guava cache.
	private MustacheFactory mFactory = new DefaultMustacheFactory();
	
	private MustacheViewMapper(){
		init();
	}
	
	private void init(){
		
	}
	
	public Mustache getView(String viewKey){
		return mFactory.compile(viewKey);
	}
	
	public static final MustacheViewMapper getInstance(){
		return INSTANCE;
	}

}
