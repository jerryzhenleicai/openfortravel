package com.gaocan.train.train;

public class TripCost {
    private int yzPrice = 0;
    private int rzPrice = 0;
    private int ywPrice = 0;
    private int rwPrice = 0;

    public TripCost() {
    }

    public TripCost(int yz, int rz, int yw, int rw) {
        setYzPrice(yz);
        setRzPrice(rz);
        setYwPrice(yw);
        setRwPrice(rw);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String toString() {
        return "YZ: " + getYzPrice() + ", RZ: " + getRzPrice() + ", YW: " + getYwPrice() + ", RW: " + getRwPrice();
    }

	public void setYzPrice(int yzPrice) {
		this.yzPrice = yzPrice;
	}

	public int getYzPrice() {
		if (yzPrice != -1)
			return yzPrice;
		return getRzPrice();
	}

	public void setRzPrice(int rzPrice) {
		this.rzPrice = rzPrice;
	}

	public int getRzPrice() {
		if (rzPrice != -1)
			return rzPrice;
		return getYwPrice();
		
	}

	public void setYwPrice(int ywPrice) {
		this.ywPrice = ywPrice;
	}

	public int getYwPrice() {
		if (ywPrice != -1)
			return ywPrice;
		return getRwPrice();
	}

	public int setRwPrice(int rwPrice) {
		return this.rwPrice = rwPrice;
	}

	public int getRwPrice() {
		return rwPrice;
	}
}
