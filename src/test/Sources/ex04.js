// ex4

function A() {}

A.prototype.foo = function() {};

A.prototype.bar = function(x) {
    x.foo(); // possible usage
};

function B() {}

B.prototype./*FIND:*/foo = function() {};

B.prototype.bar = function(x) {
    x.foo(); // exact usage
};

function test1(c) {
    c.foo() // possible usage

    if (c instanceof A) {
        c.foo()
    }
    if (c instanceof B) {
        c.foo() // exact usage
        c.bar(c)
        test2(c)
    }
}

function test2(c) {
    c.foo() // exact usage
    c.bar()
}