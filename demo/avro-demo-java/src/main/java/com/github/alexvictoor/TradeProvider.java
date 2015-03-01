package com.github.alexvictoor;

import com.google.common.base.Throwables;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaCompatibility;
import org.apache.avro.SchemaParseException;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.apache.avro.SchemaCompatibility.SchemaCompatibilityType;
import static org.apache.avro.SchemaCompatibility.SchemaCompatibilityType.COMPATIBLE;
import static org.apache.avro.SchemaCompatibility.SchemaCompatibilityType.INCOMPATIBLE;

/**
 * Hello world!
 *
 */
public class TradeProvider
{
    public static final Schema TRADE_SCHEMA = new Schema.Parser().parse("{\n" +
            "    \"type\":\"record\",\n" +
            "    \"namespace\": \"DevoxxFr.Example\",\n" +
            "    \"name\":\"Trade\",\n" +
            "    \"fields\":\n" +
            "        [\n" +
            "            { \"name\":\"ProductId\", \"type\":\"int\" },\n" +
            "            { \"name\":\"Quantity\", \"type\":\"int\" },\n" +
            "            { \"name\":\"Nominal\", \"type\":\"double\" },\n" +
            "            { \"name\":\"Date\", \"type\":\"string\" }\n" +
            "        ]\n" +
            "}");
    public static final String TRADE_SCHEMA_JSON = TRADE_SCHEMA.toString(true);
    private TradeSerializer serializer = new TradeSerializer();
    private Schema.Parser schemaParser;
    private ZMQ.Socket schemaSocket;
    private ZMQ.Socket tradeSocket;

    private void handleSchemaRequest(String schemaRequest) {
        String response;
        try {
            Schema schema = new Schema.Parser().parse(schemaRequest);
            System.out.println("Connection request using schema: " + schema.toString(true));
            System.out.println("Checking compatibility with schema: " + TRADE_SCHEMA_JSON);
            SchemaCompatibilityType compatibilityType
                    = SchemaCompatibility.checkReaderWriterCompatibility(schema, TRADE_SCHEMA).getType();

            if (compatibilityType == COMPATIBLE) {
                System.out.println("Schemas are compatible!");
                response = TRADE_SCHEMA_JSON;
            } else {
                System.out.println("Schemas are not compatible!");
                response = "ERROR\nSchemas are incompatible";
            }
        } catch (SchemaParseException ex) {
            ex.printStackTrace();
            String stackTrace = Throwables.getStackTraceAsString(ex);
            response = "ERROR\n" + ex.getMessage() + "\n" + stackTrace;
        }
        schemaSocket.send(response);
    }



    public void run() throws InterruptedException {
        System.out.println( "Starting server" );
        ZContext ctx = new ZContext();

        tradeSocket = ctx.createSocket(ZMQ.PUB);
        tradeSocket.bind("tcp://127.0.0.1:8765");

        schemaSocket = ctx.createSocket(ZMQ.REP);
        schemaSocket.setReceiveTimeOut(0);
        schemaSocket.bind("tcp://127.0.0.1:8777");

        schemaParser = new Schema.Parser();

        System.out.println("Ready, sending trades!");
        while (true) {
            String schemaRequest = schemaSocket.recvStr();
            if (schemaRequest != null) {
                handleSchemaRequest(schemaRequest);
            }
            publishNewTrade();
            //boolean sendOk = tradeSocket.send("coucou");
            //System.out.println("send ok " + sendOk);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private void publishNewTrade() {
        Trade trade = Trade.createRandom();
        byte[] bytes = serializer.serialize(trade);
        tradeSocket.send(bytes);
        //System.out.println("Trade sent: " + trade);
    }


    public static void main( String[] args ) throws IOException, InterruptedException {
        new TradeProvider().run();
    }
}
