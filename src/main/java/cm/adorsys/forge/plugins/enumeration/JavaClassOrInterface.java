package cm.adorsys.forge.plugins.enumeration;

import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.JavaInterface;

public class JavaClassOrInterface {
    
	private final JavaClass javaClass;
	private final JavaInterface javaInterface;
	public JavaClassOrInterface(JavaClass javaClass) {
		this.javaClass = javaClass;
		this.javaInterface = null;
	}
	public JavaClassOrInterface(JavaInterface javaInterface) {
		this.javaClass = null;
		this.javaInterface = javaInterface;
	}
	public JavaClass getJavaClass() {
		return javaClass;
	}
	public JavaInterface getJavaInterface() {
		return javaInterface;
	}

	public boolean isClass(){return javaClass!=null;}
	public boolean isInterface(){return javaInterface!=null;}

	
}
