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

import static vcsreader.Change.Type.MOVED;

class CommitParser {
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
        private String comment;
        private List<Change> changes = new ArrayList<Change>();

        private String fileName;
        private Change.Type changeType;

        private boolean expectAuthor;
        private boolean expectDate;
        private boolean expectComment;
        private boolean expectFileName;
        private boolean isFileChange;
        private boolean isCopy;
        private String copyFromFileName;
        private String copyFromRevision;
        private final Set<String> movedPaths = new HashSet<String>();

        @Override
        public void startElement(String uri, String localName, @NotNull String name, Attributes attributes) throws SAXException {
            if (name.equals("logentry")) {
                revision = attributes.getValue("revision");
                revisionBefore = previous(revision);
            } else if (name.equals("author")) {
                expectAuthor = true;
            } else if (name.equals("date")) {
                expectDate = true;
            } else if (name.equals("msg")) {
                expectComment = true;
            } else if (name.equals("path")) {
                changeType = asChangeType(attributes.getValue("action"));
                String kind = attributes.getValue("kind");
                isFileChange = (kind == null || kind.isEmpty() || "file".equals(kind));
                isCopy = attributes.getValue("copyfrom-path") != null;
                copyFromFileName = trimPath(attributes.getValue("copyfrom-path"));
                copyFromRevision = attributes.getValue("copyfrom-rev");
                expectFileName = true;
            }
        }

        @Override public void characters(char[] ch, int start, int length) throws SAXException {
            if (expectAuthor) {
                expectAuthor = false;
                author = String.valueOf(ch, start, length);
            } else if (expectDate) {
                expectDate = false;
                try {
                    commitDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(String.valueOf(ch, start, length));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else if (expectComment) {
                expectComment = false;
                comment = String.valueOf(ch, start, length);
            } else if (expectFileName) {
                expectFileName = false;
                fileName = trimPath(String.valueOf(ch, start, length));
            }
        }

        private static String trimPath(String path) {
            if (path == null || path.length() < 1) return null;
            return path.charAt(0) == '/' ? path.substring(1) : null;
        }

        @Override public void endElement(String uri, String localName, @NotNull String name) throws SAXException {
            if (name.equals("logentry")) {
                commits.add(new Commit(revision, revisionBefore, commitDate, author, comment, new ArrayList<Change>(changes)));
                changes.clear();
            } else if (name.equals("path") && isFileChange && !movedPaths.contains(fileName)) {
                if (isCopy) {
                    changes.add(new Change(MOVED, fileName, copyFromFileName, revision, copyFromRevision));
                    movedPaths.add(copyFromFileName);
                } else if (changeType == Change.Type.NEW) {
                    changes.add(new Change(changeType, fileName, revision));
                } else if (changeType == Change.Type.DELETED) {
                    changes.add(new Change(changeType, Change.noFileName, fileName, revision, revisionBefore));
                } else {
                    changes.add(new Change(changeType, fileName, fileName, revision, revisionBefore));
                }
            }
        }

        private static Change.Type asChangeType(String action) {
            if (action.equals("A")) return Change.Type.NEW;
            else if (action.equals("D")) return Change.Type.DELETED;
            else if (action.equals("M")) return Change.Type.MODIFICATION;
            else throw new IllegalStateException("Unknown svn action: " + action);
        }

        private static String previous(String revision) {
            try {
                Integer i = Integer.valueOf(revision);
                return i == 1 ? Change.noRevision : String.valueOf(i - 1);
            } catch (NumberFormatException e) {
                return "";
            }
        }
    }

}
