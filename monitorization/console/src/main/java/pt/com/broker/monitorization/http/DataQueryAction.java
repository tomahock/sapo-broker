package pt.com.broker.monitorization.http;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caudexorigo.http.netty.HttpAction;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.db.queries.QueryDataProvider;
import pt.com.broker.monitorization.db.queries.RateQueries;
import pt.com.broker.monitorization.db.queries.SnapshotQueries;
import pt.com.broker.monitorization.db.queries.agents.AgentInformationRouter;
import pt.com.broker.monitorization.db.queries.faults.FaultsInformationRouter;
import pt.com.broker.monitorization.db.queries.queues.InactiveQueueInformationRouter;
import pt.com.broker.monitorization.db.queries.queues.QueueInformationRouter;
import pt.com.broker.monitorization.db.queries.subscriptions.SubscriptionsInformationRouter;

public class DataQueryAction extends HttpAction
{
	private static final Logger log = LoggerFactory.getLogger(DataQueryAction.class);

	private final String pathPrefix;

	private final static String CONTENT_TYPE = "application/json";
	private final static String CONTENT_ENCODING = "UTF-8";
	private final static Charset UTF8 = Charset.forName("utf-8");

	private static Map<String, QueryDataProvider> data_providers = new HashMap<String, QueryDataProvider>();

	static
	{
		SnapshotQueries sq = new SnapshotQueries();
		data_providers.put(sq.getType(), sq);
		RateQueries rq = new RateQueries();
		data_providers.put(rq.getType(), rq);
		SubscriptionsInformationRouter sir = new SubscriptionsInformationRouter();
		data_providers.put(sir.getType(), sir);
		FaultsInformationRouter fir = new FaultsInformationRouter();
		data_providers.put(fir.getType(), fir);
		QueueInformationRouter qir = new QueueInformationRouter();
		data_providers.put(qir.getType(), qir);
		AgentInformationRouter air = new AgentInformationRouter();
		data_providers.put(air.getType(), air);
		InactiveQueueInformationRouter iqir = new InactiveQueueInformationRouter();
		data_providers.put(iqir.getType(), iqir);
	}

	public DataQueryAction(String queryPrefix)
	{
		pathPrefix = queryPrefix;
	}

	public void service(ChannelHandlerContext context, HttpRequest request, HttpResponse response)
	{
		String path = request.getUri();

		path = path.substring(pathPrefix.length()).toLowerCase();

		Map<String, List<String>> params = getParams(request);

		int index = path.indexOf('?');

		String queryType = (index > 0) ? path.substring(0, index) : path;

		QueryDataProvider queryDataProvider = data_providers.get(queryType);

		if (queryDataProvider == null)
		{
			throw new IllegalArgumentException("Invalid query string...");
		}

		StringBuilder sb = new StringBuilder();

		sb.append("[");
		sb.append(queryDataProvider.getData(queryType, params));
		sb.append("]");

		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(ChannelBuffers.BIG_ENDIAN, sb.toString(), UTF8);

		response.addHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
		response.addHeader(HttpHeaders.Names.CONTENT_ENCODING, CONTENT_ENCODING);
		response.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.writerIndex());

		response.setContent(buffer);
	}

	private static Map<String, List<String>> getParams(HttpRequest request)
	{
		return new QueryStringDecoder(request.getUri()).getParameters();
	}
}
