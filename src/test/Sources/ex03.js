// Ex3

function A() {}

A.prototype./*FIND:*/foo = function() {}; // exact declaration

function B() {}

B.prototype.foo = function() {};

function test1(c) {
    c.foo() // possible usage

    if (c instanceof A) {
        test2(c);
    }
    if (c instanceof B) {
        c.foo();
    }
}

function test2(c) {
    c.foo(); // exact usage
}