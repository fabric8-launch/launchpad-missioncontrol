package org.kontinuity.catapult.core.impl;

import com.sun.org.apache.regexp.internal.RE;
import org.junit.Assert;
import org.junit.Test;
import org.kontinuity.catapult.core.api.Projectile;
import org.kontinuity.catapult.core.api.ProjectileBuilder;

/**
 * Test cases to ensure the {@link org.kontinuity.catapult.core.api.ProjectileBuilder}
 * is working as contracted
 *
 * @author <a href="mailto:alr@redhat.com">Andrew Lee Rubinger</a>
 */
public class ProjectileBuilderTest {

	private static final String SOME_VALUE = "test";
	private static final String REPO_VALUE = "ALRubinger/testrepo";
	private static final String EMPTY = "";

	@Test(expected = IllegalStateException.class)
	public void requiresSourceGitHubRepo() {
		this.getPopulatedBuilder().sourceGitHubRepo(null).build();
	}

	@Test(expected = IllegalStateException.class)
	public void requiresSourceGitHubRepoNotEmpty() {
		this.getPopulatedBuilder().sourceGitHubRepo(EMPTY).build();
	}

	@Test(expected = IllegalStateException.class)
	public void requiresGitHubAccessToken() {
		this.getPopulatedBuilder().gitHubAccessToken(null).build();
	}

	@Test(expected = IllegalStateException.class)
	public void requiresGitHubAccessTokenNotEmpty() {
		this.getPopulatedBuilder().gitHubAccessToken(EMPTY).build();
	}

	@Test(expected = IllegalStateException.class)
	public void requiresGitRef() {
		this.getPopulatedBuilder().gitRef(null).build();
	}

	@Test(expected = IllegalStateException.class)
	public void requiresGitRefNotEmpty() {
		this.getPopulatedBuilder().gitRef(EMPTY).build();
	}

	@Test(expected = IllegalStateException.class)
	public void requiresPipelineTemplatePath() {
		this.getPopulatedBuilder().pipelineTemplatePath(null).build();
	}

	@Test(expected = IllegalStateException.class)
	public void requiresPipelineTemplatePathNotEmpty() {
		this.getPopulatedBuilder().pipelineTemplatePath(EMPTY).build();
	}

	@Test
	public void createsProjectile(){
		final Projectile projectile = this.getPopulatedBuilder().build();
		Assert.assertNotNull("projectile should have been created", projectile);
	}

	@Test
	public void createsProjectileWithDefaultedOpenShiftProjectName(){
		final Projectile projectile = this.getPopulatedBuilder().openShiftProjectName(null).build();
		Assert.assertEquals("openshiftProjectName was not defaulted correctly", "testrepo", projectile.getOpenShiftProjectName());
	}

	@Test
	public void createsProjectileWithExplicitOpenShiftProjectName(){
		final Projectile projectile = this.getPopulatedBuilder().openShiftProjectName("changedfromtest").build();
		Assert.assertEquals("openshiftProjectName was not set correctly", "changedfromtest", projectile.getOpenShiftProjectName());
	}

	@Test(expected = IllegalStateException.class)
	public void sourceRepoMustBeInCorrectForm(){
		new ProjectileBuilderImpl().sourceGitHubRepo("doesntFollowForm").build();
	}

	@Test
	public void sourceRepoMustAcceptDashes(){
		this.mustAcceptDashes("ALRubinger/my-test-thing");
	}

	@Test
	public void sourceOwnerMustAcceptDashes(){
		this.mustAcceptDashes("redhat-organization/something-with-dashes");
	}

	private void mustAcceptDashes(final String fullRepoName){
		final Projectile projectile = this.getPopulatedBuilder()
				.sourceGitHubRepo(fullRepoName)
				.build();
		Assert.assertNotNull("projectile should have been created", projectile);
	}

	/**
	 * @return A builder with all properties set so we can manually
	 * set one property to empty and test {@link ProjectileBuilder#build()}
	 */
	private ProjectileBuilder getPopulatedBuilder(){
		return new ProjectileBuilderImpl()
				.sourceGitHubRepo(REPO_VALUE)
				.gitHubAccessToken(SOME_VALUE)
				.openShiftProjectName(SOME_VALUE)
				.gitRef(SOME_VALUE)
				.pipelineTemplatePath(SOME_VALUE);
	}
}
