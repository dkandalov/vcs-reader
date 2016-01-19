package vcsreader.vcs.svn;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import vcsreader.Change;
import vcsreader.Commit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Boolean.parseBoolean;
import static vcsreader.Change.Type.*;
import static vcsreader.Change.noFilePath;
import static vcsreader.Change.noRevision;

class SvnCommitParser {
    static List<Commit> parseCommits(String xml) {
        try {
            CommitReadingHandler commitReadingHandler = new CommitReadingHandler();

            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();
            xmlReader.setContentHandler(commitReadingHandler);
            xmlReader.parse(new InputSource(new StringReader(xml)));

            return commitReadingHandler.commits;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to parse xml: " + xml, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CommitReadingHandler extends DefaultHandler {
        private final List<Commit> commits = new ArrayList<Commit>();

        private String revision;
        private String revisionBefore;
        private String author;
        private Date commitDate;
        private String commitDateString;
        private String comment;
        private List<Change> changes = new ArrayList<Change>();

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
        private final Set<String> movedPaths = new HashSet<String>();

        private final SimpleDateFormat dateFormat;

        private CommitReadingHandler() {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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

        @Override public void endElement(String uri, @NotNull String localName, @NotNull String name) throws SAXException {
            if (name.equals("logentry")) {
                Iterator<Change> i = changes.iterator();
                while (i.hasNext()) {
                    Change change = i.next();
                    if (change.type == DELETED && movedPaths.contains(change.filePathBefore)) {
                        i.remove();
                    }
                }

                commits.add(new Commit(revision, revisionBefore, commitDate, author, comment, new ArrayList<Change>(changes)));
                changes.clear();
                movedPaths.clear();

            } else if (name.equals("path")) {
                expectFileName = false;
                if (isFileChange) {
                    if (isCopy) {
                        changes.add(new Change(MOVED, filePath, copyFromFilePath, revision, copyFromRevision));
                        movedPaths.add(copyFromFilePath);

                    } else if (changeType == SvnChangeType.NEW) {
                        changes.add(new Change(ADDED, filePath, revision));

                    } else if (changeType == SvnChangeType.DELETED) {
                        changes.add(new Change(DELETED, noFilePath, filePath, revision, revisionBefore));

                    } else if (changeType == SvnChangeType.REPLACED) {
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
                try {
                    commitDate = dateFormat.parse(commitDateString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private static SvnChangeType asChangeType(String action) {
            if (action.equals("A")) return SvnChangeType.NEW;
            else if (action.equals("D")) return SvnChangeType.DELETED;
            else if (action.equals("M")) return SvnChangeType.MODIFICATION;
            else if (action.equals("R")) return SvnChangeType.REPLACED;
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

        public enum SvnChangeType {
            NEW,
            DELETED,
            REPLACED,
            MODIFICATION
        }
    }

}
