package org.vcsreader.lang

import org.junit.Test

import static org.vcsreader.lang.StringUtil.split
import static org.vcsreader.lang.StringUtil.trim

class StringUtilTest {
	@Test void "splitting strings"() {
		assert split("", "|") == []
		assert split("|", "|") == []
		assert split("||", "|") == [""]

		assert split("a|", "|") == ["a"]
		assert split("|a", "|") == ["a"]
		assert split("|a|", "|") == ["a"]

		assert split("a|b", "|") == ["a", "b"]
		assert split("a|b|", "|") == ["a", "b"]
		assert split("|a|b", "|") == ["a", "b"]
		assert split("|a|b|", "|") == ["a", "b"]
	}

	@Test void "trimming strings"() {
		assert trim("", " ") == ""
		assert trim(" ", " ") == ""
		assert trim("  ", " ") == ""

		assert trim("  a", " ") == "a"
		assert trim("a  ", " ") == "a"
		assert trim(" a ", " ") == "a"
		assert trim(" a a ", " ") == "a a"

		assert trim("=-=-=a-=-=-", "-=") == "a"
	}
}
