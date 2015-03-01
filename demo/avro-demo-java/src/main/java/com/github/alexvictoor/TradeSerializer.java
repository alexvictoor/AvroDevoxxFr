package com.github.alexvictoor;


import com.google.common.base.Throwables;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.github.alexvictoor.TradeProvider.TRADE_SCHEMA;

public class TradeSerializer {

    public byte[] serialize(Trade trade) {
        GenericData.Record record = new GenericData.Record(TRADE_SCHEMA);
        record.put("ProductId", trade.productId);
        record.put("Nominal", trade.nominal);
        record.put("Quantity", trade.quantity);
        record.put("Date", trade.date.toString());

        GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(TRADE_SCHEMA);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        try {
            writer.write(record, encoder);
            encoder.flush();
        } catch (IOException e) {
            // never happens
            throw Throwables.propagate(e);
        }
        return stream.toByteArray();
    }
}
