package pt.com.broker.client.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * CircularContainer is a utility class that provides circular access to the elements contained.<br/>
 * If the CircularContainer object contains X, W, Y and 7 contained objects are picked the result will be:X, W, Y, X, W, Y, X.
 * 
 */

public class CircularContainer<T>
{
	private ArrayList<T> innerContainer;
	private int index = -1;

	public CircularContainer()
	{
		innerContainer = new ArrayList<T>();
	}

	public CircularContainer(int capacity)
	{
		innerContainer = new ArrayList<T>(capacity);
	}

	public CircularContainer(Collection<T> elements)
	{
		innerContainer = new ArrayList<T>(elements);
	}

	public void add(T value)
	{
		synchronized (innerContainer)
		{
			innerContainer.add(value);
		}
	}

	public void remove(T value)
	{
		synchronized (innerContainer)
		{
			innerContainer.remove(value);
		}
	}

	public void clear()
	{
		synchronized (innerContainer)
		{
			index = -1;
			innerContainer.clear();
		}
	}

	public int size()
	{
		synchronized (innerContainer)
		{
			return innerContainer.size();
		}
	}

	/**
	 * Obtains the current value.
	 * 
	 * @return a T value.
	 */
	public T peek()
	{
		synchronized (innerContainer)
		{
			if (index == -1)
				return null;

			if (innerContainer.isEmpty())
				return null;

			if (index >= innerContainer.size())
				index = 0;

			return innerContainer.get(index);
		}
	}

	/**
	 * Adds the indexer, moving it to the next position (or beginning).
	 * 
	 * @return a T value.
	 */
	public T get()
	{
		synchronized (innerContainer)
		{
			if (innerContainer.isEmpty())
				return null;

			if ((++index) >= innerContainer.size())
				index = 0;
			return innerContainer.get(index);
		}
	}
}
