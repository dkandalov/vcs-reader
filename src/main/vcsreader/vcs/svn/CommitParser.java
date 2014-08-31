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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Collections.emptyList;

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
            e.printStackTrace();
            return emptyList(); // TODO
        } catch (SAXException e) {
            e.printStackTrace();
            return emptyList(); // TODO
        } catch (IOException e) {
            e.printStackTrace();
            return emptyList(); // TODO
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
                    commitDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(String.valueOf(ch, start, length));
                } catch (ParseException e) {
                    e.printStackTrace(); // TODO
                }
            } else if (expectComment) {
                expectComment = false;
                comment = String.valueOf(ch, start, length);
            } else if (expectFileName) {
                expectFileName = false;
                fileName = String.valueOf(ch, start, length);
                if (fileName.length() > 0 && fileName.charAt(0) == '/') {
                    fileName = fileName.substring(1);
                }
            }
        }

        @Override public void endElement(String uri, String localName, @NotNull String name) throws SAXException {
            if (name.equals("logentry")) {
                commits.add(new Commit(revision, revisionBefore, commitDate, author, comment, new ArrayList<Change>(changes)));
                changes.clear();
            } else if (name.equals("path")) {
                if (changeType == Change.Type.NEW) {
                    changes.add(new Change(changeType, fileName, revision));
                } else {
                    changes.add(new Change(changeType, fileName, fileName, revision, revisionBefore));
                }
            }
        }

        private static Change.Type asChangeType(String action) {
            if (action.equals("A")) return Change.Type.NEW;
            else if (action.equals("D")) return Change.Type.DELETED;
            else if (action.equals("M")) return Change.Type.MODIFICATION;
                // TODO implement
            else return null;
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
