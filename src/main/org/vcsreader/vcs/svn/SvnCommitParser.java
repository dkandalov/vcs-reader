package org.vcsreader.vcs.svn;

import org.jetbrains.annotations.NotNull;
import org.vcsreader.VcsCommit;
import org.vcsreader.lang.DateTimeUtil;
import org.vcsreader.vcs.Change;
import org.vcsreader.vcs.Commit;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Boolean.parseBoolean;
import static org.vcsreader.VcsChange.Type.*;
import static org.vcsreader.VcsChange.noFilePath;
import static org.vcsreader.VcsChange.noRevision;

class SvnCommitParser {
	static List<VcsCommit> parseCommits(String xml) {
		try {
			CommitReadingHandler commitReadingHandler = new CommitReadingHandler();

			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();
			xmlReader.setContentHandler(commitReadingHandler);
			xmlReader.parse(new InputSource(new StringReader(xml)));

			return commitReadingHandler.commits;
		} catch (SAXException e) {
			throw new RuntimeException("Failed to parse xml: " + xml, e);
		} catch (ParserConfigurationException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static class CommitReadingHandler extends DefaultHandler {
		private final List<VcsCommit> commits = new ArrayList<>();

		private String revision;
		private String revisionBefore;
		private String author;
		private Instant dateTime;
		private String commitDateString;
		private String comment;
		private List<Change> changes = new ArrayList<>();

		private String filePath;
		private SvnChangeType changeType;

		private boolean expectAuthor;
		private boolean expectDate;
		private boolean expectComment;
		private boolean expectFileName;
		private boolean isFileChange;
		private boolean isTextModification;
		private boolean isCopy;
		private String copyFromFilePath;
		private String copyFromRevision;
		private final Set<String> movedPaths = new HashSet<>();

		private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;


		private CommitReadingHandler() {
		}

		@Override public void startElement(@NotNull String uri, @NotNull String localName,
		                                   @NotNull String name, @NotNull Attributes attributes) throws SAXException {
			if (name.equals("logentry")) {
				revision = attributes.getValue("revision");
				revisionBefore = previous(revision);
			} else if (name.equals("author")) {
				expectAuthor = true;
				author = "";
			} else if (name.equals("date")) {
				expectDate = true;
				commitDateString = "";
			} else if (name.equals("msg")) {
				expectComment = true;
				comment = "";
			} else if (name.equals("path")) {
				changeType = asChangeType(attributes.getValue("action"));
				String kind = attributes.getValue("kind");
				isFileChange = (kind == null || kind.isEmpty() || "file".equals(kind));
				isCopy = attributes.getValue("copyfrom-path") != null;
				copyFromFilePath = trimPath(attributes.getValue("copyfrom-path"));
				copyFromRevision = attributes.getValue("copyfrom-rev");
				filePath = "";
				expectFileName = true;

				boolean isPropertiesModification = parseBoolean(attributes.getValue("prop-mods"));
				isTextModification =
						(attributes.getValue("text-mods") == null && !isPropertiesModification) ||
								parseBoolean(attributes.getValue("text-mods"));
			}
		}

		@Override public void characters(char[] ch, int start, int length) throws SAXException {
			if (expectAuthor) {
				author += String.valueOf(ch, start, length);
			} else if (expectDate) {
				commitDateString += String.valueOf(ch, start, length);
			} else if (expectComment) {
				comment += String.valueOf(ch, start, length);
			} else if (expectFileName) {
				filePath += trimPath(String.valueOf(ch, start, length));
			}
		}

		private static String trimPath(String path) {
			if (path == null || path.length() < 1) return null;
			return path.charAt(0) == '/' ? path.substring(1) : null;
		}

		@Override
		public void endElement(String uri, @NotNull String localName, @NotNull String name) throws SAXException {
			if (name.equals("logentry")) {
				Iterator<Change> i = changes.iterator();
				while (i.hasNext()) {
					Change change = i.next();
					if (change.getType() == DELETED && movedPaths.contains(change.getFilePathBefore())) {
						i.remove();
					}
				}

				commits.add(new Commit(revision, revisionBefore, dateTime, author, comment, new ArrayList<>(changes)));
				changes.clear();
				movedPaths.clear();

			} else if (name.equals("path")) {
				expectFileName = false;
				if (isFileChange) {
					if (isCopy) {
						changes.add(new Change(MOVED, filePath, copyFromFilePath, revision, copyFromRevision));
						movedPaths.add(copyFromFilePath);

					} else if (changeType == SvnChangeType.Added) {
						changes.add(new Change(ADDED, filePath, revision));

					} else if (changeType == SvnChangeType.Delete) {
						changes.add(new Change(DELETED, noFilePath, filePath, revision, revisionBefore));

					} else if (changeType == SvnChangeType.Replaced) {
						changes.add(new Change(DELETED, noFilePath, filePath, revision, revisionBefore));
						changes.add(new Change(ADDED, filePath, revision));

					} else {
						// check for text modification because there can also be svn properties modifications
						if (isTextModification) {
							changes.add(new Change(MODIFIED, filePath, filePath, revision, revisionBefore));
						}
					}
				}
			} else if (name.equals("path")) {
				expectFileName = false;
			} else if (name.equals("author")) {
				expectAuthor = false;
			} else if (name.equals("msg")) {
				expectComment = false;
			} else if (name.equals("date")) {
				expectDate = false;
				dateTime = dateTimeFormatter.parse(commitDateString, DateTimeUtil::asInstant);
			}
		}

		private static SvnChangeType asChangeType(String action) {
			if (action.equals("A")) return SvnChangeType.Added;
			else if (action.equals("D")) return SvnChangeType.Delete;
			else if (action.equals("M")) return SvnChangeType.Modified;
			else if (action.equals("R")) return SvnChangeType.Replaced;
			else throw new IllegalStateException("Unknown svn action: " + action);
		}

		private static String previous(String revision) {
			try {
				Integer i = Integer.valueOf(revision);
				return i == 1 ? noRevision : String.valueOf(i - 1);
			} catch (NumberFormatException e) {
				return "";
			}
		}

		private enum SvnChangeType {
			Added,
			Delete,
			Replaced,
			Modified
		}
	}

}
