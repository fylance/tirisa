package de.fylance.tirisa;

public class GeneratorProperties {
	private String targetPath = "";
	private String targetPackage = "";
	
	public GeneratorProperties(String targetPath, String targetPackage) {
		this.targetPath = targetPath;
		this.targetPackage = targetPackage;
	}
	
	public GeneratorProperties(Object obj) {
		this.targetPackage = obj.getClass().getPackage().getName();
		this.targetPath = "src/" + targetPackage.replace('.', '/');
		System.out.println("writing to "+this.targetPath);
	}
	
	public String getTargetPath() {
		return targetPath;
	}
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	public String getTargetPackage() {
		return targetPackage;
	}
	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}
	
}
