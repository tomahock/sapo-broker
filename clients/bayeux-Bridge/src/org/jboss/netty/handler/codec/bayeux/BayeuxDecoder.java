/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.codec.bayeux;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * BayeuxDecoder should be used with HTTPDecoder, because it only suppots Bayeux
 * protocol transporting on HTTP now. When browser request with Bayeux messages,
 * BayuexDecoder only decode and validate them from content of HTTP request.
 * Then BayeuxDecoder create or map this request to a BayeuxConnection instance
 * and put the valid Bayeux messages to it. At last, BayeuxDecoder throw the
 * connection instance to higer layer, by which user can develop their
 * application logics.
 *
 * @author daijun
 */
@ChannelPipelineCoverage("one")
public class BayeuxDecoder extends OneToOneDecoder {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(BayeuxDecoder.class.getName());

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof HttpRequest)) {
            return msg;
        }

        HttpRequest request = (HttpRequest) msg;
        HttpMethod method = request.getMethod();
        HttpVersion version = request.getProtocolVersion();
        String json = "";
        String jsonp = "";
        if (HttpMethod.POST == method && HttpVersion.HTTP_1_1 == version) {//Callback polling connection type
            String charset = "utf-8";//Default unicode char encoding
            if (request.containsHeader(HttpHeaders.Names.CONTENT_TYPE)) {
                String contentType = request.getHeader(HttpHeaders.Names.CONTENT_TYPE);
                charset = contentType.indexOf("charset=") > -1 ? contentType.substring(contentType.indexOf("charset=") + 8) : charset;
                charset = isUnicode(charset) ? charset : "utf-8";
            }

            String httpContent = ((ChannelBuffer) request.getContent()).toString(charset);
            logger.debug("HTTP POST: " + httpContent);
            String content = URLDecoder.decode(httpContent, charset);

            int begin = content.indexOf("message=");
            if (begin == -1) {
                json = content;
            } else {
                int end = content.indexOf("\n", begin) == -1 ? content.length() : content.indexOf("\n", begin);
                json = content.substring(begin + 8, end);
            }
        } else if (HttpMethod.GET == method) {//Callback polling
            logger.debug("HTTP GET: " + request.getUri());
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> params = queryStringDecoder.getParameters();
            if (!params.isEmpty() && params.containsKey("message")) {
                List<String> vals = params.get("message");
                for (String val : vals) {
                    json += val;
                }
                vals = params.get("jsonp");
                for (String val : vals) {
                    jsonp += val;
                }
            } else {
                return msg;
            }
        } else {
            return msg;
        }
        logger.info("Request:" + json);
        Object jsonObject = new JSONParser().parse(json);
        if (jsonObject == null || !(jsonObject instanceof Object[]) || ((Object[]) jsonObject).length == 0) {
            return null;
        }

        Object[] jsonObjectArray = (Object[]) jsonObject;
        BayeuxMessageFactory factory = BayeuxMessageFactory.getInstance();
        BayeuxConnection connection = null;
        for (Object o : jsonObjectArray) {
            BayeuxMessage bayeux = factory.create((Map) o);
            connection = BayeuxRouter.getInstance().getConnection(bayeux.clientId);
            if (connection == null) {//New client, when handshakeing or publishing withoud connect before
                connection = new BayeuxConnection(channel);
            } else if (connection.getChannel() != channel) {//Client is polling. Replace the older HTTP connection with the new one.
                ConnectResponse[] responses=new ConnectResponse[1];
                responses[0]=new ConnectResponse(connection.getClientId(),true);
                responses[0].setId(connection.getId());
                responses[0].setTimestamp(BayeuxUtil.getCurrentTime());
                connection.send(JSONParser.toJSON(responses));
                connection.setChannel(channel);
            }
            connection.setId(bayeux.id);
            if (jsonp.length() > 0) {
                connection.setJsonp(jsonp);
            }
            if (HandshakeRequest.isValid(bayeux)) {
                connection.receiveToQueue(new HandshakeRequest(bayeux));
                BayeuxRouter.getInstance().addConnection(connection);
            } else if (ConnectRequest.isValid(bayeux)) {
                connection.receiveToQueue(new ConnectRequest(bayeux));
            } else if (DisconnectRequest.isValid(bayeux)) {
                connection.receiveToQueue(new DisconnectRequest(bayeux));
            } else if (SubscribeRequest.isValid(bayeux)) {
                connection.receiveToQueue(new SubscribeRequest(bayeux));
            } else if (UnsubscribeRequest.isValid(bayeux)) {
                connection.receiveToQueue(new UnsubscribeRequest(bayeux));
            } else if (PublishRequest.isValid(bayeux)) {
                connection.receiveToQueue(new PublishRequest(bayeux));
            }
        }
        return connection;
    }

    private boolean isUnicode(String charset) {
        String unicodes[] = {"utf-8", "utf-16", "utf-16le", "utf-16be", "utf-32", "utf-32le", "utf-32be"};
        for (String unicode : unicodes) {
            if (unicode.equalsIgnoreCase(charset)) {
                return true;
            }
        }
        return false;
    }
}
