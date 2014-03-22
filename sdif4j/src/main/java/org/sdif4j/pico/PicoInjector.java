package org.sdif4j.pico;

import com.picocontainer.PicoContainer;

import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

/**
 * Pico Injector implementation
 *
 * This implementation is waiting for
 * https://github.com/picocontainer/picocontainer
 *
 * @author Sergey Chernov
 */
@Alternative // ignored by PicoContainer, required for JavaEE CDI
@Singleton
public class PicoInjector extends AbstractPicoInjector {
	//	@org.picocontainer.annotations.Inject
	@javax.inject.Inject
	private PicoContainer picoContainer;

	// todo public constructor required by PicoContainer
	public PicoInjector() {
	}

	@Override
	protected PicoContainer getPicoContainer() {
		final PicoContainer picoContainer = this.picoContainer;
		if (picoContainer == null) {
			throw new IllegalStateException("PicoContainer is not set");
		}
		return picoContainer;
	}

	// only for tests, not public api
	// todo refactor to reflection
	public PicoContainer getPicoContainerAccessor() {
		return picoContainer;
	}
}
