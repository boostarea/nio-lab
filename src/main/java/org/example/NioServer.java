package org.example;

import javax.security.auth.login.Configuration;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class NioServer {
    private Selector selector;
    public void init() throws Exception {
        this.selector = Selector.open();
        //创建ServerSocketChannel
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        ServerSocket serverSocket = channel.socket();
        InetSocketAddress address = new InetSocketAddress(8080);
        serverSocket.bind(address);
        channel.register(this.selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws Exception {
        while (true) {
            this.selector.select();
            Iterator<SelectionKey> ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = ite.next();
                ite.remove();
                if (key.isAcceptable()) {
                    accept(key);
                } else if(key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        String request = new String(buffer.array()).trim();
        System.out.println("Server received: " + request);
        ByteBuffer outBuffer = ByteBuffer.wrap("请求收到".getBytes());
        channel.write(outBuffer);
    }

    public static void main(String[] args) throws Exception {
        NioServer nioServer = new NioServer();
        nioServer.init();
        nioServer.start();
    }

}
