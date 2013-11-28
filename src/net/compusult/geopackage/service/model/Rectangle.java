package net.compusult.geopackage.service.model;

public class Rectangle {

	private double llx, lly, urx, ury;

	public Rectangle() {
		this(-180, -90, 180, 90);
	}

	public Rectangle(double llx, double lly, double urx, double ury) {
		this.llx = llx;
		this.lly = lly;
		this.urx = urx;
		this.ury = ury;
	}

	public double getLlx() {
		return llx;
	}

	public void setLlx(double llx) {
		this.llx = llx;
	}

	public double getLly() {
		return lly;
	}

	public void setLly(double lly) {
		this.lly = lly;
	}

	public double getUrx() {
		return urx;
	}

	public void setUrx(double urx) {
		this.urx = urx;
	}

	public double getUry() {
		return ury;
	}

	public void setUry(double ury) {
		this.ury = ury;
	}
	
	public void setFrom(Rectangle r) {
		this.llx = r.llx;
		this.lly = r.lly;
		this.urx = r.urx;
		this.ury = r.ury;
	}
	
}
