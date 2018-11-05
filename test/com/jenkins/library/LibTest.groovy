package com.jenkins.library

import spock.lang.Specification

class LibTest extends Specification {
    def 'returnTrue returns true'() {
        given:
        Lib lib = new Lib()

        when:
        def result = lib.returnTrue()

        then:
        result
    }

    def 'returnFalse returns false'() {
        given:
        Lib lib = new Lib()

        when:
        def result = lib.returnFalse()

        then:
        !result
    }
}
