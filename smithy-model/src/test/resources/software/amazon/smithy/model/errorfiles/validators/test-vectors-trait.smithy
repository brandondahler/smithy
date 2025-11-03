$version: "2"

namespace smithy.example

@length(min: 1, max: 3)
@pattern("^a+$")
@testVectors({
    allowed: [
        "a"
        "aa"
        "aaa"
    ]
    disallowed: [
        ""
        "b"
        "ab"
        "ba"
        "aaaa"
    ]
})
string WithValidVectorsString

@length(min: 1, max: 3)
@pattern("^a+$")
@testVectors({
    allowed: [
        ""
        "a"
        "aaaa"
        "aab"
    ]
    disallowed: [
        "aa"
        "aaa"
    ]
})
string WithInvalidVectorsString


@length(min: 1, max: 3)
@testVectors({
    allowed: [
        "YQ=="
        "YWE="
        "YWFh"
    ]
    disallowed: [
        ""
        "YWFhYQ=="
    ]
})
blob WithValidVectorsBlob

@length(min: 1, max: 3)
@testVectors({
    allowed: [
        ""
        "YQ=="
        "YWFhYQ=="
    ]
    disallowed: [
        "YWE="
        "YWFh"
    ]
})
blob WithInvalidVectorsBlob

@range(min: 1, max: 3)
@testVectors({
    allowed: [
        1
        2
        3
    ]
    disallowed: [
        0
        4
    ]
})
integer WithValidVectorsInteger

@range(min: 1, max: 3)
@testVectors({
    allowed: [
        4
        2
    ]
    disallowed: [
        1
        3
    ]
})
integer WithInvalidVectorsInteger

