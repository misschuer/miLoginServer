package cc.mi.login.loginClient;

import java.util.List;

import cc.mi.core.coder.Coder;
import cc.mi.core.generate.Opcodes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class GateClientDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int opcode = in.getInt(in.readerIndex());
		if (!Opcodes.contains(opcode)) {
			ctx.close();
			return;
		}
		Coder object = Opcodes.newInstance(opcode);
		object.onDecode(in);
		out.add(object);
	}
}
