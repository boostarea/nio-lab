package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioClient {
    private Selector selector;

    private BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));

    public void init() throws Exception {
        this.selector = Selector.open();
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress("127.0.0.1", 8080));
        channel.register(selector, SelectionKey.OP_CONNECT);
    }

    public void start() throws Exception {
        while (true) {
            selector.select();
            Iterator<SelectionKey> ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = ite.next();
                ite.remove();
                if (key.isConnectable()) {
                    connect(key);
                } else if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    public void connect(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            if (channel.finishConnect()) {
                channel.configureBlocking(false);
                channel.register(this.selector, SelectionKey.OP_READ);
                String request = clientInput.readLine();
                channel.write(ByteBuffer.wrap(request.getBytes()));
            } else {
                key.cancel();
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        String response = new String(buffer.array()).trim();
        System.out.println("Server response: " + response);
        String nextRequest = clientInput.readLine();
        ByteBuffer outBuffer = ByteBuffer.wrap(nextRequest.getBytes());
        channel.write(outBuffer);
    }

    public static void main(String[] args) throws Exception {
        NioClient nioClient = new NioClient();
        nioClient.init();
        nioClient.start();
    }
}
