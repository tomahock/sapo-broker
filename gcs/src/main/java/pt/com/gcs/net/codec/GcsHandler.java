package pt.com.gcs.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.messaging.GcsAcceptorProtocolHandler;
import pt.com.gcs.net.ssl.RequiredSslException;

/**
 * This Decoder main responsibility is to detect SSL/TLS support for the channel.
 * If the channel supports SSL/TLS, the SSL Handler is added to the pipeline, otherwise
 * all agent communications are unsecured.
 * */
public class GcsHandler extends ByteToMessageDecoder {
	
	private static final Logger log = LoggerFactory.getLogger(GcsHandler.class);
	
	private final SslContext sslCtx;
	private final boolean forceSsl;
	
	public GcsHandler(SslContext sslCtx, boolean forceSsl){
		this.sslCtx = sslCtx;
		this.forceSsl = forceSsl;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		if(in.readableBytes() >= GcsCodec.HEADER_LENGTH){
			ChannelPipeline p = ctx.pipeline();
			if(SslHandler.isEncrypted(in)){
				log.debug("SSL Support added to the channel.");
				p.addLast("broker-ssl-handler", sslCtx.newHandler(ctx.alloc()));
			} else {
				if(forceSsl){
					log.error("Ssl is mandatory for all agent communications.");
					ctx.close();
					throw new RequiredSslException();
				} else {
					log.warn("No SSL Support. All communications between {} and {} will be unsecured.",
						ctx.channel().localAddress().toString(),
						ctx.channel().remoteAddress().toString()
					);
				}
			}
			p.addLast("broker-codec", new GcsCodec());
            p.addLast("broker-handler", new GcsAcceptorProtocolHandler());
			p.remove(this);
		}
	}

}
