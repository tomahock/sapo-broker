package pt.com.broker.http.views;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.caudexorigo.http.netty4.HttpAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

public abstract class MustacheHttpAction extends HttpAction {
	
	private static final Logger log = LoggerFactory.getLogger(MustacheHttpAction.class);
	
	private final String view;
	private Mustache mustache;
	
	public MustacheHttpAction(String view){
		this.view = view;
		init();
	}
	
	private void init(){
		mustache = MustacheViewMapper.getInstance().getView(view);
	}
	
	protected abstract Object buildViewObject(ChannelHandlerContext ctx, FullHttpRequest req,
			FullHttpResponse resp);
	
	protected void renderView(Object viewObject, ChannelHandlerContext ctx, FullHttpResponse resp){
		//TODO: Remove this line in PRD
		mustache = new DefaultMustacheFactory().compile(view);
//		mustache = MustacheViewMapper.getInstance().getView(view);
		ByteBuf bbo = Unpooled.buffer();
		try(OutputStream out = new ByteBufOutputStream(bbo)){
			mustache.execute(new OutputStreamWriter(out), viewObject).flush();
		} catch (IOException e) {
			log.error("Exception caught writting response.", e);
		} finally {
			resp.headers().set("Pragma", "no-cache");
			resp.headers().set("Cache-Control", "no-cache");
			resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");
			resp.setStatus(HttpResponseStatus.OK);
			log.debug("Buffer contents: {}", bbo.toString(Charset.forName("UTF-8")));
			resp.content().writeBytes(bbo);
		}
	}
	
	@Override
	public void service(ChannelHandlerContext ctx, FullHttpRequest req,
			FullHttpResponse resp) {
		Object viewObject = buildViewObject(ctx, req, resp);
		renderView(viewObject, ctx, resp);
	}

}
