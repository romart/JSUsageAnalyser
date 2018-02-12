// Ex3

function A() {}

A.prototype./*FIND:*/foo = function bb() {}; // exact declaration

function B() {}

B.prototype.foo = function ff() {};

function C() {}

function test1(c) {
    c.foo() // possible usage

    if (c instanceof A) {
        test2(c);
        if (c instanceof C) {
            c.foo();
        }
    }
    if (c instanceof B) {
        c.foo();
    }
}

function test2(c) {
    c.foo(); // exact usage
}

//test1(new A)
test1(new B)