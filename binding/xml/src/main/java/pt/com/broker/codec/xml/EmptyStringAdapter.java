package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * @see LICENSE.TXT <p/>
 *      Created by Luis Santos<luis.santos@telecom.pt> on 22-07-2014.
 */
public class EmptyStringAdapter extends XmlAdapter<String, String>
{

	@Override
	public String unmarshal(String v) throws Exception
	{
		if ("".equals(v))
		{
			return null;
		}
		return v;
	}

	@Override
	public String marshal(String v) throws Exception
	{
		if ("".equals(v))
		{
			return null;
		}
		return v;
	}

}