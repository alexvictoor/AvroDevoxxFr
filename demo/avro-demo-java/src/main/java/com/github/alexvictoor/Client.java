package com.github.alexvictoor;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class Client
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        System.out.println( "Hello World!" );
        ZContext ctx = new ZContext();
        ZMQ.Socket socket = ctx.createSocket(ZMQ.SUB);
        socket.subscribe(new byte[]{});
        //socket.subscribe("ou".getBytes());
        socket.connect("tcp://127.0.0.1:8765");
        ZMQ.Socket schemaSocket = ctx.createSocket(ZMQ.REQ);
        schemaSocket.connect("tcp://127.0.0.1:8777");
        schemaSocket.send("hello");
        String schema = schemaSocket.recvStr();

        System.out.println("Client connected " + schema);
        schemaSocket.disconnect("tcp://127.0.0.1:8777");
        while (true)
        {
            Thread.sleep(100);
            String msg = socket.recvStr();
            System.out.println("Received " + msg);
        }

    }
}
