using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using Microsoft.Hadoop.Avro;
using Microsoft.Hadoop.Avro.Schema;
using NetMQ;
using NetMQ.Sockets;

namespace AvroDotNetDemo
{
    class Program
    {

        const string TradeSchema = @"{
                                ""type"":""record"",
                                ""namespace"": ""DevoxxFr.Example"",
                                ""name"":""Trade"",

                                ""fields"":
                                    [
                                        { ""name"":""ProductId"", ""type"":""int"" },
                                        { ""name"":""Nominal"", ""type"":""double"" },
                                        { ""name"":""Date"", ""type"":""string"" },
                                        {
                                            ""name"":""TradingPlace"",    
                                            ""default"": ""Paris"",
                                            ""type"": ""string""
                                        }
                                    ]
                            }";
        
        static void Main(string[] args)
        {
            using (var ctx = NetMQContext.Create())
            {
                using (var socket = ctx.CreateSubscriberSocket())
                {
                    socket.Subscribe("");
                    socket.Connect("tcp://127.0.0.1:8765");
                    var schemaSocket = ctx.CreateRequestSocket();
                    schemaSocket.Connect("tcp://127.0.0.1:8777");
                    schemaSocket.Send(TradeSchema);
                    var schema = schemaSocket.ReceiveString();
                    Console.Out.WriteLine("Schema received: "+schema);
                    IAvroSerializer<dynamic> deserializer = AvroSerializer.CreateGenericDeserializerOnly(schema, TradeSchema);

                    Console.Out.WriteLine(".NET Client connected! " + deserializer.ReaderSchema);
                    while (true)
                    {
                        Thread.Sleep(100);
                        var msg = socket.Receive();
                        var stream = new MemoryStream(msg);
                        var trade = deserializer.Deserialize(stream);
                        Console.Out.WriteLine("trade [ ProductId = {0}, Trading Place = {1}, Nominal = {2}, Date = {3} ]", trade.ProductId, trade.TradingPlace, trade.Nominal, trade.Date);
               
                    }    
                                       
                }
            }
        }
    }
}
