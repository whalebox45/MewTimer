package tku2c.mewtimer;

import java.security.Permission;
import java.util.UUID;

enum ProductStatus {
    RUNNING, PAUSED, STOPPED, DELETED
}

/**
 * Created by whale on 2017/5/17.
 */

public class Product {
    public static final String DEFAULT_NAME = "Product";
    public static final long DEFAULT_DURATION = 10000;
    private static final int DEFAULT_TONE = R.raw.ovending;
    private final UUID ProductUUID = UUID.randomUUID();

    private String name;
    private long expirationTime;
    private long duration;
    private long remainTime;

    public int getToneID() {
        return toneID;
    }

    public void setToneID(int toneID) {
        this.toneID = toneID;
    }

    private int toneID;

    private ProductStatus currentStatus;

    public Product(String name, long duration,int toneID) {
        this.toneID = toneID;
        this.name = name;
        this.duration = duration;
        this.expirationTime = System.currentTimeMillis() + duration;
        this.currentStatus = ProductStatus.STOPPED;
        this.remainTime = duration;
    }

    public Product(long duration) {
        this(DEFAULT_NAME,duration,DEFAULT_TONE);
    }

    public Product() {
        this(DEFAULT_NAME, DEFAULT_DURATION,DEFAULT_TONE);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getExpirationTime() {
        return this.expirationTime;
    }

    public void setExpirationTime(long et) {
        this.expirationTime = et;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public ProductStatus getCurrentStatus() {
        return this.currentStatus;
    }

    public void setCurrentStatus(ProductStatus ps) {
        this.currentStatus = ps;
    }

    public long getRemainTime() {
        return this.remainTime;
    }

    public void setRemainTime(long rt) {
        this.remainTime = rt;
    }


}