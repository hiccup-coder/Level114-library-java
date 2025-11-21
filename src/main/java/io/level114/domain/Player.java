package io.level114.domain;
import java.util.logging.Logger;

public final class Player {
    private String name;
    private String uuid;
    private double power;
    private static final Logger logger = Logger.getLogger(Player.class.getName());

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    
    public double getPower() {
        logger.info("Hiccup -- getPower = " + this.power);
        return this.power;
    }

    public void setPower(double power) {
        logger.info("Hiccup -- setPower = " + power);
        this.power = power;
    }
}


