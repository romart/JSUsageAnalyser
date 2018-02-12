// Ex1

function /*FIND:*/foo() {}

function bar() {
    foo() // exact usage
}

function test(a) {
    if (a == null) {
        foo() // exact usage
    }
    else {
        boo()
    }
}