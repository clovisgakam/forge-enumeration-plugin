package cm.adorsys.forge.plugins.enumeration;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.dependencies.DependencyInstaller;
import org.jboss.forge.project.dependencies.ScopeType;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.packaging.PackagingType;
import org.jboss.forge.shell.plugins.RequiresFacet;

@RequiresFacet({ DependencyFacet.class })
public class EnumerationFacet  extends BaseFacet{


	public static final Dependency JAVAEE6 = DependencyBuilder
			.create("org.jboss.spec:jboss-javaee-6.0")
			.setScopeType(ScopeType.IMPORT)
			.setPackagingType(PackagingType.BASIC);

	private final DependencyInstaller installer;

	@Inject
	public EnumerationFacet(final DependencyInstaller installer) {
		this.installer = installer;
	}

	@Override
	public boolean install() {
		for (Dependency requirement : getRequiredDependencies()) {
			if (!getInstaller().isInstalled(project, requirement)) {
				DependencyFacet deps = project.getFacet(DependencyFacet.class);
				if (!deps.hasEffectiveManagedDependency(requirement)
						&& !deps.hasDirectManagedDependency(JAVAEE6)) {
					getInstaller().installManaged(project, JAVAEE6);
				}
				getInstaller()
						.install(project, requirement, ScopeType.PROVIDED);
			}
		}
		return true;
	}

	@Override
	public boolean isInstalled() {
		DependencyFacet deps = project.getFacet(DependencyFacet.class);
		for (Dependency requirement : getRequiredDependencies()) {
			if (!deps.hasEffectiveDependency(requirement)) {
				return false;
			}
		}
		
		return true;
	}

	protected List<Dependency> getRequiredDependencies() {
		return Arrays
				.asList((Dependency) DependencyBuilder
						.create("org.adorsys.javaext:javaext.description:0.0.1-SNAPSHOT"));

	}

	protected DependencyInstaller getInstaller() {
		return installer;
	}

}
