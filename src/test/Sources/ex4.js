// Ex4



function test4(a, b) {

    b.foo();

loop1:
    while (a instanceof A) {
        a = a.foo();

        while (b instanceof B) {
          b.foo();
        }
loop2:
        while (b instanceof B) {
            b = b.bar();
            if (b instanceof A) {
              break loop2;
            } else {
              continue loop1;
            }

            b = b.bar();
            b = b.foobar();
        }

        if (a instanceof B) {
          break loop1;
        } else {
          continue;
        }
        b = b.bar();
        b = b.foobar();
    }
}

//function test5(a, b, c) {
//
//    if ((a + b) > c) {
//        return a * 10;
//    } else {
//        if (c < 0) {
//            return b + "aaa";
//        } else {
//            if (b == 30.8) {
//                return null;
//            } else {
//                return undefined;
//            }
//        }
//    }
//}

function A() {}

A.prototype.foo = function() {};


function B() {}

B.prototype./*FIND:*/foo = function() {};


A.prototype.bar = function(x) {
    x.foo(); // possible usage
};

B.prototype.bar = function(x) {
    x.foo(); // exact usage
};

var c = new B

function test1(c) {
    c.foo() // possible usage

    if (!(c instanceof A)) {
        c.foo()
    }
    if (c instanceof B) {
        c.foo() // exact usage
        c.bar(c)
        test2(c)
    } else {
        c.foo();
    }
}

function test2(c) {
    c.foo() // exact usage
    c.bar()
}

if (c instanceof B) {
  c.foo();
}