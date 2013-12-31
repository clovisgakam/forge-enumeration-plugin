package cm.adorsys.forge.plugins.enumeration;

import java.io.FileNotFoundException;
import java.util.List;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.Enumerated;

import org.adorsys.javaext.description.Description;
import org.jboss.forge.parser.java.Annotation;
import org.jboss.forge.parser.java.EnumConstant;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.JavaEnum;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.PropertiesFileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.completer.EnumCompleter;
import org.jboss.forge.shell.completer.PropertyCompleter;
import org.jboss.forge.shell.events.PickupResource;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Current;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;
/**
 *
 */
/**
 * 
 * @author gakam clovis
 * 
 */
@Alias("enum")
@RequiresProject
@Help("This plugin will help you to add @description annotation on Enum or Enum constant and add @Enumerated on enum field of java class.")
@RequiresFacet({ EnumerationFacet.class })
public class EnumerationPlugin implements Plugin
{

	public static final String DESCRIPTION_CONSTANT = "description";
	/*
	 * Description title will be used in user interfaces as field descriptors, form titles.
	 */
	public static final String TITLE_SUFFIX = "title";
	/*
	 * Text will be used as help messages, popup illustrations of fields and forms
	 */
	public static final String TEXT_SUFFIX = "text";
	public static final String DOT_CONSTANT = ".";
	public static final String UNDERSCORE_CONSTANT = "_";


	@Inject
	private Project project;

	@Inject
	private Event<InstallFacets> request;

	@Inject
	private Event<PickupResource> pickup;

	@Inject
	private Shell shell;

	@Inject
	private ShellPrompt prompt;

	@Inject
	@Current
	private Resource<?> currentResource;


	@SetupCommand
	public void setup(final PipeOut out) {
		if (!project.hasFacet(EnumerationFacet.class)) {
			request.fire(new InstallFacets(EnumerationFacet.class));
		}
		if (project.hasFacet(EnumerationFacet.class)) {
			ShellMessages.success(out, "Enumeration service installed.");
		} else {
			ShellMessages.error(out,
					"Enumeration service could not be installed.");
		}
	}

	@Command(value = "enumerated-field", help = "add @Enumerated annotation enum Entity field")
	public void addEnumeratedOnField(
			final PipeOut out,
			@Option(name = "onProperty", required = true, completer = PropertyCompleter.class) String fieldname,
			@Option(required = false, type = PromptType.JAVA_CLASS) JavaResource targets)
					throws FileNotFoundException {
		if ((targets == null) && (currentResource instanceof JavaResource)) {
			targets = (JavaResource) currentResource;
		}
		if (targets == null) {
			ShellMessages.error(out,
					"Must specify a domain @Entity on which to operate.");
			return;
		}

		final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
		JavaClass auditableClass = (JavaClass) targets.getJavaSource();
		Field<JavaClass> fieldToAudited = auditableClass.getField(fieldname);
		enumeratedField(fieldToAudited);
		java.saveJavaSource(auditableClass);
		pickup.fire(new PickupResource(java.getJavaResource(auditableClass)));

	}


	@Command(value = "add-enum-class-description", help = "Adds a @Description annotation to the current Enum class")
	public void addEnumDescription( 
			@Option(name = "title") String title, 
			@Option(name = "text") String text, 
            @Option(name = "locale") String locale,			
			final PipeOut out) throws FileNotFoundException{
		final Resource<?> currentResource = shell.getCurrentResource();

		if( currentResource instanceof JavaResource){
			JavaResource jr =  (JavaResource) currentResource;
			JavaSource<?> javaSource = jr.getJavaSource();
			if(javaSource.isEnum()){
				JavaEnum javaEnum = (JavaEnum) javaSource ;
				String key = getDescriptionKey(javaEnum);
				updateResourceBundleFiles(javaEnum.getOrigin().getQualifiedName(), locale, key, title, text);
				Annotation<JavaEnum> annotation = null;
				if (!javaEnum.hasAnnotation(Description.class)) {
					annotation = javaEnum.addAnnotation(Description.class);
					annotation.setStringValue(key);
					saveAndFire(javaEnum);
				} else {
					annotation = javaEnum.getAnnotation(Description.class);
					if(!key.equals(annotation.getStringValue())){
						annotation.setStringValue(key);
						saveAndFire(javaEnum);
					}
				}
			}else {
				ShellMessages.error(out,
						"Must specify and Enum class on which to operate.");

			}			
		}
	}

	@Command(value = "add-enum-constant-description", help = "Adds a @Description annotation to the enum constant of an Enum")
	public void addConstantDescription(
			@Option(name = "onConstant", completer =EnumConstantCompleter.class, required = true) String constant,
			@Option(name = "title") String title, 
			@Option(name = "text") String text, 
            @Option(name = "locale") String locale,		
			final PipeOut out) throws FileNotFoundException {
		final Resource<?> currentResource = shell.getCurrentResource();

		if( currentResource instanceof JavaResource){
			JavaResource jr =  (JavaResource) currentResource;
			JavaSource<?> javaSource = jr.getJavaSource();
			if(javaSource.isEnum()){
				JavaEnum javaEnum = (JavaEnum) javaSource ;
				EnumConstant<JavaEnum> enumConstant = javaEnum.getEnumConstant(constant);
				if (enumConstant == null)
					throw new IllegalStateException(
							"The current Enum has no constant named '" + constant
							+ "'");
				addEnumConstantDescription(enumConstant, locale, title, text);
				saveAndFire(javaEnum);

			}else {
				ShellMessages.error(out,
						"Must specify and  Enum class on which to operate.");

			}			
		}

	}

