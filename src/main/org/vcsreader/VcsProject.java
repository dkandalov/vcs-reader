package org.vcsreader;

import org.vcsreader.lang.TimeRange;
import org.vcsreader.vcs.VcsCommand;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * Represents a project as a set of {@link VcsRoot}s.
 * This class is the main entry point for cloning/updating/reading version control history.
 */
public class VcsProject {
	private final List<VcsRoot> vcsRoots;
	private final CompositeListener compositeListener;

	public VcsProject(VcsRoot... vcsRoots) {
		this(asList(vcsRoots));
	}

	public VcsProject(List<VcsRoot> vcsRoots) {
		this.compositeListener = new CompositeListener();
		this.vcsRoots = unmodifiableList(vcsRoots.stream().map((vcsRoot) -> {
			if (vcsRoot instanceof VcsCommand.Owner) {
				return (VcsRoot)((VcsCommand.Owner) vcsRoot).withListener(compositeListener);
			} else {
				return vcsRoot;
			}
		}).collect(toList()));
	}

	/**
	 * For distributed VCS clones {@link VcsRoot}s to local file system and must be called before querying commits.
	 * Does nothing for centralized VCS because commit history can be queried from server.
	 */
	public CloneResult cloneToLocal() {
		CloneResult result = new CloneResult();
		for (VcsRoot vcsRoot : vcsRoots) {
			CloneResult cloneResult = vcsRoot.cloneToLocal();
			result = result.aggregateWith(cloneResult);
		}
		return result;
	}

	/**
	 * For distributed VCS pulls updates from upstream for all {@link VcsRoot}s
	 * Does nothing for centralized VCS because commit history can be queried from server.
	 */
	public UpdateResult update() {
		UpdateResult result = new UpdateResult();
		for (VcsRoot vcsRoot : vcsRoots) {
			UpdateResult updateResult = vcsRoot.update();
			result = result.aggregateWith(updateResult);
		}
		return result;
	}

	/**
	 * Request commits from VCS for all {@link VcsRoot}s within specified time range.
	 * For distributed VCS commits are read from currently checked out branch.
	 * <p>
	 * Merge commits are not logged, the commit which was merged into branch is logged instead.
	 * Therefore, it's possible to see commit with time outside of requested time range.
	 *
	 * @param timeRange range of timestamps used to request commits;
	 *                  start is inclusive with one second resolution, end is exclusive with one second resolution
	 */
	public LogResult log(TimeRange timeRange) {
		LogResult result = new LogResult();
		for (VcsRoot vcsRoot : vcsRoots) {
			LogResult logResult = vcsRoot.log(timeRange);
			logResult = (logResult != null ? logResult.setVcsRoot(vcsRoot) : null);
			result = result.aggregateWith(logResult);
		}
		return result;
	}

	public VcsProject addListener(VcsCommand.Listener listener) {
		compositeListener.add(listener);
		return this;
	}

	public VcsProject removeListener(VcsCommand.Listener listener) {
		compositeListener.remove(listener);
		return this;
	}

	public List<VcsRoot> vcsRoots() {
		return vcsRoots;
	}

	@Override public String toString() {
		return "VcsProject{" + vcsRoots + '}';
	}


	private class CompositeListener implements VcsCommand.Listener {
		private final List<VcsCommand.Listener> listeners = new ArrayList<>();

		public void add(VcsCommand.Listener listener) {
			listeners.add(listener);
		}

		public void remove(VcsCommand.Listener listener) {
			listeners.remove(listener);
		}

		@Override public void beforeCommand(VcsCommand<?> command) {
			for (VcsCommand.Listener listener : listeners) {
				listener.beforeCommand(command);
			}
		}

		@Override public void afterCommand(VcsCommand<?> command) {
			for (VcsCommand.Listener listener : listeners) {
				listener.afterCommand(command);
			}
		}
	}
}
