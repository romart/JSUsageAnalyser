// Ex2

function A() {}

A.prototype./*FIND:*/bar = function foo() {};

function B() {}

B.prototype.bar = function foo() {};

function test1(a) {
    a.bar() // exact usage
    var b = a
    b.bar() // exact usage
}

test1(new A)