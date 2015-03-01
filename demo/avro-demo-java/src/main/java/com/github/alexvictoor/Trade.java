package com.github.alexvictoor;

import java.util.Date;
import java.util.Random;

public class Trade {

    public final int productId;
    public final int quantity;
    public final double nominal;
    public final Date date;
    public Trade(int productId, int quantity, double nominal, Date date) {
        this.productId = productId;
        this.quantity = quantity;
        this.nominal = nominal;
        this.date = date;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                ", nominal=" + nominal +
                ", date='" + date + '\'' +
                '}';
    }

    private static Random random = new Random();
    private static int counter = 0;

    public static Trade createRandom() {
        int productId = counter++;
        int quantity = Math.abs(random.nextInt());
        double nominal = Math.abs(random.nextDouble()) * 1000000D;
        return new Trade(productId, quantity, nominal, new Date());
    }
}
