// Ex8

function A() {}

A.prototype./*FIND:*/foo = function bb() {}; // exact declaration

function B() {}

B.prototype.foo = function ff() {};

function C() {}

function test1(c) {
    c.foo() // possible usage

    var d = c;
    if (c instanceof A) {
        test2(c);
        if (c instanceof C) {
            c.foo();
        }

        // we have to create new clean state

        if (d == c) {
          d.foo();
        }

        if (d == null) {
          d.foo();
        }
    }
    if (c instanceof B) {
        c.foo();
    }
}

function test2(c) {
    c.foo(); // exact usage
}


function test3(a, b, c) {

  a.foo();
  b.foo()
  c.foo

  if (a == b) {
    while (a instanceof B) {
      a = a.foo();

      if (a  == c) {
        return new C;
      }

      if (c instanceof C) {
        break;
      }

      a.foo();
    }
    c.foo();
  }


}


test2(test3(new A, new B, new C))
//test1(new A)
test1(new B)