	@Command(value = "generate-description-keys", help = "Adds a description annotation to the all enum constant of current Enum")
	public void generateDescriptionKeys( 
			@Option(name = "locale") String locale,
			final PipeOut out) throws FileNotFoundException {
		final Resource<?> currentResource = shell.getCurrentResource();

		if( currentResource instanceof JavaResource){
			JavaResource jr =  (JavaResource) currentResource;
			JavaSource<?> javaSource = jr.getJavaSource();
			if(javaSource.isEnum()){
				JavaEnum javaEnum = (JavaEnum) javaSource ;
				List<EnumConstant<JavaEnum>> enumConstants = javaEnum.getEnumConstants();
				for (EnumConstant<JavaEnum> enumConstant : enumConstants) {
					addEnumConstantDescription(enumConstant, locale, "", "");
				}
				saveAndFire(javaEnum);
			}else {
				ShellMessages.error(out,
						"Must specify and  Enum class on which to operate.");

			}
		}

	}

	public void enumeratedField(Field<JavaClass> field){
		if(field.hasAnnotation(Enumerated.class))throw new IllegalStateException("The element '"
				+ field.getName() + "' is already annotated with @"
				+ Enumerated.class.getSimpleName());
		field.getOrigin().addImport(Enumerated.class);
		field.addAnnotation(Enumerated.class);
	}

	private void saveAndFire(JavaSource<?> source){
		final JavaSourceFacet javaSourceFacet = project.getFacet(JavaSourceFacet.class);
		try {
			javaSourceFacet.saveJavaSource(source);
			pickup.fire(new PickupResource(javaSourceFacet.getJavaResource(source)));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(
					"The current resource '"
							+ source.getName() + "' was deleted from the file system.");
		}

	}

	private String getDescriptionKey(JavaEnum javaType) {
		return javaType.getQualifiedName() + DOT_CONSTANT + DESCRIPTION_CONSTANT;
	}

	private String getDescriptionKey(EnumConstant<JavaEnum> member){
		return member.getOrigin().getQualifiedName() + DOT_CONSTANT + member.getName() + DOT_CONSTANT + DESCRIPTION_CONSTANT;
	}

	private class BundleBaseNameResourceFilter implements ResourceFilter
	{
		private String fileName;

		public BundleBaseNameResourceFilter(String fileName)
		{
			this.fileName = fileName;
		}

		@Override
		public boolean accept(Resource<?> resource)
		{
			return (resource instanceof PropertiesFileResource && resource.getName().startsWith(fileName));
		}
	}

	/**
	 * Gets another file resource. Creates a file in case it does not exist
	 * 
	 * @param bundleName
	 * @return
	 */
	protected PropertiesFileResource getOrCreate(final String bundleName)
	{
		final ResourceFacet resourceFacet = project.getFacet(ResourceFacet.class);
		final BundleBaseNameResourceFilter filter = new BundleBaseNameResourceFilter(bundleName);
		PropertiesFileResource newFileResource = null;
		for (DirectoryResource directoryResource : resourceFacet.getResourceFolders())
		{
			for (Resource<?> resource : directoryResource.listResources(filter))
			{
				newFileResource = (PropertiesFileResource) resource;
				// Using the first resource found
				break;
			}
		}
		if (newFileResource == null)
		{
			newFileResource = resourceFacet.getResourceFolder().getChildOfType(PropertiesFileResource.class,
					bundleName);
			if (!newFileResource.exists())
			{
				newFileResource.createNewFile();
			}
		}
		return newFileResource;
	}   
	/*
	 * Will update the resource bundle file. We will us a single file for each package.
	 */
	private void updateResourceBundleFiles(String klassName, String locale, String key, String title, String text){
		String bundleName = klassName + ".descriptions.messages"+ (locale!=null?"_"+locale:"")+".properties";
		PropertiesFileResource propertiesFileResource = getOrCreate(bundleName);
		String keyFormated = key.replace(DOT_CONSTANT, UNDERSCORE_CONSTANT);
		String titleKey = keyFormated + DOT_CONSTANT + TITLE_SUFFIX;
		String textKey = keyFormated + DOT_CONSTANT + TEXT_SUFFIX;
		propertiesFileResource.putProperty(titleKey, title);
		propertiesFileResource.putProperty(textKey, text);
	}


	public void addEnumConstantDescription(EnumConstant<JavaEnum> enumConstant, String locale, String title, String text){

		String descriptionKey = getDescriptionKey(enumConstant);
		updateResourceBundleFiles(enumConstant.getOrigin().getQualifiedName(), locale, descriptionKey, title, text);
		Annotation<JavaEnum> annotation = null;
		if (!enumConstant.hasAnnotation(Description.class)) {
			annotation = enumConstant.addAnnotation(Description.class);
			annotation.setStringValue(descriptionKey);
		} else {
			annotation = enumConstant.getAnnotation(Description.class);
			if(!descriptionKey.equals(annotation.getStringValue())){
				annotation.setStringValue(descriptionKey);
			}
		}
	}
}
