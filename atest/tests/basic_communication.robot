*** Settings ***
Resource         resource.robot

*** Variables ***
${COUNT}    100

*** Test Cases ***
Passing
    Passing

Failing
    [Documentation]    FAIL This is the error we get
    Failing    This is the error we get

Logging
    [Documentation]    LOG 1 INFO Hello, world! LOG 2 WARN Warning, warning!!
    Logging    Hello, world!
    Logging    Warning, warning!!  WARN

Returning
    ${ret} =    Returning
    Should Be Equal    ${ret}    returned string

Use Remote Keyword Multiple Times
    [Documentation]
    ...    LOG 1.1.1 Round 1
    ...    LOG 1.2.1 Round 2
    ...    LOG 1.${COUNT}.1 Round ${COUNT}
    : FOR    ${i}    IN RANGE    ${COUNT}
    \  Logging    Round ${i + 1}

