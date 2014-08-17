package vcsreader.lang

import org.junit.Test

class StringUtilTest {
    @Test void "splitting strings"() {
        assert StringUtil.split("", "|") == []
        assert StringUtil.split("|", "|") == []
        assert StringUtil.split("||", "|") == [""]

        assert StringUtil.split("a|", "|") == ["a"]
        assert StringUtil.split("|a", "|") == ["a"]
        assert StringUtil.split("|a|", "|") == ["a"]

        assert StringUtil.split("a|b", "|") == ["a", "b"]
        assert StringUtil.split("a|b|", "|") == ["a", "b"]
        assert StringUtil.split("|a|b", "|") == ["a", "b"]
        assert StringUtil.split("|a|b|", "|") == ["a", "b"]
    }
}
