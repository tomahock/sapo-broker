package pt.com.broker.client.nio.utils;

import java.util.concurrent.Future;

import pt.com.broker.client.nio.server.HostInfo;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * @see LICENSE.TXT <p/>
 *      Created by Luis Santos<luis.santos@telecom.pt> on 23-06-2014.
 * @author vagrant
 * @version $Id: $Id
 */
abstract public class HostInfoFuture<T extends HostInfo> implements Future<T>
{
}