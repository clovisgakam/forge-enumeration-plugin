package cm.adorsys.forge.plugins.enumeration;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.parser.java.EnumConstant;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.JavaEnum;
import org.jboss.forge.parser.java.JavaInterface;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.completer.SimpleTokenCompleter;

public class EnumConstantCompleter  extends SimpleTokenCompleter{

	private final Shell shell;

	@Inject
	public EnumConstantCompleter(Shell shell) {
		this.shell = shell ;
	}

	@Override
	public List<String> getCompletionTokens() {
		final List<String> tokens = new ArrayList<String>();
		final Resource<?> currentResource = shell.getCurrentResource();
		if(currentResource instanceof JavaResource){
			JavaResource jr =  (JavaResource) currentResource;
			try {
				JavaSource<?> javaSource = jr.getJavaSource();
				if(javaSource instanceof JavaEnum ){
					JavaEnum javaEnum = (JavaEnum) javaSource ;
					List<EnumConstant<JavaEnum>> enumConstants = javaEnum.getEnumConstants();
					for (EnumConstant<JavaEnum> enumConstant : enumConstants) {
						tokens.add(enumConstant.getName());
						
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		return tokens;
	}


}
