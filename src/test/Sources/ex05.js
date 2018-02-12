// Ex5

function A() {}

A.prototype.foo = function() {};
A.prototype./*FIND:*/bar = function() {};

function B() {}
B.prototype = Object.create(A.prototype);
B.prototype.foo = function() {};

function C() {}
C.prototype = Object.create(A.prototype);
C.prototype.bar = function() {};

function test1(a) {
    a.bar() // potential usage

    if (a instanceof B) {
        test2(a)
    }
    if (a instanceof C) {
        a.bar()
    }
}

function test2(a) {
    a.bar() // exact usage
}