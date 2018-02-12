// Ex2

function A() {}

A.prototype./*FIND:*/bar = function foo() {};

function B() {}

B.prototype.bar = function foo() {};

function test1(a) {
    a.bar() // exact usage
    var b = a
    var c;
    b.c.bar() // exact usage
    c = b;
    a.bar();
    test1(test2());
    c.bar();
}

function test2() {

  return new A;
}

test1(test2())