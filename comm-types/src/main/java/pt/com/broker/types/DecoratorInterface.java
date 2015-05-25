package pt.com.broker.types;

/**
 * Created by luissantos on 06-06-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public interface DecoratorInterface<T>
{

	/**
	 * <p>
	 * getInstance.
	 * </p>
	 *
	 * @return a T object.
	 */
	public T getInstance();

}
