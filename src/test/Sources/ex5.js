// Ex5

function A() {}

A.prototype.foo = function() {};
A.prototype./*FIND:*/bar = function() {};

function B() {}
B.prototype = Object.create(A.prototype);
B.prototype.foo = function() {
  bar();
};

function C() {}
C.prototype = Object.create(A.prototype);
C.prototype.bar = function() {};

function D() {}
D.prototype = C.prototype;

function test1(a) {
    a.bar() // potential usage

    if (a instanceof B) {
        test2(a)
        test3(a);
    }
    if (a instanceof C) {
        a.bar()
    }
}

function test2(a) {
    a.bar() // exact usage
    test1(a)
}

test1(new C)

function test3(a) {
  b = a.bar;
  b.bar();
}