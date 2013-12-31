package cm.adorsys.forge.plugins.enumeration;

import java.io.FileNotFoundException;

import org.jboss.forge.parser.java.EnumConstant;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.JavaEnum;
import org.jboss.forge.parser.java.JavaInterface;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.parser.java.Method;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.enumtype.EnumConstantResource;
import org.jboss.forge.resources.java.JavaFieldResource;
import org.jboss.forge.resources.java.JavaMethodResource;
import org.jboss.forge.resources.java.JavaResource;

public final class EnumerationPluginUtils {

	public static final JavaClassOrInterface inspectResource(
			Resource<?> resource) {
		if (resource == null) {
			throw new IllegalArgumentException(
					"The current resource can not be null");
		}
		
		JavaSource<?> javaSource;
		try {
			if(resource instanceof JavaFieldResource){
				JavaFieldResource jfr =  (JavaFieldResource) resource;
				Field<? extends JavaSource<?>> field = jfr.getUnderlyingResourceObject();
				javaSource = field.getOrigin();
			} else if (resource instanceof JavaMethodResource){
				JavaMethodResource jmr = (JavaMethodResource) resource;
				Method<? extends JavaSource<?>> method = jmr.getUnderlyingResourceObject();
				javaSource = method.getOrigin();
			} else if (resource instanceof JavaResource) {
				final JavaResource javaResource = (JavaResource) resource;
				javaSource = javaResource.getJavaSource();
			} else {
				throw new IllegalArgumentException("The given resource '"
						+ resource.getName() + "' is not a Java resource");
			}
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("The given resource '"
					+ resource.getName()
					+ "' has be deleted from the file system.");
		}

		if (javaSource.isClass()) {
			return new JavaClassOrInterface((JavaClass) javaSource);
		} else if (javaSource.isInterface()) {
			return new JavaClassOrInterface((JavaInterface) javaSource);
		} else {
			throw new IllegalArgumentException("The given resource '"
					+ resource.getName()
					+ "' is not a class or an interface");
		}
	}

}
