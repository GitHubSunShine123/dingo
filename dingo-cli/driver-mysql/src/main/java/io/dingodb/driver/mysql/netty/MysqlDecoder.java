/*
 * Copyright 2021 DataCanvas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dingodb.driver.mysql.netty;

import io.dingodb.common.util.Optional;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MysqlDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Optional.ifPresent(read(in), out::add);
    }

    public ByteBuf read(ByteBuf buf) {
        if (buf.readableBytes() < 4) {
            return null;
        }
        buf.markReaderIndex();
        byte[] headLength = new byte[3];
        buf.readBytes(headLength);
        int contentLength = readLength(headLength);
        // mysql msg:  3bytes -> headLen  1 byte -> packetId  headLen bytes -> content(sql,...)
        if (!buf.isReadable(contentLength + 1)) {
            buf.resetReaderIndex();
            return null;
        }

        ByteBuf buf1 = buf.readBytes(contentLength + 1);

        return buf1;
    }

    public int readLength(byte[] data) {
        int position = 0;
        int i = data[position++] & 0xff;
        i |= (data[position++] & 0xff) << 8;
        i |= (data[position++] & 0xff) << 16;
        return i;
    }
}
