package net.compusult.geopackage.service.geopackager;

public class ProgressTracker {

	private int itemCount;
	private int percentComplete;
	private int thisItem;
	
	public ProgressTracker() {
		this.itemCount = 0;
		this.percentComplete = 0;
		this.thisItem = 0;
	}
	
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public void newItem() {
		++ thisItem;
	}
	
	public void setProgress(int percentage) {
		this.percentComplete = percentage;
	}
	
	public int getPercentComplete() {
		// 75% of the way through item 2 of 3 means (200 + 75) / 3 = 92
		return (int) ((100.0 * thisItem + percentComplete) / itemCount + 0.5);
	}

}
