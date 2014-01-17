package com.example.tema2si;


public class PowerEvent{

	private long cpuTime;
	private long gpsTime;
	private long cpuFgTime;
	private long bytesReceived;
	private long bytesSent;
	private double powerTCP;
	private double powerGPS;
	private double powerCPU;
	private String defaultPackageName;
	private String appName;
	private String uid;

	
	public PowerEvent(){
		
	}
	
	public PowerEvent(long cpuTime, long gpsTime, long bytesReceived,
			long bytesSent, double powerTCP, double powerGPS, double powerCPU,
			String defaultPackageName, String appName, String uid) {
		super();
		this.cpuTime = cpuTime;
		this.gpsTime = gpsTime;
		this.bytesReceived = bytesReceived;
		this.bytesSent = bytesSent;
		this.powerTCP = powerTCP;
		this.powerGPS = powerGPS;
		this.powerCPU = powerCPU;
		this.defaultPackageName = defaultPackageName;
		this.appName = appName;
		this.uid = uid;
	}

	public void setAppName(String name){
		appName = name;
	}
	
	public void setUid(String uid){
		this.uid = uid;
	}
	
	public void setPowerTCP(double power){
		powerTCP = power;
	}
	
	public double getPowerTCP(){
		return powerTCP;
	}
	
	public void setBytesReceived(long bytes){
		bytesReceived = bytes;
	}
	
	public void setBytesSent(long bytes){
		bytesSent = bytes;
	}
	
	public long getBytesReceived(){
		return bytesReceived;
	}
	
	public long getBytesSent(){
		return bytesSent;
	}
	
	public String getUid(){
		return uid;
	}
	
	public String getAppName(){
		return appName;
	}

	
	public long getCpuTime(){
		return cpuTime;
	}
	
	public void setCpuTime(long time){
		cpuTime = time;
	}
		
	public void setPowerCPU(double power){
		this.powerCPU = power;
	}
	public double getPowerCPU(){
		return powerCPU;
	}
	
	public void setPowerGPS(double power){
		this.powerGPS = power;
	}
	public double getPowerGPS(){
		return powerGPS;
	}
	
	public long getGpsTime(){
		return gpsTime;
	}
	
	public void setGpsTime(long time){
		gpsTime = time;
	}

	public long getCpuFgTime() {
		return cpuFgTime;
	}

	public void setCpuFgTime(long cpuFgTime) {
		this.cpuFgTime = cpuFgTime;
	}


	public String getDefaultPackageName() {
		return defaultPackageName;
	}

	public void setDefaultPackageName(String defaultPackageName) {
		this.defaultPackageName = defaultPackageName;		
	}
	


}